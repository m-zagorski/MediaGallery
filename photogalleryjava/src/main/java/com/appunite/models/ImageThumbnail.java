package com.appunite.models;


import com.appunite.rx.internal.Objects;

import javax.annotation.Nonnull;

public class ImageThumbnail implements Thumbnail {

    @Nonnull
    private final String thumbnailurl;

    private ImageThumbnail(@Nonnull String thumbnailurl) {
        this.thumbnailurl = thumbnailurl;
    }

    @Nonnull
    public static ImageThumbnail create(@Nonnull String thumbnailUrl) {
        return new ImageThumbnail(thumbnailUrl);
    }

    @Nonnull
    public String thumbnailUrl() {
        return thumbnailurl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageThumbnail)) return false;
        final ImageThumbnail that = (ImageThumbnail) o;
        return Objects.equal(thumbnailurl, that.thumbnailurl);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(thumbnailurl);
    }
}
