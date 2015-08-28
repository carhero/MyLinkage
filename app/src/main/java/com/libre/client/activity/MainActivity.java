package com.libre.client.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.DMRProcessor;
import com.app.dlna.dmc.processor.interfaces.DMRProcessor.DMRProcessorListener;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;
import com.libre.client.AppPreference;
import com.libre.client.WifiConnect;
import com.libre.client.util.DMRControlHelper;
import com.libre.client.util.PlaybackHelper;
import com.libre.client.util.UpnpDeviceManager;
import com.libre.constants.WIFICONST;

import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.types.ServiceType;

import java.util.Iterator;

public class MainActivity extends BaseActivity implements DMRProcessorListener {
	private static final int MSG_REG_BROADCAST = 0x01;
	private static final int MSG_DMR_UPDATEPOSITION = 0xA0;
	private static final int MSG_DMR_PLAYCOMPLETED = 0xA1;
	private static final int MSG_DMR_PLAYING = 0xA2;
	private static final int MSG_DMR_PAUSED = 0xA3;
	private static final int MSG_DMR_STOPPED = 0xA4;
	private static final int MSG_DMR_SETURI = 0xA5;
    private static final int MSG_ONACTIONFAIL=0xA6;
    private static final int MSG_ONACTIONVOLUME=0xA7;


	private static final String TAG = "PLAY VIEW";
	private LibreApplication m_myApp;
	private long m_waitTime = 2000;
	private long m_lastBackTime = 0;
	private ImageButton m_speaker = null;
	private ImageButton m_music = null;
	private ImageButton m_next = null;
	private ImageButton m_previous = null;
	private TextView m_nowTime, m_totalTime;
	String URL,META;
	private NetworkStateReceiver m_networkReceiver;
	private UpnpProcessor m_upnpProcessor = null;
	private PlaybackHelper m_playbackHelper = null;
	private DMRControlHelper m_dmrControlHelper = null;
	private DMRProcessor m_dmrProcessor = null;
	private boolean m_isPlaybackSeeking = false;
	private TextView m_singer;
	private TextView m_song;
	private ImageView m_songimg;
	
	private ImageView m_source;
	private TextView m_speakername;
	private AudioManager m_audioManager;
	
	private SeekBar m_sb_playingProgress;
	private MySlider m_sb_volume;
	private ImageButton m_btn_PlayPause;
	private ImageButton m_volumePlus;
	
	private boolean restartneeded=false;

	private PLAY_STATE m_currentPlayState = PLAY_STATE.STOP;
	private enum PLAY_STATE {
		PAUSE,
		PLAYING,
		STOP
	}
	
	
	WifiConnect connect;
	int attempts=0;
	private ProgressDialog m_progressDlg;
	protected int OOH_CONNECT_FAILED=0x30;
	protected int OOH_CONNECT_SUCCESS=0x31;
	
	@Override
	protected void loadViewLayout() {
	    this.setContentView(R.layout.main); 
	    
	}

	@Override
	protected void findViewById() {
		
		m_speaker = (ImageButton) findViewById(R.id.speaker);
		m_music = (ImageButton) findViewById(R.id.music);
		m_next = (ImageButton) findViewById(R.id.next);
		m_previous = (ImageButton) findViewById(R.id.previous);
		m_sb_playingProgress = (SeekBar) findViewById(R.id.seektime);
		m_sb_volume = (MySlider) findViewById(R.id.seekvolume);
		m_btn_PlayPause = (ImageButton) findViewById(R.id.play);
		m_singer = (TextView) findViewById(R.id.singer);
		m_song = (TextView) findViewById(R.id.song);
		m_songimg = (ImageView) findViewById(R.id.songimg);
		m_speakername = (TextView) findViewById(R.id.speakername);
		m_nowTime = (TextView) findViewById(R.id.nowtime);
		m_totalTime = (TextView) findViewById(R.id.totaltime);
		m_volumePlus = (ImageButton) findViewById(R.id.plus);
		
		m_source=(ImageView) findViewById(R.id.source);
		m_audioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
	}

