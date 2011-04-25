/*
SalStreamDescription.java
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

import org.linphone.jortp.PayloadType;

public class SalStreamDescription {
	private String mAddress;
	private PayloadType [] mPayloadTypes;
	private int mPort;
	private Proto mProto;
	private Type mType;
	private int mPtime;
	
	public static class Proto{
		static public Proto RtpAvp = new Proto("RtpAvp");
		static public Proto RtpSavp = new Proto("RtpSavp");
		private String mStringValue;
		private Proto(String aStringValue) {
			mStringValue = aStringValue;
		}
		public String toString() {
			return mStringValue;
		}
	};
	public static class Type{
		static public Type Audio = new Type("Audio");
		static public Type Video = new Type("Video");
		static public Type Other = new Type("Other");
		private String mStringValue;
		private Type(String aStringValue) {
			mStringValue = aStringValue;
		}
		public String toString() {
			return mStringValue;
		}
	};
	
	public String getAddress() {
		return mAddress;
	}

	public PayloadType[] getPayloadTypes() {
		return mPayloadTypes;
	}

	public int getPort() {
		return mPort;
	}

	public Proto getProto() {
		return mProto;
	}

	public Type getType() {
		return mType;
	}

	public void setAddress(String addr) {
		mAddress=addr;
	}

	public void setPayloadTypes(PayloadType[] pts) {
		mPayloadTypes=pts;
	}

	public void setPort(int port) {
		mPort=port;
	}

	public void setProto(Proto p) {
		mProto=p;
	}

	public void setType(Type t) {
		mType=t;
	}

	public void setPtime(int ptime) {
		mPtime=ptime;
	}

	public int getPtime(){
		return mPtime;
	}
	static private boolean payloadsEquals(PayloadType [] pts1, PayloadType []pts2){
		if (pts1.length!=pts2.length)
			return false;
		for (int i=0;i<pts1.length;++i){
			PayloadType p1=pts1[i];
			PayloadType p2=pts2[i];
			if (!p1.equals(p2)) return false;
		}
		return true;
	}
	private boolean stringEquals(String s1, String s2){
		if (s1!=null && s2!=null) return s1.equals(s2);
		if (s1==null && s2==null) return true;
		return false;
	}
	public boolean equals(SalStreamDescription sd){
		if (!stringEquals(getAddress(),sd.getAddress()) 
				|| this.getType()!=sd.getType()
				|| this.getPort()!=sd.getPort()
				|| this.getProto()!=sd.getProto()
				|| this.getPtime()!=sd.getPtime())
			return false;
		return payloadsEquals(this.getPayloadTypes(), sd.getPayloadTypes());
	}
}
