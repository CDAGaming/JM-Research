package journeymap.client.ui.minimap;

import journeymap.client.ui.option.*;
import journeymap.client.*;

public enum Orientation implements KeyedEnum
{
    North("jm.minimap.orientation.north"), 
    OldNorth("jm.minimap.orientation.oldnorth"), 
    PlayerHeading("jm.minimap.orientation.playerheading");
    
    public final String key;
    
    private Orientation(final String key) {
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
