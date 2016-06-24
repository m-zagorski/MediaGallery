package com.appunite.videos.dao;


import com.appunite.models.GalleryBucket;
import com.appunite.models.TotalCountWithThumbnail;
import com.appunite.rx.RxOperators;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.operators.MoreOperators;
import com.appunite.utils.Cache;
import com.appunite.utils.ThumbnailProvider;
import com.appunite.videos.models.GalleryVideo;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

import static com.appunite.utils.Preconditions.checkNotNull;

@Singleton
public class GalleryVideosDao {

    @Nonnull
    private final GalleryVideosDatabase database;
    @Nonnull
    private final Scheduler networkScheduler;
    @Nonnull
    private final ThumbnailProvider thumbnailProvider;
    @Nonnull
    private final Cache<String, VideoGalleryForBucketDao> cache;
    @Nonnull
    private final Observable<List<GalleryBucket>> galleryBucketsObservable;
    @Nonnull
    private final Observable<TotalCountWithThumbnail> totalCountWithThumbnailObservable;
    @Nonnull
    private final PublishSubject<Object> refreshSubject = PublishSubject.create();

    @Inject
    public GalleryVideosDao(@Nonnull final GalleryVideosDatabase database,
                            @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                            @Nonnull ThumbnailProvider thumbnailProvider) {
        this.database = database;
        this.networkScheduler = networkScheduler;
        this.thumbnailProvider = thumbnailProvider;

        cache = new Cache<>(new Cache.CacheProvider<String, VideoGalleryForBucketDao>() {
            @Nonnull
            @Override
            public VideoGalleryForBucketDao load(@Nonnull String bucketName) {
                return new VideoGalleryForBucketDao(bucketName);
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
    public VideoGalleryForBucketDao forBucket(@Nonnull String bucketName) {
        return cache.get(checkNotNull(bucketName));
    }

    @Nonnull
    public Observable<List<GalleryBucket>> galleryBucketsObservable() {
        return galleryBucketsObservable;
    }

    @Nonnull
    public Observable<TotalCountWithThumbnail> photosCountWithThumbnailObservable() {
        return totalCountWithThumbnailObservable;
    }

    public final class VideoGalleryForBucketDao {

        @Nonnull
        private final Observable<List<GalleryVideo>> videosObservable;

        VideoGalleryForBucketDao(@Nonnull final String bucketName) {

            final Observable<GalleryVideo> videoObservable = Observable
                    .create(RxOperators.fromAction(new Func0<List<GalleryVideo>>() {
                        @Override
                        public List<GalleryVideo> call() {
                            return database.galleryVideosForBucket(bucketName);
                        }
                    }))
                    .switchMap(new Func1<List<GalleryVideo>, Observable<GalleryVideo>>() {
                        @Override
                        public Observable<GalleryVideo> call(List<GalleryVideo> galleryVideos) {
                            return Observable.from(galleryVideos)
                                    .map(new Func1<GalleryVideo, GalleryVideo>() {
                                        @Override
                                        public GalleryVideo call(GalleryVideo galleryVideo) {
                                            return GalleryVideo.create(
                                                    galleryVideo.id(),
                                                    galleryVideo.data(),
                                                    thumbnailProvider.provideFullscreenThumbnailForVideo(galleryVideo.data())
                                            );
                                        }
                                    });
                        }
                    })
                    .subscribeOn(networkScheduler);

            videosObservable = videoObservable
                    .scan(new ArrayList<GalleryVideo>(), new Func2<List<GalleryVideo>, GalleryVideo, List<GalleryVideo>>() {
                        @Override
                        public List<GalleryVideo> call(List<GalleryVideo> galleryVideos, GalleryVideo galleryVideo) {
                            final List<GalleryVideo> items = new ArrayList<>(galleryVideos);
                            items.add(galleryVideo);
                            return items;
                        }
                    })
                    .compose(MoreOperators.<List<GalleryVideo>>refresh(refreshSubject))
                    .compose(MoreOperators.<List<GalleryVideo>>cacheWithTimeout(networkScheduler));
        }

        @Nonnull
        public Observable<List<GalleryVideo>> videosObservable() {
            return videosObservable;
        }
    }

    public interface GalleryVideosDatabase {
        @Nonnull
        List<GalleryBucket> galleryBuckets();

        @Nonnull
        List<GalleryVideo> galleryVideosForBucket(@Nonnull String bucketName);

        @Nonnull
        TotalCountWithThumbnail totalCountWithThumbnail();
    }
}
