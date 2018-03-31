package journeymap.client.forge.event;

import com.google.common.base.Strings;
import journeymap.client.waypoint.WaypointChatParser;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ChatEventHandler implements EventHandlerManager.EventHandler {
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void invoke(final ClientChatReceivedEvent event) {
        final ITextComponent message = event.getMessage();
        if (message != null) {
            try {
                if (message instanceof TextComponentTranslation) {
                    final EntityPlayerSP player = Journeymap.clientPlayer();
                    if (player != null && "gameMode.changed".equals(((TextComponentTranslation) message).getKey())) {
                        return;
                    }
                }
                final String text = event.getMessage().getFormattedText();
                if (!Strings.isNullOrEmpty(text)) {
                    WaypointChatParser.parseChatForWaypoints(event, text);
                }
            } catch (Exception e) {
                Journeymap.getLogger().warn("Unexpected exception on ClientChatReceivedEvent: " + LogFormatter.toString(e));
            }
        }
    }
}
