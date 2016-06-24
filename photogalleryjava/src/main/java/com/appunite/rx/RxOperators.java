package com.appunite.rx;


import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Producer;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Func0;

public class RxOperators {

    @Nonnull
    public static <T> Observable.OnSubscribe<T> fromAction(@Nonnull final Func0<T> call) {
        return new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> child) {
                final AtomicBoolean produce = new AtomicBoolean(true);
                child.setProducer(new Producer() {
                    @Override
                    public void request(long n) {
                        if (n <= 0) {
                            return;
                        }
                        if (produce.getAndSet(false)) {
                            produceValue(child);
                        }
                    }
                });
            }

            private void produceValue(Subscriber<? super T> child) {
                try {
                    if (child.isUnsubscribed()) {
                        return;
                    }
                    final T result = call.call();
                    if (child.isUnsubscribed()) {
                        return;
                    }
                    child.onNext(result);
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    child.onError(OnErrorThrowable.addValueAsLastCause(e, call));
                }
                if (child.isUnsubscribed()) {
                    return;
                }
                child.onCompleted();
            }
        };
    }
}
