package org.linphone.jortp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;


public class RtpJ2seTransport implements Runnable, RtpTransport {
	private DatagramSocket mSocket;
	private final Thread mThread;
	private ListHead mRecvQueue;
	boolean mRunning;
	
	static private SocketAddress convertSockAddr(org.linphone.jortp.SocketAddress addr){
		return new InetSocketAddress(addr.getHost(), addr.getPort());
	}
	static private org.linphone.jortp.SocketAddress convertSockAddr(SocketAddress addr){
		return JOrtpFactory.instance().createSocketAddress(((InetSocketAddress)addr).getHostName(),
				((InetSocketAddress)addr).getPort());
	}
	
	public RtpJ2seTransport(){
		mThread=new Thread(this,"Transport thread");
		mRunning=true;
		mRecvQueue=new ListHead();
	}
	
	public RtpPacket recvfrom() {
		RtpPacket rp=null;
		
		synchronized(mRecvQueue){
			rp=(RtpPacket)mRecvQueue.popFront();
		}
		return rp;
	}

	public void sendto(RtpPacket p) {
		DatagramPacket dp=new DatagramPacket(p.getBytes(),p.getRealLength());
		dp.setSocketAddress(convertSockAddr(p.getSocketAddress()));
		try {
			mSocket.send(dp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void init(org.linphone.jortp.SocketAddress local) throws RtpException {
		try {
			mSocket=new DatagramSocket(convertSockAddr(local));
			mThread.start();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			throw new RtpException(e);
		}
	}

	public void run() {
		while(mRunning){
			RtpPacket rp=JOrtpFactory.instance().createRtpPacket(1500);
			DatagramPacket dp=new DatagramPacket(rp.getBytes(),rp.getPhysicalSize());
			try {
				mSocket.receive(dp);
				rp.setSocketAddress(convertSockAddr(dp.getSocketAddress()));
				rp.setRealLength(dp.getLength());
				synchronized (mRecvQueue){
					mRecvQueue.pushBack((Node)rp);
				}
			} catch (Exception e) {
				e.printStackTrace();
				rp=null;
			}
		}
	}
	
	public void close(){
		mRunning=false;
		mSocket.close();
	}
	public void setListener(RtpTransportListener l) {
		// TODO Auto-generated method stub
		
	}
}
