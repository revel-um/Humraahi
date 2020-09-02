package com.revel.humraahi.classes;

import android.graphics.Bitmap;

public class Model {
    private Bitmap imageView;
    private String  username;

    public Bitmap getImage() {
        return imageView;
    }

    public String getUsername() {
        return username;
    }

    public Model(Bitmap imageView, String username) {
        this.imageView = imageView;
        this.username = username;
    }
}
