package com.appunite.videos.models;

import com.appunite.rx.NonJdkKeeper;
import com.google.auto.value.AutoValue;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import rx.subscriptions.SerialSubscription;

@AutoValue
public abstract class FullscreenGalleryVideo {

    @Nonnull
    public abstract GalleryVideo galleryVideo();

    @Nullable
    public abstract NonJdkKeeper thumbnailKeeper();

    @Nonnull
    public abstract Observable<Boolean> selectedObservable();

    @Nonnull
    public abstract Observer<String> selectObserver();

    @Nonnull
    public abstract Observer<String> playVideoObserver();

    @Nonnull
    public abstract SerialSubscription subscription();

    public abstract boolean triggerTransitions();

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

        return new AutoValue_FullscreenGalleryVideo(galleryVideo, thumbnailKeeper, currentlySelectedObservable, selectObserver, playVideoObserver, new SerialSubscription(), triggerTransitions);
    }

}
