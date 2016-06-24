package com.appunite.images.presenter;


import com.appunite.images.dao.GalleryImagesDao;
import com.appunite.images.models.FullscreenGalleryImage;
import com.appunite.images.models.GalleryImage;
import com.appunite.presenter.GalleryBasePresenter;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.BothParams;
import com.appunite.rx.functions.Functions2;
import com.appunite.rx.internal.Objects;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class GalleryImagesFullscreenPresenter {

    @Nonnull
    private final Observable<List<FullscreenGalleryImage>> dataObservable;
    @Nonnull
    private final Observable<Integer> currentPositionObservable;
    @Nonnull
    private final GalleryImagesDao.PhotoGalleryForBucketDao bucketDao;
    @Nonnull
    private final GalleryBasePresenter basePresenter;
    @Nonnull
    private final PublishSubject<Integer> currentPositionSubject = PublishSubject.create();

    @Inject
    public GalleryImagesFullscreenPresenter(@Nonnull final GalleryBasePresenter basePresenter,
                                            @Nonnull @Named("currentElement") final String currentElement,
                                            @Nonnull final Set<String> currentlySelected,
                                            @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                                            @Nonnull @UiScheduler final Scheduler uiScheduler,
                                            @Nonnull final GalleryImagesDao dao) {
        this.basePresenter = basePresenter;
        bucketDao = dao.forBucket(basePresenter.bucketName());
        basePresenter.multiSelectionObserver().onNext(currentlySelected);

        dataObservable = bucketDao.imagesObservable()
                .switchMap(new Func1<List<GalleryImage>, Observable<List<FullscreenGalleryImage>>>() {
                    @Override
                    public Observable<List<FullscreenGalleryImage>> call(List<GalleryImage> galleryImages) {
                        return Observable.from(galleryImages)
                                .map(new Func1<GalleryImage, FullscreenGalleryImage>() {
                                    @Override
                                    public FullscreenGalleryImage call(GalleryImage galleryImage) {
                                        return FullscreenGalleryImage.create(
                                                galleryImage,
                                                basePresenter.selectedObservable(),
                                                basePresenter.singleSelectionObserver(),
                                                Objects.equal(galleryImage.data(), currentElement)
                                        );
                                    }
                                })
                                .toList()
                                .subscribeOn(networkScheduler);
                    }
                })
                .observeOn(uiScheduler);

        currentPositionObservable = bucketDao.imagesObservable()
                .switchMap(new Func1<List<GalleryImage>, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(List<GalleryImage> galleryImages) {
                        for (int i = 0, galleryImagesSize = galleryImages.size(); i < galleryImagesSize; i++) {
                            final GalleryImage image = galleryImages.get(i);
                            if (Objects.equal(image.data(), currentElement)) {
                                return Observable.just(i);
                            }
                        }
                        return Observable.just(-1);
                    }
                })
                .observeOn(uiScheduler);
    }

    @Nonnull
    public Observable<List<FullscreenGalleryImage>> dataObservable() {
        return dataObservable;
    }

    @Nonnull
    public Observable<Integer> currentPositionObservable() {
        return Observable.combineLatest(dataObservable, currentPositionObservable, new Func2<List<FullscreenGalleryImage>, Integer, Integer>() {
            @Override
            public Integer call(List<FullscreenGalleryImage> fullscreenGalleryImages, Integer integer) {
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
                        bucketDao.imagesObservable(),
                        new Func2<Integer, List<GalleryImage>, BothParams<Integer, Integer>>() {
                            @Override
                            public BothParams<Integer, Integer> call(Integer integer, List<GalleryImage> galleryImages) {
                                return BothParams.of(integer, galleryImages.size());
                            }
                        });
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
