package journeymap.server.properties;

import journeymap.common.properties.config.*;

public class GlobalProperties extends PermissionProperties
{
    public final BooleanField teleportEnabled;
    public final BooleanField useWorldId;
    
    public GlobalProperties() {
        super("Global Server Configuration", "Applies to all dimensions unless overridden. 'WorldID is Read Only'");
        this.teleportEnabled = new BooleanField(ServerCategory.General, "Enable Players to teleport", false);
        this.useWorldId = new BooleanField(ServerCategory.General, "Use world id", false);
    }
    
    @Override
    public String getName() {
        return "global";
    }
    
    @Override
    protected void postLoad(final boolean isNew) {
        super.postLoad(isNew);
    }
    
    @Override
    protected void preSave() {
        super.preSave();
    }
}
