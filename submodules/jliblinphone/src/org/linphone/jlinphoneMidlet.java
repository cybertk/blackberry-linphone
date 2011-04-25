package org.linphone;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.LinphoneCore.GeneralState;
import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;

public class jlinphoneMidlet extends MIDlet implements CommandListener, LinphoneCoreListener {

	private DisplayManager manager;
	private Displayable screen1;
	private Displayable screen2;
	private Displayable screen3;
	private Command	 back;
	private Command	 next;
	private LinphoneCore lc;
	Logger mLog = JOrtpFactory.instance().createLogger("jlinphone");
	/**
	 * Creates several screens and navigates between them.
	 */
	public jlinphoneMidlet() {
		this.manager = new DisplayManager(Display.getDisplay(this));
		this.back = new Command("Back", Command.BACK, 1);
		this.next = new Command("Next", Command.OK, 1);
		
		this.screen1 = getSreen1();
		this.screen1.setCommandListener(this);
		this.screen1.addCommand(this.back);
		this.screen1.addCommand(this.next);
		
		this.screen2 = getSreen2();
		this.screen2.setCommandListener(this);
		this.screen2.addCommand(this.back);
		this.screen2.addCommand(this.next);
		
		this.screen3 = getSreen3();
		this.screen3.setCommandListener(this);
		this.screen3.addCommand(this.back);
		this.screen3.addCommand(this.next);
	}

	private Displayable getSreen1() {
		return new TextBox("Text [Screen 1]", "", 100, TextField.ANY);
	}

	private Displayable getSreen2() {
		return new TextBox("Text [Screen 2]", "", 100, TextField.ANY);
	}

	private Displayable getSreen3() {
		return new TextBox("Text [Screen 3]", "", 100, TextField.ANY);
	}
	
	/* (non-Javadoc)
	 * @see javax.microedition.midlet.MIDlet#startApp()
	 */
	protected void startApp() throws MIDletStateChangeException {
		this.manager.next(this.screen1);
		//nokia filesystem
		String rootDir = System.getProperty("fileconn.dir.private.name");

		try {
			LinphoneCoreFactory.setFactoryClassName("org.linphone.jlinphone.core.LinphoneFactoryImpl");
			lc = LinphoneCoreFactory.instance().createLinphoneCore(this, rootDir+".linphonerc", null, null);
			LinphoneProxyConfig lProxy = LinphoneCoreFactory.instance().createProxyConfig("sip:0952636505@freephonie.net", "sip:freephonie.net", null, true);
			lc.setDefaultProxyConfig(lProxy);
		} catch (LinphoneCoreException e) {
			mLog.error("cannot start linphone",e);
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
	 */
	public void commandAction(Command command, Displayable displayable) {
		if (command == this.next) {
			if (displayable == this.screen1) {
				this.manager.next(this.screen2);
			} else
			if (displayable == this.screen2) {
				this.manager.next(this.screen3);
			}
		}
		
		if (command == this.back) {
			this.manager.back();
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.microedition.midlet.MIDlet#destroyApp(boolean)
	 */
	protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {}

	/* (non-Javadoc)
	 * @see javax.microedition.midlet.MIDlet#pauseApp()
	 */
	protected void pauseApp() {}

	public void authInfoRequested(LinphoneCore lc, String realm, String username) {
		// TODO Auto-generated method stub
		
	}

	public void byeReceived(LinphoneCore lc, String from) {
		// TODO Auto-generated method stub
		
	}

	public void displayMessage(LinphoneCore lc, String message) {
		// TODO Auto-generated method stub
		
	}

	public void displayStatus(LinphoneCore lc, String message) {
		// TODO Auto-generated method stub
		
	}

	public void displayWarning(LinphoneCore lc, String message) {
		// TODO Auto-generated method stub
		
	}

	public void generalState(LinphoneCore lc, GeneralState state) {
		// TODO Auto-generated method stub
		
	}

	public void inviteReceived(LinphoneCore lc, String from) {
		// TODO Auto-generated method stub
		
	}

	public void show(LinphoneCore lc) {
		// TODO Auto-generated method stub
		
	}
}
