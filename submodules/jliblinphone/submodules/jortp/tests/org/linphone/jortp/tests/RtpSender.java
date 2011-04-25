package org.linphone.jortp.tests;

import java.util.TimerTask;
import org.linphone.jortp.*;

public class RtpSender extends TimerTask {
	private RtpSession mRtpSession;
	private byte mFiller=0;
	private int mTs;

	public RtpSender(SocketAddress local, SocketAddress dest, int pt) throws RtpException{
		mRtpSession=JOrtpFactory.instance().createRtpSession();
		mRtpSession.setLocalAddr(local);
		mRtpSession.setRemoteAddr(dest);
		mRtpSession.setSendPayloadTypeNumber(pt);
		mTs=0;
		mFiller=0;
	}
	public void run() {
		int i;
		if (mTs % 160 == 0){
			RtpPacket p=JOrtpFactory.instance().createRtpPacket(160);
			byte[] buffer= p.getBytes();
			for(i=p.getDataOffset();i<p.getRealLength();i++){
				buffer[i]=mFiller++;
			}
			try {
				mRtpSession.sendPacket(p,mTs);
			} catch (RtpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mTs+=80;
	}
	public RtpStatistics getSendStats(){
		return mRtpSession.getSendStats();
	}
	public RtpStatistics getRecvStats(){
		return mRtpSession.getRecvStats();
	}
}
