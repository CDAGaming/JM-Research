package journeymap.client.ui.waypoint;

import journeymap.client.model.Waypoint;
import net.minecraft.client.gui.GuiChat;

public class WaypointChat extends GuiChat {
    public WaypointChat(final Waypoint waypoint) {
        this(waypoint.toChatString());
    }

    public WaypointChat(final String text) {
        super(text);
    }

    public void initGui() {
        super.initGui();
        this.inputField.setCursorPositionZero();
    }
}
