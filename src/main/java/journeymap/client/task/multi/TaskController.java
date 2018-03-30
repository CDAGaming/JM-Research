package journeymap.client.task.multi;

import org.apache.logging.log4j.*;
import net.minecraft.client.*;
import java.util.concurrent.locks.*;
import net.minecraftforge.fml.client.*;
import journeymap.common.thread.*;
import java.util.*;
import journeymap.common.log.*;
import journeymap.client.log.*;
import journeymap.client.thread.*;
import java.util.concurrent.*;
import net.minecraft.profiler.*;
import journeymap.common.*;

public class TaskController
{
    static final Logger logger;
    final ArrayBlockingQueue<Future> queue;
    final List<ITaskManager> managers;
    final Minecraft minecraft;
    final ReentrantLock lock;
    private volatile ScheduledExecutorService taskExecutor;
    
    public TaskController() {
        this.queue = new ArrayBlockingQueue<Future>(1);
        this.managers = new LinkedList<ITaskManager>();
        this.minecraft = FMLClientHandler.instance().getClient();
        this.lock = new ReentrantLock();
        this.managers.add(new MapRegionTask.Manager());
        this.managers.add(new SaveMapTask.Manager());
        this.managers.add(new MapPlayerTask.Manager());
        this.managers.add(new InitColorManagerTask.Manager());
    }
    
    private void ensureExecutor() {
        if (this.taskExecutor == null || this.taskExecutor.isShutdown()) {
            this.taskExecutor = Executors.newScheduledThreadPool(1, new JMThreadFactory("task"));
            this.queue.clear();
        }
    }
    
    public Boolean isActive() {
        return this.taskExecutor != null && !this.taskExecutor.isShutdown();
    }
    
    public void enableTasks() {
        this.queue.clear();
        this.ensureExecutor();
        final List<ITaskManager> list = new LinkedList<ITaskManager>(this.managers);
        for (final ITaskManager manager : this.managers) {
            final boolean enabled = manager.enableTask(this.minecraft, null);
            if (!enabled) {
                TaskController.logger.debug("Task not initially enabled: " + manager.getTaskClass().getSimpleName());
            }
            else {
                TaskController.logger.debug("Task ready: " + manager.getTaskClass().getSimpleName());
            }
        }
    }
    
