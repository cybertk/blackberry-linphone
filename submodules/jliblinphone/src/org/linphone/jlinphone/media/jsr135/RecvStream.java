/*
RecvStream.java
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
package org.linphone.jlinphone.media.jsr135;

import java.io.IOException;

import javax.microedition.media.Control;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VolumeControl;
import javax.microedition.media.protocol.ContentDescriptor;
import javax.microedition.media.protocol.DataSource;
import javax.microedition.media.protocol.SourceStream;

import net.rim.device.api.media.control.AudioPathControl;

import org.linphone.jortp.JOrtpFactory;
import org.linphone.jortp.Logger;
import org.linphone.jortp.RtpException;
import org.linphone.jortp.RtpPacket;
import org.linphone.jortp.RtpSession;
import org.linphone.jortp.TimestampClock;

public class RecvStream implements /*Runnable,*/ PlayerListener {
	private Player mPlayer;
	private SendStream mSendStream;
	
	private RtpSession mSession;
	private long mStartTime=0;
	private boolean mFirstRead=true;
	private boolean mRunning;
	private boolean mBuffering=true;
	private static Logger sLogger=JOrtpFactory.instance().createLogger("RecvStream");
	private long mPlayerTs=-1;
	private long mReturnedMs=0;
	private void reset() {
		mPlayer=null;
		mStartTime=0;
		mFirstRead=true;
		mBuffering=true;
		mPlayerTs=-1;
		mReturnedMs=0;
	}
	private SourceStream mInput= new SourceStream(){
		 byte [] sSilentAmr= {  (byte)0x3c, (byte)0x48, (byte)0xf5, (byte)0x1f,
			        (byte)0x96, (byte)0x66, (byte)0x79, (byte)0xe1,
			        (byte)0xe0, (byte)0x01, (byte)0xe7, (byte)0x8a,
			        (byte)0xf0, (byte)0x00, (byte)0x00, (byte)0x00,
			        (byte)0xc0, (byte)0x00, (byte)0x00, (byte)0x00,
			        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
			        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
			        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 };

		private RtpPacket mTroncatedPacket;
		private int mTroncatedPacketSize;
		private boolean priority_set=false;
		
		ContentDescriptor mContentDescriptor=new ContentDescriptor("audio/amr");
		 /* (non-Javadoc)
		 * @see java.io.InputStream#read(byte[], int, int)
		 */
		public int read(byte[] b, int offset, int length) throws IOException {
			int bytesToReturn=sSilentAmr.length;
			
			if (mBuffering){
				bytesToReturn=sSilentAmr.length*8;
			}
			try {
				
				if (!priority_set && Thread.currentThread().getPriority() != Thread.MAX_PRIORITY) {
					Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
					priority_set=true;
				}
				//if (sLogger.isLevelEnabled(Logger.Info) && ((mPlayer.getMediaTime()<<3) % 1000 == 0))sLogger.info("Player media time ["+mPlayer.getMediaTime()+"]");
				int lWrittenLenth=0;
				
				while(lWrittenLenth<=bytesToReturn && mRunning){
					long ts=getCurTs();
					if (mPlayerTs==-1) {
						mPlayerTs=ts;
						sLogger.info("Initializing timestamp to ["+mPlayerTs+"]");
					}
					RtpPacket packet=null;
					try {
						long diff;
						
						while((diff=(ts-mPlayerTs))>=0){
							if (diff> 800){
								mPlayerTs=ts-800;
								sLogger.warn("Too late, skipping "+ ((diff-800)/8) +" ms...");
							}
							packet=mSession.recvPacket((int)mPlayerTs);
							if (packet!=null){
								mReturnedMs+=20;
								if (mFirstRead) {
									String lAmrHeader="#!AMR\n";
									lWrittenLenth=lAmrHeader.length();
									System.arraycopy(lAmrHeader.getBytes("US-ASCII"),0, b, offset, lWrittenLenth );
									length = length - lWrittenLenth;
									offset+=lWrittenLenth;
									mFirstRead=false;
								}
								if ((length < sSilentAmr.length))  {
									// special case for end of buffer
									
									System.arraycopy(packet.getBytes(),packet.getDataOffset()+1, b, offset, length );
									lWrittenLenth+= length;
									mTroncatedPacketSize=length;
									mTroncatedPacket = packet;
									if (sLogger.isLevelEnabled(Logger.Warn)) sLogger.warn("End of buffer, ["+lWrittenLenth+"] bytes returned");
									return lWrittenLenth;
								}else{
									if (mTroncatedPacket != null) {
										// special case for troncated packet
										int remain=mTroncatedPacket.getRealLength()-mTroncatedPacket.getDataOffset()-1- mTroncatedPacketSize;
										System.arraycopy(mTroncatedPacket.getBytes(),mTroncatedPacket.getDataOffset()+1+mTroncatedPacketSize, b, offset, remain);
										lWrittenLenth+= remain;
										mTroncatedPacketSize=0;
										mTroncatedPacket = null;
										offset+= remain;
										length-=remain;
									}
									//+1 because we need to skip the CMR bytes
									int datalen=packet.getRealLength()-packet.getDataOffset()-1;
									System.arraycopy(packet.getBytes(),packet.getDataOffset()+1, b, offset, datalen );
									lWrittenLenth+= datalen;
									length-=datalen;
									offset+=datalen;
								}
							}
							mPlayerTs+=160;
						}
	
					} catch (RtpException e) {
						sLogger.error("Bad RTP packet", e);
					}
					if (packet==null) Thread.sleep(20);
				}
				if (!mRunning){
					//to notify end of stream.
					return -1;
				}
				if (sLogger.isLevelEnabled(Logger.Debug)) sLogger.debug("["+lWrittenLenth+"] bytes returned");
				return lWrittenLenth;
			} catch (Throwable e) {
				sLogger.error("Exiting player input stream",e);
				return -1;
			}
			finally {
				if (bytesToReturn > sSilentAmr.length) {
					// we were buffering, so now it's ok resetting value
					mBuffering=false;
				}
			}
		}



		public ContentDescriptor getContentDescriptor() {
			return mContentDescriptor;
		}

		public long getContentLength() {
			return -1;
		}

		public int getSeekType() {
			return SourceStream.SEEKABLE_TO_START; 
		}

		public int getTransferSize() {
			return sSilentAmr.length;
		}

		public long seek(long where) throws IOException {
			sLogger.info("seeking to ["+where+"] just ignored");
			return where;
			//throw new IOException("not seekable");
		}

		public long tell() {
			if (mStartTime==0) return 0;
			return (System.currentTimeMillis() - mStartTime);
		}

		public Control getControl(String controlType) {
			return null;
		}

		public Control[] getControls() {
			return null;
		}
	};

