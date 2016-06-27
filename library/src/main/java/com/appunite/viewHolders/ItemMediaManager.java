package com.appunite.viewHolders;


import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appunite.R;
import com.appunite.dagger.ForActivity;
import com.appunite.models.MediaItem;
import com.appunite.rx.NonJdkKeeper;
import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.appunite.rx.functions.Functions1;
import com.appunite.views.CheckableImageButton;
import com.appunite.views.ForegroundSquareImageView;
import com.bumptech.glide.RequestManager;
import com.jakewharton.rxbinding.view.RxView;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action1;
import rx.subscriptions.SerialSubscription;
import rx.subscriptions.Subscriptions;

public class ItemMediaManager implements ViewHolderManager {

    @Nonnull
    private final Context context;
    @Nonnull
    private final RequestManager glide;

    @Inject
    public ItemMediaManager(@Nonnull @ForActivity Context context,
                            @Nonnull RequestManager requestManager) {
        this.context = context;
        this.glide = requestManager;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem baseAdapterItem) {
        return baseAdapterItem instanceof MediaItem;
    }

    @Nonnull
    @Override
    public BaseViewHolder createViewHolder(@Nonnull ViewGroup viewGroup, @Nonnull LayoutInflater layoutInflater) {
        return new ViewHolder(layoutInflater.inflate(R.layout.item_media, viewGroup, false));
    }

    private class ViewHolder extends BaseViewHolder<MediaItem> {

        @Nonnull
        private final ForegroundSquareImageView thumbnail;
        @Nonnull
        private final CheckableImageButton selection;
        @Nonnull
        private final Observable<Void> selectionClickObservable;
        @Nonnull
        private final Observable<Void> clickObservable;

        @Nonnull
        private final SerialSubscription subscription = new SerialSubscription();

        ViewHolder(@Nonnull View itemView) {
            super(itemView);

            thumbnail = (ForegroundSquareImageView) itemView.findViewById(R.id.media_thumbnail);
            selection = (CheckableImageButton) itemView.findViewById(R.id.media_selection);
            selectionClickObservable = RxView.clicks(selection).share();
            clickObservable = RxView.clicks(thumbnail).share();
        }

        @Override
        public void bind(@Nonnull MediaItem item) {
            ViewCompat.setTransitionName(thumbnail, "Position" + getAdapterPosition());

            final NonJdkKeeper nonJdkKeeper = item.thumbnailKeeper();
            if (nonJdkKeeper != null) {
                thumbnail.setImageBitmap(nonJdkKeeper.element(Bitmap.class));
            } else {
                glide.load(item.data())
//                        .override(thumbnail.getResources().getDimensionPixelSize(R.dimen.com_appunite_gallery_bucket_fragment_min_size), thumbnail.getResources().getDimensionPixelSize(R.dimen.com_appunite_gallery_bucket_fragment_min_size))
                        .into(thumbnail);
            }

            subscription.set(Subscriptions.from(
                    item.selectedObservable()
                            .subscribe(new Action1<Boolean>() {
                                @Override
                                public void call(Boolean aBoolean) {
                                    selection.setChecked(aBoolean);
                                }
                            }),
                    item.selectedObservable()
                            .subscribe(new Action1<Boolean>() {
                                @Override
                                public void call(Boolean selected) {
                                    thumbnail.addForeground(selected
                                            ? ContextCompat.getDrawable(context, R.drawable.selector_image_selected)
                                            : ContextCompat.getDrawable(context, R.drawable.selector_image_not_selected)
                                    );
                                }
                            }),
                    selectionClickObservable
                            .subscribe(item.selectObserver()),
                    clickObservable
                            .map(Functions1.returnJust(new NonJdkKeeper(thumbnail)))
                            .subscribe(item.clickObserver())
            ));
        }

        @Override
        public void onViewRecycled() {
            super.onViewRecycled();
            subscription.set(Subscriptions.empty());
        }
    }
}
