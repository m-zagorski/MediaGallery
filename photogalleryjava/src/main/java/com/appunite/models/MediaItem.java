package com.appunite.models;


import com.appunite.rx.NonJdkKeeper;
import com.appunite.rx.android.adapter.BaseAdapterItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Observable;
import rx.Observer;

public interface MediaItem extends BaseAdapterItem {
    @Nullable
    NonJdkKeeper thumbnailKeeper();

    @Nonnull
    Observable<Boolean> selectedObservable();

    @Nonnull
    Observer<Void> selectObserver();

    @Nonnull
    Observer<NonJdkKeeper> clickObserver();

    @Nonnull
    String data();
}