	@Override
	protected void setListener() {
		m_speaker.setOnClickListener(this);
		m_music.setOnClickListener(this);
		m_next.setOnClickListener(this);
		m_source.setOnClickListener(this);
		m_previous.setOnClickListener(this);
		m_sb_playingProgress.setOnSeekBarChangeListener(playbackSeekListener);
		m_sb_volume.setOnSeekBarChangeListener(volumeSeekListener);
		
		
		m_btn_PlayPause.setOnClickListener(playPauseListener);
		m_btn_PlayPause.setOnTouchListener(new OnTouchListener() {
			
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					switch (m_currentPlayState) {
					case PAUSE:
                        v.setBackgroundResource(R.drawable.play2);

                        break;
					case STOP:
						v.setBackgroundResource(R.drawable.play2);
						break;
					case PLAYING:
						v.setBackgroundResource(R.drawable.pause2);
						break;
					}
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					switch (m_currentPlayState) {
					case PAUSE:
                        v.setBackgroundResource(R.drawable.pause);
					case STOP:
						v.setBackgroundResource(R.drawable.play);
						break;
					case PLAYING:
						v.setBackgroundResource(R.drawable.play);
						break;
					}
				}
				return false;
			}
		});
		m_volumePlus.setOnClickListener(volumePlusTextListener);
		//PlaybackHelper.MAIN_CONTEXT.registerReceiver(m_networkReceiver=new BroadcastReceiver())
	/*	m_networkReceiver = new NetworkStateReceiver(new NwStateListener() {							
			@Override
			public void onNetworkChanged(NetworkInterface ni) {
				Log.v(TAG,"--Network has changed ...Do something");
			}

			});*/
		
		Log.d(TAG, "Volume +");
		
	}

	@Override
	protected void processLogic() {
		m_myApp = (LibreApplication)getApplication();
		ViewGroup.LayoutParams imgParams = m_songimg.getLayoutParams();
		imgParams.width = m_myApp.getImageViewSize();
		imgParams.height = m_myApp.getImageViewSize();
		m_songimg.setLayoutParams(imgParams);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		m_upnpProcessor = new UpnpProcessorImpl(MainActivity.this);
		m_upnpProcessor.bindUpnpService();
		m_upnpProcessor.addListener(UpnpDeviceManager.getInstance());
		
		AppPreference.PREF = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		
		NetworkStateReceiver.registerforNetchange(wifihandler);
		
	}
	
	@Override
	protected void onCreate(Bundle paramBundle) {

        super.onCreate(paramBundle);
        Log.d(TAG,"OnCreate");
        PlaybackHelper.MAIN_CONTEXT = this;
	}


	@Override
	protected void onResume() {
		super.onResume();
        Log.d(TAG,"OnResume");
		
		if(restartneeded)
		{
			restartneeded=false;
			//finish();
			//System.exit(1);
		
		//	finish();
		//	startActivity(new Intent(this, LodingActivity.class));
		}
		m_upnpProcessor.addListener(MainActivity.this);
		String udn = m_myApp.getCurrentDmrDeviceUdn();
		if (udn == null || udn == "")
			return;
		m_playbackHelper = m_myApp.getCurrentPlaybackHelper();
		if (m_playbackHelper == null) {
			if (udn.equals(LibreApplication.LOCAL_UDN)) return;
			RemoteDevice deviceMeta = UpnpDeviceManager.getInstance().getRemoteDmrMap().get(udn);
			if (deviceMeta == null) return;
			RemoteService service = deviceMeta.findService(new ServiceType(DMRControlHelper.SERVICE_NAMESPACE,
					DMRControlHelper.SERVICE_AVTRANSPORT_TYPE));
			if (service == null) return;
			DMRControlHelper dmrControl = new DMRControlHelper(udn, 
					m_upnpProcessor.getControlPoint(), deviceMeta, service);
			m_playbackHelper = new PlaybackHelper(dmrControl);
			LibreApplication.PLAYBACK_HELPER_MAP.put(udn, m_playbackHelper);
		}
		m_dmrControlHelper = m_playbackHelper.getDmrHelper();
		m_dmrProcessor = m_dmrControlHelper.getDmrProcessor();

		m_dmrProcessor.addListener(MainActivity.this);
		m_myApp.setDmsBrowseHelperSaved(m_playbackHelper.getDmsHelper());
		
		
	
		handleUI();

		if (m_myApp.isPlayNewSong()) {
			try {
				m_playbackHelper.playSong();
				m_myApp.setPlayNewSong(false);
			} catch (Throwable t) {}
		} else {
			//TODO set playback screen: now, total, image...
			setupPlaybackScreen();
			if (m_playbackHelper.isPlaying()) {
				setPlayStartStatus();
			} else {
				setPlayStopStatus();
			}
		}
		
	}
	
	private void handleUI() {
	
		resetPlayBackScreen();
		
		if (m_dmrControlHelper.isLocalDevice()) {
			m_sb_volume.setMax(m_dmrProcessor.getMaxVolume());
			m_sb_volume.setProgress(m_dmrProcessor.getVolume());
		//	m_speakername.setText(m_dmrControlHelper.getDmrDisplayName());
		
			
		}
		else
		{
			if (LibreApplication.PLAYBACK_HELPER_MAP.containsKey(LibreApplication.LOCAL_UDN)) {
				LibreApplication.PLAYBACK_HELPER_MAP.get(LibreApplication.LOCAL_UDN)
						.getDmrHelper().getDmrProcessor().reset();
			}
			m_sb_volume.setMax(m_dmrProcessor.getMaxVolume());
			m_sb_volume.setProgress(m_dmrProcessor.getVolume());
			m_speakername.setText(m_myApp.getSpeakerName());
			
			
		}
		
		handler.sendEmptyMessage(0x01);
	}

	private void resetPlayBackScreen() {
		// TODO Auto-generated method stub
		m_singer.setText("");
		m_song.setText("");
		m_sb_playingProgress.setProgress(0);
		m_sb_playingProgress.setMax(0);
		m_nowTime.setText("00:00:00");
		m_totalTime.setText("00:00:00");
		m_songimg.setImageResource(R.drawable.defaultcover);
		setPlayStopStatus();
	}

	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		boolean isVolumeChange = false;
		switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				m_audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, 
						AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
				isVolumeChange = true;
				break;
			case KeyEvent.KEYCODE_VOLUME_UP:
				m_audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, 
						AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
				isVolumeChange = true;
				break;
			
			default:
				break;
		}
		
		if (m_dmrControlHelper == null)
			return super.onKeyDown(keyCode, event);
		
		if (isVolumeChange && m_dmrControlHelper.isLocalDevice()) {
			m_sb_volume.setProgress(m_audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}

	
	
	@Override
	protected void onPause() {
		super.onPause();
		if (m_dmrProcessor != null) {
			m_dmrProcessor.removeListener(MainActivity.this);
		}
		if (m_upnpProcessor != null) {
			m_upnpProcessor.removeListener(MainActivity.this);
		}
	}

	@Override
	protected void onDestroy() {
		Log.v(TAG,"Destroy Main Activity");
		
		for (Iterator<String> i = LibreApplication.PLAYBACK_HELPER_MAP.keySet().iterator(); i.hasNext(); ) {
			String key = i.next();
			LibreApplication.PLAYBACK_HELPER_MAP.get(key).getDmrHelper().getDmrProcessor().dispose();
		}
		
		if (m_upnpProcessor != null) {
			m_upnpProcessor.removeListener(UpnpDeviceManager.getInstance());
			m_upnpProcessor.unbindUpnpService();
			m_upnpProcessor.stopMusicServer();
		}
        if(AppPreference.getKillProcessStatus())
            System.exit(0);
		NetworkStateReceiver.unregisterforNetchange(wifihandler);
        m_myApp.getScanThread().close();
       if (m_playbackHelper!=null) {
           m_playbackHelper.StopPlayback();
           m_myApp.getCurrentPlaybackHelper().setDmsHelper(null);
           m_playbackHelper.getDmrHelper().getDmrProcessor().removeListener(this);
       }

		super.onDestroy();

	}
	@Override
	public void onBackPressed() {
	
		long currentBackTime = System.currentTimeMillis();
		if ((currentBackTime - m_lastBackTime) > m_waitTime) {
			Toast.makeText(getApplicationContext(), "Press again to exit", Toast.LENGTH_SHORT).show();
			m_lastBackTime = currentBackTime;
		}
		else {
			super.onBackPressed();
		}
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.source:
			if(!AppPreference.HideDMRList())
			{
				startActivity(new Intent(this, DMRActivity.class));
				overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			}
			break;
			
	/*	case R.id.setting:
			startActivity(new Intent(this, SettingsActivity.class));
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			break;*/
		case R.id.speaker:
			startActivity(new Intent(this, GroupListActivity.class));
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			break;
	
		case R.id.music:
			startActivity(new Intent(this, DMSListActivity.class));
			overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			break;
		case R.id.next:


			playNextSong(1);


			break;
		case R.id.previous:
			if (m_sb_playingProgress.getProgress() == 0) {
				playNextSong(-1);
			} else {
				if (m_dmrProcessor != null)
					m_dmrProcessor.seek(0
                    );
				m_sb_playingProgress.setProgress(0);
			}
			break;
		default:
			break;
		}

	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			
			case MSG_DMR_UPDATEPOSITION:
				int position = msg.arg1;
				int duration = msg.arg2;
				if (m_isPlaybackSeeking) return;
				if (m_sb_playingProgress.getMax() == 0) {
					m_sb_playingProgress.setMax((int) duration);
					m_totalTime.setText(ModelUtil.toTimeString(duration));
				}

				m_sb_playingProgress.setProgress((int) position);
				break;
			case MSG_DMR_PAUSED:
				setPlayPauseStatus();
				break;
			case MSG_DMR_PLAYING:
				setPlayStartStatus();
				break;
			case MSG_DMR_STOPPED:
				setPlayStopStatus();
				break;
			case MSG_DMR_PLAYCOMPLETED:
				Log.v(TAG,"MSG_DMR_PLAYCOMPLETED");
				setPlayStopStatus();
//				playNextSong(1);
				break;
			case MSG_DMR_SETURI:
				//TODO refresh UI
				setupPlaybackScreen();
				break;
          /*  case MSG_ONACTIONFAIL:

                   String cause = msg.toString();
                    Toast.makeText(getApplicationContext(), cause, Toast.LENGTH_SHORT).show();

                break;*/
            /*
               */






			default:
				break;
			}
		}
	};

	public void onNextClick(View view) {
	}

	protected void setupPlaybackScreen() {
		// TODO Auto-generated method stub
		m_singer.setText(m_playbackHelper.getSinger());
		m_song.setText(m_playbackHelper.getSongName());

		Bitmap bm = m_playbackHelper.getSongImage();
		if (bm != null) {
			m_songimg.setImageBitmap(bm);
		} else {
			m_songimg.setImageResource(R.drawable.defaultcover);
		}
		
		int durationSeconds = m_playbackHelper.getDurationSeconds();
		int relTime = m_playbackHelper.getRelTime();
		m_sb_playingProgress.setMax((int) durationSeconds);
		m_sb_playingProgress.setProgress((int) relTime);
		m_totalTime.setText(ModelUtil.toTimeString(durationSeconds));
		m_nowTime.setText(ModelUtil.toTimeString(relTime));
	}

	public void onPreviousClick(View view) {
	}

	
	
	
	

	@Override
	protected void onStop() {
		
		super.onStop();
	}



	private void setPlayStartStatus() {
		m_currentPlayState = PLAY_STATE.PLAYING;
		m_btn_PlayPause.setBackgroundResource(R.drawable.pause);
	}
	
	private void setPlayPauseStatus() {
		m_currentPlayState = PLAY_STATE.PAUSE;
		m_btn_PlayPause.setBackgroundResource(R.drawable.play);
	}
	
	private void setPlayStopStatus() {
		m_currentPlayState = PLAY_STATE.STOP;
		m_btn_PlayPause.setBackgroundResource(R.drawable.play);
	}
	
	private OnClickListener playPauseListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (m_dmrProcessor == null) {
				Toast.makeText(getApplicationContext(), "Please choose speaker", Toast.LENGTH_SHORT).show();
				return;
			}
			switch (m_currentPlayState) {
				case PAUSE:
                    m_dmrProcessor.play();
                    m_currentPlayState=PLAY_STATE.PLAYING;
                    break;
				case STOP:
					m_dmrProcessor.play();
                    m_currentPlayState=PLAY_STATE.PLAYING;
					break;
				case PLAYING:
					m_dmrProcessor.pause();
                    m_currentPlayState=PLAY_STATE.PAUSE;

					break;
				default:
					break;

			}

		}
	};
	
	private OnClickListener volumePlusTextListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}
	};

	private OnSeekBarChangeListener volumeSeekListener = new OnSeekBarChangeListener() {
     int m_progress;
		@Override
		public void onStopTrackingTouch(final SeekBar seekBar) {

			if (m_dmrControlHelper == null) return;
			
			m_dmrProcessor.setVolume(m_progress);

		
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

			
		}

		
		@Override
		public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
			if (!fromUser) return;
			Log.e(TAG,"Volume onProgressChanged"+progress);
			m_progress=progress;
			//if (m_dmrControlHelper == null) return;
			
				//m_dmrProcessor.setVolume(progress);
				
			
		}
	};

	private OnSeekBarChangeListener playbackSeekListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			m_isPlaybackSeeking = false;
			if(m_dmrProcessor != null) {
				m_dmrProcessor.seek(ModelUtil.toTimeString(seekBar.getProgress()));
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			m_isPlaybackSeeking = true;
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			int duration = seekBar.getMax();
			Log.i(TAG, "progress:" + progress + " duration:" + duration);
			String nowTime = ModelUtil.toTimeString(progress);
			String totalTime = ModelUtil.toTimeString(duration);
			Log.i(TAG, "now:" + nowTime + " total:" + totalTime);
			m_nowTime.setText(nowTime);
			m_totalTime.setText(totalTime);
		}
	};

