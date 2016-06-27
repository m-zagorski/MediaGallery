package com.appunite.bucket;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appunite.BaseFragment;
import com.appunite.R;
import com.appunite.dagger.ForActivity;
import com.appunite.dagger.FragmentModule;
import com.appunite.dagger.GalleryFragmentSingleton;
import com.appunite.models.FullscreenData;
import com.appunite.rx.android.adapter.UniversalAdapter;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.appunite.rx.android.widget.RxToolbarMore;
import com.appunite.rx.functions.BothParams;
import com.appunite.rx.functions.Functions2;
import com.appunite.singleView.GalleryMediaFullscreenActivity;
import com.appunite.utils.Consts;
import com.appunite.videos.presenter.GalleryVideosBucketPresenter;
import com.appunite.viewHolders.ItemMediaManager;
import com.jakewharton.rxbinding.view.RxView;

import org.pcollections.TreePVector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import dagger.Provides;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subscriptions.SerialSubscription;
import rx.subscriptions.Subscriptions;

import static com.appunite.utils.Preconditions.checkNotNull;

public class GalleryVideosBucketFragment extends BaseFragment {

    private static final String ARGS_BUCKET_NAME = "args_bucket_name";
    private static final String STATE_CURRENTLY_SELECTED = "state_currently_selected";
    private static final int GALLERY_FULLSCREEN_REQUEST_CODE = 1;

    @Inject
    @ForActivity
    Context context;
    @Inject
    ItemMediaManager mediaManager;
    @Inject
    GalleryVideosBucketPresenter presenter;

    @Nonnull
    private final SerialSubscription subscription = new SerialSubscription();
    @Nonnull
    private final PublishSubject<Bundle> saveInstanceStateSubject = PublishSubject.create();

