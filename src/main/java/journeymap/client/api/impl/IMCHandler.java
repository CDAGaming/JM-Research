package journeymap.client.api.impl;

import net.minecraftforge.fml.common.event.*;
import journeymap.common.*;
import com.google.common.collect.*;

public class IMCHandler
{
    public static void handle(final FMLInterModComms.IMCEvent event) {
        try {
            for (final FMLInterModComms.IMCMessage message : event.getMessages()) {
                final String key = message.key.toLowerCase();
                key.getClass();
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().error("Error processing IMCEvent: " + t, t);
        }
    }
}
