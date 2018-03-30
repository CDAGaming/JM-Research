package journeymap.server.api;

import journeymap.common.api.*;
import javax.annotation.*;
import net.minecraft.world.*;
import java.util.*;
import journeymap.common.api.feature.*;

@ParametersAreNonnullByDefault
public interface IServerAPI extends IJmAPI
{
    void setPlayerFeatures(final String p0, final UUID p1, final int p2, final GameType p3, final Map<Feature, Boolean> p4);
    
    Map<Feature, Boolean> getPlayerFeatures(final UUID p0, final int p1, final GameType p2);
    
    Map<Feature, Boolean> getServerFeatures(final int p0, final GameType p1);
}
