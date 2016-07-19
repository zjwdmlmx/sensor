package sensor;

import java.util.ArrayList;
import java.util.List;

/**
 * Object memory cache
 */

public class Cache<T> {
    public interface Flusher<M> {
        void flush(List<M> objs);
    }

    private ArrayList<T> cache;
    private Flusher<T> flusher = null;
    private int maxCacheSize = 1024;

    public Cache() {
        cache = new ArrayList<>(maxCacheSize);
    }

    public void put(T obj) {
        cache.add(obj);
        if (cache.size() >= maxCacheSize) {
            flusher.flush(cache);
            cache.clear();
        }
    }

    public void put(Iterable<T> obj) {
        for (T o : obj) {
            put(o);
        }
    }

    public void setFlusher(Flusher<T> flusher) {
        this.flusher = flusher;
    }

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }
}
