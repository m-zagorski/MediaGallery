package com.appunite.viewHolders;


import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.R;
import com.appunite.rx.NonJdkKeeper;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.appunite.videos.presenter.GalleryVideosPresenter.GalleryVideoBucketItem;
import com.jakewharton.rxbinding.view.RxView;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.subscriptions.SerialSubscription;
import rx.subscriptions.Subscriptions;

public class ItemVideoBucketManager implements ViewHolderManager {

    @Inject
    public ItemVideoBucketManager() {
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
        return baseAdapterItem instanceof GalleryVideoBucketItem;
    }

    @Nonnull
    @Override
    public BaseViewHolder createViewHolder(@Nonnull ViewGroup viewGroup, @Nonnull LayoutInflater layoutInflater) {
        return new ViewHolder(layoutInflater.inflate(R.layout.item_bucket, viewGroup, false));
    }

    private class ViewHolder extends BaseViewHolder<GalleryVideoBucketItem> {

        @Nonnull
        private final ImageView thumbnail;
        @Nonnull
        private final TextView name;
        @Nonnull
        private final TextView count;
        @Nonnull
        private final Observable<Void> bucketClickObservable;

        @Nonnull
        private final SerialSubscription subscription = new SerialSubscription();

        ViewHolder(@Nonnull View itemView) {
            super(itemView);

            thumbnail = (ImageView) itemView.findViewById(R.id.bucket_thumbnail);
            name = (TextView) itemView.findViewById(R.id.bucket_name);
            count = (TextView) itemView.findViewById(R.id.bucket_count);
            bucketClickObservable = RxView.clicks(itemView).share();
        }

        @Override
        public void bind(@Nonnull GalleryVideoBucketItem item) {
            name.setText(item.name());
            count.setText(String.valueOf(item.imagesCount()));

            final NonJdkKeeper viewKeeper = item.thumbnailKeeper();
            final Bitmap thumbnailBitmap = viewKeeper != null
                    ? viewKeeper.element(Bitmap.class)
                    : null;

            thumbnail.setImageBitmap(thumbnailBitmap);

            subscription.set(
                    bucketClickObservable.subscribe(item.bucketClickObserver())
            );
        }

        @Override
        public void onViewRecycled() {
            super.onViewRecycled();
            subscription.set(Subscriptions.empty());
        }
    }
}
