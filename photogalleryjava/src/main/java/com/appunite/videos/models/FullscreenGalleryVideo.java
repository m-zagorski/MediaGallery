package com.appunite.videos.models;

import com.appunite.rx.NonJdkKeeper;
import com.appunite.rx.internal.Objects;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import rx.subscriptions.SerialSubscription;

public class FullscreenGalleryVideo {

    @Nonnull
    private final GalleryVideo galleryVideo;
    @Nullable
    private final NonJdkKeeper thumbnailKeeper;
    @Nonnull
    private final Observable<Boolean> selectedObservable;
    @Nonnull
    private final Observer<String> selectObserver;
    @Nonnull
    private final Observer<String> playVideoObserver;
    @Nonnull
    private final SerialSubscription subscription;
    private final boolean triggerTransitions;

    public FullscreenGalleryVideo(@Nonnull GalleryVideo galleryVideo,
                                  @Nullable NonJdkKeeper thumbnailKeeper,
                                  @Nonnull Observable<Boolean> selectedObservable,
                                  @Nonnull Observer<String> selectObserver,
                                  @Nonnull Observer<String> playVideoObserver,
                                  @Nonnull SerialSubscription subscription,
                                  boolean triggerTransitions) {
        this.galleryVideo = galleryVideo;
        this.thumbnailKeeper = thumbnailKeeper;
        this.selectedObservable = selectedObservable;
        this.selectObserver = selectObserver;
        this.playVideoObserver = playVideoObserver;
        this.subscription = subscription;
        this.triggerTransitions = triggerTransitions;
    }

    @Nonnull
    public static FullscreenGalleryVideo create(@Nonnull final GalleryVideo galleryVideo,
                                                @Nullable final NonJdkKeeper thumbnailKeeper,
                                                @Nonnull final Observable<Set<String>> selectedObservable,
                                                @Nonnull final Observer<String> selectObserver,
                                                @Nonnull final Observer<String> playVideoObserver,
                                                boolean triggerTransitions) {
        final Observable<Boolean> currentlySelectedObservable = selectedObservable
                .map(new Func1<Set<String>, Boolean>() {
                    @Override
                    public Boolean call(Set<String> strings) {
                        return strings.contains(galleryVideo.data());
                    }
                })
                .distinctUntilChanged();

        return new FullscreenGalleryVideo(galleryVideo, thumbnailKeeper, currentlySelectedObservable, selectObserver, playVideoObserver, new SerialSubscription(), triggerTransitions);
    }

    @Nonnull
    public GalleryVideo galleryVideo() {
        return galleryVideo;
    }

    @Nullable
    public NonJdkKeeper thumbnailKeeper() {
        return thumbnailKeeper;
    }

    @Nonnull
    public Observable<Boolean> selectedObservable() {
        return selectedObservable;
    }

    @Nonnull
    public Observer<String> selectObserver() {
        return selectObserver;
    }

    @Nonnull
    public Observer<String> playVideoObserver() {
        return playVideoObserver;
    }

    @Nonnull
    public SerialSubscription subscription() {
        return subscription;
    }

    public boolean triggerTransitions() {
        return triggerTransitions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FullscreenGalleryVideo)) return false;
        final FullscreenGalleryVideo that = (FullscreenGalleryVideo) o;
        return triggerTransitions == that.triggerTransitions &&
                Objects.equal(galleryVideo, that.galleryVideo) &&
                Objects.equal(thumbnailKeeper, that.thumbnailKeeper) &&
                Objects.equal(selectedObservable, that.selectedObservable) &&
                Objects.equal(selectObserver, that.selectObserver) &&
                Objects.equal(playVideoObserver, that.playVideoObserver) &&
                Objects.equal(subscription, that.subscription);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(galleryVideo, thumbnailKeeper, selectedObservable, selectObserver, playVideoObserver, subscription, triggerTransitions);
    }
}
