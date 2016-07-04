package com.appunite;


import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.appunite.dagger.ForActivity;
import com.appunite.dagger.GalleryActivityModule;
import com.appunite.dagger.GalleryApplicationComponent;
import com.appunite.dagger.GalleryComponentProvider;
import com.appunite.dagger.GalleryDaoComponent;

import javax.annotation.Nonnull;

public abstract class GalleryBaseActivity extends AppCompatActivity {

    public interface BaseActivityComponent extends GalleryDaoComponent {
        @ForActivity
        Resources resources();

        @ForActivity
        Context activityContext();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        final GalleryApplicationComponent applicationComponent = ((GalleryComponentProvider) getApplication()).provideApplicationComponent();
        final GalleryActivityModule galleryActivityModule = new GalleryActivityModule(this);
        inject(applicationComponent, galleryActivityModule);
        super.onCreate(savedInstanceState);
    }

    @Nonnull
    public abstract BaseActivityComponent inject(@Nonnull GalleryApplicationComponent applicationComponent,
                                                 @Nonnull GalleryActivityModule galleryActivityModule);

}