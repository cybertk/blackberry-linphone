package org.linphone.jlinphone.media;


public class AudioStreamFactory {
	static AudioStreamFactory sFactory=new AudioStreamFactory();
	private AudioStreamFactory(){
		
	}
	static public AudioStreamFactory instance(){
		return sFactory;
	}
	public AudioStream createAudioStream(){
		try {
			Class clasz = Class.forName("org.linphone.jlinphone.media.jsr180.AudioStreamImpl");
			return (AudioStream) clasz.newInstance();
		} catch (Exception e) {
			return createEchoAudioStream();
		}
	}
	public AudioStream createEchoAudioStream(){
		return new AudioStreamEchoImpl();
	}
}
