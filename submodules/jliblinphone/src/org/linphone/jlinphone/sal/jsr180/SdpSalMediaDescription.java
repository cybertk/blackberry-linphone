/*
SdpSalMediaDescription.java
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

import org.linphone.sal.SalMediaDescription;
import org.linphone.sal.SalMediaDescriptionBase;

public class SdpSalMediaDescription extends SalMediaDescriptionBase {

	public String toString() {
		return SdpUtils.toSessionDescription(this).toString();
	}

	private boolean stringEquals(String s1, String s2){
		if (s1!=null && s2!=null) return s1.equals(s2);
		if (s1==null && s2==null) return true;
		return false;
	}
	public boolean equals(SalMediaDescription md) {
		if (!stringEquals(getAddress(),md.getAddress()))
			return false;
		if (getAddress()==null && md.getAddress()==null)
		if (!getAddress().equals(md.getAddress())) return false;
		if (this.getNumStreams()!=md.getNumStreams()) return false;
		for (int i=0;i<this.getNumStreams();++i){
			if (!this.getStream(i).equals(md.getStream(i)))
				return false;
		}
		return true;
	}
}
