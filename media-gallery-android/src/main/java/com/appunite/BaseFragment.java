package com.appunite;


import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.appunite.dagger.ForActivity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public abstract class BaseFragment extends Fragment {

    protected abstract void inject(@Nonnull FragmentActivity activity);

    @Inject
    @ForActivity
    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inject(getActivity());
    }

    protected void parseCustomStyle(@Nonnull View bottomContainer,
                                    @Nonnull TextView cancel,
                                    @Nonnull TextView send) {
        final TypedArray typedArray = getActivity().obtainStyledAttributes(R.styleable.PhotoGallery);
        try {
            final int backgroundColorId = typedArray.getResourceId(R.styleable.PhotoGallery_photoGalleryBottomContainerBackgroundColor, 0);
            final int cancelColorId = typedArray.getResourceId(R.styleable.PhotoGallery_photoGalleryBottomCancelTextViewColor, 0);
            final int sendColorId = typedArray.getResourceId(R.styleable.PhotoGallery_photoGalleryBottomSendTextViewColor, 0);

            if (backgroundColorId != 0) {
                bottomContainer.setBackgroundColor(ContextCompat.getColor(context, backgroundColorId));
            }
            if (cancelColorId != 0) {
                cancel.setTextColor(ContextCompat.getColor(context, cancelColorId));
            }
            if (sendColorId != 0) {
                send.setTextColor(ContextCompat.getColor(context, sendColorId));
            }
        } finally {
            typedArray.recycle();
        }
    }
}
