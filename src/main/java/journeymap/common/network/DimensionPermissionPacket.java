package journeymap.common.network;

import io.netty.buffer.ByteBuf;
import journeymap.client.feature.FeatureManager;
import journeymap.common.Journeymap;
import journeymap.server.properties.DimensionProperties;
import journeymap.server.properties.PermissionProperties;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class DimensionPermissionPacket implements IMessage {
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
        } catch (Throwable t) {
            Journeymap.getLogger().error(String.format("Failed to read message: %s", t));
        }
    }

    public void toBytes(final ByteBuf buf) {
        try {
            if (this.prop != null) {
                ByteBufUtils.writeUTF8String(buf, this.prop);
            }
        } catch (Throwable t) {
            Journeymap.getLogger().error("[toBytes]Failed to read message: " + t);
        }
    }

    public static class Listener implements IMessageHandler<DimensionPermissionPacket, IMessage> {
        public IMessage onMessage(final DimensionPermissionPacket message, final MessageContext ctx) {
            final PermissionProperties prop = new DimensionProperties(0).load(message.getProp(), false);
            FeatureManager.INSTANCE.updateDimensionFeatures(prop);
            Journeymap.getClient().setServerEnabled(true);
            return null;
        }
    }
}
