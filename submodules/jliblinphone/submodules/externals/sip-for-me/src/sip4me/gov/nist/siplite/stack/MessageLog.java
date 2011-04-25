/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/

package sip4me.gov.nist.siplite.stack;

/**
* This class stores a message along with some other informations
* Used to log messages.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan 
*@author Marc Bednarek  
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

class MessageLog {
 
    private String message;

    private String source;

    private String destination;

    private long timeStamp;

    private boolean isSender;

    private String firstLine;

    private String statusMessage;
	
    private String tid;

    private String callId;

    public  boolean equals(Object other) {
	if ( ! (other instanceof MessageLog)) {
		return false;
	} else {
		MessageLog otherLog = (MessageLog) other;
		return otherLog.message.equals(message) &&
			otherLog.timeStamp == timeStamp;
	}
    }

    /**
     * Constructor
     */
 
    public MessageLog(String message, String source, String destination,
		      String timeStamp, boolean isSender, 
		      String firstLine, String statusMessage, 
		      String tid, String callId) throws IllegalArgumentException {
	if (message == null
	  || message.equals("")) 
		throw new IllegalArgumentException("null msg");
        this.message = message;
        this.source = source;
        this.destination = destination;
	try {
	   long ts = Long.parseLong(timeStamp);
	   if (ts < 0) 
		throw new IllegalArgumentException("Bad time stamp ");
           this.timeStamp = ts;
	} catch (NumberFormatException ex) {
	   throw new IllegalArgumentException("Bad number format " + timeStamp);
	}
        this.isSender = isSender;
	this.firstLine = firstLine;
	this.statusMessage = statusMessage;
	this.tid = tid;
	this.callId = callId;
    }

    protected long getTimeStamp() { 
	return this.timeStamp;
    }

    public MessageLog(String message, String source, String destination,
		      long timeStamp, boolean isSender,
		      String firstLine, String statusMessage, 
		      String tid, String callId) throws IllegalArgumentException{
	if (message == null
	  || message.equals("")) 
		throw new IllegalArgumentException("null msg");
        this.message = message;
        this.source = source;
        this.destination = destination;
	if (timeStamp < 0) throw new IllegalArgumentException("negative ts");
	this.timeStamp = timeStamp ;
        this.isSender = isSender;
	this.firstLine = firstLine;
	this.statusMessage = statusMessage;
	this.tid = tid;
	this.callId = callId;
    }

    public String flush(long startTime) {
	String log;

	if (statusMessage != null) {
		log = "<message\nfrom=\"" + source + 
	    	"\" \nto=\"" + destination + 
	    	"\" \ntime=\"" + (timeStamp  - startTime) + 
	    	"\" \nisSender=\"" + isSender  + 
	    	"\" \nstatusMessage=\"" + statusMessage +
	    	"\" \ntransactionId=\"" + tid +
	    	"\" \ncallId=\"" + callId + 
	    	"\" \nfirstLine=\"" + firstLine.trim() + 
	    	"\">\n";
		log += "<![CDATA[";	
		log += message;
		log += "]]>\n";
		log += "</message>\n";	
	} else {
		log = "<message\nfrom=\"" + source + 
	    	"\" \nto=\"" + destination + 
	    	"\" \ntime=\"" + (timeStamp - startTime)  + 
	    	"\" \nisSender=\"" + isSender  + 
	    	"\" \ntransactionId=\"" + tid +
	    	"\" \ncallId=\"" + callId + 
	    	"\" \nfirstLine=\"" + firstLine.trim() + 
	    	"\">\n";
		log += "<![CDATA[";	
		log += message;
		log += "]]>\n";
		log += "</message>\n";	
	}
	return log;
    }
    /**
     * Get an XML String for this message
     */
 
    public String flush() {
	String log;

	if (statusMessage != null) {
		log = "<message\nfrom=\"" + source + 
	    	"\" \nto=\"" + destination + 
	    	"\" \ntime=\"" + timeStamp  + 
	    	"\" \nisSender=\"" + isSender  + 
	    	"\" \nstatusMessage=\"" + statusMessage +
	    	"\" \ntransactionId=\"" + tid +
	    	"\" \nfirstLine=\"" + firstLine.trim() + 
	    	"\" \ncallId=\"" + callId + 
	    	"\" \n>\n";
		log += "<![CDATA[";	
		log += message;
		log += "]]>\n";
		log += "</message>\n";	
	} else {
		log = "<message\nfrom=\"" + source + 
	    	"\" \nto=\"" + destination + 
	    	"\" \ntime=\"" + timeStamp  + 
	    	"\" \nisSender=\"" + isSender  + 
	    	"\" \ntransactionId=\"" + tid +
	    	"\" \ncallId=\"" + callId + 
	    	"\" \nfirstLine=\"" + firstLine.trim() + 
	    	"\" \n>\n";
		log += "<![CDATA[";	
		log += message;
		log += "]]>\n";
		log += "</message>\n";	
	}
	return log;
    }
    
}
