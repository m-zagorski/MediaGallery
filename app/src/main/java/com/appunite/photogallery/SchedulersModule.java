package com.appunite.photogallery;


import com.appunite.rx.android.MyAndroidSchedulers;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;
import rx.schedulers.Schedulers;

@Module
public class SchedulersModule {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 4 + 1;
    private static final int KEEP_ALIVE_SECONDS = 1;

    @Nonnull
    @Provides
    @Singleton
    @UiScheduler
    Scheduler provideUiScheduler() {
        return MyAndroidSchedulers.mainThread();
    }

    @Nonnull
    @Provides
    @Singleton
    @NetworkScheduler
    Scheduler provideNetworkScheduler() {
        final AtomicInteger threadCount = new AtomicInteger(0);
        final ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                final Thread thread = new Thread(r);
                thread.setName("NetworkScheduler-" + threadCount.getAndIncrement());
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_LOWEST);
                return thread;
            }
        };
        final LinkedBlockingDeque<Runnable> workQueue = new LinkedBlockingDeque<>(1000000);
        return Schedulers.from(new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_SECONDS,
                TimeUnit.SECONDS,
                workQueue,
                threadFactory));
    }
}