package com.example.localadmin.testapp1.ViewRecipe;

/**
 * Created on 22-6-2015.
 * Last changed on 15-7-2015
 * Current version: V 1.02
 * <p>
 * changes:
 * V1.02 - 23-7-2015: removal of unnecessary library
 * V1.01 - 9-7-2015: implementation of FoldableLayout for recipes, implementation of scrollview on viewing a single recipe.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.localadmin.testapp1.DbAdapter;
import com.example.localadmin.testapp1.ViewRecipe.FoldableItem.UnfoldableView;
import com.example.localadmin.testapp1.ViewRecipe.FoldableItem.shading.GlanceFoldShading;
import com.example.localadmin.testapp1.R;

public class ViewRecipeListActivity extends AppCompatActivity {

    private ListView mListView;
    private View mListTouchInterceptor;
    private View mDetailsLayout;
    private View mDetailsScrollView;
    private UnfoldableView mUnfoldableView;

    DbAdapter dbHelper;

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    MyCardAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("RRROBIN APP", "onCreate ViewRecipeListActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe_list);
        dbHelper = new DbAdapter(this);

        createRecipeList(); //create the recipe list RecyclerView
        createRecipeLayout();//create the view for the full recipe view

        setFoldingListener(); //set listener for onUnfolding, onUnfolded, onFoldingBack and onFoldedBack for the full recipe view
        setScrollViewListener();//Update full recipe view's FoldableItemLayout with ScrollView's Scroll-position

    }

    private void createRecipeLayout(){
        mListTouchInterceptor = (View)findViewById(R.id.touch_interceptor_view);
        mListTouchInterceptor.setClickable(false);

        mDetailsLayout = (View)findViewById(R.id.details_layout);
        mDetailsLayout.setVisibility(View.INVISIBLE);
        mUnfoldableView = (UnfoldableView)findViewById(R.id.unfoldable_view);

        Bitmap glance = BitmapFactory.decodeResource(getResources(), R.drawable.unfold_glance);
        mUnfoldableView.setFoldShading(new GlanceFoldShading(this, glance));

        mDetailsScrollView = findViewById(R.id.recipe_detail_scroll_view);
    }

    private void createRecipeList() {
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
            recipeCard.setImagePath(dbHelper.getRecipeImagePath(i));
            mAdapter.addItem(recipeCard);
        }

        mRecyclerView.setAdapter(mAdapter);
    }


    public interface ScrollViewListener {
        void onScrollChanged(ScrollViewExt scrollView,
                             int x, int y, int oldX, int oldY);
    }

    private void setScrollViewListener() {
        ((ScrollViewExt) mDetailsScrollView).setScrollViewListener(new ScrollViewListener() {
            @Override
            public void onScrollChanged(ScrollViewExt scrollView, int x, int y, int oldX, int oldY) {
                if (mUnfoldableView.isUnfolded() || mUnfoldableView.isFoldingBack()) {
                    mUnfoldableView.parentScrollViewPosition = scrollView.getScrollY();
                }
                else{
                }
            }
        });
    }

    private void setFoldingListener() {
        mUnfoldableView.setOnFoldingListener(new UnfoldableView.SimpleFoldingListener() {
            @Override
            public void onUnfolding(UnfoldableView unfoldableView) {
                mDetailsScrollView.scrollTo(0, 0);
                mListTouchInterceptor.setClickable(true);
                mDetailsLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onUnfolded(UnfoldableView unfoldableView) {
                mListTouchInterceptor.setClickable(false);
                mDetailsScrollView.setScrollY(1);
            }

            @Override
            public void onFoldingBack(UnfoldableView unfoldableView) {
                mListTouchInterceptor.setClickable(true);
            }

            @Override
            public void onFoldedBack(UnfoldableView unfoldableView) {
                mDetailsScrollView.setScrollY(0);
                mListTouchInterceptor.setClickable(false);
                mDetailsLayout.setVisibility(View.INVISIBLE);
            }
        });
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
        ImageView image = (ImageView)findViewById(R.id.details_image);
        TextView title = (TextView)findViewById(R.id.details_title);
        TextView description = (TextView)findViewById(R.id.details_text);
        image.setImageBitmap(BitmapFactory.decodeFile(recipeImagePath));
        title.setText(recipeTitle.getText());
        description.setText(recipeDescription.getText());
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
