/*
LinphoneFactoryImpl.java
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


import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneFriend;
import org.linphone.core.LinphoneLogHandler;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.jortp.Logger;
import org.linphone.sal.SalFactory;



public class LinphoneFactoryImpl extends LinphoneCoreFactory {
	
	public LinphoneCore createLinphoneCore(LinphoneCoreListener listener, String userConfig,String factoryConfig,Object  userdata) throws LinphoneCoreException {
		return new LinphoneCoreImpl(listener,userConfig,factoryConfig,userdata);
	}

	public LinphoneAuthInfo createAuthInfo(String username, String password, String realm) {
		return new LinphoneAuthInfoImpl(username,password, null);
	}

	public LinphoneAddress createLinphoneAddress(String username,
			String domain, String displayName) {
		return null;
	}
	
	public LinphoneProxyConfig createProxyConfig(String identity, String proxy,
			String route, boolean enableRegister) throws LinphoneCoreException {
		return new LinphoneProxyConfigImpl(identity,proxy,route,enableRegister);
	}

	
	public void setDebugMode(boolean enable) {
		if (enable) {
			Logger.setGlobalLogLevel(Logger.Info);
		} else {
			Logger.setGlobalLogLevel(Logger.Warn);
		}
		SalFactory.instance().setDebugMode(enable);
		
	}
	public LinphoneFactoryImpl(){
		
	}

	public LinphoneAddress createLinphoneAddress(String address) {
		LinphoneAddress ret=null;
		try{
			ret=new LinphoneAddressImpl(address);
		}catch(Exception e){
			
		}
		return ret;
	}

	public void setLogHandler(final LinphoneLogHandler handler) {
		Logger.setLogHandler(new Logger.Handler() {
			
			public void log(String loggerName, int level, String levelName, String msg, Throwable e) {
				handler.log(loggerName, level, levelName, msg, e);
				
			}
		});
		
	}

	public LinphoneCore createLinphoneCore(LinphoneCoreListener listener)
			throws LinphoneCoreException {
		throw new RuntimeException("Not implemenetd yet");
	}

	public LinphoneFriend createLinphoneFriend(String friendUri) {
		throw new RuntimeException("Not implemenetd yet");
	}

	public LinphoneFriend createLinphoneFriend() {
		throw new RuntimeException("Not implemenetd yet");
	}

}
