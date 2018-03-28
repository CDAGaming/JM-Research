package journeymap.client.network;

import journeymap.common.Journeymap;
import journeymap.common.network.WorldIDPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Deprecated
public class WorldInfoHandler {
    public static final int PACKET_WORLDID = 0;
    public static final int MIN_DELAY_MS = 1000;
    private static long lastRequest;
    private static long lastResponse;
    private static SimpleNetworkWrapper channel;
    Minecraft mc;

    public WorldInfoHandler() {
        this.mc = FMLClientHandler.instance().getClient();
        try {
            WorldInfoHandler.channel = NetworkRegistry.INSTANCE.newSimpleChannel("world_info");
            if (WorldInfoHandler.channel != null) {
                WorldInfoHandler.channel.registerMessage((Class) WorldIdListener.class, (Class) WorldIDPacket.class, 0, Side.CLIENT);
                Journeymap.getLogger().info(String.format("Registered channel: %s", "world_info"));
                MinecraftForge.EVENT_BUS.register((Object) this);
            }
        } catch (Throwable t) {
            Journeymap.getLogger().error(String.format("Failed to register channel %s: %s", "world_info", t));
        }
    }

    public static void requestWorldID() {
        if (WorldInfoHandler.channel != null) {
            final long now = System.currentTimeMillis();
            if (WorldInfoHandler.lastRequest + 1000L < now && WorldInfoHandler.lastResponse + 1000L < now) {
                Journeymap.getLogger().info("Requesting World ID");
                WorldInfoHandler.channel.sendToServer((IMessage) new WorldIDPacket());
                WorldInfoHandler.lastRequest = System.currentTimeMillis();
            }
        }
    }

    @SubscribeEvent
    public void onConnected(final FMLNetworkEvent.ClientConnectedToServerEvent event) {
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void on(final EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayerMP && !this.mc.isSingleplayer() && this.mc.player != null && !this.mc.player.isDead && event.getEntity().getName().equals(this.mc.player.getName())) {
            requestWorldID();
        }
    }

    public static class WorldIdListener implements IMessageHandler<WorldIDPacket, IMessage> {
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(final WorldIDPacket message, final MessageContext ctx) {
            WorldInfoHandler.lastResponse = System.currentTimeMillis();
            Journeymap.getLogger().info(String.format("Got the World ID from server: %s", message.getWorldID()));
            Journeymap.proxy.handleWorldIdMessage(message.getWorldID(), null);
            return null;
        }
    }
}
