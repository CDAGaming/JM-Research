package journeymap.client.task.multi;

import net.minecraft.client.*;
import journeymap.client.*;
import java.io.*;
import journeymap.client.cartography.color.*;

public class InitColorManagerTask implements ITask
{
    @Override
    public int getMaxRuntime() {
        return 5000;
    }
    
    @Override
    public void performTask(final Minecraft mc, final JourneymapClient jm, final File jmWorldDir, final boolean threadLogging) throws InterruptedException {
        ColorManager.INSTANCE.ensureCurrent(false);
    }
    
    public static class Manager implements ITaskManager
    {
        static boolean enabled;
        
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
        
        static {
            Manager.enabled = false;
        }
    }
}
