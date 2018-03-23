package journeymap.server.properties;

import journeymap.common.properties.config.*;

public class DimensionProperties extends PermissionProperties
{
    public final BooleanField enabled;
    protected final Integer dimension;
    
    public DimensionProperties(final Integer dimension) {
        super(String.format("Dimension %s Configuration", dimension), "Overrides the Global Server Configuration for this dimension - sent enable true to override global settings for this dim");
        this.enabled = new BooleanField(ServerCategory.General, "Enable Configuration", false).categoryMaster(true);
        this.dimension = dimension;
    }
    
    @Override
    public String getName() {
        return "dim" + this.dimension;
    }
    
    public Integer getDimension() {
        return this.dimension;
    }
    
    public DimensionProperties build() {
        final GlobalProperties gProp = PropertiesManager.getInstance().getGlobalProperties();
        this.opCaveMappingEnabled.set(gProp.opCaveMappingEnabled.get());
        this.caveMappingEnabled.set(gProp.caveMappingEnabled.get());
        this.opSurfaceMappingEnabled.set(gProp.opSurfaceMappingEnabled.get());
        this.surfaceMappingEnabled.set(gProp.surfaceMappingEnabled.get());
        this.opTopoMappingEnabled.set(gProp.opTopoMappingEnabled.get());
        this.topoMappingEnabled.set(gProp.topoMappingEnabled.get());
        this.opRadarEnabled.set(gProp.opRadarEnabled.get());
        this.radarEnabled.set(gProp.radarEnabled.get());
        this.playerRadarEnabled.set(gProp.playerRadarEnabled.get());
        this.villagerRadarEnabled.set(gProp.villagerRadarEnabled.get());
        this.animalRadarEnabled.set(gProp.animalRadarEnabled.get());
        this.mobRadarEnabled.set(gProp.mobRadarEnabled.get());
        this.save();
        return this;
    }
}
