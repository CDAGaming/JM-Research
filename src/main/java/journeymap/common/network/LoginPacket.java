package journeymap.common.network;

import io.netty.buffer.ByteBuf;
import journeymap.common.Journeymap;
import journeymap.common.network.model.InitLogin;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class LoginPacket implements IMessage {
    public static final String CHANNEL_NAME = "jm_init_login";
    private String packet;

    public LoginPacket() {
    }

    public LoginPacket(final InitLogin packet) {
        this.packet = InitLogin.GSON.toJson(packet);
    }

    public String getPacket() {
        return this.packet;
    }

    public void fromBytes(final ByteBuf buf) {
        try {
            this.packet = ByteBufUtils.readUTF8String(buf);
        } catch (Throwable t) {
            Journeymap.getLogger().error(String.format("Failed to read message: %s", t));
        }
    }

    public void toBytes(final ByteBuf buf) {
        try {
            if (this.packet != null) {
                ByteBufUtils.writeUTF8String(buf, this.packet);
            }
        } catch (Throwable t) {
            Journeymap.getLogger().error("[toBytes]Failed to read message: " + t);
        }
    }

    public static class Listener implements IMessageHandler<LoginPacket, IMessage> {
        public IMessage onMessage(final LoginPacket message, final MessageContext ctx) {
            Journeymap.getLogger().info("Login Packet received");
            final InitLogin packet = (InitLogin) InitLogin.GSON.fromJson(message.getPacket(), InitLogin.class);
            Journeymap.getClient().setServerTeleportEnabled(packet.isTeleportEnabled());
            Journeymap.getClient().setServerEnabled(true);
            return null;
        }
    }
}
