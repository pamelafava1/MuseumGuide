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
import it.uniupo.museumguide.models.Museum;
import it.uniupo.museumguide.models.Schedule;

public class MuseumAdapter extends ArrayAdapter<Museum> {

    private Context mContext;
    private List<Museum> mDataset;

    public MuseumAdapter(Context context, int resource, List<Museum> dataset) {
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
                    .inflate(R.layout.museum_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Museum museum = mDataset.get(position);
        List<Schedule> schedules = museum.getSchedules();
        String tmp;
        holder.name.setText(museum.getName());
        if (schedules.get(0).isOpen()) {
            tmp = schedules.get(0).getOpeningTime() + "-" + schedules.get(0).getClosingTime();
            holder.textMon.setText(tmp);
        }
        if (schedules.get(1).isOpen()) {
            tmp = schedules.get(1).getOpeningTime() + "-" + schedules.get(1).getClosingTime();
            holder.textTues.setText(tmp);
        }
        if (schedules.get(2).isOpen()) {
            tmp = schedules.get(2).getOpeningTime() + "-" + schedules.get(2).getClosingTime();
            holder.textWed.setText(tmp);
        }
        if (schedules.get(3).isOpen()) {
            tmp = schedules.get(3).getOpeningTime() + "-" + schedules.get(3).getClosingTime();
            holder.textThurs.setText(tmp);
        }
        if (schedules.get(4).isOpen()) {
            tmp = schedules.get(4).getOpeningTime() + "-" + schedules.get(4).getClosingTime();
            holder.textFri.setText(tmp);
        }
        if (schedules.get(5).isOpen()) {
            tmp = schedules.get(5).getOpeningTime() + "-" + schedules.get(5).getClosingTime();
            holder.textSat.setText(tmp);
        }
        if (schedules.get(6).isOpen()) {
            tmp = schedules.get(6).getOpeningTime() + "-" + schedules.get(6).getClosingTime();
            holder.textSun.setText(tmp);
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return mDataset.size();
    }

    public class ViewHolder {
        TextView name;
        TextView textMon;
        TextView textTues;
        TextView textWed;
        TextView textThurs;
        TextView textFri;
        TextView textSat;
        TextView textSun;

        ViewHolder(View view) {
            name = view.findViewById(R.id.museum_name);
            textMon = view.findViewById(R.id.text_view_mon);
            textTues = view.findViewById(R.id.text_view_tues);
            textWed = view.findViewById(R.id.text_view_wed);
            textThurs = view.findViewById(R.id.text_view_thurs);
            textFri = view.findViewById(R.id.text_view_fri);
            textSat = view.findViewById(R.id.text_view_sat);
            textSun = view.findViewById(R.id.text_view_sun);
        }
    }
}