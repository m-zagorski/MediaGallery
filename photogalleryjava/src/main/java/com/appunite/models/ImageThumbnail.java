package com.appunite.models;


import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;

@AutoValue
public abstract class ImageThumbnail implements Thumbnail {

    @Nonnull
    public abstract String thumbnailUrl();

    @Nonnull
    public static ImageThumbnail create(@Nonnull String thumbnailUrl) {
        return new AutoValue_ImageThumbnail(thumbnailUrl);
    }


}
