/*
Sal.java
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



import org.linphone.jortp.SocketAddress;

public interface Sal {
	static public class  Reason{
		public static Reason Declined = new Reason("Declined");
		public static Reason Busy = new Reason("Busy");
		public static Reason Redirect = new Reason("Redirect");
		public static Reason TemporarilyUnavailable = new Reason("TemporarilyUnavailable");
		public static Reason NotFound = new Reason("NotFound");
		public static Reason DoNotDisturb = new Reason("DoNotDisturb");
		public static Reason Media = new Reason("Media");
		public static Reason Forbidden = new Reason("Forbidden");
		public static Reason Unknown = new Reason("Unknown");
		private String mStringValue;
		private Reason(String aStringValue) {
			mStringValue = aStringValue;
		}
		public String toString() {
			return mStringValue;
		}
	}

	static public class Transport{
		public static Transport Datagram = new Transport("Datagram");
		public static Transport Stream = new Transport("Stream");
		private String mStringValue;
		private Transport(String aStringValue) {
			mStringValue = aStringValue;
		}
		public String toString() {
			return mStringValue;
		}
	}

	//global
	public void setListener(SalListener listener);
	public void listenPort(SocketAddress addr, Transport t, boolean isSecure )throws SalException;
	public void setUserAgent(String ua);
	public SalOp createSalOp();
	public Transport getTransport();
	
	
	
	
	//close
	public void close();
	public String getLocalAddr() throws SalException;
	public int getLocalPort() throws SalException;
	
	
}
