package com.appunite.dagger;


import com.appunite.BaseFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;

@Module
public class FragmentModule {
    @Nonnull
    private final BaseFragment fragment;

    public FragmentModule(@Nonnull BaseFragment fragment) {
        this.fragment = fragment;
    }

    @Nonnull
    @Provides
    public RequestManager provideGlide() {
        return Glide.with(fragment);
    }
}

