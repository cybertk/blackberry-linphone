/*
SalListener.java
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


public interface SalListener {
	public void onCallReceived(SalOp op);
	public void onCallRinging(SalOp op);
	public void onCallAccepted(SalOp op);
	public void onCallTerminated(SalOp op);
	public void onAuthRequested(SalOp op, String parameter, String userName);
	public void onAuthSuccess(SalOp op, String realm, String username);
	public void onCallFailure(SalOp op, String reasonPhrase);
	//Registrations
	public void OnRegisterSuccess(SalOp op, boolean registered);
	public void OnRegisterFailure(SalOp op, SalError error, SalReason reason, String details);
}
