/*
LinphoneCoreImpl.java
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
package org.linphone.jlinphone.core;



import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.ToneControl;


import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.media.control.AudioPathControl;
import net.rim.device.api.system.ControlledAccessException;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.WLANInfo;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.plazmic.mediaengine.MediaPlayer;

import org.linphone.core.CallDirection;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallLog;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneFriend;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.OnlineStatus;
import org.linphone.core.PayloadType;
import org.linphone.core.VideoSize;
import org.linphone.core.LinphoneCallLog.CallStatus;
import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;
import org.linphone.jortp.RtpTransport;
import org.linphone.jortp.SocketAddress;
import org.linphone.sal.Sal;
import org.linphone.sal.SalAuthInfo;
import org.linphone.sal.SalError;
import org.linphone.sal.SalException;
import org.linphone.sal.SalFactory;
import org.linphone.sal.SalListener;
import org.linphone.sal.SalMediaDescription;
import org.linphone.sal.SalOp;
import org.linphone.sal.SalReason;
import org.linphone.sal.Sal.Reason;


public class LinphoneCoreImpl implements LinphoneCore {
	Sal mSal;
	LinphoneAuthInfo mAuthInfo;
	LinphoneProxyConfigImpl mProxyCfg;
	LinphoneCallImpl mCall;
	LinphoneCoreListener mListener;

	Logger mLog = JOrtpFactory.instance().createLogger("LinphoneCore");
	Player mRingPlayer;
	Player mRingBackPlayer;
	Vector mCallLogs;
	private PersistentObject mPersistentObject;
	boolean mNetworkIsUp=false;
	Sal.Transport mTransport = Sal.Transport.Datagram;
	int mSipPort=5060;
	RtpTransport mRtpTransport;
	SalListener mSalListener= new SalListener(){

		public void onCallAccepted(SalOp op) {
			LinphoneCallImpl c=(LinphoneCallImpl)op.getUserContext();
			SalMediaDescription oldmd=null;
			boolean keepStreams=false;
			
			if (mCall==null || mCall!=c){
				op.callTerminate();
			}
			oldmd=c.getFinalMediaDescription();
			c.setFinalMediaDescription(op.getFinalMediaDescription());
			deallocateRingBackPlayerIfNeeded();
			
			if (oldmd!=null){
				if (oldmd.equals(op.getFinalMediaDescription())){
					mLog.info("Early media and 200 Ok media descriptions are equal, keeping same streams");
					keepStreams=true;
				}else 
					mLog.info("Early media and 200 Ok media descriptions are different, need to restart streams");
			}
			
			if (c.getAudioStream().isStarted() && !keepStreams) {
				c.terminateMediaStreams(); //comes from; early media state
				try {
					mCall.initMediaStreams();
				} catch (SalException e) {
					mLog.error("Cannot create media stream",e);
					op.callTerminate();
					return;
				}
			}
			c.setState(LinphoneCall.State.Connected);
			if (!keepStreams) c.startMediaStreams();
		}

		public void onCallReceived(SalOp op) {
			try {
				
				if (mCall!=null || Phone.getActiveCall() != null){
					op.callDecline(Sal.Reason.Busy, null);
				}else{
					mCall=createIncomingCall(op);
					mCall.setState(LinphoneCall.State.IncomingReceived);
					mCallLogs.addElement(mCall.getCallLog());

					op.callRinging();
					mRingPlayer = Manager.createPlayer(getClass().getResourceAsStream("/oldphone.wav"),"audio/wav");
					mRingPlayer.realize();
					mRingPlayer.prefetch();
					mRingPlayer.setLoopCount(Integer.MAX_VALUE);
					AudioPathControl  lPathCtr = (AudioPathControl) mRingPlayer.getControl("net.rim.device.api.media.control.AudioPathControl");
					lPathCtr.setAudioPath(AudioPathControl.AUDIO_PATH_HANDSFREE);
					mRingPlayer.start();
					
					
				} 
			}catch (Throwable e) {
				mLog.error("Cannot create incoming call",e);
				mCall.terminateMediaStreams();
				op.callDecline(Sal.Reason.Unknown, null);
				return;
			}

		}

		public void onCallRinging(SalOp op) {
			LinphoneCallImpl c=(LinphoneCallImpl)op.getUserContext();
			if (mCall==null || mCall!=c){
				op.callTerminate();
			}
			if (mCall!=null && op.getFinalMediaDescription() == null){
				mCall.setState(LinphoneCall.State.OutgoingRinging);
				try {
					mRingBackPlayer =Manager.createPlayer(getClass().getResourceAsStream("/ringback.wav"),"audio/wav");
					mRingBackPlayer.realize();
					mRingBackPlayer.prefetch();
					AudioPathControl  lPathCtr = (AudioPathControl) mRingBackPlayer.getControl("net.rim.device.api.media.control.AudioPathControl");
					lPathCtr.setAudioPath(AudioPathControl.AUDIO_PATH_HANDSET);
					mRingBackPlayer.setLoopCount(-1);
					mRingBackPlayer.start();
				}catch (Throwable e) {
					mLog.error("cannot play ringback tone", e);
					deallocateRingBackPlayerIfNeeded();
					return;
				}
			}
			if (op.getFinalMediaDescription() !=null && c.getFinalMediaDescription()==null) {
				mCall.setState(LinphoneCall.State.OutgoingEarlyMedia);
				deallocateRingBackPlayerIfNeeded(); //just in case
				c.setFinalMediaDescription(op.getFinalMediaDescription());
				c.startMediaStreams();
			}
		}

		public void onCallTerminated(SalOp op) {
			if (mCall!=null){
				deallocateRingBackPlayerIfNeeded();
				deallocateRingPlayerIfNeeded();
				mCall.terminateMediaStreams();
				mCall.setState(LinphoneCall.State.CallEnd);
				mCall=null;
			}
		}

		public void onAuthRequested(SalOp op, String realm, String userName) {
			try {
				if (mAuthInfo != null && userName.equalsIgnoreCase(mAuthInfo.getUsername())) {
					op.authenticate( new SalAuthInfo(realm,userName,mAuthInfo.getPassword()));
				} else {
					mListener.authInfoRequested(LinphoneCoreImpl.this, realm, userName);
				}
			} catch (Exception e) {
				mLog.error("Cannot provide auth info", e);
			}

		}

		public void onAuthSuccess(SalOp lSalOp, String realm, String username) {
			mProxyCfg.setRegistered(true);
			LinphoneAddress lTo = LinphoneCoreFactory.instance().createLinphoneAddress(lSalOp.getTo());
			mListener.displayStatus(LinphoneCoreImpl.this,"Registered to "+lTo.getDomain());
		}

		public void onCallFailure(SalOp op, String reasonPhrase) {
			deallocateRingBackPlayerIfNeeded();
			mCall.terminateMediaStreams();
			mCall.setState(LinphoneCall.State.Error,reasonPhrase);
			mCall=null;
			
		}

		public void OnRegisterFailure(SalOp op, SalError error,
				SalReason reason, String details) {
			mListener.displayStatus(LinphoneCoreImpl.this,"Registration failure ["+details+"]");
			mListener.registrationState(LinphoneCoreImpl.this, getDefaultProxyConfig(),RegistrationState.RegistrationFailed,details);
			
		}

		public void OnRegisterSuccess(SalOp op, boolean registered) {
			mListener.registrationState(LinphoneCoreImpl.this, getDefaultProxyConfig()
										,registered? RegistrationState.RegistrationOk:RegistrationState.RegistrationCleared,null);
			
		}
		
	};


	private LinphoneCallImpl createIncomingCall(SalOp op) throws SalException{
		return new LinphoneCallImpl(this,op,CallDirection.Incoming);
	}
	private LinphoneCallImpl createOutgoingCall(LinphoneAddress addr) throws SalException{
		SalOp op= mSal.createSalOp();
		//is route header required ?
		if (getDefaultProxyConfig() != null
				&& getDefaultProxyConfig().getDomain().equalsIgnoreCase(addr.getDomain())) {
			op.setRoute(getDefaultProxyConfig().getProxy());
		}
		op.setFrom(LinphoneCoreImpl.this.getIdentity());
		op.setTo(addr.asString());
		LinphoneCallImpl c=new LinphoneCallImpl(this,op,CallDirection.Outgoing);
		return c;
	}
	
	private String getIdentity() throws SalException {
		if (mProxyCfg!=null){
			return mProxyCfg.getIdentity();
		}
		return "sip:anonymous@"+mSal.getLocalAddr()+":"+Integer.toString(mSipPort);
	}


	
	public LinphoneCoreImpl(LinphoneCoreListener listener, String userConfig,
			String factoryConfig, Object userdata) throws LinphoneCoreException{

		try {
			mPersistentObject = PersistentStore.getPersistentObject( "org.jlinphone.logs".hashCode() );
			if (mPersistentObject.getContents() != null) {
				mCallLogs = (Vector) mPersistentObject.getContents();
				//limit Call logs number
				while (mCallLogs.size()>30) {
					mCallLogs.removeElementAt(0);
				}
			} else {
				mCallLogs = new Vector();
			}
			
			mListener=listener;
			mListener.globalState(this, GlobalState.GlobalOn,null);
			createSal();
		}  catch (ControlledAccessException e1) {
			throw new LinphoneCoreException("Persistent store permission denied");
		}  
		catch (Throwable e ) {
			destroy();
			mLog.error("Cannot create Linphone core for user conf ["
					+userConfig
					+"] factory conf ["+factoryConfig+"]",e);
			throw new LinphoneCoreException("Initialization failure caused by: "+e.getMessage());

		}
	}

	public void acceptCall(LinphoneCall aCall) throws LinphoneCoreException {
		if (mCall!=null){
			try {
				mCall.getOp().callAccept();
				mCall.setFinalMediaDescription(mCall.getOp().getFinalMediaDescription());
				mCall.setState(LinphoneCall.State.Connected,null);
				deallocateRingPlayerIfNeeded();
				mCall.startMediaStreams();
			} catch (Throwable e) {
				throw new LinphoneCoreException("cannot accept call from ["+getRemoteAddress()+"]");
			}
		}
	}

	public void addAuthInfo(LinphoneAuthInfo info) {
		 mAuthInfo=info;
	}

	public void addProxyConfig(LinphoneProxyConfig proxyCfg)
			throws LinphoneCoreException {
		mProxyCfg=(LinphoneProxyConfigImpl) proxyCfg;
		proxyCfg.done();
	}

	public void clearAuthInfos() {
		mAuthInfo=null;
	}

	public void clearCallLogs() {
		mCallLogs.removeAllElements();
	}

	public void clearProxyConfigs() {
		mProxyCfg=null;
	}

	public void destroy() {
		if (isIncall()) {
			terminateCall(mCall);
		}
		if (mSal != null) {
			mSal.close();
			mSal=null;
		}
		if (mPersistentObject != null) {
			mPersistentObject.setContents(mCallLogs);
			mPersistentObject.commit();
		}
		if (mListener !=null) mListener.globalState(this, GlobalState.GlobalOff,null);
	}

	public Vector getCallLogs() {
		return mCallLogs;
	}

	public LinphoneProxyConfig getDefaultProxyConfig() {
		return mProxyCfg;
	}

	public LinphoneAddress getRemoteAddress() {
		if (mCall!=null) return mCall.getRemoteAddress();
		return null;
	}

	public float getSoftPlayLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	public LinphoneAddress interpretUrl(String rawDestination)
			throws LinphoneCoreException {
		//remove space and -
		String lDest = rawDestination.trim(); 
		StringBuffer lFilteredDestination=new StringBuffer();
		for (int i=0;i<lDest.length();i++) {
			char lCurrentChar = lDest.charAt(i);
			if ( lCurrentChar!= ' ' && lCurrentChar != '-' ) {
				lFilteredDestination.append(lCurrentChar);
			}
		}
		lDest = lFilteredDestination.toString();
		LinphoneAddress addr=LinphoneCoreFactory.instance().createLinphoneAddress(lDest);
		if (addr==null){
			if (lDest.indexOf("@") == -1){
				LinphoneProxyConfigImpl cfg=(LinphoneProxyConfigImpl) getDefaultProxyConfig();
				if (cfg!=null){

					LinphoneAddress tmp=LinphoneCoreFactory.instance()
						.createLinphoneAddress(cfg.getIdentity());
					tmp.setDisplayName(null);
					if (lDest.startsWith("+") && cfg.isDialPlusEscaped()) {
						tmp.setUserName("00"+lDest.substring(1));
					} else {
						tmp.setUserName(lDest);
					}
					return tmp;
				}else throw new LinphoneCoreException("Bad destination ["+lDest+"]");
			}else{
				addr=LinphoneCoreFactory.instance().createLinphoneAddress(
						"sip:"+lDest);
				if (addr==null){
					throw new LinphoneCoreException("Bad destination ["+lDest+"]");
				}
			}
		}
		return addr;
	}

	public LinphoneCall invite(String address) throws LinphoneCoreException {
		LinphoneAddress addr=interpretUrl(address);
		return invite(addr);
	}

	public LinphoneCall invite(LinphoneAddress to) throws LinphoneCoreException {
		if (mCall!=null){
			return null;
		}
		try {
			
			LinphoneCallImpl c=createOutgoingCall(to);
			c.getOp().call();
			mCall=c;
			c.setState(LinphoneCall.State.OutgoingInit,null);
			mCallLogs.addElement(c.getCallLog());
			return c;
			
		} catch (Throwable e) {
			terminateCall(mCall);
			throw new LinphoneCoreException("Cannot place call to ["+to+"]",e);
			
		}
	}

	public boolean isInComingInvitePending() {
		return mCall!=null 
			&& mCall.getState()==LinphoneCall.State.IncomingReceived;
	}

	public boolean isIncall() {
		return mCall!=null;
	}

	public boolean isMicMuted() {
		if (mCall != null && mCall.getAudioStream()!= null) 
			return mCall.getAudioStream().isMicMuted();
		else
			return false;
	}

	public synchronized void iterate() {
		if (mSal != null  && mSal.getTransport() != mTransport) {
			//transport has changed, reconfiguring
			closeSal();
		}
		if (mSal == null && mNetworkIsUp ==true) {
			try {
				  //create new sal
                createSal();
				if (mProxyCfg != null && mProxyCfg.registerEnabled()) {
//					mProxyCfg.edit();
//					mProxyCfg.enableRegister(false);
//					mProxyCfg.done();
//					//send unregister
//					((LinphoneProxyConfigImpl)mProxyCfg).check(this);
//					mProxyCfg.edit();
//					mProxyCfg.enableRegister(true);
					mProxyCfg.done();
				}

			} catch (Throwable e) {

				mLog.error("Cannot create listening point",e);
			}  
		} else if (mSal !=null && mNetworkIsUp == false) {
			closeSal();
		}
		if (mSal!=null && mProxyCfg!=null)
			((LinphoneProxyConfigImpl)mProxyCfg).check(this);
	}

	public void muteMic(boolean isMuted) {
		if (mCall != null && mCall.getAudioStream()!= null) 
			mCall.getAudioStream().muteMic(isMuted);

	}

	public void sendDtmf(char number) {
		if (mCall != null ) {
			mCall.sendDtmf(number);
		}
	}

	public void setDefaultProxyConfig(LinphoneProxyConfig proxyCfg) {
		mProxyCfg=(LinphoneProxyConfigImpl) proxyCfg;
	}

	public void setNetworkReachable(boolean isReachable) {
		
		if (mNetworkIsUp != isReachable) {
			mLog.warn("New network state is ["+isReachable+"]");
		}
		mNetworkIsUp=isReachable;

	}

	public void setSoftPlayLevel(float gain) {
		// TODO Auto-generated method stub

	}

	public void terminateCall(LinphoneCall aCall) {
		if (mCall!=null){
			if (isInComingInvitePending()) {
				try {
					mRingPlayer.stop();
					mRingPlayer.close();
					mRingPlayer=null;
				} catch (MediaException e) {
					mLog.error("cannot stop ringer", e);
				}
				mCall.getOp().callDecline(Reason.Declined, null);
			} else {
				deallocateRingBackPlayerIfNeeded();
				mCall.terminateMediaStreams();
				mCall.getOp().callTerminate();
			}
			if (mCall !=null && mCall.getState() != LinphoneCall.State.CallEnd) {
				mCall.setState(LinphoneCall.State.CallEnd, null);
			}
			mCall=null;
		}
	}
	
	public Sal getSal(){
		return mSal;
	}
	public void setSignalingTransport(Transport aTransport) {
		if (aTransport == Transport.udp) {
			mTransport = Sal.Transport.Datagram;
		} else if (aTransport == Transport.tcp) {
			mTransport = Sal.Transport.Stream; 
		} else {
			mLog.error("Unexpected transport ["+aTransport+"]");
		}
	}
	private void closeSal() {
		if (isIncall()) {
			terminateCall(mCall);
		}
		if (mProxyCfg != null && mProxyCfg.isRegistered() ) {
			if (mNetworkIsUp == true) {
				mProxyCfg.edit(); // just to trigger unregister
				mProxyCfg.done();
			} else {
				//just notify registration is gone
				mListener.displayStatus(LinphoneCoreImpl.this,"Registration to ["+mProxyCfg.getProxy()+"] lost");
				mListener.registrationState(LinphoneCoreImpl.this
							, getDefaultProxyConfig()
							,RegistrationState.RegistrationFailed,"connection lost");
	
			}
		}
		mSal.close();
		mSal=null;		
	}
	public void enableEchoCancellation(boolean enable) {
		mLog.error("ec not implemenetd");
	}
	public void enablePayloadType(org.linphone.core.PayloadType pt,
			boolean enable) throws LinphoneCoreException {
		mLog.error("enablePayloadType not implemenetd");
		
	}
	public org.linphone.core.PayloadType findPayloadType(String mime,
			int clockRate) {
		mLog.error("findPayloadType not implemenetd");
		return null;
	}

	public boolean isEchoCancellationEnabled() {

		return false;
	}
	public int getPlayLevel() {
		if (mCall != null && mCall.getAudioStream()!= null) 
			return mCall.getAudioStream().getPlayLevel();
		else
			return -1;
	}
	public void setPlayLevel(int level) {
		if (mCall != null && mCall.getAudioStream()!= null) 
			mCall.getAudioStream().setPlayLevel(level);
		else 
			mLog.error("Cannot ajust playback level to ["+level+"]");
	}
	public float getPlaybackGain() {
		return 0;
	}
	public void setPlaybackGain(float gain) {
		// TODO Auto-generated method stub
		
	}
	private void createSal() throws SalException {
		//create Sal
		String localAdd=null;

		String dummyConnString = "socket://www.linphone.org:80;deviceside=true";
		if (WLANInfo.getWLANState() == WLANInfo.WLAN_STATE_CONNECTED) {
			dummyConnString+=";interface=wifi";
		}

		try {
		mLog.info("Opening dummy socket connection to : " + dummyConnString);
		SocketConnection dummyCon = (SocketConnection) Connector.open(dummyConnString);
		localAdd = dummyCon.getLocalAddress();
		dummyCon.close();
		} catch (Throwable e) {
			String lErrorStatus="Network unreachable, please enable wifi/or 3G";
			mLog.error(lErrorStatus,e);
			mListener.displayStatus(LinphoneCoreImpl.this,lErrorStatus);
			setNetworkReachable(false);
			return;
		}
		
		// check if local port is available
		//		try {
		//			Connector.open("datagram://:"+mSipPort);
		//		} catch (IOException e1) {
		//			// TODO Auto-generated catch block
		//			e1.printStackTrace();
		//		}

		SocketAddress addr=JOrtpFactory.instance().createSocketAddress(localAdd, mSipPort);
		mSal=SalFactory.instance().createSal();
		mSal.setUserAgent("jLinphone/0.0.1");
		try {
			mSal.listenPort(addr, mTransport, false);
		} catch (SalException e) {
			mLog.error("Cannot listen of on ["+addr+"]",e);
			throw e;
		}
		mSal.setListener(mSalListener);

		mListener.displayStatus(LinphoneCoreImpl.this,"Ready");		
	}
	public void enableSpeaker(boolean value) {
		if (mCall != null && mCall.getAudioStream()!= null) 
			mCall.getAudioStream().enableSpeaker(value);
		
	}
	public boolean isSpeakerEnabled() {
		if (mCall != null && mCall.getAudioStream()!= null) 
			return mCall.getAudioStream().isSpeakerEnabled();
		else
			return false;
	}
	public LinphoneCall getCurrentCall() {
		return mCall;
	}
	public void playDtmf(char number, int duration) {

		
	}
	public void stopDtmf() {
		// TODO Auto-generated method stub
		
	}
	private void deallocateRingBackPlayerIfNeeded() {
		deallocatePlayerIfNeeded(mRingBackPlayer);
		mRingBackPlayer=null;
	}
	private void deallocateRingPlayerIfNeeded() {
		deallocatePlayerIfNeeded(mRingPlayer);
		mRingPlayer=null;
		
	}
	private void deallocatePlayerIfNeeded(Player aPlayer) {
		if (aPlayer!=null) {
			try {
				aPlayer.stop();
				aPlayer.close(); 
			} catch (MediaException e) {
				mLog.error("Cannot stop  player", e);
			}

		}
		
	}
	protected LinphoneCoreListener getListener() {
		return mListener;
	}
	public void addFriend(LinphoneFriend lf) throws LinphoneCoreException {
		throw new RuntimeException("Not implemenetd yet");

	}
	public LinphoneChatRoom createChatRoom(String to) {
		throw new RuntimeException("Not implemenetd yet");

	}
	public void enableVideo(boolean vcapEnabled, boolean displayEnabled) {
		throw new RuntimeException("Not implemenetd yet");

	}
	public FirewallPolicy getFirewallPolicy() {
		throw new RuntimeException("Not implemenetd yet");

	}
	public String getStunServer() {
		throw new RuntimeException("Not implemenetd yet");

	}
	public boolean isVideoEnabled() {
		throw new RuntimeException("Not implemenetd yet");

	}
	public void setFirewallPolicy(FirewallPolicy pol) {
		throw new RuntimeException("Not implemenetd yet");

	}
	public void setPresenceInfo(int minuteAway, String alternativeContact,
			OnlineStatus status) {
		throw new RuntimeException("Not implemenetd yet");

		
	}
	public void setPreviewWindow(Object w) {
		throw new RuntimeException("Not implemenetd yet");

	}
	public void setStunServer(String stunServer) {
		throw new RuntimeException("Not implemenetd yet");

	}
	public void setVideoWindow(Object w) {
		throw new RuntimeException("Not implemenetd yet");

	}
	public boolean getNetworkStateReachable() {
		return mNetworkIsUp;
	}
	public LinphoneCallParams createDefaultCallParameters() {
		throw new RuntimeException("Not implemenetd yet");
	}
	public VideoSize getPreferredVideoSize() {
		throw new RuntimeException("Not implemenetd yet");
	}
	public String getRing() {
		throw new RuntimeException("Not implemenetd yet");
	}
	public LinphoneCall inviteAddressWithParams(LinphoneAddress destination,
			LinphoneCallParams params) throws LinphoneCoreException {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not implemenetd yet");
	}
	public void setDownloadBandwidth(int bw) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not implemenetd yet");
	}
	public void setPreferredVideoSize(VideoSize vSize) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not implemenetd yet");
	}
	public void setRing(String path) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not implemenetd yet");
	}
	public void setUploadBandwidth(int bw) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not implemenetd yet");
	}
	public int updateCall(LinphoneCall call, LinphoneCallParams params) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not implemenetd yet");
	}
	public void setRtpTransport(RtpTransport t){
		mRtpTransport=t;
	}
	public Transport getSignalingTransport() {
		if (mTransport == Sal.Transport.Datagram) {
			return Transport.udp;
		} else if (mTransport == Sal.Transport.Stream) {
			return Transport.tcp;
		} else {
			throw new RuntimeException("Unexpected transport ["+mTransport+"]");
		}
	}
	public void enableKeepAlive(boolean enable) {
		// TODO Auto-generated method stub
		
	}
	public boolean isKeepAliveEnabled() {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean isNetworkReachable() {
		return getNetworkStateReachable();
	}
	public PayloadType[] listVideoCodecs() {
		// TODO Auto-generated method stub
		return null;
	}

	public void startEchoCalibration(Object data) throws LinphoneCoreException {
		throw new RuntimeException("Not implemented yet");
		
	}
}
