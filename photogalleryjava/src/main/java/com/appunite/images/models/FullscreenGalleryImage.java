package com.appunite.images.models;

import com.appunite.rx.internal.Objects;

import java.util.Set;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import rx.subscriptions.SerialSubscription;

public class FullscreenGalleryImage {
    @Nonnull
    private final GalleryImage galleryImage;
    @Nonnull
    private final Observable<Boolean> selectedObservable;
    @Nonnull
    private final Observer<String> selectObserver;
    @Nonnull
    private final SerialSubscription subscription;
    private final boolean triggerTransitions;

    private FullscreenGalleryImage(@Nonnull GalleryImage galleryImage,
                                   @Nonnull Observable<Boolean> selectedObservable,
                                   @Nonnull Observer<String> selectObserver,
                                   @Nonnull SerialSubscription subscription,
                                   boolean triggerTransitions) {
        this.galleryImage = galleryImage;
        this.selectedObservable = selectedObservable;
        this.selectObserver = selectObserver;
        this.subscription = subscription;
        this.triggerTransitions = triggerTransitions;
    }

    @Nonnull
    public static FullscreenGalleryImage create(@Nonnull final GalleryImage galleryImage,
                                                @Nonnull Observable<Set<String>> selectedObservable,
                                                @Nonnull Observer<String> selectObserver,
                                                boolean triggerTransitions) {
        final Observable<Boolean> currentlySelectedObservable = selectedObservable
                .map(new Func1<Set<String>, Boolean>() {
                    @Override
                    public Boolean call(Set<String> strings) {
                        return strings.contains(galleryImage.data());
                    }
                })
                .distinctUntilChanged();

        return new FullscreenGalleryImage(galleryImage, currentlySelectedObservable, selectObserver, new SerialSubscription(), triggerTransitions);
    }

    @Nonnull
    public GalleryImage galleryImage() {
        return galleryImage;
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
    public SerialSubscription subscription() {
        return subscription;
    }

    public boolean triggerTransitions() {
        return triggerTransitions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FullscreenGalleryImage)) return false;
        FullscreenGalleryImage that = (FullscreenGalleryImage) o;
        return triggerTransitions == that.triggerTransitions &&
                Objects.equal(galleryImage, that.galleryImage) &&
                Objects.equal(selectedObservable, that.selectedObservable) &&
                Objects.equal(selectObserver, that.selectObserver) &&
                Objects.equal(subscription, that.subscription);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(galleryImage, selectedObservable, selectObserver, subscription, triggerTransitions);
    }
}
