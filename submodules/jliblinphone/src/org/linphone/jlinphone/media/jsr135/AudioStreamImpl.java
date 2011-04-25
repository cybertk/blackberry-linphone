/*
AudioStreamImpl.java
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
package org.linphone.jlinphone.media.jsr135;

import java.util.Timer;
import java.util.TimerTask;

import org.linphone.jlinphone.media.AudioStream;
import org.linphone.jlinphone.media.AudioStreamParameters;
import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;
import org.linphone.jortp.RtpException;
import org.linphone.jortp.RtpSession;
import org.linphone.jortp.RtpTransport;
import org.linphone.jortp.SocketAddress;

public class AudioStreamImpl implements AudioStream {
	private RecvStream mRecvStream;
	private SendStream mSendStream;
	static Logger mLog = JOrtpFactory.instance().createLogger("AudioStream");
	private RtpSession mSession;
	static Timer mTimer = new Timer();
	private  TimerTask mTimerTask; 
	private boolean mIsStarted=false;
	RtpTransport mRtpTransport;
	public void stop() {
		if (mRecvStream!=null)
			mRecvStream.stop();
		if (mSendStream!=null)
			mSendStream.stop();
		mSession.close();
		if (mTimerTask!=null) mTimerTask.cancel();
		mLog.warn("received stats :"+mSession.getRecvStats().toString());
		mLog.warn("send stats :"+mSession.getSendStats().toString());
		mIsStarted=false;
	}
	public void init(SocketAddress local) throws RtpException {
		mSession=JOrtpFactory.instance().createRtpSession();
		if (mRtpTransport!=null) mSession.setTransport(mRtpTransport);
		mSession.setLocalAddr(local);
	}
	public void start(AudioStreamParameters params) throws RtpException {
		mIsStarted=true;
		mSession.setRemoteAddr(params.getRemoteDest());
		mSession.setProfile(params.getRtpProfile());
		mSession.setSendPayloadTypeNumber(params.getActivePayloadTypeNumber());
		mSession.setRecvPayloadTypeNumber(params.getActivePayloadTypeNumber());
		
		mTimer.scheduleAtFixedRate(mTimerTask= new TimerTask() {

			public void run() {
				if (mSession != null) {
					mLog.warn("received stats :"+mSession.getRecvStats().toString()
							+"\n Player timestamp ="+mRecvStream.getPlayerTs());
					mLog.warn("send stats :"+mSession.getSendStats().toString());
					mLog.warn("Jitter buffer statistics\n"+mSession.getJitterBufferStatistics());
				}
				
			}
			
		}, 5000, 5000);
		
		mSendStream=new SendStream(mSession);
		mRecvStream=new RecvStream(mSession,mSendStream);
		mSendStream.start();
		Thread lTmpThread = new Thread(new Runnable(){
			public void run() {
				mRecvStream.start();				
			}
		});
		lTmpThread.start();
	
		
	}
	public int getPlayLevel() {
		if (mRecvStream!= null) 
			return mRecvStream.getPlayLevel();
		else 
			return 0;
	}
	public void setPlayLevel(int level) {
		if (mRecvStream!= null) 
			mRecvStream.setPlayLevel(level);
	}
	public boolean isStarted() {
		return mIsStarted;
	}
	public void enableSpeaker(boolean value) {
		if (mRecvStream!= null) 
			mRecvStream.enableSpeaker(value);
		if (mSendStream!= null) //workaround because both streams must have the same audio path !!
			mSendStream.enableSpeaker(value);
		
	}
	public boolean isSpeakerEnabled() {
		if (mRecvStream!= null) 
			return mRecvStream.isSpeakerEnabled();
		else
			return false;
	}
	public void muteMic(boolean value) {
		if (mSendStream!= null) 
			mSendStream.muteMic(value);
		
	}
	public boolean isMicMuted() {
		if (mSendStream!= null) 
			return mSendStream.isMicMuted();
		else
			return false;
	}
	public void setRtpTransport(RtpTransport t) {
		mRtpTransport=t;
	}
		
}
