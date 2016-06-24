package com.appunite.buckets;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.appunite.R;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

public final class PagerAdapter extends FragmentPagerAdapter {

    @Nonnull
    private final List<Fragment> tabs = new ArrayList<>();
    @Nonnull
    private final List<String> names = new ArrayList<>();
    private final int totalCount;

    @Inject
    public PagerAdapter(@Nonnull FragmentManager fm,
                        @Nonnull Resources resources,
                        @Named("images") boolean images,
                        @Named("videos") boolean videos) {
        super(fm);
        totalCount = images && videos ? 2 : 1;
        if (images) {
            tabs.add(GalleryImageFragment.newInstance());
            names.add(resources.getString(R.string.com_appunite_gallery_gallery_images));
        }
        if (videos) {
            tabs.add(GalleryVideoFragment.newInstance());
            names.add(resources.getString(R.string.com_appunite_gallery_gallery_videos));
        }
    }

    @Override
    public Fragment getItem(int position) {
        return tabs.get(position);
    }

    @Override
    public int getCount() {
        return totalCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return names.get(position);
    }
}
