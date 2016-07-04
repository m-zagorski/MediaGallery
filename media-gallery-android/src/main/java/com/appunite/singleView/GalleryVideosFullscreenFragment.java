package com.appunite.singleView;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.appunite.R;
import com.appunite.dagger.ForActivity;
import com.appunite.dagger.FragmentModule;
import com.appunite.dagger.GalleryFragmentSingleton;
import com.appunite.rx.NonJdkKeeper;
import com.appunite.rx.android.MyAndroidSchedulers;
import com.appunite.rx.android.widget.RxToolbarMore;
import com.appunite.rx.functions.BothParams;
import com.appunite.rx.functions.Functions2;
import com.appunite.utils.Consts;
import com.appunite.utils.RxViewPagerListener;
import com.appunite.videos.models.FullscreenGalleryVideo;
import com.appunite.videos.presenter.GalleryVideosFullscreenPresenter;
import com.appunite.views.CheckableImageButton;
import com.appunite.views.ForegroundImageView;
import com.jakewharton.rxbinding.view.RxView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import dagger.Provides;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subscriptions.SerialSubscription;
import rx.subscriptions.Subscriptions;

import static com.appunite.utils.Preconditions.checkNotNull;

public class GalleryVideosFullscreenFragment extends FragmentWithBackButtonBehavior {

    private static final String ARGS_BUCKET_NAME = "args_bucket_name";
    private static final String ARGS_CURRENT_ELEMENT = "args_current_element";
    private static final String ARGS_CURRENTLY_SELECTED = "args_currently_selected";
    private static final String STATE_CURRENTLY_SELECTED = "state_currently_selected";

    @Inject
    @ForActivity
    Context context;
    @Inject
    GalleryVideosFullscreenPresenter presenter;

    @Nonnull
    private final SerialSubscription subscription = new SerialSubscription();
    @Nonnull
    private final PublishSubject<Bundle> saveInstanceStateSubject = PublishSubject.create();

    @Nonnull
    static GalleryVideosFullscreenFragment newInstance(@Nonnull String bucketName,
                                                       @Nonnull String currentElement,
                                                       @Nonnull ArrayList<String> currentlySelected) {
        final GalleryVideosFullscreenFragment fragment = new GalleryVideosFullscreenFragment();
        final Bundle args = new Bundle();
        args.putString(ARGS_BUCKET_NAME, bucketName);
        args.putString(ARGS_CURRENT_ELEMENT, currentElement);
        args.putStringArrayList(ARGS_CURRENTLY_SELECTED, currentlySelected);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery_media_fullscreen, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ActivityCompat.postponeEnterTransition(getActivity());

        final ViewPager pager = (ViewPager) view.findViewById(R.id.gallery_media_fullscreen_pager);
        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.gallery_media_fullscreen_toolbar);
        final View bottomContainer = view.findViewById(R.id.gallery_media_fullscreen_bottom_container);
        final Button cancel = (Button) view.findViewById(R.id.gallery_media_fullscreen_cancel);
        final Button send = (Button) view.findViewById(R.id.gallery_media_fullscreen_send);
        parseCustomStyle(bottomContainer, cancel, send);

        if (savedInstanceState != null) {
            @SuppressWarnings("unchecked")
            final ArrayList<String> selected = checkNotNull((ArrayList<String>) savedInstanceState.getSerializable(STATE_CURRENTLY_SELECTED));
            presenter.multipleSelectionObserver().onNext(new HashSet<>(selected));
        }

        final FullscreenVideosAdapter adapter = new FullscreenVideosAdapter(context);
        pager.setAdapter(adapter);

