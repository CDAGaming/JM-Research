package journeymap.client.task.multi;

import journeymap.client.JourneymapClient;
import net.minecraft.client.Minecraft;

import java.io.File;

public interface ITask {
    int getMaxRuntime();

    void performTask(final Minecraft p0, final JourneymapClient p1, final File p2, final boolean p3) throws InterruptedException;
}
