/*
SalAddress.java
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
package org.linphone.sal;

public interface SalAddress {
	public static int PORT_NOT_SET = -1;
	public String getDisplayName();
	public String getUserName();
	public String getDomain();
	public String getPort();
	public int getPortInt();
	public Sal.Transport getTransport();
	
	public void setDisplayName(String displayName);
	public void setUserName(String username);
	public void setDomain(String domain);
	public void setPort(String port);
	public void setPortInt(int port);
	public void setTransport(Sal.Transport aTransport);
	
	/**
	 * Removes uri parameters.
	 */
	public void clean();
	
	public String asString();
	public String asStringUriOnly();
}
