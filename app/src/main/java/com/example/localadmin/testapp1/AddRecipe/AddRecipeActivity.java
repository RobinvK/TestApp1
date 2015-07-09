package com.example.localadmin.testapp1.AddRecipe;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.localadmin.testapp1.DbAdapter;
import com.example.localadmin.testapp1.R;

import java.util.ArrayList;

/**
 * Created on 22-6-2015.
 * Last changed on 9-6-2015
 *
 * V 1.01
 *
 * changes:
 * V1.01: implementation of onRestoreInstanceState & onSaveInstanceState to retain elements added to the Recyclerviews on orientation change
 *
 */
public class AddRecipeActivity extends AppCompatActivity {
    DbAdapter dbHelper;

    private MyRecyclerViewAdapter ingredientListAdapter;
    private MyRecyclerViewAdapter stepListAdapter;
    ArrayList<DataObject> ingredientData;
    ArrayList<DataObject> stepData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);
        dbHelper = new DbAdapter(this);


        if (savedInstanceState != null) {
            Log.d("AddRecipeActivity", "onCreate");
            getIngredientAndStepData(savedInstanceState);
        }
        else{
            ingredientListAdapter = new MyRecyclerViewAdapter(new ArrayList<DataObject>());
            setupRecyclerView((RecyclerView) findViewById(R.id.my_ingredient_recycler_view), ingredientListAdapter);
            stepListAdapter = new MyRecyclerViewAdapter(new ArrayList<DataObject>());
            setupRecyclerView((RecyclerView) findViewById(R.id.my_step_recycler_view), stepListAdapter);
        }
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // This is here because if you are recreating after an orientation change, for example, onCreate won't be called
        if (savedInstanceState != null) {
            Log.d("AddRecipeActivity", "onRestoreInstanceState");
            getIngredientAndStepData(savedInstanceState);
        }
    }

    private void getIngredientAndStepData(Bundle savedInstanceState){
        ingredientData = savedInstanceState.getParcelableArrayList("myIngredientData");
        stepData = savedInstanceState.getParcelableArrayList("myStepData");
        if (ingredientData != null) {
            ingredientListAdapter = new MyRecyclerViewAdapter(ingredientData);
            setupRecyclerView((RecyclerView) findViewById(R.id.my_ingredient_recycler_view), ingredientListAdapter);
        }
        else{
            Log.d("AddRecipeActivity", "ingredientData == null");
        }
        if (stepData != null) {
            stepListAdapter = new MyRecyclerViewAdapter(stepData);
            setupRecyclerView((RecyclerView) findViewById(R.id.my_step_recycler_view), stepListAdapter);
        }
        else{
            Log.d("AddRecipeActivity", "stepData == null");
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        Log.d("AddRecipeActivity", "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        ingredientData = ingredientListAdapter.getDataSet();
        outState.putParcelableArrayList("myIngredientData", ingredientData);
        stepData = stepListAdapter.getDataSet();
        outState.putParcelableArrayList("myStepData", stepData);
    }



    private void setupRecyclerView(RecyclerView mRecyclerView, RecyclerView.Adapter listAdapter) {
        // mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new MyLinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(listAdapter);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);
    }

    @Override
    public void onBackPressed() {
        Log.d("RobinDEBUG080808080 ", "onbackpressed");
        super.onBackPressed();
    }

    public void addIngredient(View view) {
        EditText edit = (EditText) findViewById(R.id.txtItem);
        String textFieldText = edit.getText().toString().trim();
        if (!textFieldText.equals("") && !textFieldText.equals(" ")) {
            ingredientListAdapter.addItem(new DataObject(textFieldText), 0);
            ((EditText) findViewById(R.id.txtItem)).getText().clear();
        }
    }

    public void addStep(View view) {
        //TODO: check if step description does not exceed 1000 characters!
        EditText edit = (EditText) findViewById(R.id.edit_text_step);
        String textFieldText = edit.getText().toString().trim();
        if (!textFieldText.equals("") && !textFieldText.equals(" ")) {
            stepListAdapter.addItem(new DataObject(textFieldText), stepListAdapter.getItemCount());
            ((EditText) findViewById(R.id.edit_text_step)).getText().clear();
        }
    }

    public void addRecipe(View view) {
        Log.d("addRecipe ", " start");
        EditText titleTextField = (EditText) findViewById(R.id.edit_text_recipe_title);
        String title = titleTextField.getText().toString().trim();

        if (title.equals("") || title.equals(" ")) {
            Toast.makeText(this, "Please add a title", Toast.LENGTH_LONG).show();//TODO: improve UI
            return;
        }
        long recipeID = dbHelper.insertRecipe(title);
        if (recipeID < 0) {
            Log.d("addRecipe ", "Something went wrong, recipe " + recipeID + " was not saved");
        } else {
            Log.d("addRecipe ", "recipe " + title + " added at " + recipeID + ".");

            String ingredients = ingredientListAdapter.getDataAsString();
            String[] separated = ingredients.split("`");

            if (separated.length == 0) {
                Toast.makeText(this, "Please add at least one ingredient", Toast.LENGTH_LONG).show();//TODO: improve UI
                return;
            }

            for (int i = 0; i < separated.length - 1; i++) {
                separated[i] = separated[i].trim();
                separated[i] = separated[i].toLowerCase();
                if (separated[i] == null || separated[i].equals("") || separated[i].equals(" ")) {
                    Log.d("addRecipe", " ingredient invalid: " + separated[i] + ".");
                } else if (dbHelper.IsIngredientAlreadyInDB(separated[i])) {
                    long ingredientID = dbHelper.getIngredientID(separated[i]);
                    Log.d("addRecipe", " ingredient " + separated[i] + " already exists @ " + ingredientID + ".");
                    dbHelper.insertIngredientRecipeLink(recipeID, ingredientID);
                    //TODO: check ID for correct entry

                } else {
                    long ingredientID = dbHelper.insertIngredient(separated[i]);
                    Log.d("addRecipe", " ingredient " + separated[i] + " added to DB @ " + ingredientID + ".");
                    //TODO: check ID for correct entry
                    dbHelper.insertIngredientRecipeLink(recipeID, ingredientID);
                    //TODO: check ID for correct entry
                }
            }


            String steps = stepListAdapter.getDataAsString();
            separated = steps.split("`");

            if (separated.length == 0) {
                Toast.makeText(this, "Please add at least one step", Toast.LENGTH_LONG).show();//TODO: improve UI
                return;
            }

            for (int i = 0; i < separated.length - 1; i++) {
                separated[i] = separated[i].trim();
                if (separated[i] == null || separated[i].equals("") || separated[i].equals(" ")) {
                    Log.d("addRecipe", " step invalid: " + separated[i] + ".");
                } else {
                    Log.d("addRecipe", " step for recipe " + recipeID + " added to DB @ " + dbHelper.insertStep(recipeID, separated[i]) + ".");
                    //TODO: check ID for correct entry
                }
            }


        }
    }
}
