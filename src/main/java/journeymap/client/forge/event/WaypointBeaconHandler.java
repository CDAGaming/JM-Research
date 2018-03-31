package journeymap.client.forge.event;

import journeymap.client.feature.ClientFeatures;
import journeymap.client.render.ingame.RenderWaypointBeacon;
import journeymap.common.Journeymap;
import journeymap.common.api.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WaypointBeaconHandler implements EventHandlerManager.EventHandler {
    final Minecraft mc;
    EntityPlayerSP player;

    public WaypointBeaconHandler() {
        this.mc = FMLClientHandler.instance().getClient();
        this.player = Journeymap.clientPlayer();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderWorldLastEvent(final RenderWorldLastEvent event) {
        if (this.player == null) {
            this.player = Journeymap.clientPlayer();
        }
        if (this.player != null && Journeymap.getClient().getWaypointProperties().beaconEnabled.get() && ClientFeatures.instance().isAllowed(Feature.Display.WaypointBeacon, this.player.dimension) && !this.mc.gameSettings.hideGUI) {
            this.mc.mcProfiler.startSection("journeymap");
            this.mc.mcProfiler.startSection("beacons");
            RenderWaypointBeacon.renderAll();
            this.mc.mcProfiler.endSection();
            this.mc.mcProfiler.endSection();
        }
    }
}
