package com.libre.client.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.libre.client.AppPreference;
import com.libre.client.activity.R.id;

import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import java.util.List;


public class DIDLObjectArrayAdapter extends ArrayAdapter<DIDLObject> {
	private static final String TAG = DIDLObjectArrayAdapter.class.getName();
	private LayoutInflater m_inflater = null;
	
	public DIDLObjectArrayAdapter(Context context, int resource,
			int textViewResourceId, List<DIDLObject> objects) {

		super(context, resource, textViewResourceId, objects);
		m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if (convertView == null) {
			Log.d(TAG, "ConvertView = null");
			convertView = m_inflater.inflate(R.layout.didlobject_listview_item, null, false);
		}
		if (convertView.getTag() == null) {
			setViewHolder(convertView);
		}
		
		DIDLObject object = getItem(position);

		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.name.setText(object.getTitle());

        if (object instanceof Container)

        {
            if (((Container) object).getChildCount()!=null) {

                String count = ((Container) object).getChildCount().toString();
                holder.count.setText(count);
            }
        }
        else if (object instanceof Item)

        {
            if (AppPreference.ShowExtesnions())
            {
            String count=((Item) object).getCreator();
            if (!Constant.isUpNPbroswer)
            holder.name.setText(object.getTitle()+"."+count);
            }

        }


		/*if (object instanceof Container) {
			holder.icon.setImageResource(R.drawable.folder);
		} else {
			holder.icon.setImageResource(R.drawable.file);
		}*/
//		holder.name.setOnTouchListener(new OnTouchListener() {
//			
//			@SuppressLint("ClickableViewAccessibility")
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				// TODO Auto-generated method stub
//				TextView tv = (TextView) v;
//				if (event.getAction() == MotionEvent.ACTION_DOWN) {
//					tv.setTextColor(Color.parseColor("#A9188D"));
//				} else if (event.getAction() == MotionEvent.ACTION_UP) {
//					tv.setTextColor(Color.parseColor("#000000"));
//				}
//				return false;
//			}
//		});

		return convertView;
	}

	public void setViewHolder(View view) {
		ViewHolder viewHolder = new ViewHolder();
	//	viewHolder.icon = (ImageView) view.findViewById(R.id.itemIcon);
		viewHolder.name = (TextView) view.findViewById(R.id.itemName);
        viewHolder.count=(TextView)view.findViewById(id.containercount);
		view.setTag(viewHolder);
	}

	private class ViewHolder {
		//ImageView icon;
		TextView name;
        TextView count;
	}

}
