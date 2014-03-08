package uk.ac.lancs.LUFELFv2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import uk.ac.lancs.LUFELFv2.R;
import uk.ac.lancs.LUFELFv2.commsV2.EventItem;

/**
 * Created by Luke on 06/03/14.
 */
public class EventsListAdapter  extends BaseAdapter {
    private ArrayList<EventItem> listData;
    private LayoutInflater layoutInflater;

    public EventsListAdapter(Context context, ArrayList<EventItem> listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.layout_events, null);
            holder = new ViewHolder();
            holder.eventName = (TextView) convertView.findViewById(R.id.eventName);
            holder.created = (TextView) convertView.findViewById(R.id.created);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.eventName.setText(listData.get(position).getName());
        holder.created.setText(listData.get(position).getCreated());

        return convertView;
    }

    static class ViewHolder {
        TextView eventName;
        TextView created;
    }
}
