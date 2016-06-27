package com.appunite.models;

import com.appunite.rx.NonJdkKeeper;
import com.appunite.rx.internal.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VideoThumbnail implements Thumbnail {

    @Nullable
    private final NonJdkKeeper thumbnailKeeper;

    private VideoThumbnail(@Nullable NonJdkKeeper thumbnailKeeper) {
        this.thumbnailKeeper = thumbnailKeeper;
    }

    @Nonnull
    public static VideoThumbnail create(@Nullable NonJdkKeeper viewKeeper) {
        return new VideoThumbnail(viewKeeper);
    }

    @Nullable
    public NonJdkKeeper thumbnailKeeper() {
        return thumbnailKeeper;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VideoThumbnail)) return false;
        final VideoThumbnail that = (VideoThumbnail) o;
        return Objects.equal(thumbnailKeeper, that.thumbnailKeeper);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(thumbnailKeeper);
    }
}
