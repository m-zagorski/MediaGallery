package com.appunite.videos.models;

import com.appunite.rx.NonJdkKeeper;
import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@AutoValue
public abstract class GalleryVideo {

    public abstract long id();

    @Nonnull
    public abstract String data();

    @Nullable
    public abstract NonJdkKeeper fullThumbnailKeeper();

    @Nonnull
    public static GalleryVideo create(long id,
                                      @Nonnull String data,
                                      @Nullable NonJdkKeeper fullThumbnailKeeper) {
        return new AutoValue_GalleryVideo(id, data, fullThumbnailKeeper);
    }

}
