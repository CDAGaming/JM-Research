package journeymap.client.task.multi;

import net.minecraft.client.*;
import journeymap.client.*;
import java.io.*;

public interface ITask
{
    int getMaxRuntime();
    
    void performTask(final Minecraft p0, final JourneymapClient p1, final File p2, final boolean p3) throws InterruptedException;
}
