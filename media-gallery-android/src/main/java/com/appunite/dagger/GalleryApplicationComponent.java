package com.appunite.dagger;


import com.appunite.bucket.GalleryMediaBucketActivity;
import com.appunite.buckets.GalleryActivity;
import com.appunite.singleView.GalleryMediaFullscreenActivity;

public interface GalleryApplicationComponent extends GalleryDaoComponent {

    GalleryActivity.Component plus(GalleryActivityModule module, GalleryActivity.Module activityModule);

    GalleryMediaBucketActivity.Component plus(GalleryActivityModule module);

    GalleryMediaFullscreenActivity.Component plusFullscreenActivity(GalleryActivityModule module);

}
