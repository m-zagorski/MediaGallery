package com.appunite.models;

import com.appunite.rx.NonJdkKeeper;
import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@AutoValue
public abstract class VideoThumbnail implements Thumbnail {

    @Nullable
    public abstract NonJdkKeeper thumbnailKeeper();

    @Nonnull
    public static VideoThumbnail create(@Nullable NonJdkKeeper viewKeeper) {
        return new AutoValue_VideoThumbnail(viewKeeper);
    }

}
