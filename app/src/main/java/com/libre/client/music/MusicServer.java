package com.libre.client.music;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.app.dlna.dmc.processor.upnp.CoreUpnpService;
import com.libre.client.activity.LibreApplication;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.WriteStatus;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.container.MusicAlbum;
import org.fourthline.cling.support.model.container.MusicArtist;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.seamless.util.MimeType;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class MusicServer {
	private static final String TAG = MusicServer.class.getName();
	private MediaServer mediaServer;
	private boolean hasPrepared = false;
	private Context m_context = null;
	private List<String> m_musicMap = null;
	private Hashtable<String, String> m_musicTable = null;
	private static final String[] AUDIO_PROJECTION = {
			MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST,
			MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.SIZE,
			MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ALBUM };
	
//	private Container audioContainer = null;
	private Container artistsContainer = null;
	private Container songsContainer = null;
	private Container albumsContainer = null;
	private Container genresContainer = null;
    Item musicTrack;


    Container genChild;

    String genres;
    List<String> genreslist = new ArrayList<>();
    HashMap<String, Container> genresMap = new HashMap<String, Container>();

	public MusicServer() {
		m_musicMap = new ArrayList<String>();
		m_musicMap.add("mp3");
		m_musicMap.add("MP3");
        m_musicMap.add("aac");
		m_musicMap.add("flac");
       // m_musicMap.add("AAC");
		m_musicTable = new Hashtable<String, String>();
		m_musicTable.put("mp3", "audio/mpeg");
		m_musicTable.put("MP3", "audio/mpeg");

//  m_musicTable.put("aac","audio/mpeg");
       // m_musicTable.put("AAC","audio/mpeg");
		//m_musicMap.add("wma");
		//m_musicMap.add("wav");
		//m_musicMap.add("mid");
		//m_musicMap.add("midi");
	}
//	private AndroidUpnpService upnpService;
	public void prepareMediaServer(Context context, CoreUpnpService.Binder service) {
        if (hasPrepared)
            return;
        try {
            InetAddress localAddress = getLocalIpAddress(context);
            LibreApplication.LOCAL_IP = localAddress.getHostAddress();
            mediaServer = new MediaServer(localAddress);
            ((AndroidUpnpService) service).getRegistry().addDevice(mediaServer.getDevice());
            LibreApplication.LOCAL_UDN = mediaServer.getDevice().getIdentity().getUdn().toString();
            hasPrepared = true;
        } catch (Exception ex) {
            // TODO: handle exception
            Log.e(TAG, "Creating local device failed" + ex);
            return;
        }

        ContentNode rootNode = ContentTree.getRootNode();
        rootNode.getContainer().setChildCount(0);

        // Audio Container
//		audioContainer = new Container(ContentTree.AUDIO_ID,
//				ContentTree.ROOT_ID, "Music", ContentTree.CREATOR,
//				new DIDLObject.Class("object.container"), 0);
//		audioContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);
//		rootNode.getContainer().addContainer(audioContainer);
//		rootNode.getContainer().setChildCount(rootNode.getContainer().getChildCount() + 1);
//		ContentTree.addNode(ContentTree.AUDIO_ID, new ContentNode(
//				ContentTree.AUDIO_ID, audioContainer));

        buildAudioContainers(rootNode.getContainer());

//		buildOtherContainers(rootNode);

        m_context = context;
        Activity activity = (Activity) context;
        activity.getLoaderManager().initLoader(0, null, new LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {

                String order = MediaStore.Audio.Media.TITLE + " COLLATE LOCALIZED ASC";


                CursorLoader cursorLoader = new CursorLoader(
                        m_context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
						AUDIO_PROJECTION, null, null, order);
                return cursorLoader;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {


                getContents(arg1);


            }



            @Override
            public void onLoaderReset(Loader<Cursor> arg0) {

                resetContents();
            }
        });
    }


       /* activity.getLoaderManager().initLoader(1, null, new LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                // TODO Auto-generated method stub
                String[] STAR = { MediaStore.Audio.Genres._ID,
                        MediaStore.Audio.Genres.NAME };
                CursorLoader cursorLoader = new CursorLoader(
                        m_context, MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                        STAR, null, null, null);

                return cursorLoader;
            }
            @Override
            public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {

                tempcursor=arg1;



                // TODO Auto-generated method stub

             //  getGenres1(arg1);


                //getContents(arg1);
            }

            private void getGenres1(Cursor cursor) {




                if (cursor.moveToFirst())
                    do {
                        genres = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME));
                        genreslist.add(genres);

                        if (genres != null) {

                            if (genresMap.containsKey(genres)) {
                                genChild = genresMap.get(genres);
                            } else {
                                String genId = ContentTree.AUDIO_GENRES_PREFIX + genres;
                                genChild = new MusicGenre(genId,
                                        ContentTree.AUDIO_GENRES_ID, genres, ContentTree.CREATOR, 0);
                                genChild.setWriteStatus(WriteStatus.NOT_WRITABLE);
                                genresContainer.addContainer(genChild);
                                genresContainer.setChildCount(genresContainer.getChildCount() + 1);
                                ContentTree.addNode(genId, new ContentNode(genId, genChild));
                                genresMap.put(genres, genChild);
                            }

                        }


                        Log.d(TAG,"generes+"+genres);
                    }while (cursor.moveToNext());
            }

            @Override
            public void onLoaderReset(Loader<Cursor> arg0) {
                // TODO Auto-generated method stub
                //resetContents();
            }
        });

//		Cursor cursor = ((Activity)context).managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//				m_audioColumns, null, null, null);
//		
//		getContents(cursor);
	} */
	
	private void buildAudioContainers(Container parent) {
		// TODO Auto-generated method stub
		// Albums
		albumsContainer = new Container(ContentTree.AUDIO_ALBUMS_ID,
				ContentTree.AUDIO_ID, "Albums", ContentTree.CREATOR,
				new DIDLObject.Class("object.container"), 0);
		albumsContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);
		parent.addContainer(albumsContainer);
		parent.setChildCount(parent.getChildCount() + 1);
		ContentTree.addNode(ContentTree.AUDIO_ALBUMS_ID, new ContentNode(
				ContentTree.AUDIO_ALBUMS_ID, albumsContainer));

		// Artists
		artistsContainer = new Container(ContentTree.AUDIO_ARTISTS_ID,
				ContentTree.AUDIO_ID, "Artists", ContentTree.CREATOR,
				new DIDLObject.Class("object.container"), 0);
		artistsContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);
		parent.addContainer(artistsContainer);
		parent.setChildCount(parent.getChildCount() + 1);
		ContentTree.addNode(ContentTree.AUDIO_ARTISTS_ID, new ContentNode(
				ContentTree.AUDIO_ARTISTS_ID, artistsContainer));

		// Genres
		/*genresContainer = new Container(ContentTree.AUDIO_GENRES_ID,
				ContentTree.AUDIO_ID, "Genres", ContentTree.CREATOR,
				new DIDLObject.Class("object.container"), 0);
		genresContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);
		parent.addContainer(genresContainer);
		parent.setChildCount(parent.getChildCount() + 1);
		ContentTree.addNode(ContentTree.AUDIO_GENRES_ID, new ContentNode(
				ContentTree.AUDIO_GENRES_ID, genresContainer)); */

		// Songs
		songsContainer = new Container(ContentTree.AUDIO_SONGS_ID,
				ContentTree.AUDIO_ID, "Songs", ContentTree.CREATOR,
				new DIDLObject.Class("object.container"), 0);
		songsContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);
		parent.addContainer(songsContainer);
		parent.setChildCount(parent.getChildCount() + 1);
		ContentTree.addNode(ContentTree.AUDIO_SONGS_ID, new ContentNode(
				ContentTree.AUDIO_SONGS_ID, songsContainer));
	}
	private boolean isFileExtSupport(String fileExtension) {
		if (fileExtension == null) return false;
		if (m_musicMap.contains(fileExtension)) return true;
		
		return false;
	}
	
	public void stop()
	{
		if (mediaServer != null)
			mediaServer.stop();
	}

	// FIXME: now only can get wifi address
	@SuppressLint("DefaultLocale")
	private InetAddress getLocalIpAddress(Context context) throws UnknownHostException {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		return InetAddress.getByName(String.format("%d.%d.%d.%d",
				(ipAddress & 0xff), (ipAddress >> 8 & 0xff),
				(ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff)));
	}

	private void resetContents() {
		// TODO Auto-generated method stub
		resetAudioContents();
	}

	private void resetAudioContents() {
		// TODO Auto-generated method stub
		albumsContainer.getContainers().clear();
		albumsContainer.getItems().clear();
		
		artistsContainer.getContainers().clear();
		artistsContainer.getItems().clear();
		
		/*genresContainer.getContainers().clear();
		genresContainer.getItems().clear(); */
		
		songsContainer.getContainers().clear();
		songsContainer.getItems().clear();
	}
	private void getContents(Cursor cursor) {
		if (cursor == null) return;

		getAudioContents(cursor);
	}
	
	private void getAudioContents(Cursor cursor) {
		// TODO Auto-generated method stub
		//HashMap<String, Container> genresMap = new HashMap<String, Container>();
		HashMap<String, Container> artistsMap = new HashMap<String, Container>();
		HashMap<String, Container> albumsMap = new HashMap<String, Container>();
		
		if (cursor.moveToFirst()) {
			do {
				long mediaId=cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
				String id = ContentTree.AUDIO_PREFIX + mediaId;
			
				String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
				String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
				String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
				String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
				long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
				long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
				String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));




				int dotPos = filePath.lastIndexOf(".");
				String fileExtension = dotPos != -1 ? filePath.substring(dotPos + 1) : null;
				if (!isFileExtSupport(fileExtension)) continue;
				if (m_musicTable.containsKey(fileExtension))
					mimeType = m_musicTable.get(fileExtension);
				if (mimeType.indexOf('/') == -1) continue;
				Res res = new Res(new MimeType(mimeType.substring(0, mimeType.indexOf('/')), mimeType.substring(mimeType
						.indexOf('/') + 1)), size, "http://" + mediaServer.getAddressAndPort() + "/" + id);
				res.setDuration(ModelUtil.toTimeString(duration / 1000));

				// Music Track must have `artist' with role field, or
				// DIDLParser().generate(didl) will throw nullpointException
				musicTrack= new MusicTrack(id,
						ContentTree.AUDIO_ID, title, artist, album,
						new PersonWithRole(artist, "Performer"), res);
                musicTrack.setCreator(fileExtension);

				songsContainer.addItem(musicTrack);



				songsContainer.setChildCount(songsContainer.getChildCount() + 1);
				ContentTree.addNode(id, new ContentNode(id, musicTrack, filePath));
				
				if (artist != null) {
					Container artistChild;
					if (artistsMap.containsKey(artist)) {
						artistChild = artistsMap.get(artist);
					} else {
						String artistId = ContentTree.AUDIO_ARTISTS_PREFIX + artist;
						artistChild = new MusicArtist(artistId,
								ContentTree.AUDIO_ARTISTS_ID, artist, ContentTree.CREATOR, 0);
						artistChild.setWriteStatus(WriteStatus.NOT_WRITABLE);
						artistsContainer.addContainer(artistChild);
						artistsContainer.setChildCount(artistsContainer.getChildCount() + 1);
						ContentTree.addNode(artistId, new ContentNode(artistId, artistChild));
						artistsMap.put(artist, artistChild);
					}
					artistChild.addItem(musicTrack);
					artistChild.setChildCount(artistChild.getChildCount() + 1);
				}
				
				if (album != null) {
					Container albumChild;
					if (albumsMap.containsKey(album)) {
						albumChild = albumsMap.get(album);
					} else {
						String albumId = ContentTree.AUDIO_ALBUMS_PREFIX + album;
						albumChild = new MusicAlbum(albumId,
								ContentTree.AUDIO_ALBUMS_ID, album, ContentTree.CREATOR, 0, new ArrayList<MusicTrack>());
						albumChild.setWriteStatus(WriteStatus.NOT_WRITABLE);
						albumsContainer.addContainer(albumChild);
						albumsContainer.setChildCount(albumsContainer.getChildCount() + 1);
						ContentTree.addNode(albumId, new ContentNode(albumId, albumChild));
						albumsMap.put(album, albumChild);
					}
					albumChild.addItem(musicTrack);


					albumChild.setChildCount(albumChild.getChildCount() + 1);
				}

		/*	 if (genres != null) {
                 Container genChild;

                if (genresMap.containsKey(genres)) {
                    genChild = genresMap.get(genres);
                } else {
                    String genId = ContentTree.AUDIO_GENRES_PREFIX + genres;
                    genChild = new MusicGenre(genId,
                            ContentTree.AUDIO_GENRES_ID, genres, ContentTree.CREATOR, 0);
                    genChild.setWriteStatus(WriteStatus.NOT_WRITABLE);
                    genresContainer.addContainer(genChild);
                    genresContainer.setChildCount(genresContainer.getChildCount() + 1);
                    ContentTree.addNode(genId, new ContentNode(genId, genChild));
                    genresMap.put(genres, genChild);
                }
                 genChild.addItem(musicTrack);
               genChild.setChildCount(genChild.getChildCount() + 1);

            } */




				
				Log.v(TAG, "added audio item, title:" + title + " ext:" + fileExtension + " mime:" + mimeType + " path:" + filePath);
			} while (cursor.moveToNext());
		}
	}
	
	@SuppressWarnings("deprecation")
    private String getGenres(long mediaId,Cursor c) {
		// TODO Auto-generated method stub
		Uri uri = Uri.parse("content://media/external/audio/media/" + mediaId + "/genres");
	    c = ((Activity)m_context).managedQuery(uri,
						new String[] { MediaStore.Audio.GenresColumns.NAME },
						null, null, null);
		if (c.moveToFirst()) {
			String genre = c.getString(c.getColumnIndex(MediaStore.Audio.GenresColumns.NAME));
			return genre;
		}
		return null;
	}





    private void buildOtherContainers(Container parent) {
		// TODO Auto-generated method stub
		
		// Image Container
		Container imageContainer = new Container(ContentTree.IMAGE_ID,
				ContentTree.ROOT_ID, "Photo", ContentTree.CREATOR,
				new DIDLObject.Class("object.container"), 0);
		imageContainer.setRestricted(true);
		imageContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);
		parent.addContainer(imageContainer);
		parent.setChildCount(parent.getChildCount() + 1);
		ContentTree.addNode(ContentTree.IMAGE_ID, new ContentNode(
				ContentTree.IMAGE_ID, imageContainer));
		
		// Video Container
		Container videoContainer = new Container(ContentTree.VIDEO_ID,
				ContentTree.ROOT_ID, "Video", ContentTree.CREATOR,
				new DIDLObject.Class("object.container"), 0);
		videoContainer.setRestricted(true);
		videoContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);
		parent.addContainer(videoContainer);
		parent.setChildCount(parent.getChildCount() + 1);
		ContentTree.addNode(ContentTree.VIDEO_ID, new ContentNode(
				ContentTree.VIDEO_ID, videoContainer));
	}
	
}
