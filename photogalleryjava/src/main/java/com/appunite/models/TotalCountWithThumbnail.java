package com.appunite.models;


import com.appunite.rx.internal.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TotalCountWithThumbnail {

    @Nullable
    private final Thumbnail thumbnail;
    private final int count;

    private TotalCountWithThumbnail(@Nullable Thumbnail thumbnail, int count) {
        this.thumbnail = thumbnail;
        this.count = count;
    }

    @Nonnull
    public static TotalCountWithThumbnail create(@Nullable Thumbnail thumbnail,
                                                 int count) {
        return new TotalCountWithThumbnail(thumbnail, count);
    }

    @Nullable
    public Thumbnail thumbnail() {
        return thumbnail;
    }

    public int count() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TotalCountWithThumbnail)) return false;
        final TotalCountWithThumbnail that = (TotalCountWithThumbnail) o;
        return count == that.count &&
                Objects.equal(thumbnail, that.thumbnail);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(thumbnail, count);
    }
}
