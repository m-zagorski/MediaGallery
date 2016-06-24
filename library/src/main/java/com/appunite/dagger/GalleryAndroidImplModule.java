package com.appunite.dagger;

import com.appunite.utils.GalleryCustomFoldersProvider;
import com.appunite.utils.GalleryCustomFoldersProviderImpl;
import com.appunite.utils.ThumbnailProvider;
import com.appunite.utils.ThumbnailProviderImpl;

import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;

@Module
public class GalleryAndroidImplModule {

    @Nonnull
    @Provides
    public GalleryCustomFoldersProvider provideGalleryCustomFoldersProvider(@Nonnull GalleryCustomFoldersProviderImpl impl) {
        return impl;
    }

    @Nonnull
    @Provides
    ThumbnailProvider thumbnailProvider(@Nonnull ThumbnailProviderImpl impl) {
        return impl;
    }
}

