package com.appunite.models;


import com.appunite.rx.NonJdkKeeper;
import com.google.auto.value.AutoValue;

import java.util.Set;

import javax.annotation.Nonnull;

@AutoValue
public abstract class FullscreenData {

    @Nonnull
    public abstract String bucketName();

    @Nonnull
    public abstract String currentElement();

    @Nonnull
    public abstract Set<String> currentlySelected();

    @Nonnull
    public abstract NonJdkKeeper viewKeeper();

    @Nonnull
    public static FullscreenData create(@Nonnull String bucketName,
                                        @Nonnull String currentElement,
                                        @Nonnull Set<String> currentlySelected,
                                        @Nonnull NonJdkKeeper viewKeeper) {
        return new AutoValue_FullscreenData(bucketName, currentElement, currentlySelected, viewKeeper);
    }

}
