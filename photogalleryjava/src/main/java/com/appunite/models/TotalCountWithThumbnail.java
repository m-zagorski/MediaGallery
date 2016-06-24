package com.appunite.models;


import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@AutoValue
public abstract class TotalCountWithThumbnail {
    @Nullable
    public abstract Thumbnail thumbnail();

    public abstract int count();

    @Nonnull
    public static TotalCountWithThumbnail create(@Nullable Thumbnail thumbnail,
                                                 int count) {
        return new AutoValue_TotalCountWithThumbnail(thumbnail, count);
    }
}
