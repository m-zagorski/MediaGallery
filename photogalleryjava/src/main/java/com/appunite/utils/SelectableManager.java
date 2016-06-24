package com.appunite.utils;


import com.appunite.rx.ObservableExtensions;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observables.ConnectableObservable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class SelectableManager<T> {

    @Nonnull
    private final PublishSubject<T> singleSelectionSubject = PublishSubject.create();
    @Nonnull
    private final BehaviorSubject<Set<T>> multipleSelectionSubject = BehaviorSubject.<Set<T>>create(new HashSet<T>());

    @Nonnull
    private final ConnectableObservable<Set<T>> selectedObservable;

    public SelectableManager() {

        selectedObservable = ObservableExtensions
                .behavior(multipleSelectionSubject.switchMap(new Func1<Set<T>, Observable<Set<T>>>() {
                    @Override
                    public Observable<Set<T>> call(Set<T> startValue) {
                        return singleSelectionSubject
                                .scan(startValue, new Func2<Set<T>, T, Set<T>>() {
                                    @Override
                                    public Set<T> call(Set<T> startValue, T selected) {
                                        final HashSet<T> ret = new HashSet<>(startValue);
                                        if (startValue.contains(selected)) {
                                            ret.remove(selected);
                                        } else {
                                            ret.add(selected);
                                        }
                                        return ret;
                                    }
                                })
                                .skip(1)
                                .startWith(startValue);
                    }
                }));

        selectedObservable.connect();
    }

    @Nonnull
    public ConnectableObservable<Set<T>> selectedObservable() {
        return selectedObservable;
    }

    @Nonnull
    public Observer<Set<T>> multipleSelectionObserver() {
        return multipleSelectionSubject;
    }

    @Nonnull
    public Observer<T> singleSelectionObserver() {
        return singleSelectionSubject;
    }
}
