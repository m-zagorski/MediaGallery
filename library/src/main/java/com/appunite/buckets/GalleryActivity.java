package com.appunite.buckets;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.appunite.GalleryBaseActivity;
import com.appunite.R;
import com.appunite.bucket.GalleryMediaBucketActivity;
import com.appunite.dagger.GalleryActivityModule;
import com.appunite.dagger.GalleryActivitySingleton;
import com.appunite.dagger.GalleryApplicationComponent;
import com.appunite.dagger.GalleryDatabaseModule;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.utils.Consts;
import com.appunite.utils.GalleryCustomFoldersProvider;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import dagger.Provides;
import dagger.Subcomponent;
import rx.Scheduler;

import static com.appunite.utils.Preconditions.checkState;

public class GalleryActivity extends GalleryBaseActivity {

    private static final String EXTRA_GALLERY_ACTIVITY_NAME = "extra_gallery_activity_name";
    private static final String EXTRA_HAS_IMAGES = "extra_has_images";
    private static final String EXTRA_HAS_VIDEOS = "extra_has_videos";
    private static final int GALLERY_REQUEST_CODE = 1;

    @Inject
    PagerAdapter adapter;

    private Toolbar toolbar;
    private View transitionView;

    @Nonnull
    public static Intent newIntent(@Nonnull final Context context,
                                   @Nonnull final String galleryName,
                                   final boolean images,
                                   final boolean videos) {
        return new Intent(context, GalleryActivity.class)
                .putExtra(EXTRA_GALLERY_ACTIVITY_NAME, galleryName)
                .putExtra(EXTRA_HAS_IMAGES, images)
                .putExtra(EXTRA_HAS_VIDEOS, videos);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.gallery_tab_layout);
        final ViewPager pager = (ViewPager) findViewById(R.id.gallery_pager);
        toolbar = (Toolbar) findViewById(R.id.gallery_toolbar);
        transitionView = findViewById(R.id.gallery_transition_view);

        final Bundle extras = getIntent().getExtras();
        toolbar.setTitle(extras.getString(EXTRA_GALLERY_ACTIVITY_NAME));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.finishAfterTransition(GalleryActivity.this);
            }
        });

        assert pager != null;
        assert tabLayout != null;

        pager.setAdapter(adapter);
        tabLayout.setupWithViewPager(pager);
    }

    @Nonnull
    static GalleryActivity fromActivity(@Nonnull FragmentActivity activity) {
        return (GalleryActivity) activity;
    }

    void startBucketActivity(@Nonnull String bucketName, boolean videos) {
        final ActivityOptionsCompat options = sharedElementActivityOptions();
        ActivityCompat.startActivityForResult(this, GalleryMediaBucketActivity.newIntent(this, bucketName, videos), GALLERY_REQUEST_CODE, options.toBundle());
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    private ActivityOptionsCompat sharedElementActivityOptions() {
        final Pair<View, String> backgroundPair = Pair.create(
                transitionView,
                getString(R.string.com_appunite_gallery_bucket_view_transition)
        );
        final Pair<View, String> toolbarPair = Pair.create(
                (View) toolbar,
                getString(R.string.com_appunite_gallery_bucket_toolbar_transition)
        );
        return ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, backgroundPair, toolbarPair);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            final ArrayList<String> selectedData = data.getStringArrayListExtra(Consts.EXTRA_SELECTED_MEDIA);
            setResult(Activity.RESULT_OK, createIntentWithData(selectedData));
            finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Nonnull
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private Intent createIntentWithData(@Nonnull ArrayList<String> selectedData) {
        checkState(!selectedData.isEmpty(), "Selected data might not be empty");

        final Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (selectedData.size() == 1) {
                intent.setData(Uri.parse(selectedData.get(0)));
            } else {
                intent.setClipData(createClipData(selectedData));
            }
        } else {
            intent.putStringArrayListExtra(Consts.EXTRA_SELECTED_MEDIA, selectedData);
        }
        return intent;
    }

    @Nonnull
    private ClipData createClipData(@Nonnull List<String> selectedData) {
        checkState(selectedData.size() > 1, "Selected data should contain more than 1 elements");
        final ClipDescription clipDescription = new ClipDescription(getString(R.string.com_appunite_gallery_clip_data_description), new String[]{ClipDescription.MIMETYPE_TEXT_URILIST});
        final ClipData.Item item = new ClipData.Item(Uri.parse(selectedData.get(0)));
        final ClipData clipData = new ClipData(clipDescription, item);

        for (int i = 1; i < selectedData.size(); i++) {
            clipData.addItem(new ClipData.Item(Uri.parse(selectedData.get(i))));
        }

        return clipData;
    }

    private Component component;

    @Nonnull
    @Override
    public BaseActivityComponent inject(@Nonnull GalleryApplicationComponent applicationComponent, @Nonnull GalleryActivityModule galleryActivityModule) {
        final Bundle extras = getIntent().getExtras();
        final boolean images = extras.getBoolean(EXTRA_HAS_IMAGES);
        final boolean videos = extras.getBoolean(EXTRA_HAS_VIDEOS);

        if (!images && !videos) {
            throw new RuntimeException("You have to specify at least one category images or video");
        }

        component = applicationComponent.plus(new GalleryActivityModule(this),
                new Module(getSupportFragmentManager(), images, videos));
        component.inject(this);
        return component;
    }

    @GalleryActivitySingleton
    @Subcomponent(
            modules = {
                    GalleryActivityModule.class,
//                    GalleryAndroidImplModule.class,
                    GalleryDatabaseModule.class,
                    Module.class
            }
    )
    public interface Component extends BaseActivityComponent {

        void inject(@Nonnull GalleryActivity galleryActivity);

        @UiScheduler
        Scheduler uiScheduler();

        @NetworkScheduler
        Scheduler networkScheduler();

        GalleryCustomFoldersProvider galleryCustomFoldersProvider();
    }

    @Nonnull
    public static Component component(@Nonnull FragmentActivity activity) {
        return ((GalleryActivity) activity).component;
    }

    @dagger.Module
    public class Module {

        @Nonnull
        private final FragmentManager fragmentManager;
        private final boolean images;
        private final boolean videos;

        Module(@Nonnull FragmentManager fragmentManager,
               boolean images, boolean videos) {
            this.fragmentManager = fragmentManager;
            this.images = images;
            this.videos = videos;
        }

        @Provides
        @Nonnull
        public FragmentManager fragmentManager() {
            return fragmentManager;
        }

        @Provides
        @Named("images")
        public boolean images() {
            return images;
        }

        @Provides
        @Named("videos")
        public boolean videos() {
            return videos;
        }

    }

}
