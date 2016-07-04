package com.appunite.dagger;


import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;

import com.appunite.GalleryBaseActivity;

import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;

@Module
public class GalleryActivityModule {
    @Nonnull
    private final GalleryBaseActivity activity;

    public GalleryActivityModule(@Nonnull GalleryBaseActivity activity) {
        this.activity = activity;
    }

    @Nonnull
    @Provides
    @ForActivity
    @GalleryActivitySingleton
    public Context activityContext() {
        return activity;
    }

    @Nonnull
    @Provides
    @ForActivity
    @GalleryActivitySingleton
    public Resources provideResource(@ForActivity Context context) {
        return context.getResources();
    }

    @Nonnull
    @Provides
    @GalleryActivitySingleton
    public LayoutInflater provideLayoutInflater(@ForActivity Context context) {
        return LayoutInflater.from(context);
    }

    @Nonnull
    @Provides
    @ForActivity
    @GalleryActivitySingleton
    public ContentResolver provideContentResolver() {
        return activity.getContentResolver();
    }
}