//	private String getTimeString(long seconds) {
//		StringBuilder sb = new StringBuilder();
//
//		long hour = seconds / 3600;
//		long minute = (seconds - hour * 3600) / 60;
//		long second = seconds - hour * 3600 - minute * 60;
//		// sb.append(String.format("%02d", hour) + ":" + String.format("%02d",
//		// minute) + ":" + String.format("%02d", second));
//		sb.append(String.format("%02d", hour) + ":"
//				+ String.format("%02d", minute) + ":"
//				+ String.format("%02d", second));
//		return sb.toString();
//	}

	@Override
	public void onUpdatePosition(final long position, final long duration) {
        if (m_dmrControlHelper.getDeviceUdn().equals(m_myApp.getCurrentDmrDeviceUdn())) {

            Message msg = Message.obtain(handler, MSG_DMR_UPDATEPOSITION, (int) position, (int) duration);
            msg.sendToTarget();
        }
	}


	@Override
	public void onUpdateVolume(final int currentVolume) {
		if (m_playbackHelper!=null&&m_dmrControlHelper!=null&&!m_dmrControlHelper.isLocalDevice())
		{
			m_dmrProcessor.setVolume(currentVolume);
			Log.d(TAG, "OnUpdateVolume" + currentVolume);
			m_sb_volume.setProgress(currentVolume);

		}
//        Message msg = Message.obtain(handler, MSG_ONACTIONVOLUME, currentVolume);
//        msg.sendToTarget();

		// TODO Auto-generated method stub
//		runOnUiThread(new Runnable() {
//			
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				if (m_isVolumeSeeking) return;
//				
//				m_sb_volume.setProgress(currentVolume);
//			}
//		});
	}

	@Override
	public void onPaused() {
		handler.sendEmptyMessage(MSG_DMR_PAUSED);
	}

	@Override
	public void onStoped() {
		handler.sendEmptyMessage(MSG_DMR_STOPPED);
	}

	@Override
	public void onPlaying() {
		handler.sendEmptyMessage(MSG_DMR_PLAYING);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onActionFail(final Action actionCallback,
			final UpnpResponse operation, final String cause) {

        Message msg = Message.obtain(handler, MSG_ONACTIONFAIL,operation.toString());
        msg.sendToTarget();


        Log.w(TAG, "onActionFail:" + cause);

	}

	@Override
	public void onStartComplete() {
		m_upnpProcessor.searchDMR();
		m_upnpProcessor.searchDMS();
		
		// setup local playback device
		DMRControlHelper dmr = new DMRControlHelper(m_audioManager);
		PlaybackHelper playback = new PlaybackHelper(dmr);
		LibreApplication.PLAYBACK_HELPER_MAP.put(LibreApplication.LOCAL_UDN, playback);
		m_myApp.setCurrentDmrDeviceUdn(LibreApplication.LOCAL_UDN);
	}

	@Override
	public void onPlayCompleted() {
		// TODO Auto-generated method stub
		Log.v(TAG,"On PlayCompleted");
		handler.sendEmptyMessage(MSG_DMR_PLAYCOMPLETED);
	}
	


	private void playNextSong(int x) {
		Log.v(TAG, "next song:" + x);
		if (m_playbackHelper == null) return;
		m_playbackHelper.playNextSong(x);
	}

	@Override
	public void onSetURI() {
		// TODO Auto-generated method stub
		handler.sendEmptyMessage(MSG_DMR_SETURI);
	}
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);
 
        return super.onCreateOptionsMenu(menu);
    }
 
	

	
 @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
       
        case R.id.settings:
        	startActivity(new Intent(this, SettingsActivity.class));       
            return true;
            
        case R.id.action_firmwareupdate:
           startActivity(new Intent(this,Version1.class));
            return true;

        case R.id.action_networksetup:
        	startActivity(new Intent(this, NetconfigActivity.class));  
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

      Handler wifihandler = new Handler() {
		@SuppressLint("InflateParams")
		@Override
		public void handleMessage(Message msg) {

			Log.v(TAG,"wifihandler : " + msg.what);

			if (msg.what == WIFICONST.NETWORK_CHANGED)
			{

				/*Log.v(TAG,"Net changed in MainActivity");
				for (Iterator<String> i = LibreApplication.PLAYBACK_HELPER_MAP.keySet().iterator(); i.hasNext(); ) {
					String key = i.next();
					LibreApplication.PLAYBACK_HELPER_MAP.get(key).getDmrHelper().getDmrProcessor().dispose();
				}
				m_playbackHelper=null;
				m_myApp.setCurrentDmrDeviceUdn(LibreApplication.LOCAL_UDN);*/

				restartneeded=true;
				
				/*if (m_upnpProcessor != null) {
					m_upnpProcessor.removeListener(UpnpDeviceManager.getInstance());
					m_upnpProcessor.unbindUpnpService();
					//m_upnpProcessor.stopMusicServer();
				}
				//m_upnpProcessor = new UpnpProcessorImpl(MainActivity.this);
				m_upnpProcessor.bindUpnpService();
				m_upnpProcessor.addListener(UpnpDeviceManager.getInstance());*/
				
				
			}
		 else if (msg.what == WIFICONST.SSID_NOT_FOUND) 
			{
				if(attempts<5)
				{
					connect.SearchMore();
					attempts++;
				}
				else
				{
					
					connect.close();
					wifihandler.sendEmptyMessageDelayed(OOH_CONNECT_FAILED, 1000);
					
				}
			}
			else if (msg.what == WIFICONST.SSID_FOUND) 
			{
				String data = (String)msg.obj;
				Log.v(TAG,"Found a Speaker Message-----");
				connect.close();
				connect.Connect(data,"hello123");
				
			} 
			else if (msg.what ==WIFICONST.SSID_CONNECTED) 
			{
				connect.close();
				wifihandler.sendEmptyMessage(OOH_CONNECT_SUCCESS);
				
			}
			else if (msg.what == WIFICONST.SSID_CONNECT_FAILED) 
			{
				connect.close();
				wifihandler.sendEmptyMessageDelayed(OOH_CONNECT_FAILED, 1000);
				//DisplayAlert("");
				
			}
			else if (msg.what == OOH_CONNECT_FAILED) 
			{
				m_progressDlg.dismiss();
				
				DisplayAlert("Failed to Connect to DDMS Network");
				//finish();
				
			}
			else if (msg.what == OOH_CONNECT_SUCCESS) 
			{
				m_progressDlg.dismiss();
				
				DisplayAlert("Connected to DDMS Network");
				//m_myApp.Restart();
				//System.exit(1);
				
			}
		}

	};
	private void DisplayAlert(String msg){

		final AlertDialog alertDialog1 = new AlertDialog.Builder(
				MainActivity.this).create();

		// Setting Dialog Title
		alertDialog1.setTitle("Alert");

		// Setting Dialog Message
		alertDialog1.setMessage(msg);

		// Setting Icon to Dialog
		alertDialog1.setIcon(R.drawable.ic_launcher);
		alertDialog1.setButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// Write your code here to execute after dialog
				// closed
				// Intent i = getBaseContext().getPackageManager()
				//       .getLaunchIntentForPackage( getBaseContext().getPackageName() );
				// i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				//startActivity(i);

				Intent mainIntent = new Intent(MainActivity.this, LodingActivity.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
				startActivity(mainIntent);
				finish();
				Toast.makeText(getApplicationContext(),
						"You clicked on OK", Toast.LENGTH_SHORT).show();
				alertDialog1.dismiss();
			}
		});



     
     // Showing
     //
     //
     // Alert Message
     alertDialog1.show();
	}


	 
}
