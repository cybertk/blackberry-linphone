/*
SalMediaDescriptionBase.java
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

import java.util.Vector;

public abstract class SalMediaDescriptionBase implements SalMediaDescription{
	Vector mStreams = new Vector();
	String mAddress;

	/* (non-Javadoc)
	 * @see org.linphone.sal.SalMediaDescription#getStream(int)
	 */
	public SalStreamDescription getStream(int index) {
		return (SalStreamDescription) mStreams.elementAt(index);
	}
	/* (non-Javadoc)
	 * @see org.linphone.sal.SalMediaDescription#getNumStreams()
	 */
	public int getNumStreams(){
		return mStreams.size();
	}
	/* (non-Javadoc)
	 * @see org.linphone.sal.SalMediaDescription#addStreamDescription(org.linphone.sal.SalStreamDescription)
	 */
	public void addStreamDescription(SalStreamDescription sd) {
		mStreams.addElement(sd);
	}
	/* (non-Javadoc)
	 * @see org.linphone.sal.SalMediaDescription#setAddress(java.lang.String)
	 */
	public void setAddress(String addr){
		mAddress=addr;
	}
	/* (non-Javadoc)
	 * @see org.linphone.sal.SalMediaDescription#getAddress()
	 */
	public String getAddress(){
		return mAddress;
	}
	
	public abstract String toString();
}
