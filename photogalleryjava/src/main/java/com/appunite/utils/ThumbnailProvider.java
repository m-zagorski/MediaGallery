package com.appunite.utils;

import com.appunite.rx.NonJdkKeeper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ThumbnailProvider {

    @Nullable
    NonJdkKeeper provideFullscreenThumbnailForVideo(@Nonnull String data);
}
