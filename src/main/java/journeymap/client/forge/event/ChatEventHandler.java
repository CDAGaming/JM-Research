package journeymap.client.forge.event;

import net.minecraftforge.fml.relauncher.*;
import net.minecraftforge.client.event.*;
import journeymap.common.*;
import com.google.common.base.*;
import journeymap.client.waypoint.*;
import journeymap.common.log.*;
import net.minecraft.util.text.*;
import net.minecraft.client.entity.*;
import net.minecraftforge.fml.common.eventhandler.*;

@SideOnly(Side.CLIENT)
public class ChatEventHandler implements EventHandlerManager.EventHandler
{
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void invoke(final ClientChatReceivedEvent event) {
        final ITextComponent message = event.getMessage();
        if (message != null) {
            try {
                if (message instanceof TextComponentTranslation) {
                    final EntityPlayerSP player = Journeymap.clientPlayer();
                    if (player != null && "gameMode.changed".equals(((TextComponentTranslation)message).func_150268_i())) {
                        return;
                    }
                }
                final String text = event.getMessage().func_150254_d();
                if (!Strings.isNullOrEmpty(text)) {
                    WaypointChatParser.parseChatForWaypoints(event, text);
                }
            }
            catch (Exception e) {
                Journeymap.getLogger().warn("Unexpected exception on ClientChatReceivedEvent: " + LogFormatter.toString(e));
            }
        }
    }
}
