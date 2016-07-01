package sensor.common;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Event emitter
 */
public class EventEmitter implements EventEmit {
    private TreeMap< String, Set<Callable> > events = new TreeMap<>();
    private ReentrantLock lock = new ReentrantLock();

    @Override
    public void on(String event, EventEmitter.Callable callable) {
        lock.lock();
        Set<Callable> callables = events.get(event);
        if (callables == null) {
            callables = new TreeSet<>();
            events.put(event, callables);
        }
        callables.add(callable);
        lock.unlock();

    }

    public void emit(String event) {
        emit(event, null);
    }

    @Override
    public void emit(String event, Map<String, Object> args) {
        Callable[] theCopy = null;
        lock.lock();
        Set<Callable> callables = events.get(event);

        if (callables != null) {
            final int len = callables.size();
            if (len > 0) {
                theCopy = new Callable[len];
                callables.toArray(theCopy);
            }
        }
        lock.unlock();

        if (theCopy == null) return;

        for (Callable callable : theCopy) {
            callable.call(args);
        }
    }

    @Override
    public void removeEvent(String event, Callable callable) {
        lock.lock();
        Set<Callable> callables = events.get(event);
        if (callables != null) {
            callables.remove(callable);
        }
        lock.unlock();
    }

    @Override
    public void removeAllEvents(String event) {
        lock.lock();
        events.remove(event);
        lock.unlock();
    }
}
