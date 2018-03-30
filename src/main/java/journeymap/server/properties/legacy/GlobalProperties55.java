package journeymap.server.properties.legacy;

import journeymap.common.properties.config.*;
import journeymap.common.properties.*;

@Deprecated
public class GlobalProperties55 extends PermissionProperties55
{
    public final BooleanField teleportEnabled;
    public final BooleanField useWorldId;
    
    public GlobalProperties55() {
        super("Global Server Configuration", "Applies to all dimensions unless overridden. 'WorldID is Read Only'");
        this.teleportEnabled = new BooleanField(Category.Hidden, "Enable Players to teleport", false);
        this.useWorldId = new BooleanField(Category.Hidden, "Use world id", false);
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
