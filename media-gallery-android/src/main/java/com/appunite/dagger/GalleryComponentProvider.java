package com.appunite.dagger;


import javax.annotation.Nonnull;

public interface GalleryComponentProvider {

    @Nonnull
    GalleryApplicationComponent provideApplicationComponent();

}
