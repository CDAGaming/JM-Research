package journeymap.client.render.draw;

import journeymap.client.render.map.*;

public interface DrawStep
{
    void draw(final Pass p0, final double p1, final double p2, final GridRenderer p3, final double p4, final double p5);
    
    int getDisplayOrder();
    
    String getModId();
    
    public enum Pass
    {
        Object, 
        Text, 
        Tooltip;
    }
}
