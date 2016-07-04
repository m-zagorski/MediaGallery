package com.appunite.viewHolders;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.R;
import com.appunite.images.presenter.GalleryImagesPresenter.GalleryImageBucketItem;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jakewharton.rxbinding.view.RxView;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.subscriptions.SerialSubscription;
import rx.subscriptions.Subscriptions;

public class ItemImageBucketManager implements ViewHolderManager {

    @Nonnull
    private final RequestManager glide;

    @Inject
    public ItemImageBucketManager(@Nonnull RequestManager glide) {
        this.glide = glide;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
        return baseAdapterItem instanceof GalleryImageBucketItem;
    }

    @Nonnull
    @Override
    public BaseViewHolder createViewHolder(@Nonnull ViewGroup viewGroup, @Nonnull LayoutInflater layoutInflater) {
        return new ViewHolder(layoutInflater.inflate(R.layout.item_bucket, viewGroup, false));
    }

    private class ViewHolder extends BaseViewHolder<GalleryImageBucketItem> {

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
        public void bind(@Nonnull GalleryImageBucketItem item) {
            name.setText(item.name());
            count.setText(String.valueOf(item.imagesCount()));
            glide.load(item.imageThumbnail())
                    .priority(Priority.IMMEDIATE)
                    .override(name.getResources().getDimensionPixelSize(R.dimen.com_appunite_gallery_fragment_bucket_min_size), name.getResources().getDimensionPixelSize(R.dimen.com_appunite_gallery_fragment_bucket_min_size))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(thumbnail);

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
