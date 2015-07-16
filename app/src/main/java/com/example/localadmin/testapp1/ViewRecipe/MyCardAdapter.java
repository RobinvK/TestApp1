package com.example.localadmin.testapp1.ViewRecipe;

import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.localadmin.testapp1.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 7-7-2015.
 * Last changed on 7-7-2015
 * Current version: V 1.0
 * <p>
 * changes:
 *
 */
public class MyCardAdapter extends RecyclerView.Adapter<MyCardAdapter.CardViewHolder> {

    List<RecipeDataCard> mItems;

    public MyCardAdapter() {
        super();
        Log.d("RRROBIN APP", "MyCardAdapter MyCardAdapter");
        mItems = new ArrayList<RecipeDataCard>();
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_recipe_card, viewGroup, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CardViewHolder viewHolder, int i) {
        RecipeDataCard recipeDataCard = mItems.get(i);
        viewHolder.title.setText(recipeDataCard.getName());
        viewHolder.description.setText(recipeDataCard.getDes());
        //viewHolder.image.setImageResource(recipeDataCard.getThumbnail());
        //viewHolder.image.getContext()
        Log.d("RRROBIN RECIPEDATA", " b4 getRecipeImagePath  ");
        Log.d("RRROBIN RECIPEDATA", "  recipeDataCard.getImagePath()  " + recipeDataCard.getImagePath());
        viewHolder.image.setImageBitmap(BitmapFactory.decodeFile(recipeDataCard.getImagePath()));
        viewHolder.imagePath=recipeDataCard.getImagePath();
       // Picasso.with(viewHolder.image.getContext()).load(recipeDataCard.getImagePath()).into(viewHolder.image);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void addItem(RecipeDataCard nature) {
        mItems.add(0, nature);
        notifyItemInserted(0);
    }

    class CardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView image;
        public TextView title;
        public TextView description;
        public String imagePath;

        public CardViewHolder(View itemView){
            super(itemView);
            image = (ImageView)itemView.findViewById(R.id.img_thumbnail);
            title = (TextView)itemView.findViewById(R.id.tv_nature);
            description = (TextView)itemView.findViewById(R.id.tv_des_nature);
            image.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.getContext() instanceof ViewRecipeListActivity) {
                ViewRecipeListActivity activity = (ViewRecipeListActivity) view.getContext();
                activity.openDetails(view, imagePath, title, description);
            }
        }
    }
}
