package journeymap.client.forge.event;

import net.minecraftforge.fml.relauncher.*;
import net.minecraftforge.event.world.*;
import journeymap.client.data.*;
import journeymap.common.*;
import journeymap.client.feature.*;
import net.minecraft.world.*;
import net.minecraftforge.fml.common.eventhandler.*;

@SideOnly(Side.CLIENT)
public class WorldEventHandler implements EventHandlerManager.EventHandler
{
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onUnload(final WorldEvent.Unload event) {
        try {
            final World world = event.getWorld();
            if (DataCache.getPlayer().dimension == world.provider.getDimension()) {
                Journeymap.getClient().stopMapping();
                FeatureManager.INSTANCE.reset();
            }
        }
        catch (Exception e) {
            Journeymap.getLogger().error("Error handling WorldEvent.Unload", (Throwable)e);
        }
    }
}
