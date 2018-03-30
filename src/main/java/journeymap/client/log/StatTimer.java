package journeymap.client.log;

import org.apache.logging.log4j.*;
import java.util.concurrent.atomic.*;
import com.google.common.util.concurrent.*;
import journeymap.common.log.*;
import java.text.*;
import java.util.concurrent.*;
import net.minecraft.util.text.*;
import journeymap.common.*;
import java.util.*;

public class StatTimer
{
    public static final double NS = 1000000.0;
    private static final int WARMUP_COUNT_DEFAULT = 10;
    private static final int MAX_COUNT = 1000000;
    private static final int MAX_ELAPSED_LIMIT_WARNINGS = 10;
    private static final int ELAPSED_LIMIT_DEFAULT = 1000;
    private static final Logger logger;
    private static Map<String, StatTimer> timers;
    private final int warmupCount;
    private final int elapsedLimit;
    private final AtomicLong counter;
    private final AtomicLong cancelCounter;
    private final AtomicDouble totalTime;
    private final String name;
    private final boolean disposable;
    private final boolean doWarmup;
    private int elapsedLimitWarnings;
    private boolean warmup;
    private boolean maxed;
    private boolean ranTooLong;
    private int ranTooLongCount;
    private Long started;
    private double max;
    private double min;
    
    private StatTimer(final String name, final int warmupCount, final int elapsedLimit, final boolean disposable) {
        this.counter = new AtomicLong();
        this.cancelCounter = new AtomicLong();
        this.totalTime = new AtomicDouble();
        this.elapsedLimitWarnings = 10;
        this.warmup = true;
        this.maxed = false;
        this.ranTooLong = true;
        this.max = 0.0;
        this.min = Double.MAX_VALUE;
        this.name = name;
        this.warmupCount = warmupCount;
        this.elapsedLimit = elapsedLimit;
        this.disposable = disposable;
        this.doWarmup = (warmupCount > 0);
        this.warmup = (warmupCount > 0);
    }
    
    public static synchronized StatTimer get(final String name) {
        return get(name, 10);
    }
    
    public static synchronized StatTimer get(final String name, final int warmupCount) {
        if (name == null) {
            throw new IllegalArgumentException("StatTimer name required");
        }
        StatTimer timer = StatTimer.timers.get(name);
        if (timer == null) {
            timer = new StatTimer(name, warmupCount, 1000, false);
            StatTimer.timers.put(name, timer);
        }
        return timer;
    }
    
    public static synchronized StatTimer get(final String name, final int warmupCount, final int elapsedLimit) {
        if (name == null) {
            throw new IllegalArgumentException("StatTimer name required");
        }
        StatTimer timer = StatTimer.timers.get(name);
        if (timer == null) {
            timer = new StatTimer(name, warmupCount, elapsedLimit, false);
            StatTimer.timers.put(name, timer);
        }
        return timer;
    }
    
    public static StatTimer getDisposable(final String name) {
        return new StatTimer(name, 0, 1000, true);
    }
    
    public static StatTimer getDisposable(final String name, final int elapsedLimit) {
        return new StatTimer(name, 0, elapsedLimit, true);
    }
    
    public static synchronized void resetAll() {
        for (final StatTimer timer : StatTimer.timers.values()) {
            timer.reset();
        }
    }
    
