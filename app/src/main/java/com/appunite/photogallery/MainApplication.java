package com.appunite.photogallery;


import android.content.ContentResolver;
import android.content.res.Resources;
import android.support.multidex.MultiDexApplication;

import com.appunite.dagger.GalleryAndroidImplModule;
import com.appunite.dagger.GalleryApplicationComponent;
import com.appunite.dagger.GalleryComponentProvider;
import com.appunite.dagger.GalleryDatabaseModule;
import com.appunite.images.dao.GalleryImagesDao;
import com.appunite.utils.GalleryCustomFoldersProvider;
import com.appunite.videos.dao.GalleryVideosDao;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

public class MainApplication extends MultiDexApplication implements GalleryComponentProvider {

    private ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        applicationComponent = DaggerMainApplication_ApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        applicationComponent.inject(this);
    }

    @Singleton
    @Component(
            modules = {
                    ApplicationModule.class,
                    SchedulersModule.class,
                    GalleryDatabaseModule.class,
                    GalleryAndroidImplModule.class
            }
    )
    public interface ApplicationComponent extends GalleryApplicationComponent {
        void inject(MainApplication application);

        Resources resources();

        GalleryImagesDao.GalleryImagesDatabase galleryImagesDatabase();

        GalleryVideosDao.GalleryVideosDatabase galleryVideosDatabase();

        ContentResolver contentResolver();

        GalleryCustomFoldersProvider provideGalleryCustomFoldersProvider();
    }

    @Module
    public static class ApplicationModule {

        @Nonnull
        private final MainApplication mainApplication;

        ApplicationModule(@Nonnull MainApplication mainApplication) {
            this.mainApplication = mainApplication;
        }

        @Provides
        ContentResolver contentResolver() {
            return mainApplication.getContentResolver();
        }

        @Provides
        Resources resources() {
            return mainApplication.getResources();
        }
    }

    @Nonnull
    @Override
    public GalleryApplicationComponent provideApplicationComponent() {
        return applicationComponent;
    }
}
