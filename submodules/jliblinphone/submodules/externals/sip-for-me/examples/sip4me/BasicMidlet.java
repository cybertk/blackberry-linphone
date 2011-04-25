package sip4me;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import sip4me.gov.nist.core.Debug;
import sip4me.gov.nist.core.LogWriter;
import sip4me.gov.nist.microedition.sip.SipConnectionNotifierImpl;
import sip4me.gov.nist.microedition.sip.SipConnector;
import sip4me.gov.nist.microedition.sip.StackConnector;
import sip4me.gov.nist.siplite.header.Header;
import sip4me.gov.nist.siplite.message.Request;
import sip4me.gov.nist.siplite.stack.ServerLog;
import sip4me.nist.javax.microedition.sip.SipClientConnection;
import sip4me.nist.javax.microedition.sip.SipClientConnectionListener;
import sip4me.nist.javax.microedition.sip.SipConnectionNotifier;
import sip4me.nist.javax.microedition.sip.SipRefreshHelper;
import sip4me.nist.javax.microedition.sip.SipRefreshListener;
import sip4me.nist.javax.microedition.sip.SipServerConnectionListener;

/**
 * This MIDlet shows how to initialize the stack, create a SipConnectionNotifier,
 * register to the outbound proxy, manage responses, use the Refresh Helper and unregister.
 * 
 * @author ArnauVP
 *
 */
public class BasicMidlet extends MIDlet implements SipServerConnectionListener, SipRefreshListener {

	Hashtable props;
	private SipConnectionNotifier connectionNotifier;
	protected SipClientConnection registerScc;
	private int registerRefreshID;

	
	public BasicMidlet() {
		props = new Hashtable();
	}
	
	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {

	}

	protected void pauseApp() {

	}

	protected void startApp() throws MIDletStateChangeException {
		System.out.println("Basic midlet starting!");
		initProperties();
		try {
			initializeStack();
		} catch (IOException e) {
			System.err.println("Error initializing stack! Aborting!");
			e.printStackTrace();
			notifyDestroyed();
		}
		
		try {
			// if no transport is specified, both TCP and UDP 
			// listening points will be created
			openSipConnectionNotifier("sip:5060" /*;transport=tcp*/);
		} catch (IOException e) {
			System.err.println("Error opening SCN!");
			e.printStackTrace();
		}
		
		try { 
			if (connectionNotifier != null) {
				register(getProperty("SIP-Domain"), getProperty("User-URI"),
					getProperty("Username"), getProperty("AuthCredentials-Username"),
					getProperty("Password"), getProperty("SIP-Realm"));
				Thread.sleep(120000); // Give time for a refresh
				unregister();
			}
			
			Thread.sleep(3000);
		} catch (Exception e) {
			System.err.println("Error registering!");
			e.printStackTrace();
		} finally {
			notifyDestroyed();
		}
		
	}

	/**
	 * Initialize application parameters 
	 * (you could get them from JAD or login window)
	 */
	public void initProperties() {
		props.put("AuthCredentials-Username","user1@sip.domain.com");
		props.put("Username","user1");
		props.put("User-URI","sip:user1@sip.domain.com");
		props.put("Password","secret");
		props.put("SIP-Proxy-IP","192.168.1.1");
		props.put("SIP-Proxy-Port","5060");
		props.put("SIP-Domain","sip.domain.com");
		props.put("SIP-Realm","sip.domain.com");
		
		System.out.println("Properties initialized");
	}
	
	public String getProperty(String key) {
		return (String) props.get(key);
	}
	
	
	/**
	 * High level Methods
	 * @throws IOException 
	 */
	
	/**
	 * Initialize the properties of the Stack.
	 */
	public void initializeStack() throws IOException {
		// Configure logging of the stack
        Debug.enableDebug(false);
		LogWriter.needsLogging = true;
		LogWriter.setTraceLevel(LogWriter.TRACE_EXCEPTION);
		ServerLog.setTraceLevel(ServerLog.TRACE_NONE);
		
		
		// If we set the outbound proxy on TCP, all the outgoing requests
		// would be on TCP
        StackConnector.properties.setProperty("javax.sip.OUTBOUND_PROXY",
				getProperty("SIP-Proxy-IP") + ":" + getProperty("SIP-Proxy-Port") + "/UDP");
        StackConnector.properties.setProperty ("javax.sip.RETRANSMISSION_FILTER", "on");
        
		// Get our real IP address, and set it on the Stack properties
		// We have to do this because inbound sockets don't return real IP 
		// (they may return 'localhost' or '127.0.0.1')
        // This address will also be set on SipConnectionNotifiers
		String dummyConnString = "socket://" + getProperty("SIP-Proxy-IP") + ":" + getProperty("SIP-Proxy-Port");
		SocketConnection dummyCon = (SocketConnection) Connector.open(dummyConnString);
		String localAdd = dummyCon.getLocalAddress();
		dummyCon.close();
		StackConnector.properties.setProperty ("javax.sip.IP_ADDRESS", localAdd);  
        System.out.println("Stack initialized with IP: " + localAdd);
	}
	
