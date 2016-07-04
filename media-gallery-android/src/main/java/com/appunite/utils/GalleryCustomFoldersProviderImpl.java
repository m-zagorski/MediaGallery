package com.appunite.utils;


import android.content.res.Resources;

import com.appunite.R;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class GalleryCustomFoldersProviderImpl implements GalleryCustomFoldersProvider {

    @Nonnull
    private final Resources resources;

    @Inject
    public GalleryCustomFoldersProviderImpl(@Nonnull Resources resources) {
        this.resources = resources;
    }

    @Nonnull
    @Override
    public String allImagesFolderName() {
        return resources.getString(R.string.com_appunite_gallery_all_images);
    }

    @Nonnull
    @Override
    public String allVideosFolderName() {
        return resources.getString(R.string.com_appunite_gallery_all_videos);
    }
}
