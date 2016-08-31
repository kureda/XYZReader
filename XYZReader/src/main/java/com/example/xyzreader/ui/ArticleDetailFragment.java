package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

import static com.example.xyzreader.R.id.photo;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    public static final String ARG_ITEM_ID = "item_id";
    private static final String TAG = "ArticleDetailFragment";
    private static final float PARALLAX_FACTOR = 1.25f;
    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int lightVibrantColor = 0xFF333333;
    private int mStatusBarColor = 0xFF000000;
    private int mOldColor = 0;
//    private ItemIdListener mItemIdListener;

    //    private ObservableScrollView mScrollView;
//    private DrawInsetsFrameLayout mDrawInsetsFrameLayout;
    private ColorDrawable mStatusBarColorDrawable;

    private int mTopInset;
    //    private View mPhotoContainerView;
    private ImageView mPhotoView;
    private int mScrollY;
    private boolean mIsCard = false;
    private int mStatusBarFullOpacityBottom;
    private String title = "notyet";//todo for debugging, delete


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }
//
//    static float progress(float v, float min, float max) {
//        return constrain((v - min) / (max - min), 0, 1);
//    }
//
//    static float constrain(float val, float min, float max) {
//        if (val < min) {
//            return min;
//        } else if (val > max) {
//            return max;
//        } else {
//            return val;
//        }
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle!=null && bundle.containsKey(ARG_ITEM_ID)) {
            mItemId = bundle.getLong(ARG_ITEM_ID);
        }

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(
                R.dimen.detail_card_top_margin);
        setHasOptionsMenu(true);
        ///       mStatusBarColor = getActivity().getIntent().getIntExtra("color", mStatusBarColor);
        Log.d("Serg",": fragment created, itemId= " +ArticleListActivity.sItemId +
                "===================");
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
//        Log.d("Serg",": fragment view creating start, ============================");

//        mDrawInsetsFrameLayout = (DrawInsetsFrameLayout)
//                mRootView.findViewById(R.id.draw_insets_frame_layout);
//        mDrawInsetsFrameLayout.setOnInsetsCallback(new DrawInsetsFrameLayout.OnInsetsCallback() {
//            @Override
//            public void onInsetsChanged(Rect insets) {
//                mTopInset = insets.top;
//            }
//        }
//        );

//        mScrollView = (ObservableScrollView) mRootView.findViewById(R.id.scrollview);
//        mScrollView.setCallbacks(new ObservableScrollView.Callbacks() {
//            @Override
//            public void onScrollChanged() {
//                mScrollY = mScrollView.getScrollY();
//                getActivityCast().onUpButtonFloorChanged(mItemId, ArticleDetailFragment.this);
//                mPhotoContainerView.setTranslationY((int) (mScrollY - mScrollY /
// PARALLAX_FACTOR));
//                updateStatusBar();
//            }
//        });

        mPhotoView = (ImageView) mRootView.findViewById(photo);
//        mPhotoContainerView = mRootView.findViewById(R.id.photo_container);

        mStatusBarColorDrawable = new ColorDrawable(0);

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        bindViews();
//        updateStatusBar();
  //      Log.d("Serg",": fragment view creating end, ============================");
        return mRootView;
    }

    public long getItemId(){
        return mItemId;
    }

    public String getTitle(){// // TODO: 8/30/2016 for debug only , remove
        return title;
    }

