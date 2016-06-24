package com.appunite.rx;


import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Scheduler;

public class RxHelpers {

    @Nonnull
    public static <T> Observable.Transformer<T, T> throttleLastWithStartValue(final int skipDuration,
                                                                              @Nonnull final TimeUnit unit,
                                                                              @Nonnull final Scheduler scheduler) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                final Observable<T> refCount = observable.replay(1).refCount();
                return refCount
                        .skip(1)
                        .throttleLast(skipDuration, unit, scheduler)
                        .startWith(refCount.first());
            }
        };
    }

}
