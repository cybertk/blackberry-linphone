package org.linphone.jortp.tests;

import java.util.Timer;
import java.util.TimerTask;

import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.RtpException;
import org.linphone.jortp.SocketAddress;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TestSuite extends TestCase {
	static private Timer mTimer=new Timer("Packet scheduler");
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSendStream(){
		SocketAddress recvaddr=JOrtpFactory.instance().createSocketAddress("127.0.0.1",9004);
		SocketAddress sendaddr=JOrtpFactory.instance().createSocketAddress("127.0.0.1", 9004);
		SocketAddress sendlocaddr=JOrtpFactory.instance().createSocketAddress("127.0.0.1", 9002);
		RtpReceiver r=null;
		RtpSender s=null;
		try {
			r = new RtpReceiver(recvaddr,0);
			s = new RtpSender(sendlocaddr,sendaddr,0);
		} catch (RtpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertNotNull("Recv session is null",r);
		Assert.assertNotNull("Send session is null",s);
		
		mTimer.scheduleAtFixedRate(r, 0, 10);
		mTimer.scheduleAtFixedRate(s, 0, 10);
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mTimer.cancel();
		System.out.println("Recv stats:\n" +r.getRecvStats().toString());
		System.out.println("Send stats:\n" +s.getSendStats().toString());
		
		Assert.assertEquals("Send and received count of packet are not the same",
				r.getRecvStats().getPlayedPacketCount(), s.getSendStats().getPacketCount());
		Assert.assertTrue("Stream integrity checking failed", r.isContentOk());
	}
}
