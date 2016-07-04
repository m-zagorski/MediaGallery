package com.appunite.models;

import com.appunite.rx.internal.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GalleryBucket {

    private final long id;
    @Nullable
    private final String name;
    @Nullable
    private final Thumbnail thumbnail;
    private final int count;

    private GalleryBucket(long id,
                          @Nullable String name,
                          @Nullable Thumbnail thumbnail, int count) {
        this.id = id;
        this.name = name;
        this.thumbnail = thumbnail;
        this.count = count;
    }

    @Nonnull
    public static GalleryBucket create(long id,
                                       @Nullable String name,
                                       @Nullable Thumbnail thumbnail,
                                       int count) {
        return new GalleryBucket(id, name, thumbnail, count);
    }

    public long id() {
        return id;
    }

    @Nullable
    public String name() {
        return name;
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
        if (!(o instanceof GalleryBucket)) return false;
        final GalleryBucket that = (GalleryBucket) o;
        return id == that.id &&
                count == that.count &&
                Objects.equal(name, that.name) &&
                Objects.equal(thumbnail, that.thumbnail);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name, thumbnail, count);
    }
}
