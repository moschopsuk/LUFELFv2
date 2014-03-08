package uk.ac.lancs.LUFELFv2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import uk.ac.lancs.LUFELFv2.R;
import uk.ac.lancs.LUFELFv2.commsV2.AppMessage;

/**
 * Created by Luke on 05/03/14.
 */
public class MessageListAdapter extends BaseAdapter {

    private ArrayList<AppMessage> listData;
    private LayoutInflater layoutInflater;

    public MessageListAdapter(Context context, ArrayList<AppMessage> listData) {
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
            convertView = layoutInflater.inflate(R.layout.layout_list_double, null);
            holder = new ViewHolder();
            holder.recipient = (TextView) convertView.findViewById(R.id.recipient);
            holder.message = (TextView) convertView.findViewById(R.id.message_sum);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.recipient.setText(listData.get(position).getRecipient());
        holder.message.setText(listData.get(position).getMessage());

        return convertView;
    }

    static class ViewHolder {
        TextView recipient;
        TextView message;
    }

}
