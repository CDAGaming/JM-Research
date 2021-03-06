package journeymap.client.task.main;

import com.google.common.collect.Queues;
import journeymap.client.JourneymapClient;
import journeymap.client.log.StatTimer;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.concurrent.ConcurrentLinkedQueue;

public class MainTaskController {
    private final ConcurrentLinkedQueue<IMainThreadTask> currentQueue;
    private final ConcurrentLinkedQueue<IMainThreadTask> deferredQueue;

    public MainTaskController() {
        this.currentQueue = Queues.newConcurrentLinkedQueue();
        this.deferredQueue = Queues.newConcurrentLinkedQueue();
    }

    public void addTask(final IMainThreadTask task) {
        synchronized (this.currentQueue) {
            this.currentQueue.add(task);
        }
    }

    public boolean isActive() {
        return !this.currentQueue.isEmpty() && (this.currentQueue.size() != 1 || !(this.currentQueue.peek() instanceof MappingMonitorTask));
    }

    public void performTasks() {
        try {
            synchronized (this.currentQueue) {
                if (this.currentQueue.isEmpty()) {
                    this.currentQueue.add(new MappingMonitorTask());
                }
                final Minecraft minecraft = FMLClientHandler.instance().getClient();
                final JourneymapClient journeymapClient = Journeymap.getClient();
                while (!this.currentQueue.isEmpty()) {
                    final IMainThreadTask task = this.currentQueue.poll();
                    if (task != null) {
                        final StatTimer timer = StatTimer.get(task.getName());
                        timer.start();
                        final IMainThreadTask deferred = task.perform(minecraft, journeymapClient);
                        timer.stop();
                        if (deferred == null) {
                            continue;
                        }
                        this.deferredQueue.add(deferred);
                    }
                }
                this.currentQueue.addAll(this.deferredQueue);
                this.deferredQueue.clear();
            }
        } catch (Throwable t) {
            final String error = "Error in TickTaskController.performMainThreadTasks(): " + t.getMessage();
            Journeymap.getLogger().error(error);
            Journeymap.getLogger().error(LogFormatter.toString(t));
        }
    }
}
