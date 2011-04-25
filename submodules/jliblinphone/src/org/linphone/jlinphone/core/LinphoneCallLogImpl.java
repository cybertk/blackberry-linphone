/*
LinphoneCallLogImpl.java
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

import net.rim.device.api.util.Persistable;

import org.linphone.core.CallDirection;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCallLog;
import org.linphone.core.LinphoneCoreFactory;

class LinphoneCallLogImpl implements LinphoneCallLog, Persistable {
	private boolean  mCallDirectionIn; //do not store CallDirection Object for persistability
	private String mFromString; //for persistance
	private String mToString; //for persistance
	private int mCallStatus=CallStatus.Aborted.toInt(); //do not store CallStatus Object for persistability
	
	LinphoneCallLogImpl(CallDirection aCallDirection,LinphoneAddress aFrom,LinphoneAddress aTo) {
		this(aCallDirection,aFrom!=null?aFrom.asString():null,aTo!=null?aTo.asString():null);
	}
	LinphoneCallLogImpl(CallDirection aCallDirection,String aFrom,String aTo) {
		mCallDirectionIn = aCallDirection == CallDirection.Incoming?true:false;
		mFromString = aFrom;
		mToString = aTo;
	}
	public CallDirection getDirection() {
		return mCallDirectionIn==true?CallDirection.Incoming:CallDirection.Outgoing;
	}

	public LinphoneAddress getFrom() {
			return mFromString!=null?LinphoneCoreFactory.instance().createLinphoneAddress(mFromString):null;
	}

	public LinphoneAddress getTo() {
		return mToString!=null?LinphoneCoreFactory.instance().createLinphoneAddress(mToString):null;
	}
	
	void setCallStatus(CallStatus status) {
		mCallStatus = status.toInt();
	}
	public CallStatus getStatus() {
		return CallStatus.fromInt(mCallStatus);
	}

}