//    private void updateStatusBar() {
//        int color = 0;
//        if (mPhotoView != null && mTopInset != 0 && mScrollY > 0) {
//            float f = progress(mScrollY,
//                    mStatusBarFullOpacityBottom - mTopInset * 3,
//                    mStatusBarFullOpacityBottom - mTopInset);
//            color = Color.argb((int) (255 * f),
//                    (int) (Color.red(lightVibrantColor) * 0.9),
//                    (int) (Color.green(lightVibrantColor) * 0.9),
//                    (int) (Color.blue(lightVibrantColor) * 0.9));
//        }
//        mStatusBarColorDrawable.setColor(color);
//
/////  todo - use color somewhre else?        mDrawInsetsFrameLayout.setInsetBackground
//// (mStatusBarColorDrawable);
//
//    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        mItemIdListener = (ItemIdListener) activity;
//    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
        TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);
        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(),
                "Rosario-Regular.ttf"));

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            String titleText = mCursor.getString(ArticleLoader.Query.TITLE);
            titleView.setText(titleText);
            title=titleText;//todo for debug, delete
            Log.d("Serg",": fragment bindingView titleText=,"+titleText+" =======================");
            Log.d("Serg",": onCreateView()->bindViews(), mItemId="+mItemId+"=====================");
            bylineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));

            String text = mCursor.getString(ArticleLoader.Query.BODY) + "<br><br>";
            bodyView.setText(Html.fromHtml(text));
            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader
                            .ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer container, boolean imm) {
                            Bitmap bitmap = container.getBitmap();
                            if (bitmap == null)
                                return;

                            Palette.PaletteAsyncListener paletteListener = new Palette
                                    .PaletteAsyncListener() {
                                public void onGenerated(Palette palette) {
                                    int vibrantColor = getSomeColor(palette);
                                    Log.d("Serg", ": ");
                                    setBarsColor(vibrantColor);
                                }
                            };

                            Palette.from(bitmap).maximumColorCount(12).generate(paletteListener);

                            //Palette p = new Palette.Builder(bitmap)
                            ///  .maximumColorCount(6)
                            ///            .generate();


                            //   lightVibrantColor = p.getLightVibrantColor(0xFFcccccc);
                            //Log.d("Serg",": color: "+String.format("#%06X", (0xFFFFFF &
                            // lightVibrantColor)));
                            mPhotoView.setImageBitmap(container.getBitmap());
                            //  View metaBar = mRootView.findViewById(R.id.meta_bar);
                            //   metaBar.setBackgroundColor(lightVibrantColor);
                            ///updateStatusBar();
                            ///   getActivity().setContentView(R.layout.activity_article_detail);
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
         //                   mPhotoView.setImageBitmap(R.drawable.logo.getBitmap())todo get bitmap
                            Log.d("Serg", ": onError response - volley cant load image" +
                                    volleyError);
                        }
                    });
        } else {
            mRootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A");
            bodyView.setText("N/A");
        }
    }

    public void setBarsColor(int color) {
        Log.d("Serg", ": settingBarColor, mItemId=" + ArticleListActivity.sItemId);

        ///    View metaBar = mRootView.findViewById(R.id.meta_bar);
        View frame = mRootView.findViewById(R.id.detail_frame_layout);
        frame.setBackgroundColor(color);
        ///    metaBar.setBackgroundColor(color);
        //    if (getUserVisibleHint() && Build.VERSION.SDK_INT >= 21) {

//        boolean twoPane = getResources().getBoolean(R.bool.twoPaneMode);
//        if (Build.VERSION.SDK_INT >= 21 && !twoPane) {
//            Activity activity = getActivity();
//            Window window = activity.getWindow();
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(color);
            //  mOldColor=color;
//        }
    }

//
//    private Palette.Swatch getDominantSwatch(Palette palette){
//            // find most-represented swatch based on population
//            return Collections.max(palette.getSwatches(), new Comparator<Palette.Swatch>() {
//                @Override
//                public int compare(Palette.Swatch sw1, Palette.Swatch sw2) {
//                    return Integer.compare(sw1.getPopulation(), sw2.getPopulation());
//                }
//            });
//        }


    //Pallete not always can create desired swatch, so we take what we got.
    private int getSomeColor(Palette p) {
        return p.getMutedColor(
                p.getVibrantColor(
                        p.getMutedColor(
                                p.getLightVibrantColor(
                                        p.getLightMutedColor(
                                                p.getDarkVibrantColor(
                                                        p.getDarkMutedColor(
                                                                0xff00ff
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );

//        Palette.Swatch swatch = getDominantSwatch(p);
//        int color = swatch.getTitleTextColor(); //getBodyTextColor();
//        return color;

//        int defaultColor = 0xff00ff;
//        if (p.getVibrantSwatch() != null)
//            return p.getVibrantColor(defaultColor);
//        if (p.getLightVibrantSwatch() != null)
//            return p.getLightVibrantColor(defaultColor);
//        if (p.getDarkVibrantSwatch() != null)
//            return p.getDarkVibrantColor(defaultColor);
//        if (p.getMutedSwatch() != null)
//            return p.getMutedColor(defaultColor);
//        if (p.getLightMutedSwatch() != null)
//            return p.getLightMutedColor(defaultColor);
//        if (p.getDarkMutedSwatch() != null)
//            return p.getDarkMutedColor(defaultColor);
//        return defaultColor;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            Log.d("Serg", ": onLoadFinished".toString());
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }

//    public int getUpButtonFloor() {
//        if (mPhotoContainerView == null || mPhotoView.getHeight() == 0) {
//            return Integer.MAX_VALUE;
//        }
//
//        // account for parallax todo remove parallax?
//        return mIsCard
//                ? (int) mPhotoContainerView.getTranslationY() + mPhotoView.getHeight() - mScrollY
//                : mPhotoView.getHeight() - mScrollY;
//    }

//    public interface ItemIdListener{
//        public void setItemId(long itemId);
//    }
}
