package com.example.xyzreader.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleListActivity";
    public static long sItemId = -1; //it is static to be changeable from other activities.
    private RecyclerView mRecyclerView;
    private boolean mTwoPane;
    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);
        mTwoPane = getResources().getBoolean(R.bool.twoPaneMode);
        sItemId = (sItemId < 0) ? 4863 : sItemId;
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        getLoaderManager().initLoader(0, null, this);
        if (savedInstanceState == null) {
            refresh();
        }


        if (mTwoPane) {
            if (sItemId == -1) {
                final int pos = 0;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        android.support.v7.widget.RecyclerView.ViewHolder vh = mRecyclerView
                                .findViewHolderForAdapterPosition(pos);
                        if (vh != null) {
                            vh.itemView.performClick();
                        }
                    }
                }, 1500);
            } else {
                View fragmentContainer = (View) findViewById(R.id.fragment_container);
                if (mTwoPane && fragmentContainer != null) { //create detail pane
                    if (savedInstanceState != null) {
                        Log.d(TAG, "ArticleActivity.onCreate(): Saved Instance was null.");
                    }
                    replaceDetailFragment(sItemId);
                }
            }
        }
    }

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Adapter adapter = new Adapter(cursor);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        LinearLayoutManager m = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(m);
        cursor.moveToFirst();
        if (sItemId < 0) {
            try {
                sItemId = cursor.getLong(ArticleLoader.Query._ID);
            } catch (Exception ex) {
                Log.e(TAG, "sItemId problems", ex);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    private int getImageColor(ImageView imageView) {
        int color = 0xFFFFC107;//default, amber
        Drawable drawable = imageView.getDrawable();
        if (drawable == null) {
            return color; //probably no internet connection, return default color
        }
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        if (bitmap != null) {
            ///todo remove Palette p = Palette.generate(bitmap, 12);
            Palette palette = new Palette.Builder(bitmap).generate();
            color = palette.getVibrantColor(color);
        }
        return color;
    }

    private void startDetailActivity(View view, long itemId) {
        ImageView thumb = (ImageView) (view.findViewById(R.id.thumbnail));
        int thumbColor = getImageColor(thumb);
        Uri uri = ItemsContract.Items.buildItemUri(itemId);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra("color", thumbColor);
        startActivity(intent);
    }

    private void replaceDetailFragment(long itemId) {
        Bundle args = new Bundle();
        args.putLong(ArticleDetailFragment.ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(args);
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public NetworkImageView thumbnailView;
        public TextView titleView;
        public TextView subtitleView;

        public ViewHolder(View view) {
            super(view);
            thumbnailView = (NetworkImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
            subtitleView = (TextView) view.findViewById(R.id.article_subtitle);
        }
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private Cursor mCursor;

        public Adapter(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(ArticleLoader.Query._ID);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final ViewHolder vh = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sItemId = getItemId(vh.getAdapterPosition());
                    if (mTwoPane) {
                        replaceDetailFragment(sItemId);
                    } else {
                        startDetailActivity(view, sItemId);
                    }
                }

// sample:
//                  public void onSelected(Movie movie) {
//                    if (mTwoPane) { //update detail pane
//                        // In two-pane mode, show the detail view in this activity by adding or
// replacing
//                        // the detail fragment using a fragment transaction.
//                        Bundle args = new Bundle();
//                        args.putParcelable(DetailFragment.MOVIE, movie);
//                        DetailFragment fragment = new DetailFragment();
//                        fragment.setArguments(args);
//                        getSupportFragmentManager().beginTransaction()
//                                .replace(R.id.movie_detail_container, fragment,
// DETAILFRAGMENT_TAG)
//                                .commit();
////todo replace fragment
//                    } else { // go to detail activity
//Intent intent = new Intent(this, DetailActivity.class);
//                intent.putExtra(Intent.EXTRA_TEXT, movie);
//                startDetailActivity(intent);
            });
            return vh;
        }


        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            mCursor.moveToPosition(position);

            holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            holder.subtitleView.setText(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR));
            holder.thumbnailView.setImageUrl(
                    mCursor.getString(ArticleLoader.Query.THUMB_URL),
                    ImageLoaderHelper.getInstance(ArticleListActivity.this).getImageLoader());
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }
}
