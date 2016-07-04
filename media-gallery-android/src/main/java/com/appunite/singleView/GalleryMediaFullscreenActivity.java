package com.appunite.singleView;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

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

import java.util.ArrayList;
import java.util.Set;

import javax.annotation.Nonnull;

import dagger.Subcomponent;
import rx.Scheduler;

import static com.appunite.utils.Preconditions.checkNotNull;

public class GalleryMediaFullscreenActivity extends GalleryBaseActivity {

    private static final String EXTRA_BUCKET_NAME = "extra_bucket_name";
    private static final String EXTRA_CURRENT_ELEMENT = "extra_current_element";
    private static final String EXTRA_CURRENTLY_SELECTED = "extra_currently_selected";
    private static final String EXTRA_ARE_VIDEOS = "extra_are_videos";
    private static final String TAG_FRAGMENT = "tag_fragment";

    private FragmentWithBackButtonBehavior fragment;

    @Nonnull
    public static Intent newIntent(@Nonnull final Context context,
                                   @Nonnull final String bucketName,
                                   @Nonnull final String currentElement,
                                   @Nonnull final Set<String> currentlySelected,
                                   final boolean videos) {
        return new Intent(context, GalleryMediaFullscreenActivity.class)
                .putExtra(EXTRA_BUCKET_NAME, bucketName)
                .putExtra(EXTRA_CURRENT_ELEMENT, currentElement)
                .putStringArrayListExtra(EXTRA_CURRENTLY_SELECTED, new ArrayList<>(currentlySelected))
                .putExtra(EXTRA_ARE_VIDEOS, videos);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frame_layout);

        if (savedInstanceState == null) {
            final Bundle extras = getIntent().getExtras();
            final String bucketName = checkNotNull(extras.getString(EXTRA_BUCKET_NAME));
            final String currentElement = checkNotNull(extras.getString(EXTRA_CURRENT_ELEMENT));
            final ArrayList<String> currentlySelected = checkNotNull(extras.getStringArrayList(EXTRA_CURRENTLY_SELECTED));
            final boolean videos = extras.getBoolean(EXTRA_ARE_VIDEOS);

            fragment = videos
                    ? GalleryVideosFullscreenFragment.newInstance(bucketName, currentElement, currentlySelected)
                    : GalleryImagesFullscreenFragment.newInstance(bucketName, currentElement, currentlySelected);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment, TAG_FRAGMENT)
                    .commit();
        } else {
            fragment = (FragmentWithBackButtonBehavior) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
        }
    }

    @Override
    public void onBackPressed() {
        if (!fragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    private Component component;

    @Nonnull
    @Override
    public BaseActivityComponent inject(@Nonnull GalleryApplicationComponent applicationComponent, @Nonnull GalleryActivityModule galleryActivityModule) {
        component = applicationComponent.plusFullscreenActivity(new GalleryActivityModule(this));
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

        void inject(@Nonnull GalleryMediaFullscreenActivity galleryMediaBucketActivity);

        @UiScheduler
        Scheduler uiScheduler();

        @NetworkScheduler
        Scheduler networkScheduler();

        GalleryCustomFoldersProvider galleryCustomFoldersProvider();
    }

    @Nonnull
    public static Component component(@Nonnull FragmentActivity activity) {
        return ((GalleryMediaFullscreenActivity) activity).component;
    }
}
