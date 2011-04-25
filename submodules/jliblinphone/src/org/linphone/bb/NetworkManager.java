/*
NetworkManager.java
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
package org.linphone.bb;

import org.linphone.core.LinphoneCore;
import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;

import net.rim.device.api.io.transport.TransportInfo;
import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.RadioStatusListener;
import net.rim.device.api.system.WLANConnectionListener;
import net.rim.device.api.system.WLANInfo;

public class NetworkManager implements RadioStatusListener,WLANConnectionListener{
	private final LinphoneCore mCore;
	private int mCurrentTransportType=0;
	private boolean isWifiConnected = WLANInfo.getWLANState() == WLANInfo.WLAN_STATE_CONNECTED;
	static Logger mLog = JOrtpFactory.instance().createLogger("LinphoneCore");
	public NetworkManager(LinphoneCore aCore) {
		mCore=aCore;
	}
	public void baseStationChange() {}
	public void networkScanComplete(boolean success) {}
	public void networkServiceChange(int networkId, int service) {}
	public void networkStarted(int networkId, int service) {
		handleCnxStateChange();
	}
	public void networkStateChange(int state) {
		handleCnxStateChange();
	}
	public void pdpStateChange(int apn, int state, int cause) {}
	public void radioTurnedOff() {
		handleCnxStateChange();
	}
	public void signalLevel(int level) {}
	public void networkConnected() {
		isWifiConnected=true;
		handleCnxStateChange();
	}
	public void networkDisconnected(int reason) {
		isWifiConnected=false;
		handleCnxStateChange();
	}
	public void handleCnxStateChange() {
		boolean lIsWifi= isWifiConnected & (WLANInfo.getWLANState() == WLANInfo.WLAN_STATE_CONNECTED); //on some hardware WLANInfo.getWLANState() is not fully reliable
		boolean lIsCellular=(CoverageInfo.getCoverageStatus(RadioInfo.WAF_3GPP|RadioInfo.WAF_CDMA, true) & CoverageInfo.COVERAGE_DIRECT )== CoverageInfo.COVERAGE_DIRECT;;
		mLog.info("Cnx state has changed, wifi ["+lIsWifi+"] WAN ["+lIsCellular+"] for coverage status ["+CoverageInfo.getCoverageStatus()+"]");
		if (lIsWifi == false && lIsCellular == false) {
			mCore.setNetworkReachable(false);
			mCurrentTransportType = 0;
			return;
		}
		if (lIsWifi == true) {
			if( mCurrentTransportType != TransportInfo.TRANSPORT_TCP_WIFI) {
				//wifi is now available, toggling
				mCore.setNetworkReachable(false);
				mCore.iterate();
				mCurrentTransportType = TransportInfo.TRANSPORT_TCP_WIFI;
				mCore.setNetworkReachable(true);
				return;
			}
		} else if (lIsCellular == true && mCurrentTransportType != TransportInfo.TRANSPORT_TCP_CELLULAR) {
			//cellular is now available, toggling
			mCore.setNetworkReachable(false);
			mCore.iterate();
			mCurrentTransportType = TransportInfo.TRANSPORT_TCP_CELLULAR;
			mCore.setNetworkReachable(true);
			return;
		}				
	}
	
}
