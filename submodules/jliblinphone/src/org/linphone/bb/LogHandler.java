/*
LogHandler.java
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

import net.rim.device.api.system.EventLogger;

import org.linphone.core.LinphoneLogHandler;

public class LogHandler implements LinphoneLogHandler {

	static final long jlinphone = 0x2c9c1cec186c8bcdL;   
	
	public LogHandler() {
    	EventLogger.register(jlinphone, "jlinphone", EventLogger.VIEWER_STRING);
	}
	public void log(String loggerName, int level, String levelName, String msg, Throwable e) {
		StringBuffer sb=new StringBuffer();
		sb.append(loggerName);
		sb.append(msg);
		if (e!=null) {
			sb.append(" ["+e.getMessage()+"]");
		}
		EventLogger.logEvent(jlinphone,sb.toString().getBytes(),jLevel2BBlevel(level));

	}
	private int jLevel2BBlevel(int level) {
		switch (level) {
		case LinphoneLogHandler.Debug: return EventLogger.ALWAYS_LOG; // to make sure both debugs and Info are logged if level are selected by Linphone
		case LinphoneLogHandler.Info: return EventLogger.ALWAYS_LOG;
		case LinphoneLogHandler.Warn: return EventLogger.WARNING;
		case LinphoneLogHandler.Error: return EventLogger.ERROR;
		case LinphoneLogHandler.Fatal: return EventLogger.SEVERE_ERROR;
		}
		return EventLogger.ERROR;
	}

}