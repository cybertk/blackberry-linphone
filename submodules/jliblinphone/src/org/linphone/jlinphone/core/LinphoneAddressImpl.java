/*
LinphoneAddressImpl.java
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
import org.linphone.sal.SalAddress;
import org.linphone.sal.SalFactory;

public class LinphoneAddressImpl implements LinphoneAddress {
	final private SalAddress mAddress;
	
	public LinphoneAddressImpl(String username, String domain,
			String displayName) {
		mAddress = SalFactory.instance().createSalAddress("sip:"+username+"@"+domain);
		if (displayName != null) {
			mAddress.setDisplayName(displayName);
		}
	}
	public LinphoneAddressImpl(String uri) {
		mAddress = SalFactory.instance().createSalAddress(uri);
	}
	
	public String getDisplayName() {
		return mAddress.getDisplayName();
	}

	public String getDomain() {
		return mAddress.getDomain();
	}

	public String getUserName() {
		return mAddress.getUserName();
	}

	public void setDisplayName(String name) {
		mAddress.setDisplayName(name);
	}

	public String asString() {
		return mAddress.asString();
	}

	public String asStringUriOnly() {
		return mAddress.asStringUriOnly();
	}

	public void clean() {
		mAddress.clean();
	}

	public String getPort() {
		return mAddress.getPort();
	}

	public int getPortInt() {
		return mAddress.getPortInt();
	}

	public void setDomain(String domain) {
		mAddress.setDomain(domain);
	}

	public void setPort(String port) {
		mAddress.setPort(port);
	}

	public void setPortInt(int port) {
		mAddress.setPortInt(port);
	}

	public void setUserName(String username) {
		mAddress.setUserName(username);
	}
	public String toString() {
		return asString();
	}

}
