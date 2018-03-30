package journeymap.client.ui.waypoint;

import net.minecraft.client.gui.*;
import journeymap.client.api.display.*;
import journeymap.client.waypoint.*;
import com.google.gson.*;

public class WaypointChat extends GuiChat
{
    public static final Gson GSON;
    
    public WaypointChat(final Waypoint waypoint) {
        this(WaypointChatParser.toChatString(waypoint));
    }
    
    public WaypointChat(final String text) {
        super(text);
    }
    
    public void func_73866_w_() {
        super.func_73866_w_();
        this.field_146415_a.func_146196_d();
    }
    
    public String toString() {
        return WaypointChat.GSON.toJson((Object)this);
    }
    
    static {
        GSON = new GsonBuilder().create();
    }
}
