package com.appunite.database;


import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;

import com.appunite.images.dao.GalleryImagesDao;
import com.appunite.images.models.GalleryImage;
import com.appunite.models.GalleryBucket;
import com.appunite.models.ImageThumbnail;
import com.appunite.models.TotalCountWithThumbnail;
import com.appunite.rx.internal.Objects;
import com.appunite.utils.GalleryCustomFoldersProvider;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class GalleryImagesDatabaseImpl implements GalleryImagesDao.GalleryImagesDatabase {

    @Nonnull
    private final ContentResolver contentResolver;
    @Nonnull
    private final GalleryCustomFoldersProvider galleryCustomFoldersProvider;

    @Inject
    public GalleryImagesDatabaseImpl(@Nonnull final ContentResolver contentResolver,
                                     @Nonnull final GalleryCustomFoldersProvider galleryCustomFoldersProvider) {
        this.contentResolver = contentResolver;
        this.galleryCustomFoldersProvider = galleryCustomFoldersProvider;
    }

    @Nonnull
    @Override
    public List<GalleryBucket> galleryBuckets() {
        final Cursor cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA},
                MediaStore.Images.Media.BUCKET_ID + " IS NOT NULL) GROUP BY(" + MediaStore.Images.Media.BUCKET_ID,
                null,
                MediaStore.Images.Media.DATE_TAKEN + " DESC"
        );

        if (cursor == null) {
            return new ArrayList<>();
        }

        final List<GalleryBucket> buckets = new ArrayList<>(cursor.getCount());

        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                final String bucketName = cursor.getString(1);
                buckets.add(GalleryBucket.create(
                        cursor.getLong(0),
                        bucketName,
                        ImageThumbnail.create(cursor.getString(2)),
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
    public List<GalleryImage> galleryImagesForBucket(@Nonnull String bucketName) {
        final Cursor cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA},
                MediaStore.Images.Media.DATA + " LIKE ('%' || ? || '%')",
                new String[]{Objects.equal(bucketName, galleryCustomFoldersProvider.allImagesFolderName()) ? "" : bucketName},
                MediaStore.Images.Media.DATE_TAKEN + " DESC"
        );

        if (cursor == null) {
            return new ArrayList<>();
        }

        final List<GalleryImage> images = new ArrayList<>(cursor.getCount());

        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                images.add(
                        GalleryImage.create(
                                cursor.getLong(0),
                                cursor.getString(1)
                        )
                );
            }
        } finally {
            cursor.close();
        }

        return images;
    }

    @Nonnull
    @Override
    public TotalCountWithThumbnail totalCountWithThumbnail() {
        final Cursor cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA},
                null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC"
        );

        if (cursor == null) {
            return TotalCountWithThumbnail.create(null, 0);
        }

        try {
            if (cursor.moveToFirst()) {
                return TotalCountWithThumbnail.create(ImageThumbnail.create(cursor.getString(1)), cursor.getCount());
            }
            return TotalCountWithThumbnail.create(null, 0);
        } finally {
            cursor.close();
        }
    }

    private int photosForBucketCount(@Nonnull String bucketName) {
        final Cursor cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + " LIKE ('%' || ? || '%')",
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
