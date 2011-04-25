/*
SalOpImpl.java
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
import java.io.InputStream;
import java.io.OutputStream;


import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;
import org.linphone.sal.OfferAnswerHelper;
import org.linphone.sal.Sal;
import org.linphone.sal.SalAddress;
import org.linphone.sal.SalAuthInfo;
import org.linphone.sal.SalError;
import org.linphone.sal.SalException;
import org.linphone.sal.SalFactory;
import org.linphone.sal.SalListener;
import org.linphone.sal.SalMediaDescription;
import org.linphone.sal.SalOpBase;
import org.linphone.sal.SalReason;

import org.linphone.sal.OfferAnswerHelper.AnswerResult;
import org.linphone.sal.Sal.Reason;

import sip4me.gov.nist.javax.sdp.SdpException;
import sip4me.gov.nist.javax.sdp.SdpFactory;
import sip4me.gov.nist.javax.sdp.SdpParseException;
import sip4me.gov.nist.javax.sdp.SessionDescription;
import sip4me.gov.nist.microedition.sip.SipConnector;
import sip4me.gov.nist.siplite.header.Header;
import sip4me.gov.nist.siplite.message.Request;
import sip4me.nist.javax.microedition.sip.SipClientConnection;
import sip4me.nist.javax.microedition.sip.SipClientConnectionListener;

import sip4me.nist.javax.microedition.sip.SipConnectionNotifier;
import sip4me.nist.javax.microedition.sip.SipDialog;
import sip4me.nist.javax.microedition.sip.SipException;
import sip4me.nist.javax.microedition.sip.SipHeader;
import sip4me.nist.javax.microedition.sip.SipRefreshHelper;
import sip4me.nist.javax.microedition.sip.SipRefreshListener;
import sip4me.nist.javax.microedition.sip.SipServerConnection;


class SalOpImpl extends SalOpBase implements SipRefreshListener {
	static Logger mLog = JOrtpFactory.instance().createLogger("Sal");
	SalAuthInfo mAuthInfo;
	SipClientConnection mClientCnx;
	SipServerConnection mInviteServerTransaction;
	final SipConnectionNotifier mConnectionNotifier;
	SalMediaDescription mLocalSalMediaDescription;
	SalMediaDescription mFinalSalMediaDescription;
	final SalListener mSalListener;
	SipDialog mDialog;
	final static int REGISTER_REFRES_HID_NOT_SET=-1;
	int mRegisterRefreshId=REGISTER_REFRES_HID_NOT_SET;
	
	public SalOpImpl(Sal sal, SipConnectionNotifier aConnectionNotifier, SalListener aSalListener) {
		super(sal);
		mConnectionNotifier = aConnectionNotifier;
		mSalListener = aSalListener;
	}
	public SalOpImpl(Sal sal, SipConnectionNotifier aConnectionNotifier, SalListener aSalListener,SipServerConnection aServerInviteTransaction) {
		this(sal,aConnectionNotifier,aSalListener);
		mInviteServerTransaction = aServerInviteTransaction;
	}

	public void authenticate(SalAuthInfo info) throws SalException {
		mAuthInfo = info;
		try {
			if (mClientCnx != null) {
				if ( info != null) {
					mClientCnx.setCredentials(info.getUserid(), info.getPassword(),info.getRealm());
				} else {
					throw new Exception("Bad auth info ["+info+"]");
				}
			} else {
				mLog.warn("no registrar connection ready yet");
			}
		} catch (Exception e) {
			throw new SalException("Cannot authenticate",e);
		}
	}

	public void register(String proxy, String from, int expires) throws SalException {
			// save from
			setFrom(from);
			setTo(from);
			setRoute(proxy);

			
			
			try {
				
				final SalAddress lAddress = SalFactory.instance().createSalAddress(from);
				mClientCnx = (SipClientConnection) SipConnector.open(lAddress.asStringUriOnly());
				
				mClientCnx.initRequest(Request.REGISTER, mConnectionNotifier);
				mClientCnx.setHeader(Header.FROM, from);
				mClientCnx.setHeader(Header.EXPIRES, String.valueOf(expires));
				if (proxy != null && proxy.length()>0) {
					mClientCnx.setHeader(Header.ROUTE, getRouteHeaderValue(proxy));
				}
				
				
				String contactHdr = "sip:"+lAddress.getUserName() 	+ "@"
															+ getSal().getLocalAddr() + ":"
															+ getSal().getLocalPort();
				mClientCnx.setHeader(Header.CONTACT, contactHdr);

				mClientCnx.setRequestURI("sip:"+lAddress.getDomain());
				mClientCnx.setListener(new SipClientConnectionListener() {
					boolean mAuthSucceded=false;
					public void notifyResponse(SipClientConnection scc) {
						// positione credential
						try {
							scc.receive(0);
							SipHeader lViaHeader = new SipHeader(Header.VIA,scc.getHeader(Header.VIA));
							if (lViaHeader.getParameter("received") !=null) {
								((SalImpl)getSal()).setPublicLocalAddress(lViaHeader.getParameter("received"));
							}
							if (lViaHeader.getParameter("rport") !=null) {
								((SalImpl)getSal()).setPublicLocalPort(Integer.parseInt(lViaHeader.getParameter("rport")));
							}
							switch (scc.getStatusCode()) {
							case 401:
							case 407:
								if(mAuthSucceded == false) {
									SipHeader lAuthHeader = getAuthHeader(scc);
									if (lAuthHeader != null) {
										mSalListener.onAuthRequested(SalOpImpl.this,lAuthHeader.getParameter("realm"),lAddress.getUserName());
									} else {
										mLog.error("Cannot get authentication header");
									}
								} //else already authenticated
								break;
								
							case 200:
								if(mAuthSucceded == false && getAuthInfo()!=null) {
									mSalListener.onAuthSuccess(SalOpImpl.this,getAuthInfo().getRealm(),getAuthInfo().getUsername());
									mAuthSucceded=true;
								}
								mSalListener.OnRegisterSuccess(SalOpImpl.this, true);
								break;
							default: 
								if (scc.getStatusCode()>=500) {
									mSalListener.OnRegisterFailure(SalOpImpl.this, SalError.Failure, SalReason.Unknown, scc.getReasonPhrase());
								} else {
									mLog.error("Unexpected answer ["+scc+"]");
									
								}
							}
						} catch (Throwable e) {
							mLog.error("Cannot process REGISTER answer", e);
						} 
						
					}
					
				});

				 
				if (expires > 0) {
					mRegisterRefreshId = mClientCnx.enableRefresh(this);
				}

				// Finally, send register
				mClientCnx.send();
				mLog.info("REGISTER sent from ["+lAddress+"] to ["+proxy+"]");
			} catch (Throwable e) {
				throw new SalException("cannot send register  from ["+from+"] to ["+proxy+"]",e);
			}


	}
	public void call() throws SalException {
		
		try {
			SalAddress lToAddress = SalFactory.instance().createSalAddress(getTo());
			
			/*if (lToAddress.getPortInt() < 0) {
				lToAddress.setPortInt(5060);
			}
			*/
			mClientCnx = (SipClientConnection) SipConnector.open(lToAddress.asStringUriOnly());
			mClientCnx.initRequest(Request.INVITE,mConnectionNotifier);
			mClientCnx.setHeader(Header.FROM, getFrom());
			mClientCnx.setRequestURI(lToAddress.asStringUriOnly());
			mClientCnx.setHeader(Header.CONTENT_TYPE, "application/sdp");
			if (getRoute() != null && getRoute().length()>0) {
				mClientCnx.setHeader(Header.ROUTE, getRouteHeaderValue(getRoute()));
			}
			final SalAddress lFromAddress = SalFactory.instance().createSalAddress(getFrom());
			String contactHdr = "sip:"+lFromAddress.getUserName() 	+ "@"
									+ getSal().getLocalAddr() + ":"
									+ getSal().getLocalPort();
			mClientCnx.setHeader(Header.CONTACT, contactHdr);
			
			if (mAuthInfo !=null) {
				mClientCnx.setCredentials(mAuthInfo.getUserid(), mAuthInfo.getPassword(),mAuthInfo.getRealm());
			}

			String lSdp = mLocalSalMediaDescription.toString();
			mClientCnx.setHeader(Header.CONTENT_LENGTH, String.valueOf(lSdp.length()));
			mClientCnx.openContentOutputStream().write(lSdp.getBytes("US-ASCII"));
			mClientCnx.setListener(new SipClientConnectionListener() {
				private void computeFinalSalMediaDesc() throws SipException, IOException, SdpParseException, SdpException, SalException {
					InputStream lSdpInputStream = mClientCnx.openContentInputStream();
					byte [] lRawSdp = new byte [lSdpInputStream.available()];
					lSdpInputStream.read(lRawSdp);
					
					SessionDescription lSessionDescription  = SdpFactory.getInstance().createSessionDescription(new String(lRawSdp)) ;
					SalMediaDescription lRemote = SdpUtils.toSalMediaDescription(lSessionDescription);
					mFinalSalMediaDescription = OfferAnswerHelper.computeOutgoing(mLocalSalMediaDescription, lRemote);
				}
				public void notifyResponse(SipClientConnection scc) {
					try {
						scc.receive(0);
						switch (scc.getStatusCode()) {
							
						case 200:
							computeFinalSalMediaDesc();
							mDialog=mClientCnx.getDialog(); 
							mClientCnx.initAck();
							mClientCnx.send();
							
							mSalListener.onCallAccepted(SalOpImpl.this);
							break;
						case 183:
							if (mFinalSalMediaDescription != null) {
								//already in early media
								break;
							}
							computeFinalSalMediaDesc();
						case 180:
							mSalListener.onCallRinging(SalOpImpl.this);
							break;
						case 401:
						case 407:
							if (mAuthInfo == null) {
								SipHeader lAuthHeader = getAuthHeader(scc);
								if (lAuthHeader != null) {
									mSalListener.onAuthRequested(SalOpImpl.this,lAuthHeader.getParameter("realm"),lFromAddress.getUserName());
								} else {
									mSalListener.onCallFailure(SalOpImpl.this,"Cannot find Auth info from sip message");
								}
							}
							break;
						case 487:
							//nop request terminated
							break;
						default:
							if (scc.getStatusCode() > 300) {
								mSalListener.onCallFailure(SalOpImpl.this,scc.getReasonPhrase());
							} else {
								mLog.warn("Unexpected answer ["+scc.getStatusCode()+" "+scc.getRequestURI()+"]");
							}
						}
					} catch (Throwable e) {
						mLog.error("cannot handle invite answer", e);
						mSalListener.onCallFailure(SalOpImpl.this,e.getMessage());
					}
					
				}
			});
			mClientCnx.send();
			((SalImpl)getSal()).setIncallOp(this);
		} catch (Throwable e) {
			throw new SalException(e);
		}

	}

	public void callAccept() throws SalException {
		try {
			InputStream lSdpInputStream = mInviteServerTransaction.openContentInputStream();
			byte [] lRawSdp = new byte [lSdpInputStream.available()];
			lSdpInputStream.read(lRawSdp);
			SessionDescription lSessionDescription;
			try {
				lSessionDescription  = SdpFactory.getInstance().createSessionDescription(new String(lRawSdp)) ;
			}catch(SdpParseException e) {
				throw new SalException("Parser error, cannot parse incoming sdp",e);
			}
			
			SalMediaDescription lRemote = SdpUtils.toSalMediaDescription(lSessionDescription);
			
			AnswerResult lAnswerResult = OfferAnswerHelper.computeIncoming(mLocalSalMediaDescription, lRemote);
			if (lAnswerResult.getResult().getNumStreams() == 0) {
				mLog.warn("No codec matching");
				mInviteServerTransaction.initResponse(404);
				mInviteServerTransaction.send();
				mSalListener.onCallFailure(this, "no matching codecs");
			} else {
				mFinalSalMediaDescription = lAnswerResult.getResult();
				mInviteServerTransaction.initResponse(200);
				mInviteServerTransaction.setHeader(Header.CONTENT_TYPE, "application/sdp");
				
				String lSdp = lAnswerResult.getAnswer().toString();
				mInviteServerTransaction.setHeader(Header.CONTENT_LENGTH, String.valueOf(lSdp.length()));
				mInviteServerTransaction.openContentOutputStream().write(lSdp.getBytes("US-ASCII"));
				mInviteServerTransaction.send();
				mDialog = mInviteServerTransaction.getDialog();
				
			}
			
			
			
			
		} catch (Throwable e) {
			throw new SalException(e);
		}

	}

	public void callDecline(Reason r, String redirectUri) {
			if (mInviteServerTransaction != null) {
				try {
					int lReason;
					if (r == Reason.Busy) {
						lReason=486;
					} else if (r == Reason.Declined){
						lReason=603;
					} else {
						lReason=603;
					}
					mInviteServerTransaction.initResponse(lReason);
					mInviteServerTransaction.send();
					mSalListener.onCallTerminated(this);
					mInviteServerTransaction=null;
					
					

				} catch (Throwable e) {
					mLog.error("cannot cancel call", e);
				} 
			}
		
	}

	public void callSetLocalMediaDescription(SalMediaDescription md) {
		mLocalSalMediaDescription = md;

	}

	public void callTerminate() {
		try {
			if (mDialog != null) {
				SipClientConnection lByeConnection = mDialog.getNewClientConnection(Request.BYE);
				lByeConnection.send();
			} else if (mClientCnx != null) {
					SipClientConnection lSipCancelCnx = mClientCnx.initCancel();
					lSipCancelCnx.send();
			}
			
		} catch (Throwable e) {
			mLog.error("cannot terminate call", e);
		}
		finally {
			mSalListener.onCallTerminated(SalOpImpl.this);
			((SalImpl)getSal()).setIncallOp(null);
		}
	}
	public SalAuthInfo getAuthInfo() {
		return mAuthInfo;
	}
	public SalMediaDescription getFinalMediaDescription() {
		return mFinalSalMediaDescription;
	}
	public void callRinging() throws SalException{
		try { 
			if (mInviteServerTransaction != null) {
				mInviteServerTransaction.initResponse(180);
				mInviteServerTransaction.send();
			} else {
				throw new SalException("no in a proper state");
			}
		} catch (Throwable e) {
			throw new SalException(e);
		}



	}

	public void refreshEvent(int refreshID, int statusCode, String reasonPhrase) {
		// TODO Auto-generated method stub
		
	}
	private SipHeader getAuthHeader(SipClientConnection scc) {
		SipHeader lAuthHeader=null;
		if (scc.getHeader(Header.PROXY_AUTHENTICATE) != null) {
			lAuthHeader = new SipHeader(Header.AUTHORIZATION,scc.getHeader(Header.PROXY_AUTHENTICATE));
		} else if (scc.getHeader(Header.WWW_AUTHENTICATE) != null){
			lAuthHeader = new SipHeader(Header.WWW_AUTHENTICATE,scc.getHeader(Header.WWW_AUTHENTICATE));
		}
		return lAuthHeader;
	}
	public void unregister() {
		if (mClientCnx != null) {
			try {
				SipRefreshHelper.getInstance().stop(mRegisterRefreshId);
				//register(getRoute(),getFrom(),0);
			} catch (Throwable e) {
				mLog.error("Cannot unregister",e);
			}
		} else {
			mLog.error("not register");
		}

	}
	public void notifyRequestReceived(SipServerConnection cnx) {
		try {
			if ("BYE".equals(cnx.getMethod()) || "CANCEL".equals(cnx.getMethod())) {
				mSalListener.onCallTerminated(this);
				cnx.initResponse(200);
				cnx.send();
				if ("CANCEL".equals(cnx.getMethod()) && mInviteServerTransaction != null) {
					mInviteServerTransaction.initResponse(487);
					mInviteServerTransaction.send();
				}
				((SalImpl)getSal()).setIncallOp(null);	
			} else {
				cnx.initResponse(500);
				cnx.send();
			}
		} catch (Throwable e) {
			mLog.error("Cannot answer to : "+cnx.getMethod()+" " +cnx.getRequestURI(), e);
		}
	}
	private String getRouteHeaderValue(String proxy) {
		String lRouteHeaderValue=proxy;
		
		if (getSal().getTransport() != null && getSal().getTransport()== Sal.Transport.Stream) {
			final SalAddress lProxy = SalFactory.instance().createSalAddress(proxy);
			if (lProxy.getTransport() == null) {
				lProxy.setTransport(Sal.Transport.Stream);
			}
			lRouteHeaderValue=lProxy.asStringUriOnly();
		}
		
		return 	lRouteHeaderValue+";lr";	
	}
	public void sendDtmf(char number) {
		mLog.info("sending dtmf ["+number+"]");
		try {
			if (mDialog != null) {
				SipClientConnection lInfoConnection = mDialog.getNewClientConnection(Request.INFO);
				lInfoConnection.setHeader(Header.CONTENT_TYPE, "application/dtmf-relay");
				String ldtmf = new String("Signal="+number+"\r\nDuration=250\r\n");
				lInfoConnection.setHeader(Header.CONTENT_LENGTH, String.valueOf(ldtmf.length()));
				OutputStream lDtmfOutputStream = lInfoConnection.openContentOutputStream();
				lDtmfOutputStream.write(ldtmf.getBytes("US-ASCII"));
				
				lInfoConnection.send();
			} 
		} catch (Throwable e) {
			mLog.error("cannot send dtmf ["+number+"]", e);
		}
		
	}
}
