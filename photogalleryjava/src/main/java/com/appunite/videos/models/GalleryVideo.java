package com.appunite.videos.models;

import com.appunite.rx.NonJdkKeeper;
import com.appunite.rx.internal.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GalleryVideo {

    private final long id;
    @Nonnull
    private final String data;
    @Nullable
    private final NonJdkKeeper fullThumbnailKeeper;

    private GalleryVideo(long id, @Nonnull String data, @Nullable NonJdkKeeper fullThumbnailKeeper) {
        this.id = id;
        this.data = data;
        this.fullThumbnailKeeper = fullThumbnailKeeper;
    }

    @Nonnull
    public static GalleryVideo create(long id,
                                      @Nonnull String data,
                                      @Nullable NonJdkKeeper fullThumbnailKeeper) {
        return new GalleryVideo(id, data, fullThumbnailKeeper);
    }

    public long id() {
        return id;
    }

    @Nonnull
    public String data() {
        return data;
    }

    @Nullable
    public NonJdkKeeper fullThumbnailKeeper() {
        return fullThumbnailKeeper;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GalleryVideo)) return false;
        GalleryVideo that = (GalleryVideo) o;
        return id == that.id &&
                Objects.equal(data, that.data) &&
                Objects.equal(fullThumbnailKeeper, that.fullThumbnailKeeper);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, data, fullThumbnailKeeper);
    }
}
