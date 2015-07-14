package com.example.localadmin.testapp1;


import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alexvasilkov.android.commons.texts.SpannableBuilder;
import com.alexvasilkov.android.commons.utils.Views;

import com.example.localadmin.testapp1.AddRecipe.ScrollViewExt;
import com.example.localadmin.testapp1.FoldableItem.UnfoldableView;
import com.example.localadmin.testapp1.FoldableItem.shading.GlanceFoldShading;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class ViewRecipeListActivity extends ViewRecipeActivity {

    private ListView mListView;
    private View mListTouchInterceptor;
    private View mDetailsLayout;
    private View mDetailsScrollView;
    private UnfoldableView mUnfoldableView;
    private String scrollviewState = "onFoldedBack";


    DbAdapter dbHelper;

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    MyCardAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("RRROBIN", "onCreate ViewRecipeListActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe_list);
        dbHelper = new DbAdapter(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recipe_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MyCardAdapter();

        int numberOfRecipes = dbHelper.numberOfRecipes();

        for (int i = 1; i <= numberOfRecipes; i++) {
            RecipeDataCard recipeCard = new RecipeDataCard();
            recipeCard.setName(dbHelper.getRecipeName(i));
            recipeCard.setDes(dbHelper.getRecipeIngredients(i));
            recipeCard.setThumbnail(R.drawable.ig);
            Log.d("RRROBIN", " b4 getRecipeImagePath  ");
            Log.d("RRROBIN", "getRecipeImagePath = " + dbHelper.getRecipeImagePath(i));
            recipeCard.setImagePath(dbHelper.getRecipeImagePath(i));
            mAdapter.addItem(recipeCard);
        }

        mRecyclerView.setAdapter(mAdapter);
        mListTouchInterceptor = Views.find(this, R.id.touch_interceptor_view);
        mListTouchInterceptor.setClickable(false);

        mDetailsLayout = Views.find(this, R.id.details_layout);
        mDetailsLayout.setVisibility(View.INVISIBLE);
        mUnfoldableView = Views.find(this, R.id.unfoldable_view);
        mDetailsScrollView = Views.find(this, R.id.recipe_detail_scroll_view);

        Bitmap glance = BitmapFactory.decodeResource(getResources(), R.drawable.unfold_glance);
        mUnfoldableView.setFoldShading(new GlanceFoldShading(this, glance));


        mDetailsScrollView = (ScrollViewExt) findViewById(R.id.recipe_detail_scroll_view);


        mUnfoldableView.setOnFoldingListener(new UnfoldableView.SimpleFoldingListener() {
            @Override
            public void onUnfolding(UnfoldableView unfoldableView) {
                scrollviewState = "onUnfolding";
                Log.d("RRROBIN", "onUnfolding");
                mDetailsScrollView.scrollTo(0, 0);
                mListTouchInterceptor.setClickable(true);
                mDetailsLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onUnfolded(UnfoldableView unfoldableView) {
                scrollviewState = "onUnfolded";
                Log.d("RRROBIN", "onUnfolded");
                mListTouchInterceptor.setClickable(false);
                mDetailsScrollView.setScrollY(1);
            }

            @Override
            public void onFoldingBack(UnfoldableView unfoldableView) {
                scrollviewState = "onFoldingBack";
                Log.d("RRROBIN", "onFoldingBack");
                mListTouchInterceptor.setClickable(true);
            }

            @Override
            public void onFoldedBack(UnfoldableView unfoldableView) {
                scrollviewState = "onFoldedBack";
                Log.d("RRROBIN", "onFoldedBack");
                mDetailsScrollView.setScrollY(0);
                mListTouchInterceptor.setClickable(false);
                mDetailsLayout.setVisibility(View.INVISIBLE);
            }
        });

//TODO difference between scrollview position and folding position
        ((ScrollViewExt) mDetailsScrollView).setScrollViewListener(new ScrollViewListener() {
            @Override
            public void onScrollChanged(ScrollViewExt scrollView, int x, int y, int oldx, int oldy) {
                if (scrollviewState.equals("onUnfolded") || scrollviewState.equals("onFoldingBack") ) {
                    Log.d("RRROBIN-----", "onScrollChanged x = " + x + " y = " + y + " oldx = " + oldx + " oldy = " + oldy);
                    // We take the last son in the scrollview
                    View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
                    int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));

                    // if diff is zero, then the bottom has been reached
                    if (diff == 0) {
                        Log.d("RRROBIN-----", " bottom reached");
                    }
                    float mScrollStartRotation = mUnfoldableView.getFoldRotation();
                    if (scrollView.getScrollY() < 5 && oldy > y) {
                        Log.d("RRROBIN-xxxxx", " YES:scrollView.getScrollY()= " + scrollView.getScrollY() + ", oldy = " + oldy + " , y = " + y);
                        Log.d("RRROBIN-0000", " YES:scrollView.getScrollY()= " + scrollView.getScrollY() + ", oldy = " + oldy + " , y = " + y);

                       // int h = mUnfoldableView.getHeight();
                        //float rotation = 180f * 1.33f / h;
                      //  Log.d("RRROBIN-xxxxx", " mScrollStartRotation " + mScrollStartRotation + " ,  rotation " + rotation);
                        mUnfoldableView.setFoldRotation(mScrollStartRotation - 5, true);//TODO: let scroll depend on distance scrolled (FoldableListLayout, onScroll)
                    } else if(oldy>(y+1)){
                        Log.d("RRROBIN-xxxxx", " NO:scrollView.getScrollY()= " + scrollView.getScrollY() + ", oldy = " + oldy + " , y = " + y);
                        mUnfoldableView.setFoldRotation(mScrollStartRotation + 5, true);
                    }
                    else if(mScrollStartRotation<180){
                      //  Log.d("RRROBIN-xxxxx", " NO, FINISH WHAT YOU STARTED :scrollView.getScrollY()= " + scrollView.getScrollY() + ", oldy = " + oldy + " , y = " + y);
                       // mUnfoldableView.setFoldRotation(mScrollStartRotation - 5, true);
                    }
                    else{
                        Log.d("RRROBIN-xxxxx", " NO:scrollView.getScrollY()= " + scrollView.getScrollY() + ", oldy = " + oldy + " , y = " + y);

                    }

                    if (scrollView.getScrollY() <= 0) {
                        scrollView.setScrollY(1);
                        Log.d("RRROBIN-----", " scrollView.setScrollY(1);");

                    }
                }
                else{
                    Log.d("RRROBIN-----", " not foldingBack or unfolded");
                }
            }
        });

    }
/*
*





        final SwipeRefreshLayout swipeView = (SwipeRefreshLayout) findViewById(R.id.swipe);

        mDetailsScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

            @Override
            public void onScrollChanged() {
                int scrollY = mDetailsScrollView.getScrollY();
                if(scrollY == 0){ swipeView.setEnabled(true);
                    Log.d("RRROBIN", " swipeView setEnabled true");}
                else {swipeView.setEnabled(false);

                    Log.d("RRROBIN", " swipeView setEnabled false");}

            }
        });

       swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeView.setRefreshing(true);
                Log.d("RRROBIN", "onRefresh ");
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("RRROBIN", "setRefreshing false");
                        swipeView.setRefreshing(false);
                    }
                }, 3000);
            }
        });





* */

    /*
    *



            mDetailsScrollView = (ScrollViewExt) findViewById(R.id.recipe_detail_scroll_view);
        ((ScrollViewExt)mDetailsScrollView).setScrollViewListener(new ScrollViewListener() {
            @Override
            public void onScrollChanged(ScrollViewExt scrollView, int x, int y, int oldx, int oldy) {
                Log.d("RRROBIN", "onScrollChanged x = "+x+" y = "+ y);
                // We take the last son in the scrollview
                View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);
                int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));

                // if diff is zero, then the bottom has been reached
                if (diff == 0) {
                    Log.d("RRROBIN", " bottom reached");
                }
            }
        });



    *
    * */

    public interface ScrollViewListener {
        void onScrollChanged(ScrollViewExt scrollView,
                             int x, int y, int oldx, int oldy);
    }

    @Override
    public void onBackPressed() {
        if (mUnfoldableView != null && (mUnfoldableView.isUnfolded() || mUnfoldableView.isUnfolding())) {
            mUnfoldableView.foldBack();
        } else {
            super.onBackPressed();
        }
    }

    public void openDetails(View coverView, String recipeImagePath, TextView recipeTitle, TextView recipeDescription) {
        Log.d("RRROBIN", "openDetails");
        ImageView image = Views.find(mDetailsLayout, R.id.details_image);
        TextView title = Views.find(mDetailsLayout, R.id.details_title);
        TextView description = Views.find(mDetailsLayout, R.id.details_text);

        //Picasso.with(this).load(painting.getImageId()).into(image);
        // TypedArray images = this.getResources().obtainTypedArray(R.array.paintings_images);
        // Picasso.with(this).load(images.getResourceId(1, -1)).into(image);
        // Log.d("RRROBIN","recipeImage.getScaleType() "+recipeImage.getScaleType());
        // Log.d("RRROBIN", "recipeImage.getImageMatrix() " + recipeImage.getImageMatrix());
        // image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        //image.setImageDrawable(recipeImage.getDrawable());

        image.setImageBitmap(BitmapFactory.decodeFile(recipeImagePath));
        Log.d("RRROBIN", "image.getScaleType() " + image.getScaleType());
        Log.d("RRROBIN", "image.getImageMatrix() " + image.getImageMatrix());
        title.setText(recipeTitle.getText());
        //title.setText(painting.getTitle());

        description.setText(recipeDescription.getText());

        Log.d("RRROBIN", "before unfold");
        mUnfoldableView.unfold(coverView, mDetailsLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_recipe_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

}
