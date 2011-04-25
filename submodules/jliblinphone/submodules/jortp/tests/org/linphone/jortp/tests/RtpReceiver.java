package org.linphone.jortp.tests;

import java.util.TimerTask;

import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.RtpException;
import org.linphone.jortp.RtpPacket;
import org.linphone.jortp.RtpSession;
import org.linphone.jortp.RtpStatistics;
import org.linphone.jortp.SocketAddress;

public class RtpReceiver extends TimerTask {
	private RtpSession mRtpSession;
	private byte mFiller=0;
	private int mTs;
	private boolean mContentOk;
	
	public RtpReceiver(SocketAddress local, int pt) throws RtpException{
		mRtpSession=JOrtpFactory.instance().createRtpSession();
		mRtpSession.setLocalAddr(local);
		mRtpSession.setSendPayloadTypeNumber(pt);
		mContentOk=true;
	}
	public void run() {
		RtpPacket p=null;
		try {
			p = mRtpSession.recvPacket(mTs);
		} catch (RtpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (p!=null){
			byte data[]=p.getBytes();
			int i;
			for(i=p.getDataOffset();i<p.getRealLength();++i){
				if (data[i]!=mFiller){
					mContentOk=false;
				}
				mFiller++;
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
	public boolean isContentOk(){
		return mContentOk;
	}
}
