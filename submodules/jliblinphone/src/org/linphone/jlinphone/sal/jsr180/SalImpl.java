/*
SalImpl.java
Copyright (C) 2010  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package org.linphone.jlinphone.sal.jsr180;


import java.io.IOException;

import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;
import org.linphone.jortp.SocketAddress;
import org.linphone.sal.Sal;
import org.linphone.sal.SalAddress;
import org.linphone.sal.SalException;
import org.linphone.sal.SalFactory;
import org.linphone.sal.SalListener;
import org.linphone.sal.SalOp;


import sip4me.gov.nist.core.Debug;
import sip4me.gov.nist.core.LogWriter;
import sip4me.gov.nist.microedition.sip.SipConnector;
import sip4me.gov.nist.microedition.sip.StackConnector;
import sip4me.gov.nist.siplite.header.Header;
import sip4me.gov.nist.siplite.stack.ServerLog;
import sip4me.nist.javax.microedition.sip.SipClientConnection;
import sip4me.nist.javax.microedition.sip.SipConnectionNotifier;
import sip4me.nist.javax.microedition.sip.SipServerConnection;
import sip4me.nist.javax.microedition.sip.SipServerConnectionListener;

class SalImpl implements Sal, SipServerConnectionListener {
	private SipConnectionNotifier mConnectionNotifier;
	Logger mLog = JOrtpFactory.instance().createLogger("Sal");
	SipClientConnection mRegisterCnx;
	int mRegisterRefreshID;
	private SalListener mSalListener;
	private SalOp mIncallOp;
	private Transport mTransport;
	private String mPublicLocalAddress;
	private int mPublicLocalPort=-1;
	SalImpl() {
	
	}

	public void setIncallOp(SalOp anIncallOp) {
		mIncallOp = anIncallOp;
	}
	public void close() {
		if (mConnectionNotifier != null) {
			try {
				mConnectionNotifier.close();
			} catch (IOException e) {
				mLog.error("cannot close Sal connection", e);
			}
			mConnectionNotifier = null;
		}

	}


	public String getLocalAddr() throws SalException{
		try {
			if (mPublicLocalAddress!=null) {
				return mPublicLocalAddress;
			} else if (mConnectionNotifier != null) {
				return mConnectionNotifier.getLocalAddress();
			} else {
				throw new Exception("no notification listener");
			}

		} catch (Throwable e) {
			throw new SalException("Cannot get Local address from notification listener",e);
		}
	}
	public int getLocalPort() throws SalException {
		try {
			if (mPublicLocalPort!=-1) {
				return mPublicLocalPort;
			} else if (mConnectionNotifier != null) {
				return mConnectionNotifier.getLocalPort();
			} else {
				throw new Exception("no notification listener");
			}

		} catch (Throwable e) {
			throw new SalException("Cannot get Local port from notification listener",e);
		}
	}
	public void listenPort(SocketAddress addr, Transport t, boolean isSecure) throws SalException {
		// Configure logging of the stack
        try {
        
		Debug.enableDebug(false);
		LogWriter.needsLogging = true;
		
		ServerLog.setTraceLevel(ServerLog.TRACE_NONE);
		
        StackConnector.properties.setProperty ("javax.sip.RETRANSMISSION_FILTER", "on");
        if (!StackConnector.properties.containsKey("sip4me.gov.nist.javax.sip.NETWORK_LAYER")) {
        		StackConnector.properties.setProperty("sip4me.gov.nist.javax.sip.NETWORK_LAYER", "sip4me.gov.nist.core.net.BBNetworkLayer");		
        }
        StackConnector.properties.setProperty ("javax.sip.IP_ADDRESS", addr.getHost());  
		mLog.info("Stack initialized with IP: " + addr.getHost());
        String SipConnectorUri = "sip:";
//        if (addr.getHost().equalsIgnoreCase("0.0.0.0")) {
        	SipConnectorUri+=addr.getPort();
//        } else {
//        	SipConnectorUri+="anonymous@"+addr.getHost()+":"+addr.getPort();
//        }
        mTransport =t;
        if (t == Transport.Stream) {
        	SipConnectorUri+=";transport=tcp";
        }
        mConnectionNotifier = (SipConnectionNotifier) SipConnector.open(SipConnectorUri);
        mConnectionNotifier.setListener(this);
		System.out.println("SipConnectionNotifier opened at: "
				+ mConnectionNotifier.getLocalAddress() + ":"
				+ mConnectionNotifier.getLocalPort());
		
        } catch (Exception e) {
        	throw new SalException("Cannot listen port for ["+addr+"] reason ["+e.getMessage()+"]",e);
        }
	}



	public void setListener(SalListener listener) {
		mSalListener = listener;
	}

	public void setUserAgent(String ua) {
		// TODO Auto-generated method stub

	}
	public void setPublicLocalAddress(String aPublicLocalAddress) {
		mPublicLocalAddress=aPublicLocalAddress;
	}
	public void setPublicLocalPort(int aPublicLocalPort) {
		mPublicLocalPort=aPublicLocalPort;
	}


	public void notifyRequest(SipConnectionNotifier ssc) {
		SipServerConnection lCnx=null;
		try {
			lCnx = ssc.acceptAndOpen();
			mLog.info("receiving request: "+lCnx.getMethod()+" " +lCnx.getRequestURI());
			if ("INVITE".equals(lCnx.getMethod())) {
				SalOp lOp = new SalOpImpl(this,mConnectionNotifier,mSalListener,lCnx);
				SalAddress lFrom = SalFactory.instance().createSalAddress(lCnx.getHeader(Header.FROM));
				SalAddress lTo = SalFactory.instance().createSalAddress(lCnx.getHeader(Header.TO));
				lOp.setFrom(lFrom.asStringUriOnly());
				lOp.setTo(lTo.asStringUriOnly());
				lCnx.initResponse(100);
				lCnx.send();
				mIncallOp = lOp;
				mSalListener.onCallReceived(lOp);
				
			} else if (mIncallOp !=null) {
				((SalOpImpl)mIncallOp).notifyRequestReceived(lCnx);
			} else {
				lCnx.initResponse(500);
				lCnx.send();
			}
		} catch (Throwable e) {
			if (lCnx !=null) {
				mLog.error("Cannot answer to : "+lCnx.getMethod()+" " +lCnx.getRequestURI(), e);
			} else {
				mLog.error("Unknown error while processing Request",e);
			}
		}
		
		
	}
	public SalOp createSalOp() {
		return new SalOpImpl(this,mConnectionNotifier,mSalListener);
	}
	public Transport getTransport() {
		return mTransport;
	}





}
