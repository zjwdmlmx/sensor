package sensor.common;

import java.util.Map;

/**
 * EventEmitter interface
 */
public interface EventEmit {
    static abstract class Callable implements Comparable<Callable> {
        public abstract void call(Map<String, Object> args);

        @Override
        public int compareTo(Callable another) {
            return this.hashCode() - another.hashCode();
        }
    }

    void on(String event, Callable callable);

    void emit(String event, Map<String, Object> args);

    void removeEvent(String event, Callable callable);

    void removeAllEvents(String event);
}
