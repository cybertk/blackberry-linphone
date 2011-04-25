/*
SalOpBase.java
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

public abstract class SalOpBase implements SalOp {
	String mContact;
	String mFrom;
	String mTo;
	String mRoute;
	Object mContext;
	Sal mRoot;
	
	public SalOpBase(Sal sal){
		mRoot=sal;
	}
	
	public String getContact() {
		return mContact;
	}

	public String getFrom() {
		return mFrom;
	}

	public String getTo() {
		return mTo;
	}

	public Object getUserContext() {
		return mContext;
	}

	public void setContact(String contact) {
		mContact=contact;
	}

	public void setFrom(String from) {
		mFrom=from;
	}

	public void setTo(String to) {
		mTo=to;
	}

	public void setUserContext(Object obj) {
		mContext=obj;
	}

	public Sal getSal() {
		return mRoot;
	}

	public String getRoute() {
		return mRoute;
	}

	public void setRoute(String route) {
		mRoute = route;
	}

}
