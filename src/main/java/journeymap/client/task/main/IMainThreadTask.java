package journeymap.client.task.main;

import net.minecraft.client.*;
import journeymap.client.*;

public interface IMainThreadTask
{
    IMainThreadTask perform(final Minecraft p0, final JourneymapClient p1);
    
    String getName();
}
