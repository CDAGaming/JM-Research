package journeymap.client.api;

import javax.annotation.*;
import journeymap.client.api.event.*;

@ParametersAreNonnullByDefault
public interface IClientPlugin
{
    void initialize(final IClientAPI p0);
    
    String getModId();
    
    void onEvent(final ClientEvent p0);
}
