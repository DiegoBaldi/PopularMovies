package diegobaldi.popularmovies.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import diegobaldi.popularmovies.R;
import diegobaldi.popularmovies.models.Video;

/**
 * Created by diego on 10/11/2016.
 */

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.ViewHolder> {

    private final List<Video> mValues;

    private Context mContext;

    public VideosAdapter(Context context, List<Video> items) {
        mValues = items;
        mContext = context;
    }

    @Override
    public VideosAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false);
        return new VideosAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final VideosAdapter.ViewHolder holder, int position) {
        holder.mVideo = mValues.get(position);
        String thumbURL = String.format(mContext.getString(R.string.trailer_thumb_url), holder.mVideo.getKey());
        final String trailerURL = String.format(mContext.getString(R.string.trailer_link), holder.mVideo.getKey());
        Picasso.with(mContext).load(thumbURL).into(holder.mThumb);
        holder.mName.setText(holder.mVideo.getName());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailerURL)));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mThumb;
        public Video mVideo;
        public final TextView mName;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mThumb = (ImageView) view.findViewById(R.id.video_thumb);
            mName = (TextView) view.findViewById(R.id.video_name);

        }
    }
}