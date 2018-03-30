package journeymap.common.network;

import io.netty.buffer.*;
import net.minecraftforge.fml.common.network.*;
import journeymap.common.*;
import net.minecraftforge.fml.common.network.simpleimpl.*;
import net.minecraft.entity.player.*;

public class WorldIDPacket implements IMessage
{
    public static final String CHANNEL_NAME = "world_info";
    private String worldID;
    
    public WorldIDPacket() {
    }
    
    public WorldIDPacket(final String worldID) {
        this.worldID = worldID;
    }
    
    public String getWorldID() {
        return this.worldID;
    }
    
    public void fromBytes(final ByteBuf buf) {
        try {
            this.worldID = ByteBufUtils.readUTF8String(buf);
        }
        catch (Throwable t) {
            Journeymap.getLogger().error(String.format("Failed to read message: %s", t));
        }
    }
    
    public void toBytes(final ByteBuf buf) {
        try {
            if (this.worldID != null) {
                ByteBufUtils.writeUTF8String(buf, this.worldID);
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().error("[toBytes]Failed to read message: " + t);
        }
    }
    
    public static class WorldIdListener implements IMessageHandler<WorldIDPacket, IMessage>
    {
        public IMessage onMessage(final WorldIDPacket message, final MessageContext ctx) {
            Journeymap.getLogger().info(String.format("Got the World ID from server: %s", message.getWorldID()));
            Journeymap.proxy.handleWorldIdMessage(message.getWorldID(), null);
            return null;
        }
    }
}
