package com.appunite.photogallery;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.appunite.buckets.GalleryActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startActivity(GalleryActivity.newIntent(this, "Send to someone", true, true));
    }
}
