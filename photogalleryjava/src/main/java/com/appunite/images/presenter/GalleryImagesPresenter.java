package com.appunite.images.presenter;


import com.appunite.images.dao.GalleryImagesDao;
import com.appunite.models.GalleryBucket;
import com.appunite.models.ImageThumbnail;
import com.appunite.models.TotalCountWithThumbnail;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.internal.Objects;
import com.appunite.utils.GalleryCustomFoldersProvider;

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

public class GalleryImagesPresenter {

    private static final long ALL_PHOTOS_ID = -1L;

    @Nonnull
    private final Observable<List<BaseAdapterItem>> dataObservable;
    @Nonnull
    private final PublishSubject<String> bucketClickSubject = PublishSubject.create();

    @Inject
    public GalleryImagesPresenter(@Nonnull final GalleryImagesDao dao,
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
                                        return new GalleryImageBucketItem(galleryBucket, bucketClickSubject);
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
                                        items.add(new GalleryImageBucketItem(
                                                GalleryBucket
                                                        .create(
                                                                ALL_PHOTOS_ID,
                                                                foldersProvider.allImagesFolderName(),
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

    public static final class GalleryImageBucketItem implements BaseAdapterItem {
        @Nonnull
        private final GalleryBucket bucket;
        @Nonnull
        private final Observer<String> bucketClickObserver;

        GalleryImageBucketItem(@Nonnull GalleryBucket bucket,
                               @Nonnull Observer<String> bucketClickObserver) {
            this.bucket = bucket;
            this.bucketClickObserver = bucketClickObserver;
        }

        @Nullable
        public String imageThumbnail() {
            if (bucket.thumbnail() instanceof ImageThumbnail) {
                final ImageThumbnail thumbnail = (ImageThumbnail) bucket.thumbnail();
                return thumbnail != null ? thumbnail.thumbnailUrl() : null;
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
            return baseAdapterItem instanceof GalleryImageBucketItem
                    && Objects.equal(bucket.id(), ((GalleryImageBucketItem) baseAdapterItem).bucket.id());
        }

        @Override
        public boolean same(@Nonnull BaseAdapterItem baseAdapterItem) {
            return equals(baseAdapterItem);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GalleryImageBucketItem)) return false;
            final GalleryImageBucketItem that = (GalleryImageBucketItem) o;
            return Objects.equal(bucket, that.bucket) &&
                    Objects.equal(bucketClickObserver, that.bucketClickObserver);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(bucket, bucketClickObserver);
        }
    }

}
