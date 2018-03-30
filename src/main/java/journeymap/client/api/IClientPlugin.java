package journeymap.client.api;

import javax.annotation.*;
import journeymap.client.api.event.*;
import journeymap.common.api.*;

@ParametersAreNonnullByDefault
public interface IClientPlugin extends IJmPlugin<IClientAPI>
{
    void initialize(final IClientAPI p0);
    
    String getModId();
    
    void onEvent(final ClientEvent p0);
}