	public RecvStream(RtpSession session,SendStream sendStream) {
		//mThread=new Thread(this,"RecvStream thread");
		mSession=session;
		mSendStream = sendStream;
		
	}


	public void stop() {
		if (mPlayer == null) return;//nothing to stop
		mRunning=false;
		try {
			if (mPlayer.getState() == Player.STARTED) {
				mPlayer.stop();
			} 
			if (mPlayer.getState() != Player.CLOSED) {
				mPlayer.close();
			}
		} catch (MediaException e) {
			sLogger.error("Error stopping reveive stream",e);
		}
	}

	public void start() {
		mRunning=true;
		mSession.setTimestampClock(new TimestampClock(){
			public int getCurrentTimestamp() {
				return getCurTs();
			}
		});
		try{
			mPlayer = Manager.createPlayer(new DataSource (null) {
				SourceStream[] mStream = {mInput};
				public void connect() throws IOException {
					sLogger.info("connect data source");
					
				}

				public void disconnect() {
					sLogger.info("disconnect data source");
					
				}

				public String getContentType() {
					return "audio/amr";
				}

				public SourceStream[] getStreams() {
					return mStream;
				}

				public void start() throws IOException {
					sLogger.info("start data source");
					
				}

				public void stop() throws IOException {
					sLogger.info("start data source");
					
				}

				public Control getControl(String controlType) {
					return null;
				}

				public Control[] getControls() {
					return null;
				}
				
			});
	
			mPlayer.addPlayerListener(this);
			mPlayer.realize();
			AudioPathControl  lPathCtr = (AudioPathControl) mPlayer.getControl("net.rim.device.api.media.control.AudioPathControl");
			lPathCtr.setAudioPath(AudioPathControl.AUDIO_PATH_HANDSET);
			mPlayer.prefetch();
			//if ( DeviceInfo.isSimulator() == false) { //only start player on real device
			mPlayer.start();
			
			if (sLogger.isLevelEnabled(Logger.Info)) sLogger.info("Player is started .");
			//}
	
		}catch (Throwable e){
			sLogger.error("player error:",e);
		}

	}

