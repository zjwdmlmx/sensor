package sensor.common;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Global
 */
public class Global {
    private Global() {}

    public final static ScheduledExecutorService pool = Executors.newScheduledThreadPool(4);
}
