package it.uniupo.museumguide.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

import it.uniupo.museumguide.R;
import it.uniupo.museumguide.models.Object;
import it.uniupo.museumguide.util.FirebaseUtil;

public class ObjectAdapter extends ArrayAdapter<Object> {

    private Context mContext;
    private List<Object> mDataset;

    public ObjectAdapter(Context context, int resource, List<Object> dataset) {
        super(context, resource);
        mContext = context;
        mDataset = dataset;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.object_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Object object = mDataset.get(position);
        holder.name.setText(object.getName());
        if (object.getImage() != null) {
            FirebaseUtil.downloadImage(object.getImage())
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            FirebaseUtil.updateImageView(mContext, uri, holder.image);
                        }
                    });
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return mDataset.size();
    }

    public class ViewHolder {
        TextView name;
        ImageView image;

        ViewHolder(View view) {
            name = view.findViewById(R.id.object_name);
            image = view.findViewById(R.id.object_image);
        }
    }
}
