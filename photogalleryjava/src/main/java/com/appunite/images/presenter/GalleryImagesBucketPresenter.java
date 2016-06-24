package com.appunite.images.presenter;


import com.appunite.images.dao.GalleryImagesDao;
import com.appunite.images.models.GalleryImage;
import com.appunite.models.FullscreenData;
import com.appunite.models.MediaItem;
import com.appunite.presenter.GalleryBasePresenter;
import com.appunite.rx.NonJdkKeeper;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.BothParams;
import com.appunite.rx.functions.Functions2;
import com.appunite.rx.internal.Objects;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observers.Observers;

public class GalleryImagesBucketPresenter {

    @Nonnull
    private final GalleryBasePresenter basePresenter;
    @Nonnull
    private final Observable<List<BaseAdapterItem>> dataObservable;

    @Inject
    public GalleryImagesBucketPresenter(@Nonnull final GalleryBasePresenter basePresenter,
                                        @Nonnull final GalleryImagesDao dao,
                                        @Nonnull @UiScheduler final Scheduler uiScheduler) {
        this.basePresenter = basePresenter;
        final GalleryImagesDao.PhotoGalleryForBucketDao bucketDao = dao.forBucket(basePresenter.bucketName());

        dataObservable = bucketDao.imagesObservable()
                .switchMap(new Func1<List<GalleryImage>, Observable<List<BaseAdapterItem>>>() {
                    @Override
                    public Observable<List<BaseAdapterItem>> call(List<GalleryImage> galleryImages) {
                        return Observable.from(galleryImages)
                                .map(new Func1<GalleryImage, BaseAdapterItem>() {
                                    @Override
                                    public BaseAdapterItem call(GalleryImage galleryImage) {
                                        return new GalleryImageItem(
                                                galleryImage,
                                                basePresenter.selectedObservable(),
                                                basePresenter.singleSelectionObserver(),
                                                basePresenter.clickWithViewObserver()
                                        );
                                    }
                                })
                                .toList();
                    }
                })
                .observeOn(uiScheduler);
    }

    @Nonnull
    public String toolbarTitle() {
        return basePresenter.bucketName();
    }

    @Nonnull
    public Observable<FullscreenData> openFullscreenObservable() {
        return basePresenter.clickWithViewObservable()
                .withLatestFrom(basePresenter.selectedObservable(), Functions2.<BothParams<String, NonJdkKeeper>, Set<String>>bothParams())
                .map(new Func1<BothParams<BothParams<String, NonJdkKeeper>, Set<String>>, FullscreenData>() {
                    @Override
                    public FullscreenData call(BothParams<BothParams<String, NonJdkKeeper>, Set<String>> bothParams) {
                        return FullscreenData.create(
                                basePresenter.bucketName(),
                                bothParams.param1().param1(),
                                bothParams.param2(),
                                bothParams.param1().param2());
                    }
                });
    }

    @Nonnull
    public Observable<List<BaseAdapterItem>> dataObservable() {
        return dataObservable;
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
    public Observable<Set<String>> selectedObservable() {
        return basePresenter.selectedObservable();
    }

    @Nonnull
    public Observable<Set<String>> sendSelectedObservable() {
        return basePresenter.sendSelectedObservable();
    }

    @Nonnull
    public Observer<Set<String>> multipleSelectionObserver() {
        return basePresenter.multiSelectionObserver();
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

    private static final class GalleryImageItem implements MediaItem {

        @Nonnull
        private final GalleryImage galleryImage;
        @Nonnull
        private final Observer<String> selectImageObserver;
        @Nonnull
        private final Observer<BothParams<String, NonJdkKeeper>> clickObserver;
        @Nonnull
        private final Observable<Boolean> selectedObservable;

        GalleryImageItem(@Nonnull final GalleryImage galleryImage,
                         @Nonnull final Observable<Set<String>> selectedImagesObservable,
                         @Nonnull final Observer<String> selectImageObserver,
                         @Nonnull final Observer<BothParams<String, NonJdkKeeper>> clickObserver) {
            this.galleryImage = galleryImage;
            this.selectImageObserver = selectImageObserver;
            this.clickObserver = clickObserver;
            this.selectedObservable = selectedImagesObservable
                    .map(new Func1<Set<String>, Boolean>() {
                        @Override
                        public Boolean call(Set<String> strings) {
                            return strings.contains(galleryImage.data());
                        }
                    })
                    .distinctUntilChanged();
        }

        @Override
        @Nullable
        public NonJdkKeeper thumbnailKeeper() {
            return null;
        }

        @Nonnull
        @Override
        public Observable<Boolean> selectedObservable() {
            return selectedObservable;
        }

        @Nonnull
        @Override
        public Observer<Void> selectObserver() {
            return Observers.create(new Action1<Void>() {
                @Override
                public void call(Void aVoid) {
                    selectImageObserver.onNext(galleryImage.data());
                }
            });
        }

        @Nonnull
        @Override
        public Observer<NonJdkKeeper> clickObserver() {
            return Observers.create(new Action1<NonJdkKeeper>() {
                @Override
                public void call(NonJdkKeeper keeper) {
                    clickObserver.onNext(BothParams.of(galleryImage.data(), keeper));
                }
            });
        }

        @Nonnull
        @Override
        public String data() {
            return galleryImage.data();
        }

        @Override
        public long adapterId() {
            return galleryImage.id();
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem instanceof GalleryImageItem
                    && Objects.equal(galleryImage.id(), ((GalleryImageItem) baseAdapterItem).galleryImage.id());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem baseAdapterItem) {
            return equals(baseAdapterItem);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GalleryImageItem)) return false;
            final GalleryImageItem that = (GalleryImageItem) o;
            return Objects.equal(galleryImage, that.galleryImage) &&
                    Objects.equal(selectedObservable, that.selectedObservable);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(galleryImage, selectedObservable);
        }
    }
}