	public void playerUpdate(Player arg0, String event, Object eventData) {
		if (sLogger.isLevelEnabled(Logger.Info))
				sLogger.info("Got event " + event + "[" + (eventData == null ? "" : eventData.toString()) + "]"
						+ "returned ms="+mReturnedMs);
		if (event==PlayerListener.BUFFERING_STARTED){
			mBuffering=true;
		} else if (event == PlayerListener.DEVICE_UNAVAILABLE) {
			//pausing both player and recorder
			try {
				//already stooped mPlayer.stop(); 
				 mSendStream.getPlayer().stop();
			} catch (Throwable e) {
				sLogger.error("Enable to pause media players", e);
			}
		} else if (event == PlayerListener.DEVICE_AVAILABLE) {
			//starting both player and recorder
			try {
				mSendStream.getPlayer().start();
				stop();
				reset();
				start();
			} catch (Throwable e) {
				sLogger.error("Enable to restart media players", e);
			}			
		}
	}
	
	public int getPlayLevel() {
		if (mPlayer !=null) {
			return ((VolumeControl)mPlayer.getControl("VolumeControl")).getLevel();
		} else {
			return 0;
		}
	}
	
	public void setPlayLevel(int level) {
		if (mPlayer !=null) {
			((VolumeControl)mPlayer.getControl("VolumeControl")).setLevel(level);
		}
	}
	
	private int getCurTs(){
		if (mStartTime==0) {
			mStartTime=System.currentTimeMillis();
		}
		return (int)((System.currentTimeMillis() - mStartTime)*8);
	}
	public void enableSpeaker(boolean value) {
		if (mPlayer == null) return;//just ignore
		AudioPathControl  lPathCtr = (AudioPathControl) mPlayer.getControl("net.rim.device.api.media.control.AudioPathControl");
		try {
			lPathCtr.setAudioPath(value?AudioPathControl.AUDIO_PATH_HANDSFREE:AudioPathControl.AUDIO_PATH_HANDSET);
			sLogger.info("Speaker is "+(lPathCtr.getAudioPath()==AudioPathControl.AUDIO_PATH_HANDSFREE?"enabled":"disabled"));
		} catch (Throwable e) {
			sLogger.error("Cannot "+(value?"enable":"disable")+" speaker", e);
		}		
	}
	
	public boolean isSpeakerEnabled() {
		if (mPlayer == null) return false;//just ignore
		AudioPathControl  lPathCtr = (AudioPathControl) mPlayer.getControl("net.rim.device.api.media.control.AudioPathControl");
		return	lPathCtr.getAudioPath()==AudioPathControl.AUDIO_PATH_HANDSFREE;
	}
	public long getPlayerTs() {
		return mPlayerTs;
	}
}
