package com.example.localadmin.testapp1.ViewRecipe;

/**
 * Created on 7-7-2015.
 * Last changed on 7-7-2015
 * Current version: V 1.0
 * <p>
 * changes:
 *
 */
public class RecipeDataCard {
    private String mName;
    private String mDes;
    private String imagePath;
    private int mThumbnail;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getDes() {
        return mDes;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setDes(String des) {
        this.mDes = des;
    }

    public int getThumbnail() {
        return mThumbnail;
    }

    public void setThumbnail(int thumbnail) {
        this.mThumbnail = thumbnail;
    }

}
