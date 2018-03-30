package journeymap.client.api.event;

import journeymap.client.api.util.*;
import com.google.common.base.*;

public class DisplayUpdateEvent extends ClientEvent
{
    public final UIState uiState;
    
    public DisplayUpdateEvent(final UIState uiState) {
        super(Type.DISPLAY_UPDATE, uiState.dimension);
        this.uiState = uiState;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("uiState", (Object)this.uiState).toString();
    }
}
