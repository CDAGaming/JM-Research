package journeymap.client.service;

import journeymap.client.data.WorldData;
import journeymap.client.io.FileHandler;
import journeymap.client.io.RegionImageHandler;
import journeymap.client.model.MapType;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.Level;
import se.rupy.http.Event;
import se.rupy.http.Query;

import java.awt.image.BufferedImage;
import java.io.File;

public class TileService extends FileService {
    public static final String CALLBACK_PARAM = "callback";
    public static final String CHARACTER_ENCODING = "UTF-8";
    private static final long serialVersionUID = 4412225358529161454L;
    private byte[] blankImage;

    @Override
    public String path() {
        return "/tile";
    }

    @Override
    public void filter(final Event event) throws Event, Exception {
        final long start = System.currentTimeMillis();
        final Query query = event.query();
        query.parse();
        final Minecraft minecraft = FMLClientHandler.instance().getClient();
        final World world = minecraft.world;
        if (world == null) {
            this.throwEventException(503, "World not connected", event, false);
        }
        if (!Journeymap.getClient().isMapping()) {
            this.throwEventException(503, "JourneyMap not started", event, false);
        }
        final File worldDir = FileHandler.getJMWorldDir(minecraft);
        if (!worldDir.exists() || !worldDir.isDirectory()) {
            this.throwEventException(400, "World not found", event, true);
        }
        try {
            final int zoom = this.getParameter(query, "zoom", 0);
            final int x = this.getParameter(query, "x", 0);
            Integer vSlice = this.getParameter(query, "depth", (Integer) null);
            final int z = this.getParameter(query, "z", 0);
            final int dimension = this.getParameter(query, "dim", 0);
            final String mapTypeString = this.getParameter(query, "mapType", MapType.Name.day.name());
            MapType.Name mapTypeName = null;
            try {
                mapTypeName = MapType.Name.valueOf(mapTypeString);
            } catch (Exception e) {
                final String error = "Bad request: mapType=" + mapTypeString;
                this.throwEventException(400, error, event, true);
            }
            if (mapTypeName != MapType.Name.underground) {
                vSlice = null;
            }
            if (mapTypeName == MapType.Name.underground && WorldData.isHardcoreAndMultiplayer()) {
                ResponseHeader.on(event).contentType(ContentType.png).noCache();
                this.serveFile(RegionImageHandler.getBlank512x512ImageFile(), event);
            } else {
                final int scale = (int) Math.pow(2.0, zoom);
                final int distance = 32 / scale;
                final int minChunkX = x * distance;
                final int minChunkZ = z * distance;
                final int maxChunkX = minChunkX + distance - 1;
                final int maxChunkZ = minChunkZ + distance - 1;
                final ChunkPos startCoord = new ChunkPos(minChunkX, minChunkZ);
                final ChunkPos endCoord = new ChunkPos(maxChunkX, maxChunkZ);
                final boolean showGrid = Journeymap.getClient().getWebMapProperties().showGrid.get();
                final MapType mapType = new MapType(mapTypeName, vSlice, dimension);
                final BufferedImage img = RegionImageHandler.getMergedChunks(worldDir, startCoord, endCoord, mapType, true, null, 512, 512, false, showGrid);
                ResponseHeader.on(event).contentType(ContentType.png).noCache();
                this.serveImage(event, img);
            }
            final long stop = System.currentTimeMillis();
            if (Journeymap.getLogger().isEnabled(Level.DEBUG)) {
                Journeymap.getLogger().debug(stop - start + "ms to serve tile");
            }
        } catch (NumberFormatException e2) {
            this.reportMalformedRequest(event);
        }
    }
}
