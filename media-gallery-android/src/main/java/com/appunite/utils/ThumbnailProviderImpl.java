package com.appunite.utils;


import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import com.appunite.rx.NonJdkKeeper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

public class ThumbnailProviderImpl implements ThumbnailProvider {

    @Inject
    public ThumbnailProviderImpl() {
    }

    @Nullable
    @Override
    public NonJdkKeeper provideFullscreenThumbnailForVideo(@Nonnull String data) {
        final Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(data,
                MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
        return thumbnail != null
                ? new NonJdkKeeper(thumbnail)
                : null;
    }
}
