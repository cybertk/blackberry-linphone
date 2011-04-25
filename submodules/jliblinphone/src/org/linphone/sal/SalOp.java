/*
SalOp.java
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


import org.linphone.sal.Sal.Reason;

public interface SalOp {
	String getFrom();
	void setFrom(String from);
	String getTo();
	void setTo(String to);
	String getContact();
	void setContact(String contact);
	void setUserContext(Object obj);
	String getRoute();
	void setRoute(String route);
	Sal getSal();
	Object getUserContext();
	
	
	public void authenticate( SalAuthInfo info) throws SalException ;

	public void call() throws SalException ;

	public void callAccept() throws SalException;

	public void callDecline(Reason r, String redirectUri);

	public void callSetLocalMediaDescription(SalMediaDescription md) ;

	public void callTerminate();
	
	public void callRinging() throws SalException ;
	
	public void register(String proxy, String from, int expires) throws SalException;
	
	public void unregister();
	
	public SalMediaDescription getFinalMediaDescription();
	
	public void sendDtmf(char number);
	
}
