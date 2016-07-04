package com.appunite.images.models;

import com.appunite.rx.internal.Objects;

import javax.annotation.Nonnull;

public class GalleryImage {

    private final long id;
    @Nonnull
    private final String data;

    private GalleryImage(long id, @Nonnull String data) {
        this.id = id;
        this.data = data;
    }

    @Nonnull
    public static GalleryImage create(long id,
                                      @Nonnull String data) {
        return new GalleryImage(id, data);
    }

    public long id() {
        return id;
    }

    @Nonnull
    public String data() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GalleryImage)) return false;
        final GalleryImage that = (GalleryImage) o;
        return id == that.id &&
                Objects.equal(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, data);
    }
}
