package com.ppetrov.game.observable;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ObservableEx {

    public static Observable<Long> timer(Supplier<Integer> periodFunc, TimeUnit timeUnit) {
        return timer(periodFunc, timeUnit, Schedulers.computation());
    }

    private static Observable<Long> timer(Supplier<Integer> periodFunc, TimeUnit timeUnit, Scheduler scheduler) {
        return Observable.create(new Observable.OnSubscribe<Long>() {
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
