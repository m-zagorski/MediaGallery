package com.appunite.buckets;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appunite.BaseFragment;
import com.appunite.R;
import com.appunite.dagger.GalleryFragmentSingleton;
import com.appunite.rx.android.adapter.UniversalAdapter;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.appunite.videos.presenter.GalleryVideosPresenter;
import com.appunite.viewHolders.ItemVideoBucketManager;

import org.pcollections.TreePVector;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.functions.Action1;
import rx.subscriptions.SerialSubscription;
import rx.subscriptions.Subscriptions;

public class GalleryVideoFragment extends BaseFragment {

    @Inject
    ItemVideoBucketManager bucketManager;
    @Inject
    GalleryVideosPresenter presenter;

    @Nonnull
    private SerialSubscription subscription = new SerialSubscription();

    @Nonnull
    static GalleryVideoFragment newInstance() {
        return new GalleryVideoFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery_videos, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.gallery_videos_recycler);

        final UniversalAdapter adapter = new UniversalAdapter(TreePVector.<ViewHolderManager>empty()
                .plus(bucketManager)
        );
        recyclerView.setAdapter(adapter);

        subscription.set(Subscriptions.from(
                presenter.dataObservable()
                        .subscribe(adapter),
                presenter.openBucketObservable()
                        .subscribe(new Action1<String>() {
                            @Override
                            public void call(String bucketName) {
                                GalleryActivity.fromActivity(getActivity()).startBucketActivity(bucketName, true);
                            }
                        })
        ));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        subscription.set(Subscriptions.empty());
    }

    @Override
    protected void inject(@Nonnull FragmentActivity activity) {
        final Component component = DaggerGalleryVideoFragment_Component.builder()
                .component(GalleryActivity.component(activity))
                .build();
        component.inject(this);
    }

    @GalleryFragmentSingleton
    @dagger.Component(
            dependencies = GalleryActivity.Component.class
    )
    public interface Component {
        void inject(GalleryVideoFragment fragment);
    }
}
