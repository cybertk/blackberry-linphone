/*
AudioStream.java
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

import org.linphone.jortp.RtpException;
import org.linphone.jortp.RtpTransport;
import org.linphone.jortp.SocketAddress;

public interface AudioStream {
	public void init(SocketAddress local) throws RtpException;
	public void setRtpTransport(RtpTransport t);
	public void start(AudioStreamParameters params) throws RtpException;
	/**
	 * stop an release media stream and associated resources
	 */
	public void stop();
	/**
	 * set play level
	 * @param level [0..100]
	 */
	public void setPlayLevel(int level);
	/**
	 * get playback level [0..100];
	 * @return
	 */
	public int getPlayLevel();
	/**
	 * return true if stream is started
	 * @return
	 */
	public boolean isStarted();
	
	public void enableSpeaker(boolean value);
	
	public boolean isSpeakerEnabled();

	public void muteMic(boolean isMuted);

	public boolean isMicMuted();

}
