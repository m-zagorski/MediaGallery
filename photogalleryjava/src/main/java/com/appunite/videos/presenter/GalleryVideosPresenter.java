package com.appunite.videos.presenter;


import com.appunite.models.GalleryBucket;
import com.appunite.models.TotalCountWithThumbnail;
import com.appunite.models.VideoThumbnail;
import com.appunite.rx.NonJdkKeeper;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.internal.Objects;
import com.appunite.utils.GalleryCustomFoldersProvider;
import com.appunite.videos.dao.GalleryVideosDao;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observers.Observers;
import rx.subjects.PublishSubject;

public class GalleryVideosPresenter {
    private static final long ALL_VIDEOS_ID = -1L;

    @Nonnull
    private final Observable<List<BaseAdapterItem>> dataObservable;
    @Nonnull
    private final PublishSubject<String> bucketClickSubject = PublishSubject.create();

    @Inject
    public GalleryVideosPresenter(@Nonnull final GalleryVideosDao dao,
                                  @Nonnull @UiScheduler final Scheduler uiScheduler,
                                  @Nonnull final GalleryCustomFoldersProvider foldersProvider) {

        dataObservable = dao.galleryBucketsObservable()
                .switchMap(new Func1<List<GalleryBucket>, Observable<List<BaseAdapterItem>>>() {
                    @Override
                    public Observable<List<BaseAdapterItem>> call(List<GalleryBucket> galleryBuckets) {
                        return Observable.from(galleryBuckets)
                                .map(new Func1<GalleryBucket, BaseAdapterItem>() {
                                    @Override
                                    public BaseAdapterItem call(GalleryBucket galleryBucket) {
                                        return new GalleryVideoBucketItem(galleryBucket, bucketClickSubject);
                                    }
                                })
                                .toList();
                    }
                })
                .switchMap(new Func1<List<BaseAdapterItem>, Observable<? extends List<BaseAdapterItem>>>() {
                    @Override
                    public Observable<? extends List<BaseAdapterItem>> call(final List<BaseAdapterItem> baseAdapterItems) {
                        return dao.photosCountWithThumbnailObservable()
                                .first()
                                .map(new Func1<TotalCountWithThumbnail, List<BaseAdapterItem>>() {
                                    @Override
                                    public List<BaseAdapterItem> call(TotalCountWithThumbnail totalCountWithThumbnail) {
                                        final List<BaseAdapterItem> items = new ArrayList<>();
                                        items.add(new GalleryVideoBucketItem(
                                                GalleryBucket
                                                        .create(
                                                                ALL_VIDEOS_ID,
                                                                foldersProvider.allVideosFolderName(),
                                                                totalCountWithThumbnail.thumbnail(),
                                                                totalCountWithThumbnail.count()
                                                        ), bucketClickSubject)
                                        );
                                        items.addAll(baseAdapterItems);
                                        return items;
                                    }
                                });
                    }
                })
                .observeOn(uiScheduler);
    }

    @Nonnull
    public Observable<List<BaseAdapterItem>> dataObservable() {
        return dataObservable;
    }

    @Nonnull
    public Observable<String> openBucketObservable() {
        return bucketClickSubject;
    }

    public static final class GalleryVideoBucketItem implements BaseAdapterItem {
        @Nonnull
        private final GalleryBucket bucket;
        @Nonnull
        private final Observer<String> bucketClickObserver;

        GalleryVideoBucketItem(@Nonnull GalleryBucket bucket,
                               @Nonnull Observer<String> bucketClickObserver) {
            this.bucket = bucket;
            this.bucketClickObserver = bucketClickObserver;
        }

        @Nullable
        public NonJdkKeeper thumbnailKeeper() {
            if (bucket.thumbnail() instanceof VideoThumbnail) {
                final VideoThumbnail thumbnail = (VideoThumbnail) bucket.thumbnail();
                return thumbnail != null ? thumbnail.thumbnailKeeper() : null;
            } else {
                throw new RuntimeException("Image item without image thumbnail " + bucket.thumbnail());
            }
        }

        @Nullable
        public String name() {
            return bucket.name();
        }

        public int imagesCount() {
            return bucket.count();
        }

        @Nonnull
        public Observer<Object> bucketClickObserver() {
            return Observers.create(new Action1<Object>() {
                @Override
                public void call(Object o) {
                    bucketClickObserver.onNext(bucket.name());
                }
            });
        }

        @Override
        public long adapterId() {
            return bucket.id();
        }

        @Override
        public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
            return baseAdapterItem instanceof GalleryVideoBucketItem
                    && Objects.equal(bucket.id(), ((GalleryVideoBucketItem) baseAdapterItem).bucket.id());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem baseAdapterItem) {
            return equals(baseAdapterItem);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GalleryVideoBucketItem)) return false;
            final GalleryVideoBucketItem that = (GalleryVideoBucketItem) o;
            return Objects.equal(bucket, that.bucket);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(bucket);
        }
    }
}
