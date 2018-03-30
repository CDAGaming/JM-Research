package journeymap.server.properties;

import journeymap.common.properties.config.*;

public class GlobalProperties extends PermissionProperties
{
    public final BooleanField useWorldId;
    
    public GlobalProperties(final boolean isOp) {
        super(String.format("Global Server %s Configuration", isOp ? "Op" : "Player"), "Applies to all dimensions unless overridden.", isOp);
        this.useWorldId = new BooleanField(ServerCategory.General, "Use WorldID", false);
    }
    
    @Override
    public String getName() {
        final String target = this.isOp ? "op" : "player";
        return String.format("%s.global", target);
    }
    
    @Override
    public String getOriginKey() {
        return this.isOp ? "jm.common.server_config_global" : "jm.common.server_config_op_global";
    }
}