	/**
	 * Open a SCN that will listen for incoming requests.
	 * 
	 * @param connectorString
	 *            defines on which port and transport to listen, like "sip:5060"
	 *            or "sip:5060;transport=tcp".
	 * @throws IOException
	 */
	public void openSipConnectionNotifier(String connectorString) throws IOException {
		connectionNotifier = (SipConnectionNotifier) SipConnector.open(connectorString);
		connectionNotifier.setListener(this);
		System.out.println("SipConnectionNotifier opened at: "
				+ connectionNotifier.getLocalAddress() + ":"
				+ connectionNotifier.getLocalPort());
	}
	
	/**
	 * Register to the SIP Registrar, via the outbound proxy.
	 * 
	 * @param domain
	 * @param uri
	 * @param username
	 * @param credUsername
	 * @param password
	 * @param realm
	 */
	public void register(String domain, String uri, String username,
			String credUsername, String password, String realm) {
		System.out.println("Registering to outbound proxy with domain " + domain);
		
		final String uri_ = uri;
		final String username_ = username;
		final String credUsername_ = credUsername;
		final String password_ = password;
		final String realm_ = realm;
		final String domain_ = domain;
		
		new Thread() {

			public void run() {
				try {
					registerScc = (SipClientConnection) SipConnector.open(uri_);
					registerScc.setListener(new SipClientConnectionListener() {
						public void notifyResponse(SipClientConnection scc) {
							try {
								scc.receive(0);
								int status = scc.getStatusCode();
								System.out.println("Response to register: " + status);;
								
								
								if ((status >= 100) && (status < 200)) {
									// provisional response
								} else if ((status >= 200) && (status < 300)) {
									onRegisterSuccess();
								} else if (status == 401 || status == 407) {
									// sip-for-me already handles authentication
									return;
								} else {
									onRegisterFailed("Negative response received");
								}
							} catch (Exception e) {
								System.err.println("Exception thrown while receiving REGISTER response " + String.valueOf(e));
								e.printStackTrace();
								onRegisterFailed("Exception thrown while receiving");
							}
						}			
					});
					
					registerScc.initRequest(Request.REGISTER, connectionNotifier);
					registerScc.setHeader(Header.FROM, uri_);
					registerScc.setHeader(Header.EXPIRES, "100");


					String contactHdr = username_ + "@"
							+ connectionNotifier.getLocalAddress() + ":"
							+ connectionNotifier.getLocalPort();
					registerScc.setHeader(Header.CONTACT, contactHdr);
					
					registerScc.setRequestURI("sip:" + domain_);
					
					if (credUsername_ != null && password_ != null
							&& realm_ != null)
						registerScc.setCredentials(credUsername_, password_,
								realm_);
					
					registerRefreshID = registerScc.enableRefresh(BasicMidlet.this);
					
					// Finally, send register
					registerScc.send();
					System.out.println("REGISTER sent");
					
					
					
				} catch (Exception e) {
					System.err.println("Exception sending register " + e.getMessage());
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * Stop refresh and send REGISTER with Expires = 0.
	 * @throws IOException
	 */
	private void unregister() throws IOException {
		if (registerScc != null && registerRefreshID > 0) {
			
			registerScc.setListener(new SipClientConnectionListener() {
				public void notifyResponse(SipClientConnection scc) {
					System.out.println("response to unregister");
					try {				
						scc.receive(0);
						int status = scc.getStatusCode();
						System.out.println("Accepted response to unregister: " + status);
						 if (status == 401 || status == 407) {
							 // sip-for-me already handles authentication
							 return;
						 } 
						 // mainly for 200 OK responses
						 else {					
							// Close connection and unset variables
							registerScc.close();
							registerScc = null;	
							registerRefreshID = -1;
						 }
					} catch (Exception e) {
						System.err.println("Problem unregistering: " + e.getMessage());
						e.printStackTrace();
					}
				}			
			});
			
			SipRefreshHelper.getInstance().stop(registerRefreshID);
		}
	}
	
	public void onRegisterSuccess() {
		System.out.println("Registered!");
	}
	
	public void onRegisterFailed(String reason) {
		System.err.println("Registration failed! Reason: " + reason);
	};
	
	/**
	 * Implementation of interface methods 
	 */
	public void notifyRequest(SipConnectionNotifier ssc) {
		System.out.println("Request received!");
	}

	public void refreshEvent(int refreshID, int statusCode, String reasonPhrase) {
		System.out.println("Refresh event for ID: " + refreshID + " sc: " + statusCode + " reason: " + reasonPhrase);
	}

}
