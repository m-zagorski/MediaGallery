package com.appunite.images.models;

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;

@AutoValue
public abstract class GalleryImage {

    public abstract long id();

    @Nonnull
    public abstract String data();

    @Nonnull
    public static GalleryImage create(long id,
                                      @Nonnull String data) {
        return new AutoValue_GalleryImage(id, data);
    }

}
