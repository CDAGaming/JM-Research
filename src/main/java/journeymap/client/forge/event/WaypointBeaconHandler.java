package journeymap.client.forge.event;

import net.minecraft.client.*;
import net.minecraftforge.fml.client.*;
import net.minecraftforge.client.event.*;
import journeymap.common.*;
import journeymap.client.render.ingame.*;
import net.minecraftforge.fml.relauncher.*;
import net.minecraftforge.fml.common.eventhandler.*;

public class WaypointBeaconHandler implements EventHandlerManager.EventHandler
{
    final Minecraft mc;
    
    public WaypointBeaconHandler() {
        this.mc = FMLClientHandler.instance().getClient();
    }
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderWorldLastEvent(final RenderWorldLastEvent event) {
        if (this.mc.player != null && Journeymap.getClient().getWaypointProperties().beaconEnabled.get() && !this.mc.gameSettings.hideGUI) {
            this.mc.mcProfiler.startSection("journeymap");
            this.mc.mcProfiler.startSection("beacons");
            RenderWaypointBeacon.renderAll();
            this.mc.mcProfiler.endSection();
            this.mc.mcProfiler.endSection();
        }
    }
}
