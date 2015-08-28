package com.libre.client.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.fourthline.cling.model.meta.RemoteDevice;

import java.util.List;

//import android.widget.ProgressBar;

public class RemoteDMRArrayAdapter extends ArrayAdapter<RemoteDevice> {
	private static final String TAG = RemoteDMRArrayAdapter.class.getName();
	private LayoutInflater m_inflater;
	private Context m_context;
	public RemoteDMRArrayAdapter(Context context, int resource,
			int textViewResourceId, List<RemoteDevice> objects) {

		super(context, resource, textViewResourceId, objects);
		this.m_context = context;
		m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = m_inflater.inflate(R.layout.item_master, null, false);
		}
		if (convertView.getTag() == null) {
			setViewHolder(convertView);
		}
		final RemoteDevice device = getItem(position);
		Log.d(TAG, "get device:" + device.getDetails().getFriendlyName());

		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.speakername.setText(device.getDetails().getFriendlyName());
		holder.speakername.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		
				String selectUdn = device.getIdentity().getUdn().toString();

			}
		});
		
		holder.music.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
		holder.masterset.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});

		return convertView;
	}

	
	@Override
	public void add(RemoteDevice object) {
		if (object.getType().getNamespace().compareTo("schemas-upnp-org") == 0
				&& object.getType().getType().compareTo("MediaRenderer") == 0) {
			super.add(object);
		}
		notifyDataSetChanged();
	}

	public void setViewHolder(View view) {
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.music = (ImageButton) view.findViewById(R.id.music);
		viewHolder.speakername = (TextView) view.findViewById(R.id.speakername);
		viewHolder.masterset = (ImageButton) view.findViewById(R.id.masterset);
//		viewHolder.progress = (ProgressBar) view.findViewById(R.id.progress);
		view.setTag(viewHolder);
	}

	private class ViewHolder {
		ImageButton music;
		TextView speakername;
		ImageButton masterset;
//		ProgressBar progress;
	}
}
