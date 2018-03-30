package journeymap.server.feature;

import java.util.*;
import journeymap.server.*;
import journeymap.server.properties.*;
import journeymap.common.feature.*;

public class ServerFeatures extends PlayerFeatures
{
    public ServerFeatures(final UUID playerID) {
        super(playerID);
    }
    
    @Override
    protected DimensionPolicies initializePolicies(final int dimension) {
        final boolean isOp = JourneymapServer.isOp(this.id);
        return createDimensionPolicies(dimension, isOp);
    }
    
    public static DimensionPolicies createDimensionPolicies(final int dimension, final boolean isOp) {
        final DimensionPolicies dimPolicies = new DimensionPolicies(dimension);
        final DimensionProperties dimProps = PropertiesManager.getInstance().getDimProperties(dimension, isOp);
        PermissionProperties permProps;
        if (dimProps.enabled.get()) {
            permProps = dimProps;
        }
        else {
            permProps = PropertiesManager.getInstance().getGlobalProperties(isOp);
        }
        final PolicyTable otherPolicies = permProps.policies;
        if (otherPolicies != null) {
            dimPolicies.update(otherPolicies);
        }
        return dimPolicies;
    }
}
