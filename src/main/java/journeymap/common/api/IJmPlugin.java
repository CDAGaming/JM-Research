package journeymap.common.api;

import javax.annotation.*;

@ParametersAreNonnullByDefault
public interface IJmPlugin<I extends IJmAPI>
{
    String getModId();
    
    void initialize(final I p0);
}
