package journeymap.client.task.main;

import journeymap.client.JourneymapClient;
import net.minecraft.client.Minecraft;

public interface IMainThreadTask {
    IMainThreadTask perform(final Minecraft p0, final JourneymapClient p1);

    String getName();
}
