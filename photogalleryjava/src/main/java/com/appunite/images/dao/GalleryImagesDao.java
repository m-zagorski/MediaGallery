package com.appunite.images.dao;


import com.appunite.images.models.GalleryImage;
import com.appunite.models.GalleryBucket;
import com.appunite.models.TotalCountWithThumbnail;
import com.appunite.rx.RxOperators;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.utils.Cache;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func0;
import rx.subjects.PublishSubject;

import static com.appunite.utils.Preconditions.checkNotNull;

@Singleton
public class GalleryImagesDao {

    @Nonnull
    private final GalleryImagesDatabase database;
    @Nonnull
    private final Scheduler networkScheduler;
    @Nonnull
    private final Cache<String, PhotoGalleryForBucketDao> cache;
    @Nonnull
    private final Observable<List<GalleryBucket>> galleryBucketsObservable;
    @Nonnull
    private final Observable<TotalCountWithThumbnail> totalCountWithThumbnailObservable;
    @Nonnull
    private final PublishSubject<Object> refreshSubject = PublishSubject.create();

    @Inject
    public GalleryImagesDao(@Nonnull final GalleryImagesDatabase database,
                            @Nonnull @NetworkScheduler final Scheduler networkScheduler) {
        this.database = database;
        this.networkScheduler = networkScheduler;

        cache = new Cache<>(new Cache.CacheProvider<String, PhotoGalleryForBucketDao>() {
            @Nonnull
            @Override
            public PhotoGalleryForBucketDao load(@Nonnull String bucketName) {
                return new PhotoGalleryForBucketDao(bucketName);
            }
        });

        galleryBucketsObservable = Observable
                .create(RxOperators.fromAction(new Func0<List<GalleryBucket>>() {
                    @Override
                    public List<GalleryBucket> call() {
                        return database.galleryBuckets();
                    }
                }))
                .subscribeOn(networkScheduler)
                .compose(MoreOperators.<List<GalleryBucket>>refresh(refreshSubject))
                .compose(MoreOperators.<List<GalleryBucket>>cacheWithTimeout(networkScheduler));

        totalCountWithThumbnailObservable = Observable
                .create(RxOperators.fromAction(new Func0<TotalCountWithThumbnail>() {
                    @Override
                    public TotalCountWithThumbnail call() {
                        return database.totalCountWithThumbnail();
                    }
                }))
                .subscribeOn(networkScheduler)
                .compose(MoreOperators.<TotalCountWithThumbnail>refresh(refreshSubject))
                .compose(MoreOperators.<TotalCountWithThumbnail>cacheWithTimeout(networkScheduler));
    }

    @Nonnull
    public Observable<List<GalleryBucket>> galleryBucketsObservable() {
        return galleryBucketsObservable;
    }

    @Nonnull
    public Observable<TotalCountWithThumbnail> photosCountWithThumbnailObservable() {
        return totalCountWithThumbnailObservable;
    }

    @Nonnull
    public PhotoGalleryForBucketDao forBucket(@Nonnull String bucketName) {
        return cache.get(checkNotNull(bucketName));
    }

    public final class PhotoGalleryForBucketDao {

        @Nonnull
        private final Observable<List<GalleryImage>> imagesObservable;

        PhotoGalleryForBucketDao(@Nonnull final String bucketName) {

            imagesObservable = Observable
                    .create(RxOperators.fromAction(new Func0<List<GalleryImage>>() {
                        @Override
                        public List<GalleryImage> call() {
                            return database.galleryImagesForBucket(bucketName);
                        }
                    }))
                    .compose(MoreOperators.<List<GalleryImage>>refresh(refreshSubject))
                    .compose(MoreOperators.<List<GalleryImage>>cacheWithTimeout(networkScheduler));
        }

        @Nonnull
        public Observable<List<GalleryImage>> imagesObservable() {
            return imagesObservable;
        }
    }

    public interface GalleryImagesDatabase {
        @Nonnull
        List<GalleryBucket> galleryBuckets();

        @Nonnull
        List<GalleryImage> galleryImagesForBucket(@Nonnull String bucketName);

        @Nonnull
        TotalCountWithThumbnail totalCountWithThumbnail();
    }

}
