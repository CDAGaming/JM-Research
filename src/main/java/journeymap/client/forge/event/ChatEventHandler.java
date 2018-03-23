package journeymap.client.forge.event;

import net.minecraftforge.fml.relauncher.*;
import net.minecraftforge.client.event.*;
import com.google.common.base.*;
import journeymap.client.waypoint.*;
import journeymap.common.*;
import journeymap.common.log.*;
import net.minecraftforge.fml.common.eventhandler.*;

@SideOnly(Side.CLIENT)
public class ChatEventHandler implements EventHandlerManager.EventHandler
{
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void invoke(final ClientChatReceivedEvent event) {
        if (event.getMessage() != null) {
            try {
                final String text = event.getMessage().getFormattedText();
                if (!Strings.isNullOrEmpty(text)) {
                    WaypointParser.parseChatForWaypoints(event, text);
                }
            }
            catch (Exception e) {
                Journeymap.getLogger().warn("Unexpected exception on ClientChatReceivedEvent: " + LogFormatter.toString(e));
            }
        }
    }
}
