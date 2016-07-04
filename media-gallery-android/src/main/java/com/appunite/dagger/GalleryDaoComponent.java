package com.appunite.dagger;


import com.appunite.images.dao.GalleryImagesDao;
import com.appunite.videos.dao.GalleryVideosDao;

public interface GalleryDaoComponent {

    GalleryImagesDao photoGalleryDao();

    GalleryVideosDao videoGalleryDao();
}
