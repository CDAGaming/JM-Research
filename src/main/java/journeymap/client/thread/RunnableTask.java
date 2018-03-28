package journeymap.client.thread;

import journeymap.client.JourneymapClient;
import journeymap.client.io.FileHandler;
import journeymap.client.task.multi.ITask;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.concurrent.ExecutorService;

public class RunnableTask implements Runnable {
    static final JourneymapClient jm;
    static final Logger logger;
    static final Minecraft mc;
    static final boolean threadLogging;

    static {
        jm = Journeymap.getClient();
        logger = Journeymap.getLogger();
        mc = FMLClientHandler.instance().getClient();
        threadLogging = RunnableTask.jm.isThreadLogging();
    }

    private final ExecutorService taskExecutor;
    private final Runnable innerRunnable;
    private final ITask task;
    private final int timeout;

    public RunnableTask(final ExecutorService taskExecutor, final ITask task) {
        this.taskExecutor = taskExecutor;
        this.task = task;
        this.timeout = task.getMaxRuntime();
        this.innerRunnable = new Inner();
    }

    @Override
    public void run() {
        try {
            this.taskExecutor.submit(this.innerRunnable);
        } catch (Throwable t) {
            Journeymap.getLogger().warn("Interrupted task that ran too long:" + this.task);
        }
    }

    class Inner implements Runnable {
        @Override
        public final void run() {
            try {
                if (!RunnableTask.jm.isMapping()) {
                    RunnableTask.logger.debug("JM not mapping, aborting");
                    return;
                }
                final File jmWorldDir = FileHandler.getJMWorldDir(RunnableTask.mc);
                if (jmWorldDir == null) {
                    RunnableTask.logger.debug("JM world dir not found, aborting");
                    return;
                }
                RunnableTask.this.task.performTask(RunnableTask.mc, RunnableTask.jm, jmWorldDir, RunnableTask.threadLogging);
            } catch (InterruptedException e) {
                RunnableTask.logger.debug("Task interrupted: " + LogFormatter.toPartialString(e));
            } catch (Throwable t) {
                final String error = "Unexpected error during RunnableTask: " + LogFormatter.toString(t);
                RunnableTask.logger.error(error);
            }
        }
    }
}