    public void clear() {
        this.managers.clear();
        this.queue.clear();
        if (this.taskExecutor != null && !this.taskExecutor.isShutdown()) {
            this.taskExecutor.shutdownNow();
            try {
                this.taskExecutor.awaitTermination(5L, TimeUnit.SECONDS);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.taskExecutor = null;
        }
    }
    
    private ITaskManager getManager(final Class<? extends ITaskManager> managerClass) {
        ITaskManager taskManager = null;
        for (final ITaskManager manager : this.managers) {
            if (manager.getClass() == managerClass) {
                taskManager = manager;
                break;
            }
        }
        return taskManager;
    }
    
    public boolean isTaskManagerEnabled(final Class<? extends ITaskManager> managerClass) {
        final ITaskManager taskManager = this.getManager(managerClass);
        if (taskManager != null) {
            return taskManager.isEnabled(FMLClientHandler.instance().getClient());
        }
        TaskController.logger.warn("Couldn't toggle task; manager not in controller: " + managerClass.getClass().getName());
        return false;
    }
    
    public void toggleTask(final Class<? extends ITaskManager> managerClass, final boolean enable, final Object params) {
        ITaskManager taskManager = null;
        for (final ITaskManager manager : this.managers) {
            if (manager.getClass() == managerClass) {
                taskManager = manager;
                break;
            }
        }
        if (taskManager != null) {
            this.toggleTask(taskManager, enable, params);
        }
        else {
            TaskController.logger.warn("Couldn't toggle task; manager not in controller: " + managerClass.getClass().getName());
        }
    }
    
    public void toggleTask(final ITaskManager manager, final boolean enable, final Object params) {
        final Minecraft minecraft = FMLClientHandler.instance().getClient();
        if (manager.isEnabled(minecraft)) {
            if (!enable) {
                TaskController.logger.debug("Disabling task: " + manager.getTaskClass().getSimpleName());
                manager.disableTask(minecraft);
            }
            else {
                TaskController.logger.debug("Task already enabled: " + manager.getTaskClass().getSimpleName());
            }
        }
        else if (enable) {
            TaskController.logger.debug("Enabling task: " + manager.getTaskClass().getSimpleName());
            manager.enableTask(minecraft, params);
        }
        else {
            TaskController.logger.debug("Task already disabled: " + manager.getTaskClass().getSimpleName());
        }
    }
    
    public void disableTasks() {
        for (final ITaskManager manager : this.managers) {
            if (manager.isEnabled(this.minecraft)) {
                manager.disableTask(this.minecraft);
                TaskController.logger.debug("Task disabled: " + manager.getTaskClass().getSimpleName());
            }
        }
    }
    
    public boolean hasRunningTask() {
        return !this.queue.isEmpty();
    }
    
    public void queueOneOff(final Runnable runnable) throws Exception {
        try {
            this.ensureExecutor();
            if (this.taskExecutor == null || this.taskExecutor.isShutdown()) {
                throw new IllegalStateException("TaskExecutor isn't running");
            }
            this.taskExecutor.submit(runnable);
        }
        catch (Exception e) {
            TaskController.logger.error("TaskController couldn't queueOneOff(): " + LogFormatter.toString(e));
            throw e;
        }
    }
    
    public void performTasks() {
        final Profiler profiler = FMLClientHandler.instance().getClient().field_71424_I;
        profiler.func_76320_a("journeymapTask");
        final StatTimer totalTimer = StatTimer.get("TaskController.performMultithreadTasks", 1, 500).start();
        try {
            if (this.lock.tryLock()) {
                if (!this.queue.isEmpty() && this.queue.peek().isDone()) {
                    try {
                        this.queue.take();
                    }
                    catch (InterruptedException e) {
                        TaskController.logger.warn(e.getMessage());
                    }
                }
                if (this.queue.isEmpty()) {
                    ITask task = null;
                    final ITaskManager manager = this.getNextManager(this.minecraft);
                    if (manager == null) {
                        TaskController.logger.warn("No task managers enabled!");
                        return;
                    }
                    boolean accepted = false;
                    final StatTimer timer = StatTimer.get(manager.getTaskClass().getSimpleName() + ".Manager.getTask").start();
                    task = manager.getTask(this.minecraft);
                    if (task == null) {
                        timer.cancel();
                    }
                    else {
                        timer.stop();
                        this.ensureExecutor();
                        if (this.taskExecutor != null && !this.taskExecutor.isShutdown()) {
                            final RunnableTask runnableTask = new RunnableTask(this.taskExecutor, task);
                            this.queue.add(this.taskExecutor.submit(runnableTask));
                            accepted = true;
                            if (TaskController.logger.isTraceEnabled()) {
                                TaskController.logger.debug("Scheduled " + manager.getTaskClass().getSimpleName());
                            }
                        }
                        else {
                            TaskController.logger.warn("TaskExecutor isn't running");
                        }
                        manager.taskAccepted(task, accepted);
                    }
                }
                this.lock.unlock();
            }
            else {
                TaskController.logger.warn("TaskController appears to have multiple threads trying to use it");
            }
        }
        finally {
            totalTimer.stop();
            profiler.func_76319_b();
        }
    }
    
    private ITaskManager getNextManager(final Minecraft minecraft) {
        for (final ITaskManager manager : this.managers) {
            if (manager.isEnabled(minecraft)) {
                return manager;
            }
        }
        return null;
    }
    
    static {
        logger = Journeymap.getLogger();
    }
}
