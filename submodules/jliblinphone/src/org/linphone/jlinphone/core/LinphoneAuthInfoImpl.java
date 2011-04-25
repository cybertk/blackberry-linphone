/*
LinphoneAuthInfoImpl.java
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

import org.linphone.core.LinphoneAuthInfo;

public class LinphoneAuthInfoImpl implements LinphoneAuthInfo {
	private String mPassword,mUsername,mRealm;
	public LinphoneAuthInfoImpl(String username, String password, String realm){
		mUsername=username;
		mPassword=password;
		mRealm=realm;
	}
	public String getPassword() {
		return mPassword;
	}
	public String getRealm() {
		return mRealm;
	}
	public String getUsername() {
		return mUsername;
	}
	public void setPassword(String password) {
		mPassword=password;
	}
	public void setRealm(String realm) {
		mRealm=realm;
	}
	public void setUsername(String username) {
		mUsername=username;
	}
}
