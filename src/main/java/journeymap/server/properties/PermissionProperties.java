package journeymap.server.properties;

import journeymap.common.feature.*;
import journeymap.common.properties.*;

public abstract class PermissionProperties extends ServerPropertiesBase
{
    final transient boolean isOp;
    public final PolicyTable policies;
    
    protected PermissionProperties(final String displayName, final String description, final boolean isOp) {
        super(displayName, description);
        this.isOp = isOp;
        this.policies = new PolicyTable();
    }
    
    public boolean isOp() {
        return this.isOp;
    }
    
    @Override
    public <T extends PropertiesBase> void updateFrom(final T otherInstance) {
        super.updateFrom(otherInstance);
        if (otherInstance instanceof PermissionProperties) {
            final PolicyTable otherTable = ((PermissionProperties)otherInstance).policies;
            if (otherTable != null) {
                this.policies.update(otherTable);
            }
        }
    }
    
    public void copyFrom(final PermissionProperties other) {
        final PolicyTable otherTable = other.policies;
        if (otherTable != null) {
            otherTable.inflate();
            this.policies.update(otherTable);
        }
    }
    
    @Override
    protected void postLoad(final boolean isNew) {
        super.postLoad(isNew);
        this.policies.inflate();
    }
    
    public abstract String getOriginKey();
}
