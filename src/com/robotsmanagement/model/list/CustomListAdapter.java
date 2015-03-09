package com.robotsmanagement.model.list;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.robotsmanagement.R;

public class CustomListAdapter extends ArrayAdapter<CustomListItem> {

	public CustomListAdapter(Context context, int resource,
			List<CustomListItem> objects) {
		super(context, resource, objects);

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View itemsView = convertView;
		if (itemsView == null) {
			itemsView = ((Activity) getContext()).getLayoutInflater().inflate(
					R.layout.custom_list_item, parent, false);
		}

		CustomListItem currentItem = getItem(position);

		TextView robotName = (TextView) itemsView.findViewById(R.id.robotName);
		robotName.setText(currentItem.getRobotName());

		TextView robotIp = (TextView) itemsView.findViewById(R.id.robotIp);
		robotIp.setText(currentItem.getIp());

		return itemsView;
	}

}
