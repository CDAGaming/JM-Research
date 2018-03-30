package journeymap.server.properties.legacy;

import journeymap.server.properties.*;
import journeymap.common.properties.config.*;
import journeymap.common.properties.*;
import java.io.*;
import com.google.common.base.*;
import journeymap.server.*;

@Deprecated
public abstract class PermissionProperties55 extends ServerPropertiesBase
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
    
    protected PermissionProperties55(final String displayName, final String description) {
        super(displayName, description);
        this.opSurfaceMappingEnabled = new BooleanField(Category.Hidden, "Enable Op surface maps", true);
        this.surfaceMappingEnabled = new BooleanField(Category.Hidden, "Enable surface maps", true);
        this.opTopoMappingEnabled = new BooleanField(Category.Hidden, "Enable Op topo maps", true);
        this.topoMappingEnabled = new BooleanField(Category.Hidden, "Enable topo maps", true);
        this.opCaveMappingEnabled = new BooleanField(Category.Hidden, "Enable Op cave maps", true);
        this.caveMappingEnabled = new BooleanField(Category.Hidden, "Enable cave maps", true);
        this.opRadarEnabled = new BooleanField(Category.Hidden, "Enable Op radar", true);
        this.radarEnabled = new BooleanField(Category.Hidden, "Enable radar", true);
        this.playerRadarEnabled = new BooleanField(Category.Hidden, "Enable player radar", true);
        this.villagerRadarEnabled = new BooleanField(Category.Hidden, "Enable villager radar", true);
        this.animalRadarEnabled = new BooleanField(Category.Hidden, "Enable animal radar", true);
        this.mobRadarEnabled = new BooleanField(Category.Hidden, "Enable mob radar", true);
    }
    
    @Override
    public File getFile() {
        if (this.sourceFile == null) {
            final String path55 = Joiner.on(File.separator).join((Object)Constants.MC_DATA_DIR, (Object)Constants.JOURNEYMAP_DIR, new Object[] { "config", "5.5" });
            this.sourceFile = new File(path55, this.getFileName());
        }
        return this.sourceFile;
    }
}
