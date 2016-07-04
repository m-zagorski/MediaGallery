package com.appunite.database;


import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;

import com.appunite.models.GalleryBucket;
import com.appunite.models.TotalCountWithThumbnail;
import com.appunite.models.VideoThumbnail;
import com.appunite.rx.NonJdkKeeper;
import com.appunite.rx.internal.Objects;
import com.appunite.utils.GalleryCustomFoldersProvider;
import com.appunite.videos.dao.GalleryVideosDao;
import com.appunite.videos.models.GalleryVideo;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class GalleryVideosDatabaseImpl implements GalleryVideosDao.GalleryVideosDatabase {

    @Nonnull
    private final ContentResolver contentResolver;
    @Nonnull
    private final GalleryCustomFoldersProvider galleryCustomFoldersProvider;
    @Nonnull
    private final BitmapFactory.Options thumbnailOptions;

    @Inject
    public GalleryVideosDatabaseImpl(@Nonnull final ContentResolver contentResolver,
                                     @Nonnull final GalleryCustomFoldersProvider galleryCustomFoldersProvider) {
        this.contentResolver = contentResolver;
        this.galleryCustomFoldersProvider = galleryCustomFoldersProvider;
        thumbnailOptions = new BitmapFactory.Options();
        thumbnailOptions.inSampleSize = 1;
    }

    @Nonnull
    @Override
    public List<GalleryBucket> galleryBuckets() {
        final Cursor cursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID,
                        MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.DATA},
                MediaStore.Video.Media.BUCKET_ID
                        + " IS NOT NULL) GROUP BY(" + MediaStore.Video.Media.BUCKET_ID,
                null,
                MediaStore.Video.Media.DATE_TAKEN + " DESC"
        );

        if (cursor == null) {
            return new ArrayList<>();
        }

        final List<GalleryBucket> buckets = new ArrayList<>(cursor.getCount());

        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                final long bucketId = cursor.getLong(0);
                final String bucketName = cursor.getString(1);
                final Bitmap thumbnail = MediaStore.Video.Thumbnails.getThumbnail(contentResolver, bucketId, MediaStore.Video.Thumbnails.MINI_KIND, thumbnailOptions);

                buckets.add(GalleryBucket.create(
                        bucketId,
                        bucketName,
                        VideoThumbnail.create(thumbnail != null ? new NonJdkKeeper(thumbnail) : null),
                        photosForBucketCount(bucketName)
                ));
            }
        } finally {
            cursor.close();
        }

        return buckets;
    }

    @Nonnull
    @Override
    public List<GalleryVideo> galleryVideosForBucket(@Nonnull String bucketName) {
        final Cursor cursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA},
                MediaStore.Video.Media.DATA + " LIKE ('%' || ? || '%')",
                new String[]{Objects.equal(bucketName, galleryCustomFoldersProvider.allVideosFolderName()) ? "" : bucketName},
                MediaStore.Video.Media.DATE_TAKEN + " DESC"
        );

        if (cursor == null) {
            return new ArrayList<>();
        }

        final List<GalleryVideo> videos = new ArrayList<>(cursor.getCount());

        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                videos.add(
                        GalleryVideo.create(
                                cursor.getLong(0),
                                cursor.getString(1),
                                null
                        )
                );
            }
        } finally {
            cursor.close();
        }

        return videos;
    }

    @Nonnull
    @Override
    public TotalCountWithThumbnail totalCountWithThumbnail() {
        final Cursor cursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA},
                null, null, MediaStore.Video.Media.DATE_TAKEN + " DESC"
        );

        if (cursor == null) {
            return TotalCountWithThumbnail.create(null, 0);
        }

        try {
            if (cursor.moveToFirst()) {
                final long bucketId = cursor.getLong(0);
                final Bitmap thumbnail = MediaStore.Video.Thumbnails.getThumbnail(contentResolver, bucketId, MediaStore.Video.Thumbnails.MINI_KIND, thumbnailOptions);
                return TotalCountWithThumbnail.create(VideoThumbnail.create(new NonJdkKeeper(thumbnail)), cursor.getCount());
            }
            return TotalCountWithThumbnail.create(null, 0);
        } finally {
            cursor.close();
        }
    }

    private int photosForBucketCount(@Nonnull String bucketName) {
        final Cursor cursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID},
                MediaStore.Video.Media.DATA
                        + " LIKE ('%' || ? || '%')",
                new String[]{bucketName},
                null
        );

        if (cursor == null) {
            return 0;
        }

        try {
            return cursor.getCount();
        } finally {
            cursor.close();
        }
    }
}
