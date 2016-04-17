package com.ppetrov.game.observable;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action0;
import rx.plugins.RxJavaObservableExecutionHook;
import rx.plugins.RxJavaPlugins;
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ObservableEx<T> extends Observable<T> {

    private static final RxJavaObservableExecutionHook hook =
            RxJavaPlugins.getInstance().getObservableExecutionHook();

    protected ObservableEx(OnSubscribe<T> f) {
        super(f);
    }

    public static <T> ObservableEx<T> create(OnSubscribe<T> f) {
        return new ObservableEx<>(hook.onCreate(f));
    }

    public static ObservableEx<Long> timer(Supplier<Integer> periodFunc, TimeUnit timeUnit) {
        return timer(periodFunc, timeUnit, Schedulers.computation());
    }

    public static ObservableEx<Long> timer(Supplier<Integer> periodFunc, TimeUnit timeUnit, Scheduler scheduler) {
        return ObservableEx.create(new Observable.OnSubscribe<Long>() {
            private Long value = 0L;

            @Override
            public void call(Subscriber<? super Long> subscriber) {
                scheduler.createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        subscriber.onNext(value++);
                        scheduler.createWorker().schedule(this, periodFunc.get(), timeUnit);
                    }
                }, periodFunc.get(), timeUnit);
            }
        });
    }

}