    public static synchronized String getReport() {
        final List<StatTimer> list = new ArrayList<StatTimer>(StatTimer.timers.values());
        Collections.sort(list, new Comparator<StatTimer>() {
            @Override
            public int compare(final StatTimer o1, final StatTimer o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        final StringBuffer sb = new StringBuffer();
        for (final StatTimer timer : list) {
            if (timer.counter.get() > 0L) {
                sb.append(LogFormatter.LINEBREAK).append(timer.getReportString());
            }
        }
        return sb.toString();
    }
    
    public static synchronized List<String> getReportByTotalTime(final String prefix, final String suffix) {
        final List<StatTimer> list = new ArrayList<StatTimer>(StatTimer.timers.values());
        Collections.sort(list, new Comparator<StatTimer>() {
            @Override
            public int compare(final StatTimer o1, final StatTimer o2) {
                return Double.compare(o2.totalTime.get(), o1.totalTime.get());
            }
        });
        final ArrayList<String> strings = new ArrayList<String>();
        for (final StatTimer timer : list) {
            if (timer.counter.get() > 0L) {
                strings.add(prefix + timer.getSimpleReportString() + suffix);
            }
            if (strings.size() >= 30) {
                break;
            }
        }
        return strings;
    }
    
    private static String pad(final Object s, final int n) {
        return String.format("%1$-" + n + "s", s);
    }
    
    public StatTimer start() {
        synchronized (this.counter) {
            if (this.maxed) {
                return this;
            }
            if (this.started != null) {
                StatTimer.logger.warn(this.name + " is already running, cancelling first");
                this.cancel();
            }
            this.ranTooLong = false;
            if (this.counter.get() == 1000000L) {
                this.maxed = true;
                StatTimer.logger.info(this.name + " hit max count, " + 1000000);
                return this;
            }
            if (this.warmup && this.counter.get() > this.warmupCount) {
                this.warmup = false;
                this.max = 0.0;
                this.min = Double.MAX_VALUE;
                this.counter.set(0L);
                this.cancelCounter.set(0L);
                this.totalTime.set(0.0);
                if (StatTimer.logger.isTraceEnabled()) {
                    StatTimer.logger.debug(this.name + " warmup done, " + this.warmupCount);
                }
            }
            this.started = System.nanoTime();
            return this;
        }
    }
    
    public double stop() {
        synchronized (this.counter) {
            if (this.maxed) {
                return 0.0;
            }
            if (this.started == null) {
                if (this.counter.get() > 0L) {
                    StatTimer.logger.warn(this.name + " is not running.");
                }
                return 0.0;
            }
            try {
                final double elapsedMs = (System.nanoTime() - this.started) / 1000000.0;
                this.totalTime.getAndAdd(elapsedMs);
                this.counter.getAndIncrement();
                if (elapsedMs < this.min) {
                    this.min = elapsedMs;
                }
                if (elapsedMs > this.max) {
                    this.max = elapsedMs;
                }
                this.started = null;
                if (!this.warmup && elapsedMs >= this.elapsedLimit) {
                    this.ranTooLong = true;
                    ++this.ranTooLongCount;
                    if (this.elapsedLimitWarnings > 0) {
                        String msg = this.getName() + " was slow: " + elapsedMs;
                        if (--this.elapsedLimitWarnings == 0) {
                            msg += " (Warning limit reached)";
                            StatTimer.logger.warn(msg);
                            StatTimer.logger.warn(this.getReportString().replaceAll("<b>", "").replaceAll("</b>", "").trim());
                        }
                        else {
                            StatTimer.logger.debug(msg);
                        }
                    }
                }
                return elapsedMs;
            }
            catch (Throwable t) {
                StatTimer.logger.error("Timer error: " + LogFormatter.toString(t));
                this.reset();
                return 0.0;
            }
        }
    }
    
    public double elapsed() {
        synchronized (this.counter) {
            if (this.maxed || this.started == null) {
                return 0.0;
            }
            return (System.nanoTime() - this.started) / 1000000.0;
        }
    }
    
    public boolean hasReachedElapsedLimit() {
        return this.ranTooLong;
    }
    
    public int getElapsedLimitReachedCount() {
        return this.ranTooLongCount;
    }
    
    public int getElapsedLimitWarningsRemaining() {
        return this.elapsedLimitWarnings;
    }
    
    public String stopAndReport() {
        this.stop();
        return this.getSimpleReportString();
    }
    
    public void cancel() {
        synchronized (this.counter) {
            this.started = null;
            this.cancelCounter.incrementAndGet();
        }
    }
    
    public void reset() {
        synchronized (this.counter) {
            this.warmup = this.doWarmup;
            this.maxed = false;
            this.started = null;
            this.counter.set(0L);
            this.cancelCounter.set(0L);
            this.totalTime.set(0.0);
            this.elapsedLimitWarnings = 10;
            this.ranTooLong = false;
            this.ranTooLongCount = 0;
        }
    }
    
    public void report() {
        StatTimer.logger.info(this.getReportString());
    }
    
    public String getReportString() {
        final DecimalFormat df = new DecimalFormat("###.##");
        synchronized (this.counter) {
            final long count = this.counter.get();
            final double total = this.totalTime.get();
            final double avg = total / count;
            final long cancels = this.cancelCounter.get();
            String report = String.format("<b>%40s:</b> Avg: %8sms, Min: %8sms, Max: %10sms, Total: %10s sec, Count: %8s, Canceled: %8s, Slow: %8s", this.name, df.format(avg), df.format(this.min), df.format(this.max), TimeUnit.MILLISECONDS.toSeconds((long)total), count, cancels, this.ranTooLongCount);
            if (this.warmup) {
                report += String.format("* Warmup of %s not met", this.warmupCount);
            }
            if (this.maxed) {
                report += "(MAXED)";
            }
            return report;
        }
    }
    
    public String getLogReportString() {
        return TextFormatting.func_110646_a(this.getSimpleReportString());
    }
    
    public String getSimpleReportString() {
        try {
            final DecimalFormat df = new DecimalFormat("###.##");
            synchronized (this.counter) {
                final long count = this.counter.get();
                final double total = this.totalTime.get();
                final double avg = total / count;
                final StringBuilder sb = new StringBuilder(this.name);
                sb.append(TextFormatting.DARK_GRAY);
                sb.append(" count ").append(TextFormatting.RESET);
                sb.append(count);
                sb.append(TextFormatting.DARK_GRAY);
                sb.append(" avg ").append(TextFormatting.RESET);
                if (this.ranTooLongCount > 0) {
                    sb.append(TextFormatting.RESET);
                }
                sb.append(df.format(avg));
                sb.append(TextFormatting.DARK_GRAY);
                sb.append("ms");
                sb.append(TextFormatting.RESET);
                if (this.maxed) {
                    sb.append("(MAXED)");
                }
                return sb.toString();
            }
        }
        catch (Throwable t) {
            return String.format("StatTimer '%s' encountered an error getting its simple report: %s", this.name, t);
        }
    }
    
    public String getName() {
        return this.name;
    }
    
    static {
        logger = Journeymap.getLogger();
        StatTimer.timers = Collections.synchronizedMap(new HashMap<String, StatTimer>());
    }
}
