package com.appunite.dagger;


import com.appunite.database.GalleryImagesDatabaseImpl;
import com.appunite.database.GalleryVideosDatabaseImpl;
import com.appunite.images.dao.GalleryImagesDao;
import com.appunite.videos.dao.GalleryVideosDao;

import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;

@Module
public class GalleryDatabaseModule {

    @Nonnull
    @Provides
    GalleryImagesDao.GalleryImagesDatabase provideGalleryImagesDatabase(@Nonnull GalleryImagesDatabaseImpl impl) {
        return impl;
    }

    @Nonnull
    @Provides
    GalleryVideosDao.GalleryVideosDatabase provideGalleryVideosDatabase(@Nonnull GalleryVideosDatabaseImpl impl) {
        return impl;
    }

}
