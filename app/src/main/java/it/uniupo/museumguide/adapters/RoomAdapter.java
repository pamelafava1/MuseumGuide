package it.uniupo.museumguide.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import it.uniupo.museumguide.R;
import it.uniupo.museumguide.models.Room;

public class RoomAdapter extends ArrayAdapter<Room> {

    private Context mContext;
    private List<Room> mDataset;

    public RoomAdapter(Context context, int resource, List<Room> dataset) {
        super(context, resource);
        mContext = context;
        mDataset = dataset;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.room_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Room room = mDataset.get(position);
        holder.title.setText(room.getName());

        return convertView;
    }

    @Override
    public int getCount() {
        return mDataset.size();
    }

    public class ViewHolder {
        TextView title;

        ViewHolder(View view) {
            title = view.findViewById(R.id.room_name);
        }
    }
}
