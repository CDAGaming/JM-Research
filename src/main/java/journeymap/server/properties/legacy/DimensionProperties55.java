package journeymap.server.properties.legacy;

import journeymap.common.properties.config.*;
import com.google.gson.annotations.*;
import journeymap.common.properties.*;

@Deprecated
public class DimensionProperties55 extends PermissionProperties55
{
    public final BooleanField enabled;
    @Expose(deserialize = false)
    protected final Integer dimension;
    
    public DimensionProperties55(final Integer dimension) {
        super(String.format("Dimension %s Configuration", dimension), "Overrides the Global Server Configuration for this dimension - sent enable true to override global settings for this dim");
        this.enabled = new BooleanField(Category.Hidden, "Enable Configuration", false).categoryMaster(true);
        this.dimension = dimension;
    }
    
    @Override
    public String getName() {
        return "dim" + this.dimension;
    }
    
    public Integer getDimension() {
        return this.dimension;
    }
}
