package com.appunite.models;

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@AutoValue
public abstract class GalleryBucket {

    public abstract long id();

    @Nullable
    public abstract String name();

    @Nullable
    public abstract Thumbnail thumbnail();

    public abstract int count();

    @Nonnull
    public static GalleryBucket create(long id,
                                       @Nullable String name,
                                       @Nullable Thumbnail thumbnail,
                                       int count) {
        return new AutoValue_GalleryBucket(id, name, thumbnail, count);
    }

}
