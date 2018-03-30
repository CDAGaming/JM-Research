package journeymap.common.network;

import java.nio.charset.*;
import net.minecraftforge.fml.common.network.*;
import journeymap.common.*;
import io.netty.buffer.*;
import net.minecraftforge.fml.common.network.simpleimpl.*;
import journeymap.client.feature.*;
import journeymap.common.feature.*;
import com.mojang.authlib.*;

public class DimensionPoliciesPacket implements IMessage
{
    public static final String CHANNEL_NAME = "jm_dim_policy";
    private String json;
    
    public DimensionPoliciesPacket() {
    }
    
    public DimensionPoliciesPacket(final DimensionPolicies dimPolicies, final boolean compact) {
        this.json = (compact ? dimPolicies.toCompactJson() : dimPolicies.toJson());
        if (!compact) {
            final int length = this.json.getBytes(StandardCharsets.UTF_8).length;
            if (ByteBufUtils.varIntByteCount(length) > 2) {
                Journeymap.getLogger().warn("DimensionPoliciesPacket json too large for verbose format: " + length);
                this.json = dimPolicies.toCompactJson();
            }
        }
    }
    
    public String getJson() {
        return this.json;
    }
    
    public void fromBytes(final ByteBuf buf) {
        try {
            this.json = ByteBufUtils.readUTF8String(buf);
        }
        catch (Throwable t) {
            Journeymap.getLogger().error(String.format("Failed to read message: %s", t));
        }
    }
    
    public void toBytes(final ByteBuf buf) {
        try {
            if (this.json != null) {
                ByteBufUtils.writeUTF8String(buf, this.json);
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().error("[toBytes] Failed to read message: " + t);
        }
    }
    
    public static class Listener implements IMessageHandler<DimensionPoliciesPacket, IMessage>
    {
        public IMessage onMessage(final DimensionPoliciesPacket message, final MessageContext ctx) {
            try {
                if (message.getJson() == null) {
                    Journeymap.getLogger().error("DimensionPoliciesPacket had no contents");
                    return null;
                }
                Journeymap.getClient().setServerEnabled(true);
                final GameProfile gameProfile = ctx.getClientHandler().func_175105_e();
                final String name = gameProfile.getName();
                final DimensionPolicies newPolicies = DimensionPolicies.fromJson(message.getJson());
                final DimensionPolicies oldPolicies = ClientFeatures.instance().get(newPolicies.getDimension());
                final String info = String.format("Server features set for %s in dimension %s", name, newPolicies.getDimension());
                ClientFeatures.instance().logDeltas(info, oldPolicies, newPolicies);
                oldPolicies.update(newPolicies);
                return null;
            }
            catch (Throwable t) {
                Journeymap.getLogger().error("[DimensionPoliciesPacket.Listener.onMessage] " + t, t);
                return null;
            }
        }
    }
}
