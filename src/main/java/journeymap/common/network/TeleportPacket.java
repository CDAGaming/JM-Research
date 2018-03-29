package journeymap.common.network;

import io.netty.buffer.ByteBuf;
import journeymap.common.Journeymap;
import journeymap.common.network.model.Location;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TeleportPacket implements IMessage {
    public static final String CHANNEL_NAME = "jtp";
    private String location;

    public TeleportPacket() {
    }

    public TeleportPacket(final Location location) {
        this.location = Location.GSON.toJson(location);
    }

    public String getLocation() {
        return this.location;
    }

    public void fromBytes(final ByteBuf buf) {
        try {
            this.location = ByteBufUtils.readUTF8String(buf);
        } catch (Throwable t) {
            Journeymap.getLogger().error(String.format("Failed to read message: %s", t));
        }
    }

    public void toBytes(final ByteBuf buf) {
        try {
            if (this.location != null) {
                ByteBufUtils.writeUTF8String(buf, this.location);
            }
        } catch (Throwable t) {
            Journeymap.getLogger().error("[toBytes]Failed to read message: " + t);
        }
    }

    public static class Listener implements IMessageHandler<TeleportPacket, IMessage> {
        public IMessage onMessage(final TeleportPacket message, final MessageContext ctx) {
            Entity player = null;
            player = ctx.getServerHandler().player;
            final Location location = Location.GSON.fromJson(message.getLocation(), Location.class);
            return null;
        }
    }
}
