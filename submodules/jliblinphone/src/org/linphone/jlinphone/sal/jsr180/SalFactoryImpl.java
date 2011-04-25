/*
SalFactoryImpl.java
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
package org.linphone.jlinphone.sal.jsr180;

import org.linphone.sal.Sal;
import org.linphone.sal.SalAddress;
import org.linphone.sal.SalFactory;
import org.linphone.sal.SalMediaDescription;

import sip4me.gov.nist.core.LogWriter;

public class SalFactoryImpl extends SalFactory {

	public Sal createSal() {
		return new SalImpl();
	}

	public SalAddress createSalAddress(String address){
		return new SalAddressImpl(address);
	}

	public SalMediaDescription createSalMediaDescription() {
		return new SdpSalMediaDescription();
	}
	public void setDebugMode(boolean enable) {
		if (enable) {
			LogWriter.setTraceLevel(LogWriter.TRACE_MESSAGES);
		} else {
			LogWriter.setTraceLevel(LogWriter.TRACE_EXCEPTION);
		}
	}

	public SalAddress createSalAddress(String displayName, String username,
			String domain, int port) throws IllegalArgumentException {
		SalAddressImpl lAddress = new SalAddressImpl("sip:"+domain);
		if (displayName!=null) lAddress.setDisplayName(displayName);
		if (username!=null) lAddress.setUserName(username);
		if (port>0) lAddress.setPortInt(port);
		return lAddress;
	}
}
