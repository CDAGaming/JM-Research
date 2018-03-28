package journeymap.client.task.multi;

import journeymap.client.JourneymapClient;
import journeymap.client.cartography.color.ColorManager;
import net.minecraft.client.Minecraft;

import java.io.File;

public class InitColorManagerTask implements ITask {
    @Override
    public int getMaxRuntime() {
        return 5000;
    }

    @Override
    public void performTask(final Minecraft mc, final JourneymapClient jm, final File jmWorldDir, final boolean threadLogging) throws InterruptedException {
        ColorManager.INSTANCE.ensureCurrent(false);
    }

    public static class Manager implements ITaskManager {
        static boolean enabled;

        static {
            Manager.enabled = false;
        }

        @Override
        public Class<? extends ITask> getTaskClass() {
            return InitColorManagerTask.class;
        }

        @Override
        public boolean enableTask(final Minecraft minecraft, final Object params) {
            return Manager.enabled = true;
        }

        @Override
        public boolean isEnabled(final Minecraft minecraft) {
            return Manager.enabled;
        }

        @Override
        public ITask getTask(final Minecraft minecraft) {
            if (Manager.enabled) {
                return new InitColorManagerTask();
            }
            return null;
        }

        @Override
        public void taskAccepted(final ITask task, final boolean accepted) {
            Manager.enabled = false;
        }

        @Override
        public void disableTask(final Minecraft minecraft) {
            Manager.enabled = false;
        }
    }
}
