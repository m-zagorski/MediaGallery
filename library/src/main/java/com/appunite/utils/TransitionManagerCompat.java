package com.appunite.utils;


import android.annotation.TargetApi;
import android.os.Build;
import android.transition.TransitionManager;
import android.view.ViewGroup;

import javax.annotation.Nonnull;

public class TransitionManagerCompat {

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void beginDelayedTransition(@Nonnull final ViewGroup sceneRoot) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition(sceneRoot);
        }
    }

}