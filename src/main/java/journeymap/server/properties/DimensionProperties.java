package journeymap.server.properties;

import journeymap.common.properties.config.*;

public class DimensionProperties extends PermissionProperties
{
    protected final transient int dimension;
    public final BooleanField enabled;
    
    public DimensionProperties(final int dimension, final boolean isOp) {
        super(String.format("Dimension %s %s Configuration", dimension, isOp ? "Op" : "Player"), "Set \"enabled\": \"true\" to override the Global Server Configuration for this dimension", isOp);
        this.enabled = new BooleanField(ServerCategory.General, "Enable Configuration", false).categoryMaster(true);
        this.dimension = dimension;
    }
    
    @Override
    public String getName() {
        final String target = this.isOp ? "op" : "player";
        return String.format("%s.dim%s", target, this.dimension);
    }
    
    public int getDimension() {
        return this.dimension;
    }
    
    public DimensionProperties build() {
        return this.build(PropertiesManager.getInstance().getGlobalProperties(this.isOp));
    }
    
    DimensionProperties build(final GlobalProperties gProp) {
        gProp.policies.inflate();
        this.copyFrom(gProp);
        this.save();
        return this;
    }
    
    @Override
    public String getOriginKey() {
        return this.isOp ? "jm.common.server_config_dim" : "jm.common.server_config_op_dim";
    }
}
