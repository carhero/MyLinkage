package com.libre.client.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;

import org.fourthline.cling.model.meta.RemoteDevice;

import java.util.Collection;

//import android.widget.ImageView;

public class RemoteDMSArrayAdapter extends ArrayAdapter<RemoteDevice> {

	private static final String TAG = RemoteDMSArrayAdapter.class.getSimpleName();
	private LayoutInflater m_inflater;

	public RemoteDMSArrayAdapter(Context context, int resource, int textViewResourceId) {
		super(context, resource, textViewResourceId);
		m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = m_inflater.inflate(R.layout.dms_listview_item, null, false);
		}
		if (convertView.getTag() == null) {
			setViewHolder(convertView);
		}
		RemoteDevice device = getItem(position);
		Log.d(TAG, "add device:" + device.getDetails().getFriendlyName());

		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.deviceName.setText(device.getDetails().getFriendlyName());
		
		return convertView;
	}
	
	@SuppressLint("NewApi")
	@Override
	public void addAll(Collection<? extends RemoteDevice> collection) {
		// TODO Auto-generated method stub
		super.addAll(collection);
	}

	@Override
	public void add(RemoteDevice object) {
		if (object.getType().getNamespace().equals(UpnpProcessorImpl.DMS_NAMESPACE) && 
				object.getType().getType().equals(UpnpProcessorImpl.DMS_TYPE))
			super.add(object);
	}
	
	@Override
	public void remove(RemoteDevice object) {
		// TODO Auto-generated method stub
		if (object.getType().getNamespace().equals(UpnpProcessorImpl.DMS_NAMESPACE) && 
				object.getType().getType().equals(UpnpProcessorImpl.DMS_TYPE))
			super.remove(object);
	}

	public void setViewHolder(View view) {
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.deviceName = (TextView) view.findViewById(R.id.deviceName);
		//viewHolder.deviceType = (TextView) view.findViewById(R.id.deviceType);
//		viewHolder.deviceIcon = (ImageView) view.findViewById(R.id.deviceIcon);
		view.setTag(viewHolder);
	}

	private class ViewHolder {
		//TextView deviceType;
		TextView deviceName;
//		ImageView deviceIcon;
	}
}