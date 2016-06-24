package com.appunite.images.models;

import com.google.auto.value.AutoValue;

import java.util.Set;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import rx.subscriptions.SerialSubscription;

@AutoValue
public abstract class FullscreenGalleryImage {
    @Nonnull
    public abstract GalleryImage galleryImage();

    @Nonnull
    public abstract Observable<Boolean> selectedObservable();

    @Nonnull
    public abstract Observer<String> selectObserver();

    @Nonnull
    public abstract SerialSubscription subscription();

    public abstract boolean triggerTransitions();

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

        return new AutoValue_FullscreenGalleryImage(galleryImage, currentlySelectedObservable, selectObserver, new SerialSubscription(), triggerTransitions);
    }


}
