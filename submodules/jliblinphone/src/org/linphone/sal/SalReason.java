/*
SalReason.java
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

public class SalReason {
	static public final SalReason Declined = new SalReason("Declined");
	static public final SalReason Busy= new SalReason("Busy");
	static public final SalReason Redirect= new SalReason("Redirect");
	static public final SalReason TemporarilyUnavailable= new SalReason("TemporarilyUnavailable");
	static public final SalReason NotFound= new SalReason("NotFound");
	static public final SalReason DoNotDisturb= new SalReason("DoNotDisturb");
	static public final SalReason Media= new SalReason("Media");
	static public final SalReason Forbidden= new SalReason("Forbidden");
	static public final  SalReason Unknown= new SalReason("Unknown");
	
	private final String mValue;
	private SalReason(String value) {
		mValue = value;
	}
	public String toString () {
		return mValue;
	}
}
