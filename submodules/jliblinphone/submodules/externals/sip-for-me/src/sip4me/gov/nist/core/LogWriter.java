/***************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).    *
***************************************************************************/

package sip4me.gov.nist.core;
import java.io.PrintStream;

import net.rim.device.api.system.EventLogger;

import org.linphone.core.LinphoneLogHandler;



/**
*  Log System Errors. Also used for debugging log.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

public class LogWriter
{
	static final long sip_for_me = 0xf3f5b5e391e9d4aL;   
	static boolean isBB=false;
	static {
    	try {
			Class.forName("net.rim.device.api.system.EventLogger");
			net.rim.device.api.system.EventLogger.register(sip_for_me, "sip-for-me", net.rim.device.api.system.EventLogger.VIEWER_STRING);
			isBB=true;
		} catch (ClassNotFoundException e) {
			//nop not in BB case;
		}
	}

	/** 
	 * Don't trace
	 */    
	public static final int TRACE_NONE = 0;
	/**
	 * Just errors
	 */
	public static final int TRACE_EXCEPTION = 2; 
	/**
	 * Errors and flow information
	 */
	public static final int TRACE_MESSAGES = 16;
	/**
	 * Most verbose
	 */
	public static final int TRACE_DEBUG = 32;
	private static String   logFileName;
	public static PrintStream traceWriter = System.out;

/** Flag to indicate that logging is enabled. This needs to be
* static and public in order to globally turn logging on or off.
* This is static for efficiency reasons (the java compiler will not
* generate the logging code if this is set to false).
*/
	public static boolean needsLogging = false;

	/**
	*  Debugging trace stream.
	*/
	private    static  PrintStream trace = System.out;
	/** trace level
	 */        
	protected     static int traceLevel = TRACE_NONE;


	/** log a stack trace..
	 * @deprecated It's confusing to have exceptions when there are no problems (ArnauVP - Genaker)
	*/
	public static void logStackTrace() {
		return;
//		if (needsLogging) {
//		      System.out.println("------------ Traceback ------");
//		      logException(new Exception());
//		      System.out.println("----------- End Traceback ------");
//		}
	}

	/**
	 * Given an Exception object, print its stack trace
	 * @param ex
	 */
	public static void logException(Exception ex) {
	    if (needsLogging && traceLevel >= TRACE_EXCEPTION)  {
	    	String message= ex.getClass() + ": " + ex.getMessage();
	    	System.err.println(message);
	    	ex.printStackTrace();
	    	if (isBB) {
	    		net.rim.device.api.system.EventLogger.logEvent(sip_for_me, message.getBytes());
	    	}
	    }
	}
		
	/**
	 * FIXME: incorporate remote loggers (file, socket, etc) 
	 * @param message
	 * @param remoteLogger
	 * @deprecated until someone writes this functionality ?
	 */
	public synchronized static void logMessage(String message,
			String remoteLogger) {
		logMessage(TRACE_NONE, message);

	}
	

	/** Log a message into the log file, only if its level is 
	 * higher than the currently configured <i>traceLevel</i>
         * @param message message to log into the log file.
         */
	public static void logMessage(int level, String message) {
		if (needsLogging && traceLevel >= level) {
			System.out.println(System.currentTimeMillis() + " :: " + message);
			if (isBB) {
				net.rim.device.api.system.EventLogger.logEvent(sip_for_me, message.getBytes(),sipforme2BBlevel(level));
			}
		}
	}
	
	/** Log a message into the log file, assuming a MESSAGES level.
	 * and depending on current traceLevel value.
         * @param message message to log into the log file.
         */
	public static void logMessage(String message) {
		if (needsLogging && traceLevel >= TRACE_MESSAGES){
			System.out.println(System.currentTimeMillis() + " :: " + message);
			if (isBB) {
				net.rim.device.api.system.EventLogger.logEvent(sip_for_me, message.getBytes(),sipforme2BBlevel(TRACE_MESSAGES));
			}
		}
	}
	
    
	
        /** Set the trace level for the stack.
         */
        public static void setTraceLevel(int level) {
            traceLevel = level;
        }
        
        /** Get the trace level for the stack.
         */
        public static int getTraceLevel() { return traceLevel; }
        
        
        public static void setLogFileName( String logFileName) {                
        }
        
		private static int sipforme2BBlevel(int level) {
			switch (level) {
			case TRACE_DEBUG: return EventLogger.ALWAYS_LOG;
			case TRACE_MESSAGES: return EventLogger.ALWAYS_LOG;
			case TRACE_EXCEPTION: return EventLogger.ERROR;
			}
			return EventLogger.ERROR;
		}

}
