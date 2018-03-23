package journeymap.client.ui.minimap;

import journeymap.client.ui.option.*;
import journeymap.client.*;

public enum Position implements KeyedEnum
{
    TopRight("jm.minimap.position_topright"), 
    BottomRight("jm.minimap.position_bottomright"), 
    BottomLeft("jm.minimap.position_bottomleft"), 
    TopLeft("jm.minimap.position_topleft"), 
    TopCenter("jm.minimap.position_topcenter"), 
    Center("jm.minimap.position_center");
    
    public final String key;
    
    private Position(final String key) {
        this.key = key;
    }
    
    @Override
    public String getKey() {
        return this.key;
    }
    
    @Override
    public String toString() {
        return Constants.getString(this.key);
    }
}
