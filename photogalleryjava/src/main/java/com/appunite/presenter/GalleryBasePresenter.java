package com.appunite.presenter;


import com.appunite.rx.NonJdkKeeper;
import com.appunite.rx.functions.BothParams;
import com.appunite.rx.functions.Functions2;
import com.appunite.utils.SelectableManager;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class GalleryBasePresenter {

    @Nonnull
    private final String bucketName;
    @Nonnull
    private final SelectableManager<String> selectableManager = new SelectableManager<>();
    @Nonnull
    private final PublishSubject<Object> onBackClickSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> clickSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<BothParams<String, NonJdkKeeper>> clickWithViewSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> cancelClickSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> sendClickSubject = PublishSubject.create();

    @Inject
    public GalleryBasePresenter(@Nonnull @Named("bucketName") String bucketName) {
        this.bucketName = bucketName;
    }

    @Nonnull
    public String bucketName() {
        return bucketName;
    }

    @Nonnull
    public Observable<Set<String>> selectedObservable() {
        return selectableManager.selectedObservable();
    }

    @Nonnull
    public Observer<String> singleSelectionObserver() {
        return selectableManager.singleSelectionObserver();
    }

    @Nonnull
    public Observer<Set<String>> multiSelectionObserver() {
        return selectableManager.multipleSelectionObserver();
    }

    @Nonnull
    public Observable<Integer> selectedCountObservable() {
        return selectableManager.selectedObservable()
                .map(new Func1<Set<String>, Integer>() {
                    @Override
                    public Integer call(Set<String> strings) {
                        return strings.size();
                    }
                });
    }

    @Nonnull
    public Observable<Boolean> sendStateObservable() {
        return selectedCountObservable()
                .map(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        return integer > 0;
                    }
                });
    }

    @Nonnull
    public Observable<Object> closeActivityObservable() {
        return Observable.merge(onBackClickSubject, cancelClickSubject);
    }

    @Nonnull
    public Observable<Set<String>> sendSelectedObservable() {
        return sendClickSubject
                .withLatestFrom(selectableManager.selectedObservable(), Functions2.<Set<String>>secondParam())
                .filter(new Func1<Set<String>, Boolean>() {
                    @Override
                    public Boolean call(Set<String> strings) {
                        return !strings.isEmpty();
                    }
                });
    }

    @Nonnull
    public Observable<String> clickObservable() {
        return clickSubject;
    }

    @Nonnull
    public Observable<BothParams<String, NonJdkKeeper>> clickWithViewObservable() {
        return clickWithViewSubject;
    }

    @Nonnull
    public Observer<Object> onBackClickObserver() {
        return onBackClickSubject;
    }

    @Nonnull
    public Observer<String> clickObserver() {
        return clickSubject;
    }

    @Nonnull
    public Observer<Object> cancelClickObserver() {
        return cancelClickSubject;
    }

    @Nonnull
    public Observer<Object> sendClickObserver() {
        return sendClickSubject;
    }

    @Nonnull
    public Observer<BothParams<String, NonJdkKeeper>> clickWithViewObserver() {
        return clickWithViewSubject;
    }
}
