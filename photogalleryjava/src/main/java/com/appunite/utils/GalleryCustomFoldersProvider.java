package com.appunite.utils;


import javax.annotation.Nonnull;

public interface GalleryCustomFoldersProvider {

    @Nonnull
    String allImagesFolderName();

    @Nonnull
    String allVideosFolderName();

}
