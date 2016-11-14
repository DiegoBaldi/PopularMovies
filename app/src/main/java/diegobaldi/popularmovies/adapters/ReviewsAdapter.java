package diegobaldi.popularmovies.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import diegobaldi.popularmovies.R;
import diegobaldi.popularmovies.models.Review;

import static diegobaldi.popularmovies.R.id.review_author;
import static diegobaldi.popularmovies.R.id.review_text;

/**
 * Created by diego on 10/11/2016.
 */

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {

    private final List<Review> mValues;

    private Context mContext;

    public ReviewsAdapter(Context context, List<Review> items) {
        mValues = items;
        mContext = context;
    }

    @Override
    public ReviewsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_item, parent, false);
        return new ReviewsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ReviewsAdapter.ViewHolder holder, int position) {
        holder.mReview = mValues.get(position);

        holder.mAuthor.setText(holder.mReview.getAuthor());
        holder.mText.setText(holder.mReview.getContent());

        holder.mReadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(holder.mReview.getUrl())));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mAuthor, mText;
        public Button mReadMore;
        public Review mReview;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mAuthor = (TextView) view.findViewById(review_author);
            mText = (TextView) view.findViewById(review_text);
            mReadMore = (Button) view.findViewById(R.id.review_show_more);
        }
    }
}