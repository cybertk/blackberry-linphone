/*
LinphoneProxyConfigImpl.java
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

import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.LinphoneCore.RegistrationState;
import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;
import org.linphone.sal.SalAddress;
import org.linphone.sal.SalException;
import org.linphone.sal.SalFactory;
import org.linphone.sal.SalOp;

class LinphoneProxyConfigImpl implements LinphoneProxyConfig {
	private String mProxy;
	
	private String mDialPrefix;
	private int mExpires;
	private boolean mEscapePlus;
	private boolean mEnableRegister;
	private boolean mRegistered;
	private boolean mCommit;
	private SalOp mOp;
	private LinphoneCoreImpl mCore;
	private SalAddress mIdentityAddress;
	static Logger mLog = JOrtpFactory.instance().createLogger("LinphoneCore");
	
	LinphoneProxyConfigImpl(){
		mEscapePlus=false;
		mEnableRegister=false;
		mCommit=false;
		mExpires=600;
	}
	
	LinphoneProxyConfigImpl(String identity, String proxy, String route,
			boolean enableRegister) throws LinphoneCoreException {
		this();

		setIdentity(identity);
		mProxy = proxy;
		mEnableRegister = enableRegister;
	}

	public void done() {
		mCommit=true;
	}

	public void edit() {
		if (mOp!=null && mCore!=null){
			mOp.unregister();
			mOp=null;
		}
	}

	public void enableRegister(boolean value) throws LinphoneCoreException {
		mEnableRegister=value;
	}

	public String getDomain() {
		return mIdentityAddress.getDomain();
	}

	public String normalizePhoneNumber(String number) {
		return number;
	}

	public void setDialEscapePlus(boolean value) {
		mEscapePlus=value;
	}
	public boolean isDialPlusEscaped() {
		return mEscapePlus;
	}

	public void setDialPrefix(String prefix) {
		mDialPrefix=prefix;
	}

	public void setIdentity(String identity) throws LinphoneCoreException {
		mIdentityAddress = SalFactory.instance().createSalAddress(identity);
	}

	public void setProxy(String proxyUri) throws LinphoneCoreException {
		mProxy=proxyUri;
	}

	public String getIdentity() {
		return mIdentityAddress.asStringUriOnly();
	}

	public String getProxy() {
		return mProxy;
	}

	public boolean isRegistered() {
		return mRegistered;
	}
	public void setRegistered(boolean isRegistered) {
		mRegistered = isRegistered;
	}
	public boolean registerEnabled() {
		return mEnableRegister;
	}
	
	public void check(LinphoneCoreImpl core){
		mCore=core;
		if (mCommit){
			if (mEnableRegister){
				mOp=core.getSal().createSalOp();
				try {
					core.mListener.displayStatus(core, "Registration in progress");
					core.mListener.registrationState(core, this, RegistrationState.RegistrationProgress, null);
					mOp.register(mProxy, getIdentity(), mExpires);
				} catch (SalException e) {
					mLog.error("Registration Error",e);
				}
			}else if (mOp!=null){
				mOp.unregister();
				mOp=null;
			}
			mCommit=false;
		}
	}

	public String getRoute() {
		throw new RuntimeException("Not implemented yet");
	}

	public void setRoute(String routeUri) throws LinphoneCoreException {
		throw new RuntimeException("Not implemented yet");
		
	}

	public void enablePublish(boolean enable) {
		throw new RuntimeException("Not implemented yet");
	}

	public RegistrationState getState() {
		throw new RuntimeException("Not implemented yet");
	}

	public boolean publishEnabled() {
		throw new RuntimeException("Not implemented yet");
	}

	public void setExpires(int delay) {
		throw new RuntimeException("Not implemented yet");
	}

}
