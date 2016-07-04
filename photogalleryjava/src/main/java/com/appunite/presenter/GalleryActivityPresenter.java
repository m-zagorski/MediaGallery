package com.appunite.presenter;


import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

public class GalleryActivityPresenter {

    @Nonnull
    private final String title;
    private final boolean images;
    private final boolean videos;

    @Inject
    public GalleryActivityPresenter(@Nonnull @Named("title") String title,
                                    @Named("images") boolean images,
                                    @Named("videos") boolean videos) {
        this.title = title;
        this.images = images;
        this.videos = videos;
    }

    @Nonnull
    public String title() {
        return title;
    }

    @Nonnull
    public boolean tabsVisibility() {
        return images && videos;
    }
}
