package com.libre.client.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.app.dlna.dmc.processor.interfaces.DMRProcessor.DMRProcessorListener;
import com.libre.client.music.MusicBitmap;

import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.item.Item;

import java.util.List;



public class PlaybackHelper implements DMRProcessorListener {
	public static Context MAIN_CONTEXT;
	private final static String TAG = PlaybackHelper.class.getSimpleName();
	private DMRControlHelper dmrHelper;
	private DMSBrowseHelper dmsHelper;
	private String singer = "";
	private String songName = "";
	private Bitmap songImage = null;
	private int durationSeconds = 0; //seconds
	private int relTime = 0; //seconds
	private boolean isPlaying = false;
	private String URL;
	private String META;
    private int volume;

	public String getSinger() {
		return singer;
	}

	public String getSongName() {
		return songName;
	}

	public Bitmap getSongImage() {
		return songImage;
	}

	public int getDurationSeconds() {
		return durationSeconds;
	}

	public int getRelTime() {
		return relTime;
	}
	
	public DMRControlHelper getDmrHelper() {
		return dmrHelper;
	}


	public DMSBrowseHelper getDmsHelper() {
		return dmsHelper;
	}

	public void setDmsHelper(DMSBrowseHelper dmsHelper) {
		this.dmsHelper = dmsHelper;
	}
	
	public PlaybackHelper(DMRControlHelper dmr) {
		dmrHelper = dmr;
		dmrHelper.getDmrProcessor().addListener(this);
	}
	
	@Override
	public String toString() {
		return dmrHelper.getDeviceUdn();
	}

	public boolean StopPlayback() {
			dmrHelper.getDmrProcessor().stop();
		    dmrHelper.getDmrProcessor().removeListener(this);
			return true;
		}

	
	public void playSong() {

		if (dmsHelper == null) return;
		DIDLObject didlObj = dmsHelper.getDIDLObject();
		if (didlObj == null || !(didlObj instanceof Item)) return;
		
		String url = null;
		if (didlObj.getResources() != null && didlObj.getResources().get(0) != null) {
			url = didlObj.getResources().get(0).getValue();
		}
		
		if (url == null) return;
		String title = didlObj.getTitle();
		String creator = didlObj.getCreator();
//		String cls = didlObj.getClass();
		String album = didlObj.getFirstPropertyValue(DIDLObject.Property.UPNP.ALBUM.class);
		java.net.URI uri =  didlObj.getFirstPropertyValue(DIDLObject.Property.UPNP.ALBUM_ART_URI.class);
		String artist = didlObj.getCreator();
		String prtocolinfo = didlObj.getFirstResource().getProtocolInfo().getContentFormat();
		String size = didlObj.getFirstResource().getSize().toString();
		String duration =  didlObj.getFirstResource().getDuration();
		duration = duration.indexOf(".") == -1 ? duration : duration.substring(0, duration.indexOf("."));
		
		String urlMeta = "<DIDL-Lite xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\">"
				+ "<item id=\"audio-item-293\" parentID=\"2\" restricted=\"0\">"
				+ "<dc:title>"
				+ title
				+ "</dc:title>"
				+ "<dc:creator>"
				+ creator
				+ "</dc:creator>"
				+ "<upnp:class>object.item.audioItem.musicTrack</upnp:class>"
				+ "<upnp:album>"
				+ album
				+ "</upnp:album>"
				+ "<upnp:artist role=\"Performer\">"
				+ artist
				+ "</upnp:artist>"
				+ "<res protocolInfo=\"http-get:*:"
				+ prtocolinfo
				+ ":DLNA.ORG_PN=MP3;DLNA.ORG_OP=01;DLNA.ORG_FLAGS=01500000000000000000000000000000\" size=\""
				+ size
				+ "\" duration=\""
				+ duration
				+ "\">"
				+ url
				+ "</res>" + "</item>" + "</DIDL-Lite>";
		
		Log.i(TAG, "--------------------------");
		Log.i(TAG, urlMeta);
		
		singer = creator;
		songName = title;
		
		
		Bitmap bm = null;
		if (uri != null) {
			bm = MusicBitmap.getBitmap(uri);
		} else {
			bm = MusicBitmap.getBitmap(MAIN_CONTEXT, url);
		}
		songImage = bm;
		
		durationSeconds = (int) ModelUtil.fromTimeString(duration);
		relTime = 0;
		/*URL=url;
		META=urlMeta;
		final Handler myhandler = new Handler();
		myhandler.postDelayed(new Runnable() {
		  @Override
		  public void run() {
		    //Do something after 100ms
			  dmrHelper.getDmrProcessor().setURI(URL, META);
		  }
		}, 500);*/
		dmrHelper.getDmrProcessor().setURI(url, urlMeta);


	}
	
	public void playNextSong(int x) {
		Log.v(TAG,"PlayNextSong");
		if (dmsHelper == null) return;
		List<DIDLObject> adapter = dmsHelper.getDidlList();
		if (adapter == null) return;
		
		int totalCount = adapter.size();
		int currPosition = dmsHelper.getAdapterPosition();
		int nextPosition = currPosition + x;
		
		DIDLObject object = null;
		
		for (int i = 0; i < totalCount; i++) {
			if (nextPosition < 0) {
				return;
//				nextPosition = totalCount - 1;
			} else if (nextPosition >= totalCount){
				return;
//				nextPosition = 0;
			}
			
			object = dmsHelper.getDIDLObject(nextPosition);
			if (object instanceof Item) {
				break;
			} else {
				object = null;
			}
			nextPosition += x; 
		}
		
		if (object == null) return;
		
		dmsHelper.setAdapterPosition(nextPosition);
		try {
			playSong();
		} catch (Throwable t) {}
	}

	@Override
	public void onUpdatePosition(long position, long duration) {
		// TODO Auto-generated method stub
		relTime = (int) position;
		if (durationSeconds == 0)
			durationSeconds = (int) duration;

	}

	@Override
	public void onUpdateVolume(int currentVolume) {
		volume=currentVolume;


	}

    public void setVolume(int currentVolume){
        this.volume= currentVolume;
    }

    public int getVolume() {
        return volume;
    }
	@Override
	public void onPaused() {
		// TODO Auto-generated method stub
		isPlaying = false;
	}

	@Override
	public void onStoped() {
		// TODO Auto-generated method stub
		isPlaying = false;
	}

	@Override
	public void onSetURI() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPlayCompleted() {
		// TODO Auto-generated method stub
		Log.e(TAG,"On PlayCompleted-Helper");
		
		playNextSong(1);
	}

	@Override
	public void onPlaying() {
		// TODO Auto-generated method stub
		isPlaying = true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onActionFail(Action actionCallback, UpnpResponse response, String cause) {
		// TODO Auto-generated method stub
		
	}

	public boolean isPlaying() {
		return isPlaying;
	}
}
