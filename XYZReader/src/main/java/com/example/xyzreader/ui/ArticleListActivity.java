package com.example.xyzreader.ui;

import android.app.Fragment;
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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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

//    private static final String ITEM_ID = "itemId";
    public static long sItemId = -1; //it is static to be changeable from other activities.

    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private boolean mIsRefreshing = false;
    private boolean mTwoPane;
    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_article_list);
        ///    sItemId = (sItemId<0) ? 4863 : sItemId; // todo initialise properly
        ///    sItemId = (sItemId<0) ? 0 : sItemId; // todo initialise properly
            mTwoPane = getResources().getBoolean(R.bool.twoPaneMode);
            Log.d("Serg", ": twoPane=" + mTwoPane);
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            final View toolbarContainerView = findViewById(R.id.toolbar_container);
            mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
            mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            getLoaderManager().initLoader(0, null, this);
//            if (savedInstanceState == null) {
//                Log.d("Serg", ": savedInstanceState was null-----------------");
//                refresh();  //todo need it?
//            }
            //
            View fragmentContainer = (View) findViewById(R.id.fragment_container);
            if (mTwoPane && fragmentContainer != null) { //create detail pane
                if (savedInstanceState != null) {
                    Log.d("Serg", ": ArticleActivity.onCreate(): Saved Instance was null.");
                    // return;
                }

                Fragment mFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
                if (mFragment == null) {
                    ArticleDetailFragment fragment = ArticleDetailFragment.newInstance(sItemId);
                    getFragmentManager()
                            .beginTransaction()
                            .add(R.id.fragment_container, fragment)
                            .commit();
                    Log.d("Serg", ": fragment container added, itemId= " + sItemId +
                            "===================");
                }
            }

//        if (mTwoPane) { //create detail pane
//            long itemId = mRecyclerView.getAdapter().getItemId(0);
//            addDetailFragment(itemId);
//        }

//            GridLayoutManager layoutManager = ((GridLayoutManager)mRecyclerView.getLayoutManager());
//            int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();

        } catch (Exception ex) {
            Log.e("Serg", "exception in onCreate", ex);
        }
    }


//    private void addDetailFragment(long itemId) {
//        getFragmentManager()
//                .beginTransaction()
//                .add(R.id.fragment_container, ArticleDetailFragment.newInstance(itemId))
//                .commit();
//    }

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
//todo  at point where adapter is loading or created???
//        if (mTwoPane) { //create detail pane
//            long itemId = 0;
//            try {
////itemId = mRecyclerView.getAdapter().getItemId(0);//todo how we get first itemId if we have
// adapter
//            } catch (Exception ex) {
//                Log.e("Serg", "failed to get adapter.", ex);
//            }
//            replaceDetailFragment(itemId);//todo wrong itmeid
//            //addDetailFragment(itemId);
//        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }

//    @Override
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//        savedInstanceState.putLong(ITEM_ID, mItemId);
//        super.onSaveInstanceState(savedInstanceState);
//    }

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
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
///        int columnCount = getResources().getInteger(R.integer.list_column_count);
///        StaggeredGridLayoutManager sglm =
///                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
///        mRecyclerView.setLayoutManager(sglm);
        LinearLayoutManager m = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(m);

//        int p = m.findFirstVisibleItemPosition();
//        View child = m.findViewByPosition(1);

        cursor.moveToFirst();
        if(sItemId<0) sItemId = cursor.getLong(ArticleLoader.Query._ID);
//        replaceDetailFragment(sItemId);

//        View child = (View)mRecyclerView.getChildAt(p);
//        Log.d("Serg",": onLoadFinished() firstvispos="+p+", child=" + child +
//                "+++first id="+id+"+++++++++++++++");
      //  child.performClick();


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
            //int vibrantLight = palette.getLightVibrantColor(color);
            //int vibrantDark = palette.getDarkVibrantColor(color);
            //int muted = palette.getMutedColor(color);
            //int mutedLight = palette.getLightMutedColor(color);
            //int mutedDark = palette.getDarkMutedColor(color);
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

    public void setItemId(long itemId) {
        if(sItemId<0) sItemId = itemId; //where to open detail page at first run
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ///public DynamicHeightNetworkImageView thumbnailView;
        public NetworkImageView thumbnailView;///
        public TextView titleView;
        public TextView subtitleView;

        public ViewHolder(View view) {
            super(view);
            ///thumbnailView = (DynamicHeightNetworkImageView) view.findViewById(R.id.thumbnail);
            thumbnailView = (NetworkImageView) view.findViewById(R.id.thumbnail);///
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
                    Log.d("Serg", ": itemid:" + sItemId +
                            "---------------------------------------------------------");
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
            /// holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query
            // .ASPECT_RATIO));
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }
}
