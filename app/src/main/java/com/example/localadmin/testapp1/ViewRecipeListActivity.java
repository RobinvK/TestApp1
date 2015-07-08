package com.example.localadmin.testapp1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.localadmin.testapp1.AddRecipe.MyCardAdapter;

/**
 * Created by robin on 22-6-2015
 */
public class ViewRecipeListActivity extends AppCompatActivity {

    DbAdapter dbHelper;

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    MyCardAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe_list);
        dbHelper = new DbAdapter(this);

        mRecyclerView = (RecyclerView)findViewById(R.id.my_recipe_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MyCardAdapter();
        mRecyclerView.setAdapter(mAdapter);


        int numberOfRecipes = dbHelper.numberOfRecipes();
        Log.d("viewrecipelist ", numberOfRecipes + "");


        for (int i = 1; i <= numberOfRecipes; i++) {
            RecipeDataCard nature = new RecipeDataCard();
            nature.setName(dbHelper.getRecipeName(i));
            nature.setDes(dbHelper.getRecipeIngredients(i));
            nature.setThumbnail(R.drawable.ig);
            mAdapter.addItem(nature);
        }

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
