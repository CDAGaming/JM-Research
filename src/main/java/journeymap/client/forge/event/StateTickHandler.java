package journeymap.client.forge.event;

import journeymap.client.api.event.DeathWaypointEvent;
import journeymap.client.api.impl.ClientAPI;
import journeymap.client.model.Waypoint;
import journeymap.client.properties.WaypointProperties;
import journeymap.client.waypoint.WaypointStore;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class StateTickHandler implements EventHandlerManager.EventHandler {
    static boolean javaChecked;

    static {
        StateTickHandler.javaChecked = false;
    }

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
        if (this.mc.player != null && this.mc.player.isDead) {
            if (!this.deathpointCreated) {
                this.deathpointCreated = true;
                this.createDeathpoint();
            }
        } else {
            this.deathpointCreated = false;
        }
        if (!StateTickHandler.javaChecked && this.mc.player != null && !this.mc.player.isDead) {
            this.checkJava();
        }
        try {
            if (this.counter == 20) {
                this.mc.mcProfiler.startSection("mainTasks");
                Journeymap.getClient().performMainThreadTasks();
                this.counter = 0;
                this.mc.mcProfiler.endSection();
            } else if (this.counter == 10) {
                this.mc.mcProfiler.startSection("multithreadTasks");
                if (Journeymap.getClient().isMapping() && this.mc.world != null) {
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
            final EntityPlayer player = this.mc.player;
            if (player == null) {
                Journeymap.getLogger().error("Lost reference to player before Deathpoint could be created");
                return;
            }
            final WaypointProperties waypointProperties = Journeymap.getClient().getWaypointProperties();
            final boolean enabled = waypointProperties.managerEnabled.get() && waypointProperties.createDeathpoints.get();
            boolean cancelled = false;
            final BlockPos pos = new BlockPos(MathHelper.floor(player.posX), MathHelper.floor(player.posY), MathHelper.floor(player.posZ));
            if (enabled) {
                final int dim = FMLClientHandler.instance().getClient().player.world.provider.getDimension();
                final DeathWaypointEvent event = new DeathWaypointEvent(pos, dim);
                ClientAPI.INSTANCE.getClientEventManager().fireDeathpointEvent(event);
                if (!event.isCancelled()) {
                    final Waypoint deathpoint = Waypoint.at(pos, Waypoint.Type.Death, dim);
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

    private void checkJava() {
        StateTickHandler.javaChecked = true;
        try {
            Class.forName("java.util.Objects");
        } catch (ClassNotFoundException e3) {
            try {
                final String error = I18n.format("jm.error.java6");
                FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(error));
                Journeymap.getLogger().fatal("JourneyMap requires Java 7 or Java 8. Update your launcher profile to use a newer version of Java.");
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            Journeymap.getClient().disable();
        }
    }
}
