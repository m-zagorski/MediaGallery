package com.appunite.bucket;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.appunite.BaseFragmentWithActivityResult;
import com.appunite.GalleryBaseActivity;
import com.appunite.R;
import com.appunite.buckets.GalleryActivity;
import com.appunite.dagger.GalleryActivityModule;
import com.appunite.dagger.GalleryActivitySingleton;
import com.appunite.dagger.GalleryApplicationComponent;
import com.appunite.dagger.GalleryDatabaseModule;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.utils.GalleryCustomFoldersProvider;
import com.appunite.utils.ThumbnailProvider;

import javax.annotation.Nonnull;

import dagger.Subcomponent;
import rx.Scheduler;

import static com.appunite.utils.Preconditions.checkNotNull;

public class GalleryMediaBucketActivity extends GalleryBaseActivity {

    private static final String EXTRA_BUCKET_NAME = "extra_bucket_name";
    private static final String EXTRA_ARE_VIDEOS = "extra_are_videos";
    private static final String TAG_FRAGMENT = "tag_fragment";

    private BaseFragmentWithActivityResult fragment;

    @Nonnull
    public static Intent newIntent(@Nonnull final Context context,
                                   @Nonnull final String bucketName,
                                   final boolean videos) {
        return new Intent(context, GalleryMediaBucketActivity.class)
                .putExtra(EXTRA_BUCKET_NAME, bucketName)
                .putExtra(EXTRA_ARE_VIDEOS, videos);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frame_layout);

        if (savedInstanceState == null) {
            final Bundle extras = getIntent().getExtras();
            assert extras != null;
            final String bucketName = checkNotNull(extras.getString(EXTRA_BUCKET_NAME));
            final boolean videos = extras.getBoolean(EXTRA_ARE_VIDEOS);

            fragment = videos
                    ? GalleryVideosBucketFragment.newInstance(bucketName)
                    : GalleryImagesBucketFragment.newInstance(bucketName);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment, TAG_FRAGMENT)
                    .commit();
        } else {
            fragment = (BaseFragmentWithActivityResult) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (fragment != null) {
            fragment.onActivityResultFix(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private Component component;

    @Nonnull
    @Override
    public BaseActivityComponent inject(@Nonnull GalleryApplicationComponent applicationComponent, @Nonnull GalleryActivityModule galleryActivityModule) {
        component = applicationComponent.plus(new GalleryActivityModule(this));
        component.inject(this);
        return component;
    }

    @GalleryActivitySingleton
    @Subcomponent(
            modules = {
                    GalleryActivityModule.class,
                    GalleryDatabaseModule.class,
                    GalleryActivity.Module.class
            }
    )
    public interface Component extends BaseActivityComponent {

        void inject(@Nonnull GalleryMediaBucketActivity galleryMediaBucketActivity);

        @UiScheduler
        Scheduler uiScheduler();

        @NetworkScheduler
        Scheduler networkScheduler();

        GalleryCustomFoldersProvider galleryCustomFoldersProvider();

        ThumbnailProvider thumbnailProvider();

    }

    @Nonnull
    public static Component component(@Nonnull FragmentActivity activity) {
        return ((GalleryMediaBucketActivity) activity).component;
    }
}
