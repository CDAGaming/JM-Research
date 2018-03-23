package journeymap.client.task.multi;

import journeymap.client.model.*;
import net.minecraft.util.math.*;
import java.io.*;
import java.util.function.*;
import java.awt.image.*;
import journeymap.client.api.display.*;
import net.minecraftforge.fml.client.*;
import journeymap.client.io.*;
import journeymap.common.*;
import journeymap.common.log.*;
import net.minecraft.client.*;

public class ApiImageTask implements Runnable
{
    final String modId;
    final int dimension;
    final MapType mapType;
    final ChunkPos startChunk;
    final ChunkPos endChunk;
    final Integer vSlice;
    final int zoom;
    final boolean showGrid;
    final File jmWorldDir;
    final Consumer<BufferedImage> callback;
    
    public ApiImageTask(final String modId, final int dimension, final Context.MapType apiMapType, final ChunkPos startChunk, final ChunkPos endChunk, final Integer vSlice, final int zoom, final boolean showGrid, final Consumer<BufferedImage> callback) {
        this.modId = modId;
        this.dimension = dimension;
        this.startChunk = startChunk;
        this.endChunk = endChunk;
        this.zoom = zoom;
        this.showGrid = showGrid;
        this.callback = callback;
        this.vSlice = vSlice;
        this.mapType = MapType.fromApiContextMapType(apiMapType, vSlice, dimension);
        this.jmWorldDir = FileHandler.getJMWorldDir(FMLClientHandler.instance().getClient());
    }
    
    @Override
    public void run() {
        BufferedImage image = null;
        try {
            final int scale = (int)Math.pow(2.0, this.zoom);
            image = RegionImageHandler.getMergedChunks(this.jmWorldDir, this.startChunk, this.endChunk, this.mapType, scale, this.showGrid);
        }
        catch (Throwable t) {
            Journeymap.getLogger().error("Error in ApiImageTask: " + t, (Object)LogFormatter.toString(t));
        }
        final BufferedImage finalImage = image;
        Minecraft.func_71410_x().func_152344_a(() -> this.callback.accept(finalImage));
    }
}
