package spot;

import rx.Observable;
import rx.Subscriber;

public abstract class Spot implements Observable.OnSubscribe<Integer> {
    abstract public void setSample(Object sample);

    abstract public int spot();

    @Override
    public void call(Subscriber<? super Integer> subscriber) {
        subscriber.onNext(spot());
    }
}
