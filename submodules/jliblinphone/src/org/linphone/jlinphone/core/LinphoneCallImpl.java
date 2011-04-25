/*
LinphoneCallImpl.java
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

import org.linphone.core.CallDirection;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallLog;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneCallLog.CallStatus;
import org.linphone.jlinphone.media.AudioStream;
import org.linphone.jlinphone.media.AudioStreamParameters;
import org.linphone.jlinphone.media.jsr135.AudioStreamImpl;
import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;
import org.linphone.jortp.PayloadType;
import org.linphone.jortp.RtpException;
import org.linphone.jortp.RtpProfile;
import org.linphone.jortp.SocketAddress;
import org.linphone.sal.Sal;
import org.linphone.sal.SalException;
import org.linphone.sal.SalFactory;
import org.linphone.sal.SalMediaDescription;
import org.linphone.sal.SalOp;
import org.linphone.sal.SalStreamDescription;

class LinphoneCallImpl implements LinphoneCall {

	private CallDirection mDir;
	private LinphoneCall.State mState;
	private SalOp mOp;
	private SalMediaDescription mLocalDesc;
	private SalMediaDescription mFinal;
	private AudioStreamImpl mAudioStream;
	private LinphoneCallLogImpl mCallLog;
	final private LinphoneCoreImpl mCore;
	private final static Logger mLog = JOrtpFactory.instance().createLogger("LinphoneCore");
	protected LinphoneCallImpl(LinphoneCoreImpl aCore, SalOp op, CallDirection dir) throws SalException{
		mState=LinphoneCall.State.Idle;
		mOp=op;
		mDir=dir;
		mOp.setUserContext(this);
		mLocalDesc=makeLocalDesc();
		mCore = aCore;
		mCallLog = new LinphoneCallLogImpl(dir, op.getFrom(), op.getTo());
		op.callSetLocalMediaDescription(getLocalMediaDescription());
		initMediaStreams();
	}

	public LinphoneAddress getRemoteAddress(){
		if (mOp!=null){
			if (mDir==CallDirection.Incoming){
				return LinphoneCoreFactory.instance().createLinphoneAddress(mOp.getFrom());
			}else return LinphoneCoreFactory.instance().createLinphoneAddress(mOp.getTo());
		}
		return null;
	}
	public SalMediaDescription getLocalMediaDescription(){
		return mLocalDesc;
	}
	public SalMediaDescription getFinalMediaDescription(){
		return mFinal;
	}
	public void  setFinalMediaDescription(SalMediaDescription aDescription){
		mFinal = aDescription;
	}
	public void setState(LinphoneCall.State aState) {
		setState(aState, null);
	}
	public void setState(LinphoneCall.State aState,String message) {
		mState = aState;
		String displayStatus=null;
		if (aState == LinphoneCall.State.Connected) {
			displayStatus = "Connected to "+getRemoteAddress().getUserName()!=null
												?getRemoteAddress().getUserName()
												:getRemoteAddress().toString();
		mCallLog.setCallStatus(CallStatus.Sucess);
		} else if (aState == LinphoneCall.State.IncomingReceived) {
			displayStatus = getRemoteAddress().getUserName()!=null
							?getRemoteAddress().getUserName()
							:getRemoteAddress().toString()+" is calling you";
							mCallLog.setCallStatus(CallStatus.Missed);
		} else if (aState == LinphoneCall.State.OutgoingRinging) {
			displayStatus = "Remote ringing...";
			mCallLog.setCallStatus(CallStatus.Declined);
		} else if (aState == LinphoneCall.State.OutgoingEarlyMedia) {
			displayStatus = "Early media...";
			mCallLog.setCallStatus(CallStatus.Declined);
		} else if(aState == LinphoneCall.State.CallEnd) {
			displayStatus = "Call terminated";
		} else if (aState == LinphoneCall.State.Error) {
			displayStatus = "Call failure ["+message+"]";
		} else if (aState == LinphoneCall.State.OutgoingInit) {
			displayStatus= "Calling  "	+(getRemoteAddress().getUserName()!=null
										?getRemoteAddress().getUserName()
										:getRemoteAddress().toString());

		} 
		

		if (displayStatus != null) mCore.getListener().displayStatus(mCore,displayStatus);
		mCore.getListener().callState(mCore, this,mState,message);
		
	}


	public LinphoneCallLog getCallLog() {
		return mCallLog;
	}

	public CallDirection getDirection() {
		return mDir;
	}

	public State getState() {
		return mState;
	}
	private SalMediaDescription makeLocalDesc() throws SalException{
		SalMediaDescription md=SalFactory.instance().createSalMediaDescription();
		SalStreamDescription sd=new SalStreamDescription();
		PayloadType pts[]=new PayloadType[1];
		PayloadType amr;
		sd.setAddress(getSal().getLocalAddr());
		sd.setPort(7078);
		sd.setProto(SalStreamDescription.Proto.RtpAvp);
		sd.setType(SalStreamDescription.Type.Audio);
		amr=JOrtpFactory.instance().createPayloadType();
		amr.setClockRate(8000);
		amr.setMimeType("AMR");
		amr.appendRecvFmtp("octet-align=1");
		amr.setNumChannels(1);
		amr.setType(PayloadType.MediaType.Audio);
		amr.setNumber(114);
		pts[0]=amr;
		sd.setPayloadTypes(pts);
		md.addStreamDescription(sd);
		md.setAddress(getSal().getLocalAddr());
		return md;
	}
	public void initMediaStreams() throws SalException{
		mAudioStream=new AudioStreamImpl();
		try {
			String host="0.0.0.0";
			int port=getLocalMediaDescription().getStream(0).getPort();
			
			SocketAddress addr=JOrtpFactory.instance().createSocketAddress(host, port);
			if (mCore.mRtpTransport!=null)
				mAudioStream.setRtpTransport(mCore.mRtpTransport);
			mAudioStream.init(addr);
		} catch (RtpException e) {
			mAudioStream.stop();
			throw new SalException("Cannot init media stream",e);
		}
	}
	public void startMediaStreams(){
		if (mAudioStream!=null){
			try {
				mAudioStream.start(makeAudioStreamParams());
			} catch (RtpException e) {
				mLog.error("Cannot start stream", e);
			}
		}
	}
	private RtpProfile makeProfile(SalStreamDescription sd){
		RtpProfile prof=JOrtpFactory.instance().createRtpProfile();
		PayloadType pts[]=sd.getPayloadTypes();
		int i;
		for(i=0;i<pts.length;i++){
			prof.setPayloadType(pts[i], pts[i].getNumber());
		}
		return prof;
	}

	private AudioStreamParameters makeAudioStreamParams(){
		AudioStreamParameters p=null;
		SalStreamDescription sd=getFinalMediaDescription().getStream(0);
		if (sd!=null){
			SocketAddress dest=JOrtpFactory.instance().createSocketAddress(
					sd.getAddress(), sd.getPort());
			p=new AudioStreamParameters();
			p.setRtpProfile(makeProfile(sd));
			p.setRemoteDest(dest);
			p.setActivePayloadTypeNumber(sd.getPayloadTypes()[0].getNumber());
		}
		return p;
	}	
	public void terminateMediaStreams(){
		if (mAudioStream!=null){
			mAudioStream.stop();
			mAudioStream=null;
		}
	}
	public AudioStream getAudioStream() {
		return mAudioStream;
	}
	public SalOp getOp() {
		return mOp;
	}
	private Sal getSal() {
		return mOp.getSal();
	}

	private void setCallLog(LinphoneCallLogImpl aCallLog) {
		mCallLog = aCallLog;
	}
	public void sendDtmf(char number) {
		mOp.sendDtmf(number);
	}

	public LinphoneCallParams getCurrentParamsReadOnly() {
		throw new RuntimeException("Not implemented yet");
	}

	public void enableCamera(boolean enabled) {
		throw new RuntimeException("Not implemented yet");
	}

	public void enableEchoCancellation(boolean enable) {
		throw new RuntimeException("Not implemented yet");
	}

	public void enableEchoLimiter(boolean enable) {
		throw new RuntimeException("Not implemented yet");
	}

	public LinphoneCallParams getCurrentParamsCopy() {
		throw new RuntimeException("Not implemented yet");
	}

	public LinphoneCall getReplacedCall() {
		throw new RuntimeException("Not implemented yet");
	}

	public boolean isEchoCancellationEnabled() {
		throw new RuntimeException("Not implemented yet");
	}

	public boolean isEchoLimiterEnabled() {
		throw new RuntimeException("Not implemented yet");
	}
}
