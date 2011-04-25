/*
SalException.java
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

public class SalException extends Exception {
	Throwable mE;
	public SalException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SalException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}
	public SalException(Throwable e) {
		this(e.getClass().getName()+" "+ e.getMessage());
		mE = e;
	}

	public SalException(String detailMessage,Throwable e) {
		super(detailMessage + "caused by ["+e.getClass().getName()+" "+ e.getMessage()+"]");
		mE = e;
	}

	public void printStackTrace() {
		super.printStackTrace();
		if (mE != null) mE.printStackTrace();
	}
	
}
