package journeymap.server.api;

import javax.annotation.*;
import journeymap.common.api.*;

@ParametersAreNonnullByDefault
public interface IServerPlugin extends IJmPlugin<IServerAPI>
{
    void initialize(final IServerAPI p0);
    
    String getModId();
}
