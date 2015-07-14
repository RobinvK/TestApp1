package com.example.localadmin.testapp1.AddRecipe;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.localadmin.testapp1.DbAdapter;
import com.example.localadmin.testapp1.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created on 22-6-2015.
 * Last changed on 9-7-2015
 * Current version: V 1.01
 * <p>
 * changes:
 * V1.01 - 9-7-2015: implementation of onRestoreInstanceState & onSaveInstanceState to retain elements added to the Recyclerviews on orientation change
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
        } else {
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

    private void getIngredientAndStepData(Bundle savedInstanceState) {
        ingredientData = savedInstanceState.getParcelableArrayList("myIngredientData");
        stepData = savedInstanceState.getParcelableArrayList("myStepData");
        if (ingredientData != null) {
            ingredientListAdapter = new MyRecyclerViewAdapter(ingredientData);
            setupRecyclerView((RecyclerView) findViewById(R.id.my_ingredient_recycler_view), ingredientListAdapter);
        } else {
            Log.d("AddRecipeActivity", "ingredientData == null");
        }
        if (stepData != null) {
            stepListAdapter = new MyRecyclerViewAdapter(stepData);
            setupRecyclerView((RecyclerView) findViewById(R.id.my_step_recycler_view), stepListAdapter);
        } else {
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

            //-------Add ingredients---------
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

            //-------Add steps---------

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


            //-------Add images---------
            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ig);

            String imagePath = SaveImage(largeIcon, title);
            if(imagePath!="N/A"){
                Log.d("RRROBIN", " image for recipe " + recipeID + " added to DB @ " + dbHelper.insertImage(recipeID, imagePath) + ".");
            }
            else{
                Log.d("RRROBIN", "  image not saved ");
            }
        }
    }


    private String SaveImage(Bitmap finalBitmap, String title) {
        File imagesFolder;
        File myFile = null;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        if (finalBitmap != null) {
            Log.d("RRROBIN", " finalBitmap exists " );
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes);
        }
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                imagesFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/RecipeSaver");
                Log.d("RRROBIN", " 1 imagesFolder: " + imagesFolder);
            } else {
                imagesFolder = new File(Environment.getExternalStorageDirectory() + "/dcim/" + "RecipeSaver");
                Log.d("RRROBIN", " 2 imagesFolder: " + imagesFolder);
            }

            if (!imagesFolder.exists()) {
                imagesFolder.mkdirs();
                Log.d("RRROBIN", " 3 imagesFolder: " + imagesFolder);
            }


            myFile = new File(imagesFolder.toString(), "" + title + "0001.jpeg");
            if (myFile.exists())
                Log.d("RRROBIN", " :myFile.exists() ");
            myFile.delete();
            try {
                FileOutputStream out = new FileOutputStream(myFile);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
            } catch (IOException e1) {
                Log.d("RRROBIN", " e1: " + e1);
                e1.printStackTrace();
            }

            if (myFile != null) {
                MediaScannerConnection.scanFile(this, new String[]{myFile.toString()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("ExternalStorage", "Scanned " + path + ":");
                                Log.i("ExternalStorage", "-> uri=" + uri);
                            }
                        });
                return myFile.toString();
            }

        }
        return "N/A";

    }




}
