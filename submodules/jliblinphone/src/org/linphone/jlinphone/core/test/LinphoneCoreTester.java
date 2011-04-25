package org.linphone.jlinphone.core.test;

import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.LinphoneCore.GeneralState;
import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;

import jmunit.framework.cldc11.Assertion;
import jmunit.framework.cldc11.TestCase;

class DummyLinphoneCoreListener implements LinphoneCoreListener {

	public void authInfoRequested(LinphoneCore lc, String realm, String username) {
		Assertion.fail("method not implemented");
	}
	public void byeReceived(LinphoneCore lc, String from) {
		Assertion.fail("method not implemented");
	}

	public void displayMessage(LinphoneCore lc, String message) {
		Assertion.fail("method not implemented");
	}

	public void displayStatus(LinphoneCore lc, String message) {
		Assertion.fail("method not implemented");
	}

	public void displayWarning(LinphoneCore lc, String message) {
		Assertion.fail("method not implemented");
	}

	public void generalState(LinphoneCore lc, GeneralState state) {
		Assertion.fail("method not implemented");
	}

	public void inviteReceived(LinphoneCore lc, String from) {
		Assertion.fail("method not implemented");
	}

	public void show(LinphoneCore lc) {
		Assertion.fail("method not implemented");
	}
	
}
public class LinphoneCoreTester extends TestCase {

	Logger mLog = JOrtpFactory.instance().createLogger("LinphoneCore");
	/**
	 * The default constructor. It just transmits the necessary informations to
	 * the superclass.
	 * 
	 * @param totalOfTests the total of test methods present in the class.
	 * @param name this testcase's name.
	 */
	public LinphoneCoreTester() {
		super(2, "LinphoneCoreTester");
	}

	/**
	 * A empty method used by the framework to initialize the tests. If there's
	 * 5 test methods, the setUp is called 5 times, one for each method. The
	 * setUp occurs before the method's execution, so the developer can use it
	 * to any necessary initialization. It's necessary to override it, however.
	 * 
	 * @throws Throwable anything that the initialization can throw.
	 */
	public void setUp() throws Throwable {
	}

	/**
	 * A empty mehod used by the framework to release resources used by the
	 * tests. If there's 5 test methods, the tearDown is called 5 times, one for
	 * each method. The tearDown occurs after the method's execution, so the
	 * developer can use it to close something used in the test, like a
	 * nputStream or the RMS. It's necessary to override it, however.
	 */
	public void tearDown() {
	}

	/**
	 * This method stores all the test methods invocation. The developer must
	 * implement this method with a switch-case. The cases must start from 0 and
	 * increase in steps of one until the number declared as the total of tests
	 * in the constructor, exclusive. For example, if the total is 3, the cases
	 * must be 0, 1 and 2. In each case, there must be a test method invocation.
	 * 
	 * @param testNumber the test to be executed.
	 * @throws Throwable anything that the executed test can throw.
	 */
	public void test(int testNumber) throws Throwable {
		switch (testNumber) {
		
			case 0: testLinphoneCoreFactory();break;
//			case 1: testCreateLinphoneCore();break;
//			case 1: testRegister();break;
			case 1: testInvite();break;
			
		}
	}
	
	public void testLinphoneCoreFactory() {
		Assertion.assertNull("should not be possible to instanciate linphone core factory", LinphoneCoreFactory.instance());
		LinphoneCoreFactory.setFactoryClassName("org.linphone.jlinphone.core.LinphoneFactoryImpl");
		Assertion.assertNotNull("Cannot instanciate linphone core factory", LinphoneCoreFactory.instance());
		
	}
	public void testCreateLinphoneCore() {
		try {
			LinphoneCore lc = LinphoneCoreFactory.instance().createLinphoneCore(new DummyLinphoneCoreListener(), null, null, null);
			lc.destroy();
			
		} catch (LinphoneCoreException e) {
			Assertion.fail(e.getMessage());
		}
		
	}
	public void testRegister() {
		int REGISTER_TIMEOUT = 5000;
		LinphoneCore lc=null;
		try {
			lc = LinphoneCoreFactory.instance().createLinphoneCore(new DummyLinphoneCoreListener() {

				public void generalState(LinphoneCore lc, GeneralState state) {
					if (state == GeneralState.GSTATE_REG_OK) {
						mLog.info("Registration OK");
					}
					
				}

				
				
			}, null, null, null);
			LinphoneAuthInfo lAuthInfo =  LinphoneCoreFactory.instance().createAuthInfo("8182449906ip", "49906", "Realm");

			lc.addAuthInfo(lAuthInfo);
			
			LinphoneProxyConfig lProrxy = LinphoneCoreFactory.instance().createProxyConfig("sip:8182449906ip@mty11.axtel.net"
																						, "sip:mty11.axtel.net"
																						, null, 
																						true);
			
			
			lc.addProxyConfig(lProrxy);
			lc.setDefaultProxyConfig(lProrxy);
			long startDate = System.currentTimeMillis();
			while (System.currentTimeMillis() - startDate < REGISTER_TIMEOUT && lProrxy.isRegistered()==false ) {
				lc.iterate();
				Thread.sleep(100);
			}
			
			Assertion.assertTrue("Register failed after ["+REGISTER_TIMEOUT+"] ms", lProrxy.isRegistered());
			
			
			
		} catch (Exception e) {
			Assertion.fail(e.getMessage());
		} finally {
			if (lc != null) lc.destroy();
		}
		
	}
	public void testInvite() {
		int INVITE_TIMEOUT = 5000;
		LinphoneCore lc=null;
		final Object semaphore = new Object();
		try {
			lc = LinphoneCoreFactory.instance().createLinphoneCore(new DummyLinphoneCoreListener() {
				public void generalState(LinphoneCore lc, GeneralState state) {
					if (state == GeneralState.GSTATE_CALL_OUT_CONNECTED) {
						mLog.info("Call ok OK");
						synchronized (semaphore) {
							semaphore.notify();
						}
					}
				}
				public void displayStatus(LinphoneCore lc, String message) {
					mLog.info(message);
				}

			}, null, null, null);

			try {
				lc.invite("sip:test@192.168.0.21:5060");
			} catch (LinphoneCoreException e) {
				mLog.error("",e);
				Assertion.fail(e.getMessage());
			}
			synchronized (semaphore) {
				semaphore.wait(INVITE_TIMEOUT);
			}
			Assertion.assertTrue("Invite failed after ["+INVITE_TIMEOUT+"] ms", lc.isIncall());
			
			Thread.sleep(5000);
			lc.terminateCall();

		} catch (Exception e) {
			
			Assertion.fail(e.getMessage());
		} finally {
			if (lc != null) lc.destroy();
		}
	}
	
}