        subscription.set(Subscriptions.from(
                RxToolbarMore.navigationClick(toolbar)
                        .subscribe(presenter.onBackClickObserver()),
                RxView.clicks(view.findViewById(R.id.gallery_media_fullscreen_cancel))
                        .subscribe(presenter.cancelClickObserver()),
                RxView.clicks(send)
                        .subscribe(presenter.sendClickObserver()),
                Observable.create(new RxViewPagerListener.OnPageChangeListener(pager))
                        .subscribe(presenter.currentPositionObserver()),
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
                                send.setAlpha(enabled ? 1F : .5F);
                            }
                        }),
                presenter.titleObservable()
                        .subscribe(new Action1<BothParams<Integer, Integer>>() {
                            @Override
                            public void call(BothParams<Integer, Integer> bothParams) {
                                toolbar.setTitle(getString(R.string.com_appunite_gallery_fullscreen_title, bothParams.param1() + 1, bothParams.param2()));
                            }
                        }),
                presenter.playVideoObservable()
                        .subscribe(new Action1<String>() {
                            @Override
                            public void call(String videoPath) {
                                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoPath));
                                intent.setDataAndType(Uri.parse(videoPath), "video/mp4");
                                startActivity(intent);
                            }
                        }),
                presenter.sendSelectedObservable()
                        .subscribe(new Action1<Set<String>>() {
                            @Override
                            public void call(Set<String> strings) {
                                final Intent data = new Intent();
                                data.putStringArrayListExtra(Consts.EXTRA_SELECTED_MEDIA, new ArrayList<>(strings));
                                getActivity().setResult(Activity.RESULT_OK, data);
                                getActivity().finish();
                            }
                        }),
                presenter.closeActivityObservable()
                        .subscribe(new Action1<Set<String>>() {
                            @Override
                            public void call(Set<String> strings) {
                                final Intent data = new Intent();
                                data.putStringArrayListExtra(Consts.EXTRA_SELECTED_MEDIA, new ArrayList<>(strings));
                                getActivity().setResult(Activity.RESULT_CANCELED, data);
                                ActivityCompat.finishAfterTransition(getActivity());
                            }
                        }),
                presenter.currentPositionObservable()
                        .subscribe(new Action1<Integer>() {
                            @Override
                            public void call(Integer currentPosition) {
                                pager.setCurrentItem(currentPosition, false);
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
    public boolean onBackPressed() {
        presenter.onBackClickObserver().onNext(null);
        return true;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        saveInstanceStateSubject.onNext(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        subscription.set(Subscriptions.empty());
    }

    private class FullscreenVideosAdapter extends PagerAdapter implements Action1<List<FullscreenGalleryVideo>> {

        @Nonnull
        private final Context context;
        @Nonnull
        private final LayoutInflater inflater;
        @Nonnull
        private List<FullscreenGalleryVideo> data = new ArrayList<>();

        FullscreenVideosAdapter(@Nonnull Context context) {
            this.inflater = LayoutInflater.from(context);
            this.context = context;
        }

        @Override
        public void call(@Nonnull List<FullscreenGalleryVideo> data) {
            this.data = checkNotNull(data);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final View viewLayout = inflater.inflate(R.layout.item_fullscreen_video, container, false);
            final ForegroundImageView thumbnail = (ForegroundImageView) viewLayout.findViewById(R.id.item_fullscreen_video_thumbnail);
            final CheckableImageButton selection = (CheckableImageButton) viewLayout.findViewById(R.id.item_fullscreen_video_selection);
            final View play = viewLayout.findViewById(R.id.item_fullscreen_video_play);

            final FullscreenGalleryVideo item = data.get(position);
            ViewCompat.setTransitionName(thumbnail, "Position" + position);

            final NonJdkKeeper nonJdkKeeper = item.thumbnailKeeper();
            thumbnail.setImageBitmap(nonJdkKeeper != null ? nonJdkKeeper.element(Bitmap.class) : null);

            item.subscription().set(Subscriptions.from(
                    Observable.interval(100, TimeUnit.MILLISECONDS, MyAndroidSchedulers.mainThread())
                            .first()
                            .subscribe(new Action1<Object>() {
                                @Override
                                public void call(Object o) {
                                    if (item.triggerTransitions()) {
                                        ActivityCompat.startPostponedEnterTransition(getActivity());
                                    }
                                }
                            }),
                    item.selectedObservable()
                            .subscribe(new Action1<Boolean>() {
                                @Override
                                public void call(Boolean selected) {
                                    selection.setChecked(selected);
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
                    RxView.clicks(viewLayout).share()
                            .map(new Func1<Void, String>() {
                                @Override
                                public String call(Void aVoid) {
                                    return item.galleryVideo().data();
                                }
                            })
                            .subscribe(item.selectObserver()),
                    RxView.clicks(play).share()
                            .map(new Func1<Void, String>() {
                                @Override
                                public String call(Void aVoid) {
                                    return item.galleryVideo().data();
                                }
                            })
                            .subscribe(item.playVideoObserver())
            ));

            container.addView(viewLayout);
            return viewLayout;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            data.get(position).subscription().set(Subscriptions.empty());
        }
    }

    @Override
    protected void inject(@Nonnull FragmentActivity activity) {
        final Bundle arguments = getArguments();
        final String bucketName = checkNotNull(arguments.getString(ARGS_BUCKET_NAME));
        final String currentElement = checkNotNull(arguments.getString(ARGS_CURRENT_ELEMENT));
        final ArrayList<String> currentlySelected = checkNotNull(arguments.getStringArrayList(ARGS_CURRENTLY_SELECTED));

        final Component component = DaggerGalleryVideosFullscreenFragment_Component.builder()
                .component(GalleryMediaFullscreenActivity.component(activity))
                .module(new Module(bucketName, currentElement, currentlySelected))
                .build();
        component.inject(this);
    }

    @GalleryFragmentSingleton
    @dagger.Component(
            dependencies = GalleryMediaFullscreenActivity.Component.class,
            modules = {
                    FragmentModule.class,
                    GalleryVideosFullscreenFragment.Module.class
            }
    )
    public interface Component {
        void inject(GalleryVideosFullscreenFragment fragment);
    }

    @dagger.Module
    public class Module {
        @Nonnull
        private final String bucketName;
        @Nonnull
        private final String currentElement;
        @Nonnull
        private final Set<String> currentlySelected;

        public Module(@Nonnull String bucketName,
                      @Nonnull String currentElement,
                      @Nonnull ArrayList<String> currentlySelected) {
            this.bucketName = bucketName;
            this.currentElement = currentElement;
            this.currentlySelected = new HashSet<>(currentlySelected);
        }

        @Provides
        @Named("bucketName")
        String bucketName() {
            return bucketName;
        }

        @Provides
        @Named("currentElement")
        String currentElement() {
            return currentElement;
        }

        @Provides
        Set<String> currentlySelected() {
            return currentlySelected;
        }
    }
}
