package journeymap.client.ui.minimap;

import journeymap.client.ui.option.*;
import journeymap.client.*;

public enum Shape implements KeyedEnum
{
    Square("jm.minimap.shape_square"), 
    Rectangle("jm.minimap.shape_rectangle"), 
    Circle("jm.minimap.shape_circle");
    
    public final String key;
    
    private Shape(final String key) {
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
