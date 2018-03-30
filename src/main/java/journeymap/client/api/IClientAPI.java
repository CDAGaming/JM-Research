package journeymap.client.api;

import journeymap.common.api.*;
import journeymap.common.api.feature.*;
import journeymap.client.api.util.*;
import javax.annotation.*;
import java.util.*;
import journeymap.client.api.event.*;
import journeymap.client.api.display.*;
import net.minecraft.util.math.*;
import java.util.function.*;
import java.awt.image.*;

@ParametersAreNonnullByDefault
public interface IClientAPI extends IJmAPI
{
    public static final String API_OWNER = "journeymap";
    public static final String API_VERSION = "2.0-SNAPSHOT";
    
    @Nullable
    UIState getUIState(final Feature.Display p0);
    
    void subscribe(final String p0, final EnumSet<ClientEvent.Type> p1);
    
    void show(final Displayable p0) throws Exception;
    
    void remove(final Displayable p0);
    
    void removeAll(final String p0, final DisplayType p1);
    
    void removeAll(final String p0);
    
    boolean exists(final Displayable p0);
    
    boolean playerAccepts(final String p0, final DisplayType p1);
    
    void requestMapTile(final String p0, final int p1, final Feature.MapType p2, final ChunkPos p3, final ChunkPos p4, @Nullable final Integer p5, final int p6, final boolean p7, final Consumer<BufferedImage> p8);
    
    boolean isDisplayEnabled(final int p0, final Feature.Display p1);
    
    boolean isMapTypeEnabled(final int p0, final Feature.MapType p1);
    
    boolean isRadarEnabled(final int p0, final Feature.Radar p1);
}
