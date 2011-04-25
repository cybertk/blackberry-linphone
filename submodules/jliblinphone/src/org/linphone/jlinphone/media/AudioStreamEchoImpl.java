/*
AudioStreamEchoImpl.java
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
package org.linphone.jlinphone.media;

import java.util.Timer;
import java.util.TimerTask;

import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.PayloadType;
import org.linphone.jortp.RtpException;
import org.linphone.jortp.RtpPacket;
import org.linphone.jortp.RtpProfile;
import org.linphone.jortp.RtpSession;
import org.linphone.jortp.RtpTransport;
import org.linphone.jortp.SocketAddress;

public class AudioStreamEchoImpl implements AudioStream {
	private AudioStreamParameters mParams;
	private RtpSession mSession;
	private Timer mTimer = new Timer();
	
	class EchoTask extends TimerTask{
		private long mTime;
		private RtpSession mSession;
		private int mClockrate;
		
		EchoTask(){
			mSession=AudioStreamEchoImpl.this.mSession;
			RtpProfile prof=mSession.getProfile();
			PayloadType pt=prof.getPayloadType(mSession.getSendPayloadTypeNumber());
			mClockrate=pt.getClockRate();
		}
		public void run() {
			int ts=(int)mTime*mClockrate;

			RtpPacket p=null;;
			try {
				p=mSession.recvPacket(ts);
				if (p!=null){

					mSession.sendPacket(p, ts);

				}
			} catch (RtpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	public void init(SocketAddress local) throws RtpException {
		mSession=JOrtpFactory.instance().createRtpSession();
		mSession.setLocalAddr(local);
	}

	public void start(AudioStreamParameters params) throws RtpException{
		mParams=params;
		
		mSession.setRemoteAddr(mParams.getRemoteDest());
		mSession.setProfile(mParams.getRtpProfile());
		mSession.setSendPayloadTypeNumber(mParams.getActivePayloadTypeNumber());
		mSession.setRecvPayloadTypeNumber(mParams.getActivePayloadTypeNumber());
		mTimer=new Timer();
		mTimer.scheduleAtFixedRate(new EchoTask(), 0, 10);
	}

	public void stop() {
		mTimer.cancel();
		mSession.close();
	}

	public int getPlayLevel() {
		// TODO Auto-generated method stub
		return -1;
	}

	public void setPlayLevel(int level) {
		// TODO Auto-generated method stub
		
	}

	public boolean isStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	public void enableSpeaker(boolean value) {
		// TODO Auto-generated method stub
		
	}

	public boolean isMicMuted() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSpeakerEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public void muteMic(boolean isMuted) {
		// TODO Auto-generated method stub
		
	}

	public void setRtpTransport(RtpTransport t) {
		mSession.setTransport(t);
	}

}
