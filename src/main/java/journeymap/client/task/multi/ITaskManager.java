package journeymap.client.task.multi;

import net.minecraft.client.*;

public interface ITaskManager
{
    Class<? extends ITask> getTaskClass();
    
    boolean enableTask(final Minecraft p0, final Object p1);
    
    boolean isEnabled(final Minecraft p0);
    
    ITask getTask(final Minecraft p0);
    
    void taskAccepted(final ITask p0, final boolean p1);
    
    void disableTask(final Minecraft p0);
}
