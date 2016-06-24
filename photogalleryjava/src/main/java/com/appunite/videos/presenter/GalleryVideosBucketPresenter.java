package com.appunite.videos.presenter;


import com.appunite.models.FullscreenData;
import com.appunite.models.MediaItem;
import com.appunite.presenter.GalleryBasePresenter;
import com.appunite.rx.NonJdkKeeper;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.BothParams;
import com.appunite.rx.functions.Functions2;
import com.appunite.rx.internal.Objects;
import com.appunite.videos.dao.GalleryVideosDao;
import com.appunite.videos.models.GalleryVideo;

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

public class GalleryVideosBucketPresenter {

    @Nonnull
    private final GalleryBasePresenter basePresenter;
    @Nonnull
    private final Observable<List<BaseAdapterItem>> dataObservable;

    @Inject
    public GalleryVideosBucketPresenter(@Nonnull final GalleryBasePresenter basePresenter,
                                        @Nonnull final GalleryVideosDao dao,
                                        @Nonnull @UiScheduler final Scheduler uiScheduler) {
        this.basePresenter = basePresenter;
        final GalleryVideosDao.VideoGalleryForBucketDao bucketDao = dao.forBucket(basePresenter.bucketName());

        dataObservable = bucketDao.videosObservable()
                .switchMap(new Func1<List<GalleryVideo>, Observable<List<BaseAdapterItem>>>() {
                    @Override
                    public Observable<List<BaseAdapterItem>> call(List<GalleryVideo> galleryVideos) {
                        return Observable.from(galleryVideos)
                                .map(new Func1<GalleryVideo, BaseAdapterItem>() {
                                    @Override
                                    public BaseAdapterItem call(GalleryVideo galleryVideo) {
                                        return new GalleryVideoItem(
                                                galleryVideo,
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
    public Observable<List<BaseAdapterItem>> dataObservable() {
        return dataObservable;
    }

    @Nonnull
    public Observable<Integer> selectedCountObservable() {
        return basePresenter.selectedCountObservable();
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
    public Observable<Boolean> sendStateObservable() {
        return basePresenter.sendStateObservable();
    }

    @Nonnull
    public Observable<Set<String>> sendSelectedObservable() {
        return basePresenter.sendSelectedObservable();
    }

    @Nonnull
    public Observable<Set<String>> closeActivityObservable() {
        return basePresenter.closeActivityObservable()
                .withLatestFrom(basePresenter.selectedObservable(), Functions2.<Set<String>>secondParam());
    }

    @Nonnull
    public Observer<Object> cancelClickObserver() {
        return basePresenter.cancelClickObserver();
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
    public Observer<Object> onBackClickObserver() {
        return basePresenter.onBackClickObserver();
    }

    @Nonnull
    public Observer<Object> sendClickObserver() {
        return basePresenter.sendClickObserver();
    }

    private static final class GalleryVideoItem implements MediaItem {
        @Nonnull
        private final GalleryVideo galleryVideo;
        @Nonnull
        private final Observer<String> selectImageObserver;
        @Nonnull
        private final Observer<BothParams<String, NonJdkKeeper>> clickObserver;
        @Nonnull
        private final Observable<Boolean> selectedObservable;

        GalleryVideoItem(@Nonnull final GalleryVideo galleryVideo,
                         @Nonnull final Observable<Set<String>> selectedImagesObservable,
                         @Nonnull final Observer<String> selectImageObserver,
                         @Nonnull final Observer<BothParams<String, NonJdkKeeper>> clickObserver) {
            this.galleryVideo = galleryVideo;
            this.selectImageObserver = selectImageObserver;
            this.clickObserver = clickObserver;
            this.selectedObservable = selectedImagesObservable
                    .map(new Func1<Set<String>, Boolean>() {
                        @Override
                        public Boolean call(Set<String> strings) {
                            return strings.contains(galleryVideo.data());
                        }
                    })
                    .distinctUntilChanged();
        }

        @Override
        @Nullable
        public NonJdkKeeper thumbnailKeeper() {
            return galleryVideo.fullThumbnailKeeper();
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
                    selectImageObserver.onNext(galleryVideo.data());
                }
            });
        }

        @Nonnull
        @Override
        public Observer<NonJdkKeeper> clickObserver() {
            return Observers.create(new Action1<NonJdkKeeper>() {
                @Override
                public void call(NonJdkKeeper keeper) {
                    clickObserver.onNext(BothParams.of(galleryVideo.data(), keeper));
                }
            });
        }

        @Nonnull
        @Override
        public String data() {
            return galleryVideo.data();
        }

        @Override
        public long adapterId() {
            return galleryVideo.id();
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem instanceof GalleryVideoItem
                    && Objects.equal(galleryVideo.id(), ((GalleryVideoItem) baseAdapterItem).galleryVideo.id());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem baseAdapterItem) {
            return equals(baseAdapterItem);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GalleryVideoItem)) return false;
            final GalleryVideoItem that = (GalleryVideoItem) o;
            return Objects.equal(galleryVideo, that.galleryVideo) &&
                    Objects.equal(selectImageObserver, that.selectImageObserver) &&
                    Objects.equal(clickObserver, that.clickObserver) &&
                    Objects.equal(selectedObservable, that.selectedObservable);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(galleryVideo, selectImageObserver, clickObserver, selectedObservable);
        }
    }

}
