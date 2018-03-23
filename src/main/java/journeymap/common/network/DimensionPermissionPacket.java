package journeymap.common.network;

import io.netty.buffer.*;
import net.minecraftforge.fml.common.network.*;
import journeymap.common.*;
import net.minecraftforge.fml.common.network.simpleimpl.*;
import journeymap.server.properties.*;
import journeymap.client.feature.*;

public class DimensionPermissionPacket implements IMessage
{
    public static final String CHANNEL_NAME = "jm_dim_permission";
    private String prop;
    
    public DimensionPermissionPacket() {
    }
    
    public DimensionPermissionPacket(final PermissionProperties prop) {
        this.prop = prop.toJsonString(false);
    }
    
    public String getProp() {
        return this.prop;
    }
    
    public void fromBytes(final ByteBuf buf) {
        try {
            this.prop = ByteBufUtils.readUTF8String(buf);
        }
        catch (Throwable t) {
            Journeymap.getLogger().error(String.format("Failed to read message: %s", t));
        }
    }
    
    public void toBytes(final ByteBuf buf) {
        try {
            if (this.prop != null) {
                ByteBufUtils.writeUTF8String(buf, this.prop);
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().error("[toBytes]Failed to read message: " + t);
        }
    }
    
    public static class Listener implements IMessageHandler<DimensionPermissionPacket, IMessage>
    {
        public IMessage onMessage(final DimensionPermissionPacket message, final MessageContext ctx) {
            final PermissionProperties prop = new DimensionProperties(0).load(message.getProp(), false);
            FeatureManager.INSTANCE.updateDimensionFeatures(prop);
            Journeymap.getClient().setServerEnabled(true);
            return null;
        }
    }
}
