package com.appunite.utils;


import android.support.v4.view.ViewPager;

import com.jakewharton.rxbinding.internal.MainThreadSubscription;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Subscriber;

import static com.jakewharton.rxbinding.internal.Preconditions.checkUiThread;

public class RxViewPagerListener {

    public static final class OnPageChangeListener implements Observable.OnSubscribe<Integer> {
        @Nonnull
        private final ViewPager pager;

        public OnPageChangeListener(@Nonnull ViewPager pager) {
            this.pager = pager;
        }

        @Override
        public void call(final Subscriber<? super Integer> subscriber) {
            checkUiThread();

            final ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(position);
                    }
                }

                @Override
                public void onPageSelected(int position) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(position);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            };
            pager.addOnPageChangeListener(listener);

            subscriber.add(new MainThreadSubscription() {
                @Override
                protected void onUnsubscribe() {
                    pager.removeOnPageChangeListener(listener);
                }
            });
        }
    }

}
