/*
AudioStreamParameters.java
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
package org.linphone.jlinphone.media;

import org.linphone.jortp.RtpProfile;
import org.linphone.jortp.SocketAddress;

public class AudioStreamParameters {
	SocketAddress mRemoteDest;
	RtpProfile mProfile;
	int mActivePt;
	
	public void setRemoteDest(SocketAddress addr){
		mRemoteDest=addr;
	}
	public SocketAddress getRemoteDest(){
		return mRemoteDest;
	}
	public void setRtpProfile(RtpProfile prof){
		mProfile=prof;
	}
	public RtpProfile getRtpProfile(){
		return mProfile;
	}
	public void setActivePayloadTypeNumber(int pt){
		mActivePt=pt;
	}
	public int getActivePayloadTypeNumber(){
		return mActivePt;
	}
}
