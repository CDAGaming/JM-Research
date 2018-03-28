package journeymap.client.api;

import journeymap.client.api.display.Context;
import journeymap.client.api.display.DisplayType;
import journeymap.client.api.display.Displayable;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.util.UIState;
import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public interface IClientAPI {
    public static final String API_OWNER = "journeymap";
    public static final String API_VERSION = "1.4";

    @Nullable
    UIState getUIState(final Context.UI p0);

    void subscribe(final String p0, final EnumSet<ClientEvent.Type> p1);

    void show(final Displayable p0) throws Exception;

    void remove(final Displayable p0);

    void removeAll(final String p0, final DisplayType p1);

    void removeAll(final String p0);

    boolean exists(final Displayable p0);

    boolean playerAccepts(final String p0, final DisplayType p1);

    void requestMapTile(final String p0, final int p1, final Context.MapType p2, final ChunkPos p3, final ChunkPos p4, @Nullable final Integer p5, final int p6, final boolean p7, final Consumer<BufferedImage> p8);

    void toggleDisplay(@Nullable final Integer p0, final Context.MapType p1, final Context.UI p2, final boolean p3);

    void toggleWaypoints(@Nullable final Integer p0, final Context.MapType p1, final Context.UI p2, final boolean p3);

    boolean isDisplayEnabled(@Nullable final Integer p0, final Context.MapType p1, final Context.UI p2);

    boolean isWaypointsEnabled(@Nullable final Integer p0, final Context.MapType p1, final Context.UI p2);
}
