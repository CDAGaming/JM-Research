package journeymap.client.api;

import journeymap.client.api.display.DisplayType;
import journeymap.client.api.display.Displayable;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.util.UIState;
import journeymap.common.api.IJmAPI;
import journeymap.common.api.feature.Feature;
import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public interface IClientAPI extends IJmAPI {
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
