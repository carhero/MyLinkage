package com.libre.client.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerActivity;
import com.app.dlna.dmc.processor.impl.DMSProcessorImpl;
import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.DMSProcessor;
import com.app.dlna.dmc.processor.interfaces.DMSProcessor.DMSProcessorListener;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;
import com.libre.client.music.ContentTree;
import com.libre.client.util.DMSBrowseHelper;
import com.libre.client.util.UpnpDeviceManager;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class DMSBrowserActivity extends UpnpListenerActivity implements DMSProcessorListener {
	private static final String TAG = DMSBrowserActivity.class.getName();
	protected DIDLObjectArrayAdapter m_adapter;
	protected UpnpProcessor m_upnpProcessor;
	protected DMSProcessor m_dmsProcessor;
	private TextView m_textHeadLine;
	private ListView m_listView;

	private Stack<DIDLObject> m_browseObjectStack = new Stack<DIDLObject>();
	private DMSBrowseHelper m_browseHelper;
	private ImageButton m_back;
	private ImageButton m_nowPlaying;
	private Animation m_operatingAnim = null;
	private int m_position = 0;
	private boolean m_isBrowseCancel = false;
	private boolean m_needSetListViewScroll = false;
	private List<DIDLObject> m_didlObjectList = new ArrayList<DIDLObject>();
	private LibreApplication m_myApp;

    private ProgressDialog m_progressDlg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.dmsbrowser_activity);
		
		m_myApp = (LibreApplication)getApplication();
		m_operatingAnim = AnimationUtils.loadAnimation(this, R.anim.btnani);
		LinearInterpolator lin = new LinearInterpolator();
		m_operatingAnim.setInterpolator(lin);
		
		m_adapter = new DIDLObjectArrayAdapter(this, 0, 0, new ArrayList<DIDLObject>());
		m_listView = (ListView) findViewById(R.id.lv_ServerContent);
		m_listView.setAdapter(m_adapter);
		m_listView.setOnItemClickListener(itemClickListener);

		m_upnpProcessor = new UpnpProcessorImpl(this);
		m_upnpProcessor.bindUpnpService();
		
		m_back = (ImageButton) findViewById(R.id.back);
		m_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
//				finish();
//				overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
				onBackPressed();
			}
		});
		
		m_nowPlaying = (ImageButton) findViewById(R.id.nowplaying);
		m_nowPlaying.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(DMSBrowserActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
				overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			}
		});
		
		m_textHeadLine = (TextView) findViewById(R.id.choosesong);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (m_upnpProcessor != null)
			m_upnpProcessor.addListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (m_upnpProcessor != null) {
			m_upnpProcessor.removeListener(this);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (m_upnpProcessor != null)
			m_upnpProcessor.unbindUpnpService();
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
			m_position = position;
			final DIDLObject object = m_adapter.getItem(position);
			
			if (object instanceof Container) {
				m_browseObjectStack.push(object);
				browse(object);

			} else if (object instanceof Item) {
				play(object);
			}
		}
	};
	
	private OnCancelListener cancelListener = new OnCancelListener() {
		@Override
		public void onCancel(DialogInterface dialog) {
			// TODO Auto-generated method stub
			m_isBrowseCancel = true;
			onBackPressed();
		}
	};
	
	protected void browse(DIDLObject object) {
		String id = object.getId();
		Log.i(TAG, "Browse id:" + id);
		m_isBrowseCancel = false;
		if (id.equals(ContentTree.ROOT_ID)) {
			m_textHeadLine.setText(R.string.choosesong);
		} else {
			m_textHeadLine.setText(object.getTitle());
		}
		m_progressDlg = ProgressDialog.show(this, "Notice", "Loading...", true, true, cancelListener);
		
		try {
			m_dmsProcessor.browse(id);
		} catch (Throwable t) {
			m_progressDlg.dismiss();
			Toast.makeText(this, "Browse content failed!", Toast.LENGTH_SHORT).show();
		}		
	}
	

	private void play(DIDLObject object) {
		Log.i(TAG, "play item title:" + object.getTitle());
		
		m_browseHelper.saveDidlListAndPosition(m_didlObjectList, m_position);
        Log.i(getClass().getName(),"position"+m_position);
		m_browseHelper.setBrowseObjectStack(m_browseObjectStack);
		m_browseHelper.setScrollPosition(m_listView.getFirstVisiblePosition());
		m_myApp.setDmsBrowseHelperSaved(m_browseHelper);
		m_myApp.setPlayNewSong(true);
		if (m_myApp.getCurrentPlaybackHelper() != null) {
			m_myApp.getCurrentPlaybackHelper().setDmsHelper(m_browseHelper.clone());
		}
		Intent intent = new Intent(this, MainActivity.class);

		startActivity(intent);
	}

	@Override
	public void onBrowseComplete(final Map<String, List<? extends DIDLObject>> result) {
		if (m_isBrowseCancel) {
			m_isBrowseCancel = false;
			return;
		}
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				m_adapter.clear();
				m_didlObjectList.clear();

				for (DIDLObject container : result.get("Containers"))
				{
					m_adapter.add(container);
					m_didlObjectList.add(container);
				}

				for (DIDLObject item : result.get("Items"))
				{
					m_adapter.add(item);
					m_didlObjectList.add(item);
				}
//				m_adapter.notifyDataSetChanged();
				m_listView.setAdapter(m_adapter);
				m_progressDlg.dismiss();
				
				if (m_adapter.isEmpty()) {
					Toast.makeText(getApplicationContext(), "No content", Toast.LENGTH_SHORT).show();
					return;
				}
				
				if (m_needSetListViewScroll) {
					m_needSetListViewScroll = false;
					m_listView.setSelection(m_browseHelper.getScrollPosition());
				}
			}
		});
	}

	@Override
	public void onBrowseFail(final String message) {
		if (m_isBrowseCancel) {
			m_isBrowseCancel = false;
			return;
		}
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				m_progressDlg.dismiss();
				new AlertDialog.Builder(DMSBrowserActivity.this)
						.setTitle("Error occurs")
						.setMessage(message)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										//TODO act failed
										onBackPressed();
									}
								}).show();

			}
		});
	}

	@Override
	public void onBackPressed() {
		if (m_browseObjectStack.isEmpty() || m_browseObjectStack.peek().getId().equals(ContentTree.ROOT_ID)) {
			finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
//			super.onBackPressed();
		} else {
			m_browseObjectStack.pop();
			if (!m_browseObjectStack.isEmpty()) {
				browse(m_browseObjectStack.peek());
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void onStartComplete() {
		// TODO Auto-generated method stub
		m_browseHelper = m_myApp.getDmsBrowseHelperTemp();
		if (m_browseHelper == null) return;
		
		Device dmsDev = m_browseHelper.getDevice(UpnpDeviceManager.getInstance());
		m_browseObjectStack = (Stack<DIDLObject>) m_browseHelper.getBrowseObjectStack().clone();

		if (dmsDev == null) {
			Toast.makeText(this, "Cannot get device information", Toast.LENGTH_SHORT).show();
			this.finish();
		} else {
			m_dmsProcessor = new DMSProcessorImpl(dmsDev, m_upnpProcessor.getControlPoint());
			if (m_dmsProcessor == null) {
				Toast.makeText(this, "Cannot create DMS Processor", Toast.LENGTH_SHORT).show();
				this.finish();
			} else {
				m_textHeadLine.setText(dmsDev.getDetails().getFriendlyName());
				m_dmsProcessor.addListener(this);
				m_needSetListViewScroll = true;
				browse(m_browseObjectStack.peek());
			}
		}

	}

}
