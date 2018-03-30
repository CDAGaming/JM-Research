package journeymap.client.forge.event;

import net.minecraft.client.*;
import net.minecraft.client.entity.*;
import net.minecraftforge.fml.client.*;
import journeymap.common.*;
import net.minecraftforge.client.event.*;
import journeymap.client.feature.*;
import journeymap.common.api.feature.*;
import journeymap.client.render.ingame.*;
import net.minecraftforge.fml.relauncher.*;
import net.minecraftforge.fml.common.eventhandler.*;

public class WaypointBeaconHandler implements EventHandlerManager.EventHandler
{
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
        if (this.player != null && Journeymap.getClient().getWaypointProperties().beaconEnabled.get() && ClientFeatures.instance().isAllowed(Feature.Display.WaypointBeacon, this.player.field_71093_bK) && !this.mc.field_71474_y.field_74319_N) {
            this.mc.field_71424_I.func_76320_a("journeymap");
            this.mc.field_71424_I.func_76320_a("beacons");
            RenderWaypointBeacon.renderAll();
            this.mc.field_71424_I.func_76319_b();
            this.mc.field_71424_I.func_76319_b();
        }
    }
}
