package com.appunite.models;


import com.appunite.rx.NonJdkKeeper;
import com.appunite.rx.internal.Objects;

import java.util.Set;

import javax.annotation.Nonnull;

public class FullscreenData {

    @Nonnull
    private final String bucketName;
    @Nonnull
    private final String currentElement;
    @Nonnull
    private final Set<String> currentlySelected;
    @Nonnull
    private final NonJdkKeeper viewKeeper;

    private FullscreenData(@Nonnull String bucketName,
                           @Nonnull String currentElement,
                           @Nonnull Set<String> currentlySelected,
                           @Nonnull NonJdkKeeper viewKeeper) {
        this.bucketName = bucketName;
        this.currentElement = currentElement;
        this.currentlySelected = currentlySelected;
        this.viewKeeper = viewKeeper;
    }

    @Nonnull
    public static FullscreenData create(@Nonnull String bucketName,
                                        @Nonnull String currentElement,
                                        @Nonnull Set<String> currentlySelected,
                                        @Nonnull NonJdkKeeper viewKeeper) {
        return new FullscreenData(bucketName, currentElement, currentlySelected, viewKeeper);
    }

    @Nonnull
    public String bucketName() {
        return bucketName;
    }

    @Nonnull
    public String currentElement() {
        return currentElement;
    }

    @Nonnull
    public Set<String> currentlySelected() {
        return currentlySelected;
    }

    @Nonnull
    public NonJdkKeeper viewKeeper() {
        return viewKeeper;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FullscreenData)) return false;
        FullscreenData that = (FullscreenData) o;
        return Objects.equal(bucketName, that.bucketName) &&
                Objects.equal(currentElement, that.currentElement) &&
                Objects.equal(currentlySelected, that.currentlySelected) &&
                Objects.equal(viewKeeper, that.viewKeeper);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(bucketName, currentElement, currentlySelected, viewKeeper);
    }
}
