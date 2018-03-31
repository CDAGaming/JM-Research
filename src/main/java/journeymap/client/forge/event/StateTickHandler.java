package journeymap.client.forge.event;

import journeymap.client.Constants;
import journeymap.client.api.display.Waypoint;
import journeymap.client.api.event.DeathWaypointEvent;
import journeymap.client.api.impl.ClientAPI;
import journeymap.client.api.model.MapImage;
import journeymap.client.properties.WaypointProperties;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.waypoint.WaypointStore;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.text.DateFormat;
import java.util.Date;

@SideOnly(Side.CLIENT)
public class StateTickHandler implements EventHandlerManager.EventHandler {
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
        this.mc.mcProfiler.startSection("journeymap");
        final EntityPlayerSP player = Journeymap.clientPlayer();
        if (player != null && player.isDead) {
            if (!this.deathpointCreated) {
                this.deathpointCreated = true;
                this.createDeathpoint();
            }
        } else {
            this.deathpointCreated = false;
        }
        try {
            if (this.counter == 20) {
                this.mc.mcProfiler.startSection("mainTasks");
                Journeymap.getClient().performMainThreadTasks();
                this.counter = 0;
                this.mc.mcProfiler.endSection();
            } else if (this.counter == 10) {
                this.mc.mcProfiler.startSection("multithreadTasks");
                if (Journeymap.getClient().isMapping() && Journeymap.clientWorld() != null) {
                    Journeymap.getClient().performMultithreadTasks();
                }
                ++this.counter;
                this.mc.mcProfiler.endSection();
            } else if (this.counter == 5 || this.counter == 15) {
                this.mc.mcProfiler.startSection("clientApiEvents");
                ClientAPI.INSTANCE.getClientEventManager().fireNextClientEvents();
                ++this.counter;
                this.mc.mcProfiler.endSection();
            } else {
                ++this.counter;
            }
        } catch (Throwable t) {
            Journeymap.getLogger().warn("Error during onClientTick: " + LogFormatter.toPartialString(t));
        } finally {
            this.mc.mcProfiler.endSection();
        }
    }

    private void createDeathpoint() {
        try {
            final EntityPlayerSP player = Journeymap.clientPlayer();
            if (player == null) {
                Journeymap.getLogger().error("Lost reference to player before Deathpoint could be created");
                return;
            }
            final int dim = player.dimension;
            final WaypointProperties waypointProperties = Journeymap.getClient().getWaypointProperties();
            final boolean enabled = waypointProperties.createDeathpoints.get();
            boolean cancelled = false;
            final BlockPos pos = player.getPosition();
            if (enabled) {
                final DeathWaypointEvent event = new DeathWaypointEvent(pos, dim);
                ClientAPI.INSTANCE.getClientEventManager().fireDeathpointEvent(event);
                if (!event.isCancelled()) {
                    final Date now = new Date();
                    final String name = String.format("%s %s %s", Constants.getString("jm.waypoint.deathpoint"), DateFormat.getTimeInstance().format(now), DateFormat.getDateInstance(3).format(now));
                    final Waypoint deathpoint = new Waypoint("journeymap", name, player.dimension, pos);
                    final int red = 16711680;
                    deathpoint.setLabelColor(red);
                    deathpoint.setIcon(new MapImage(TextureCache.Deathpoint, 16, 16).setColor(red));
                    WaypointStore.INSTANCE.save(deathpoint);
                } else {
                    cancelled = true;
                }
            }
            Journeymap.getLogger().info(String.format("%s died at %s. Deathpoints enabled: %s. Deathpoint created: %s", player.getName(), pos, enabled, cancelled ? "cancelled" : true));
        } catch (Throwable t) {
            Journeymap.getLogger().error("Unexpected Error in createDeathpoint(): " + LogFormatter.toString(t));
        }
    }
}
