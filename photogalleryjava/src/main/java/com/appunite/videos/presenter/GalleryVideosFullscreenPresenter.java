package com.appunite.videos.presenter;


import com.appunite.presenter.GalleryBasePresenter;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.BothParams;
import com.appunite.rx.functions.Functions2;
import com.appunite.rx.internal.Objects;
import com.appunite.videos.dao.GalleryVideosDao;
import com.appunite.videos.models.FullscreenGalleryVideo;
import com.appunite.videos.models.GalleryVideo;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class GalleryVideosFullscreenPresenter {

    @Nonnull
    private final Observable<List<FullscreenGalleryVideo>> dataObservable;
    @Nonnull
    private final Observable<Integer> currentPositionObservable;
    @Nonnull
    private final GalleryVideosDao.VideoGalleryForBucketDao bucketDao;
    @Nonnull
    private final GalleryBasePresenter basePresenter;
    @Nonnull
    private final Scheduler uiScheduler;
    @Nonnull
    private final PublishSubject<Integer> currentPositionSubject = PublishSubject.create();

    @Inject
    public GalleryVideosFullscreenPresenter(@Nonnull final GalleryBasePresenter basePresenter,
                                            @Nonnull @Named("currentElement") final String currentElement,
                                            @Nonnull final Set<String> currentlySelected,
                                            @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                                            @Nonnull @UiScheduler final Scheduler uiScheduler,
                                            @Nonnull final GalleryVideosDao dao) {
        this.basePresenter = basePresenter;
        this.uiScheduler = uiScheduler;
        bucketDao = dao.forBucket(basePresenter.bucketName());
        basePresenter.multiSelectionObserver().onNext(currentlySelected);

        dataObservable = bucketDao.videosObservable()
                .switchMap(new Func1<List<GalleryVideo>, Observable<List<FullscreenGalleryVideo>>>() {
                    @Override
                    public Observable<List<FullscreenGalleryVideo>> call(List<GalleryVideo> galleryVideos) {
                        return Observable.from(galleryVideos)
                                .map(new Func1<GalleryVideo, FullscreenGalleryVideo>() {
                                    @Override
                                    public FullscreenGalleryVideo call(GalleryVideo galleryVideo) {
                                        return FullscreenGalleryVideo.create(
                                                galleryVideo,
                                                galleryVideo.fullThumbnailKeeper(),
                                                basePresenter.selectedObservable(),
                                                basePresenter.singleSelectionObserver(),
                                                basePresenter.clickObserver(),
                                                Objects.equal(galleryVideo.data(), currentElement)
                                        );
                                    }
                                })
                                .toList()
                                .subscribeOn(networkScheduler);
                    }
                })
                .observeOn(uiScheduler);

        currentPositionObservable = bucketDao.videosObservable()
                .switchMap(new Func1<List<GalleryVideo>, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(List<GalleryVideo> galleryVideos) {
                        for (int i = 0, galleryImagesSize = galleryVideos.size(); i < galleryImagesSize; i++) {
                            final GalleryVideo video = galleryVideos.get(i);
                            if (Objects.equal(video.data(), currentElement)) {
                                return Observable.just(i);
                            }
                        }
                        return Observable.just(-1);
                    }
                })
                .observeOn(uiScheduler);
    }

    @Nonnull
    public Observable<List<FullscreenGalleryVideo>> dataObservable() {
        return dataObservable;
    }

    @Nonnull
    public Observable<Integer> currentPositionObservable() {
        return Observable.combineLatest(dataObservable, currentPositionObservable, new Func2<List<FullscreenGalleryVideo>, Integer, Integer>() {
            @Override
            public Integer call(List<FullscreenGalleryVideo> fullscreenGalleryVideos, Integer integer) {
                return integer;
            }
        });
    }

    @Nonnull
    public Observable<Integer> selectedCountObservable() {
        return basePresenter.selectedCountObservable();
    }

    @Nonnull
    public Observable<Boolean> sendStateObservable() {
        return basePresenter.sendStateObservable();
    }

    @Nonnull
    public Observable<Set<String>> closeActivityObservable() {
        return basePresenter.closeActivityObservable()
                .withLatestFrom(basePresenter.selectedObservable(), Functions2.<Set<String>>secondParam());
    }

    @Nonnull
    public Observable<Set<String>> sendSelectedObservable() {
        return basePresenter.sendSelectedObservable();
    }

    @Nonnull
    public Observable<BothParams<Integer, Integer>> titleObservable() {
        return Observable
                .combineLatest(
                        Observable.concat(currentPositionObservable.first(), currentPositionSubject),
                        bucketDao.videosObservable(),
                        new Func2<Integer, List<GalleryVideo>, BothParams<Integer, Integer>>() {
                            @Override
                            public BothParams<Integer, Integer> call(Integer integer, List<GalleryVideo> galleryVideos) {
                                return BothParams.of(integer, galleryVideos.size());
                            }
                        })
                .observeOn(uiScheduler);
    }

    @Nonnull
    public Observable<String> playVideoObservable() {
        return basePresenter.clickObservable();
    }

    @Nonnull
    public Observer<Object> cancelClickObserver() {
        return basePresenter.cancelClickObserver();
    }

    @Nonnull
    public Observer<Object> onBackClickObserver() {
        return basePresenter.onBackClickObserver();
    }

    @Nonnull
    public Observer<Object> sendClickObserver() {
        return basePresenter.sendClickObserver();
    }

    @Nonnull
    public Observable<Set<String>> selectedObservable() {
        return basePresenter.selectedObservable();
    }

    @Nonnull
    public Observer<Set<String>> multipleSelectionObserver() {
        return basePresenter.multiSelectionObserver();
    }

    @Nonnull
    public Observer<Integer> currentPositionObserver() {
        return currentPositionSubject;
    }
}
