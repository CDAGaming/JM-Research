package journeymap.common.network;

import journeymap.common.network.model.*;
import io.netty.buffer.*;
import net.minecraftforge.fml.common.network.*;
import journeymap.common.*;
import net.minecraftforge.fml.common.network.simpleimpl.*;
import net.minecraft.entity.*;

public class TeleportPacket implements IMessage
{
    public static final String CHANNEL_NAME = "jtp";
    private String location;
    
    public TeleportPacket() {
    }
    
    public TeleportPacket(final Location location) {
        this.location = Location.GSON.toJson((Object)location);
    }
    
    public String getLocation() {
        return this.location;
    }
    
    public void fromBytes(final ByteBuf buf) {
        try {
            this.location = ByteBufUtils.readUTF8String(buf);
        }
        catch (Throwable t) {
            Journeymap.getLogger().error(String.format("Failed to read message: %s", t));
        }
    }
    
    public void toBytes(final ByteBuf buf) {
        try {
            if (this.location != null) {
                ByteBufUtils.writeUTF8String(buf, this.location);
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().error("[toBytes]Failed to read message: " + t);
        }
    }
    
    public static class Listener implements IMessageHandler<TeleportPacket, IMessage>
    {
        public IMessage onMessage(final TeleportPacket message, final MessageContext ctx) {
            Entity player = null;
            player = (Entity)ctx.getServerHandler().field_147369_b;
            final Location location = (Location)Location.GSON.fromJson(message.getLocation(), (Class)Location.class);
            return null;
        }
    }
}
