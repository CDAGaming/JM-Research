package journeymap.client.forge.event;

import journeymap.client.render.ingame.RenderWaypointBeacon;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WaypointBeaconHandler implements EventHandlerManager.EventHandler {
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