    @Nonnull
    static GalleryVideosBucketFragment newInstance(@Nonnull String bucketName) {
        final GalleryVideosBucketFragment fragment = new GalleryVideosBucketFragment();
        final Bundle args = new Bundle();
        args.putString(ARGS_BUCKET_NAME, checkNotNull(bucketName));
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery_media_bucket, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.gallery_bucket_media_toolbar);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.gallery_bucket_media_recycler);
        final ViewGroup bottomContainer = (ViewGroup) view.findViewById(R.id.gallery_bucket_bottom_container);
        final TextView cancel = (TextView) view.findViewById(R.id.gallery_bucket_media_cancel);
        final TextView send = (TextView) view.findViewById(R.id.gallery_bucket_media_send);
        parseCustomStyle(bottomContainer, cancel, send);

        if (savedInstanceState != null) {
            @SuppressWarnings("unchecked")
            final ArrayList<String> selected = checkNotNull((ArrayList<String>) savedInstanceState.getSerializable(STATE_CURRENTLY_SELECTED));
            presenter.multipleSelectionObserver().onNext(new HashSet<>(selected));
        }

        final UniversalAdapter adapter = new UniversalAdapter(TreePVector.<ViewHolderManager>empty()
                .plus(mediaManager)
        );

        recyclerView.setAdapter(adapter);
        recyclerView.getItemAnimator().setSupportsChangeAnimations(false);
        ((GridLayoutManager) recyclerView.getLayoutManager()).setRecycleChildrenOnDetach(true);

        toolbar.setTitle(presenter.toolbarTitle());
        subscription.set(Subscriptions.from(
                RxToolbarMore.navigationClick(toolbar)
                        .subscribe(presenter.onBackClickObserver()),
                RxView.clicks(cancel)
                        .subscribe(presenter.cancelClickObserver()),
                RxView.clicks(send)
                        .subscribe(presenter.sendClickObserver()),
                presenter.dataObservable()
                        .subscribe(adapter),
                presenter.selectedCountObservable()
                        .subscribe(new Action1<Integer>() {
                            @Override
                            public void call(Integer integer) {
                                send.setText(integer > 0 ? getString(R.string.com_appunite_gallery_gallery_bucket_send_enabled, integer) : getString(R.string.com_appunite_gallery_gallery_bucket_send_disabled));
                            }
                        }),
                presenter.sendStateObservable()
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean enabled) {
                                send.animate().alpha(enabled ? 1F : .5F).setDuration(100).start();
                            }
                        }),
                presenter.openFullscreenObservable()
                        .subscribe(new Action1<FullscreenData>() {
                            @Override
                            public void call(FullscreenData fullscreenData) {
                                final Intent intent = GalleryMediaFullscreenActivity.newIntent(
                                        context,
                                        fullscreenData.bucketName(),
                                        fullscreenData.currentElement(),
                                        fullscreenData.currentlySelected(), true);


                                final View transitionView = fullscreenData.viewKeeper().element(View.class);
                                @SuppressWarnings("unchecked")
                                final ActivityOptionsCompat options = ActivityOptionsCompat
                                        .makeSceneTransitionAnimation(getActivity(), Pair.create(transitionView, ViewCompat.getTransitionName(transitionView)));
                                ActivityCompat.startActivityForResult(getActivity(), intent, GALLERY_FULLSCREEN_REQUEST_CODE, options.toBundle());
                            }
                        }),
                presenter.sendSelectedObservable()
                        .subscribe(new Action1<Set<String>>() {
                            @Override
                            public void call(Set<String> strings) {
                                final Intent data = new Intent();
                                data.putStringArrayListExtra(Consts.EXTRA_SELECTED_MEDIA, new ArrayList<>(strings));
                                getActivity().setResult(Activity.RESULT_OK, data);
                                ActivityCompat.finishAfterTransition(getActivity());
                            }
                        }),
                presenter.closeActivityObservable()
                        .subscribe(new Action1<Set<String>>() {
                            @Override
                            public void call(Set<String> strings) {
                                getActivity().setResult(Activity.RESULT_CANCELED);
                                ActivityCompat.finishAfterTransition(getActivity());
                            }
                        }),
                saveInstanceStateSubject
                        .withLatestFrom(presenter.selectedObservable(), Functions2.<Bundle, Set<String>>bothParams())
                        .subscribe(new Action1<BothParams<Bundle, Set<String>>>() {
                            @Override
                            public void call(BothParams<Bundle, Set<String>> bothParams) {
                                bothParams.param1().putStringArrayList(STATE_CURRENTLY_SELECTED, new ArrayList<>(bothParams.param2()));
                            }
                        })
        ));
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        saveInstanceStateSubject.onNext(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_FULLSCREEN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                final ArrayList<String> selectedData = data.getStringArrayListExtra(Consts.EXTRA_SELECTED_MEDIA);
                presenter.multipleSelectionObserver().onNext(new HashSet<>(selectedData));
            } else {
                getActivity().setResult(Activity.RESULT_OK, data);
                ActivityCompat.finishAfterTransition(getActivity());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        subscription.set(Subscriptions.empty());
    }

    @Override
    protected void inject(@Nonnull FragmentActivity activity) {
        final Component component = DaggerGalleryVideosBucketFragment_Component.builder()
                .component(GalleryMediaBucketActivity.component(activity))
                .fragmentModule(new FragmentModule(this))
                .module(new Module(checkNotNull(getArguments().getString(ARGS_BUCKET_NAME))))
                .build();
        component.inject(this);
    }

    @GalleryFragmentSingleton
    @dagger.Component(
            dependencies = GalleryMediaBucketActivity.Component.class,
            modules = {
                    FragmentModule.class,
                    Module.class
            }
    )
    public interface Component {
        void inject(GalleryVideosBucketFragment fragment);
    }

    @dagger.Module
    public class Module {
        @Nonnull
        private final String bucketName;

        public Module(@Nonnull String bucketName) {
            this.bucketName = bucketName;
        }

        @Provides
        @Named("bucketName")
        String bucketName() {
            return bucketName;
        }
    }
}

