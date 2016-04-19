package com.ppetrov.game.observable;

import rx.Observable;
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

    public static ObservableEx<Long> interval(Supplier<Integer> periodFunc, TimeUnit timeUnit) {
        return ObservableEx.create(new Observable.OnSubscribe<Long>() {
            private Long value = 0L;

            @Override
            public void call(Subscriber<? super Long> subscriber) {
                Schedulers.computation().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        subscriber.onNext(value++);
                        Schedulers.computation().createWorker().schedule(this, periodFunc.get(), timeUnit);
                    }
                }, periodFunc.get(), timeUnit);
            }
        });
    }

    public static ObservableEx<Boolean> loopWhen(Supplier<Boolean> condition) {
        return ObservableEx.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                Schedulers.computation().createWorker().schedule(new Action0() {
                    @Override
                    public void call() {
                        if (condition.get()) {
                            subscriber.onNext(true);
                        }
                        Schedulers.computation().createWorker().schedule(this);
                    }
                });
            }
        });
    }

}
