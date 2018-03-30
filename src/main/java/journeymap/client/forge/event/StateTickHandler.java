package journeymap.client.forge.event;

import net.minecraftforge.fml.relauncher.*;
import net.minecraft.client.*;
import net.minecraftforge.fml.client.*;
import net.minecraftforge.fml.common.gameevent.*;
import journeymap.common.*;
import journeymap.client.api.impl.*;
import journeymap.common.log.*;
import net.minecraft.client.entity.*;
import net.minecraftforge.fml.common.eventhandler.*;
import journeymap.client.api.event.*;
import java.util.*;
import journeymap.client.*;
import java.text.*;
import journeymap.client.api.display.*;
import journeymap.client.render.texture.*;
import journeymap.client.api.model.*;
import journeymap.client.waypoint.*;
import journeymap.client.properties.*;
import net.minecraft.util.math.*;

@SideOnly(Side.CLIENT)
public class StateTickHandler implements EventHandlerManager.EventHandler
{
    Minecraft mc;
    int counter;
    private boolean deathpointCreated;
    
    public StateTickHandler() {
        this.mc = FMLClientHandler.instance().getClient();
        this.counter = 0;
    }
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            return;
        }
        this.mc.field_71424_I.func_76320_a("journeymap");
        final EntityPlayerSP player = Journeymap.clientPlayer();
        if (player != null && player.field_70128_L) {
            if (!this.deathpointCreated) {
                this.deathpointCreated = true;
                this.createDeathpoint();
            }
        }
        else {
            this.deathpointCreated = false;
        }
        try {
            if (this.counter == 20) {
                this.mc.field_71424_I.func_76320_a("mainTasks");
                Journeymap.getClient().performMainThreadTasks();
                this.counter = 0;
                this.mc.field_71424_I.func_76319_b();
            }
            else if (this.counter == 10) {
                this.mc.field_71424_I.func_76320_a("multithreadTasks");
                if (Journeymap.getClient().isMapping() && Journeymap.clientWorld() != null) {
                    Journeymap.getClient().performMultithreadTasks();
                }
                ++this.counter;
                this.mc.field_71424_I.func_76319_b();
            }
            else if (this.counter == 5 || this.counter == 15) {
                this.mc.field_71424_I.func_76320_a("clientApiEvents");
                ClientAPI.INSTANCE.getClientEventManager().fireNextClientEvents();
                ++this.counter;
                this.mc.field_71424_I.func_76319_b();
            }
            else {
                ++this.counter;
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().warn("Error during onClientTick: " + LogFormatter.toPartialString(t));
        }
        finally {
            this.mc.field_71424_I.func_76319_b();
        }
    }
    
    private void createDeathpoint() {
        try {
            final EntityPlayerSP player = Journeymap.clientPlayer();
            if (player == null) {
                Journeymap.getLogger().error("Lost reference to player before Deathpoint could be created");
                return;
            }
            final int dim = player.field_71093_bK;
            final WaypointProperties waypointProperties = Journeymap.getClient().getWaypointProperties();
            final boolean enabled = waypointProperties.createDeathpoints.get();
            boolean cancelled = false;
            final BlockPos pos = player.func_180425_c();
            if (enabled) {
                final DeathWaypointEvent event = new DeathWaypointEvent(pos, dim);
                ClientAPI.INSTANCE.getClientEventManager().fireDeathpointEvent(event);
                if (!event.isCancelled()) {
                    final Date now = new Date();
                    final String name = String.format("%s %s %s", Constants.getString("jm.waypoint.deathpoint"), DateFormat.getTimeInstance().format(now), DateFormat.getDateInstance(3).format(now));
                    final Waypoint deathpoint = new Waypoint("journeymap", name, player.field_71093_bK, pos);
                    final int red = 16711680;
                    deathpoint.setLabelColor(red);
                    deathpoint.setIcon(new MapImage(TextureCache.Deathpoint, 16, 16).setColor(red));
                    WaypointStore.INSTANCE.save(deathpoint);
                }
                else {
                    cancelled = true;
                }
            }
            Journeymap.getLogger().info(String.format("%s died at %s. Deathpoints enabled: %s. Deathpoint created: %s", player.func_70005_c_(), pos, enabled, cancelled ? "cancelled" : true));
        }
        catch (Throwable t) {
            Journeymap.getLogger().error("Unexpected Error in createDeathpoint(): " + LogFormatter.toString(t));
        }
    }
}
