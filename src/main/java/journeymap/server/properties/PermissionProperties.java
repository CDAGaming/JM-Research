package journeymap.server.properties;

import journeymap.common.properties.config.*;

public abstract class PermissionProperties extends ServerPropertiesBase
{
    public final BooleanField opSurfaceMappingEnabled;
    public final BooleanField surfaceMappingEnabled;
    public final BooleanField opTopoMappingEnabled;
    public final BooleanField topoMappingEnabled;
    public final BooleanField opCaveMappingEnabled;
    public final BooleanField caveMappingEnabled;
    public final BooleanField opRadarEnabled;
    public final BooleanField radarEnabled;
    public final BooleanField playerRadarEnabled;
    public final BooleanField villagerRadarEnabled;
    public final BooleanField animalRadarEnabled;
    public final BooleanField mobRadarEnabled;
    
    protected PermissionProperties(final String displayName, final String description) {
        super(displayName, description);
        this.opSurfaceMappingEnabled = new BooleanField(ServerCategory.Surface, "Enable Op surface maps", true);
        this.surfaceMappingEnabled = new BooleanField(ServerCategory.Surface, "Enable surface maps", true);
        this.opTopoMappingEnabled = new BooleanField(ServerCategory.Topo, "Enable Op topo maps", true);
        this.topoMappingEnabled = new BooleanField(ServerCategory.Topo, "Enable topo maps", true);
        this.opCaveMappingEnabled = new BooleanField(ServerCategory.Cave, "Enable Op cave maps", true);
        this.caveMappingEnabled = new BooleanField(ServerCategory.Cave, "Enable cave maps", true);
        this.opRadarEnabled = new BooleanField(ServerCategory.Radar, "Enable Op radar", true);
        this.radarEnabled = new BooleanField(ServerCategory.Radar, "Enable radar", true);
        this.playerRadarEnabled = new BooleanField(ServerCategory.Radar, "Enable player radar", true);
        this.villagerRadarEnabled = new BooleanField(ServerCategory.Radar, "Enable villager radar", true);
        this.animalRadarEnabled = new BooleanField(ServerCategory.Radar, "Enable animal radar", true);
        this.mobRadarEnabled = new BooleanField(ServerCategory.Radar, "Enable mob radar", true);
    }
}
