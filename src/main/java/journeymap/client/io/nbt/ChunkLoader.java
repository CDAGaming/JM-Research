package journeymap.client.io.nbt;

import org.apache.logging.log4j.*;
import net.minecraft.world.chunk.storage.*;
import net.minecraft.client.*;
import net.minecraft.util.math.*;
import journeymap.client.model.*;
import journeymap.common.*;
import java.io.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;

public class ChunkLoader
{
    private static Logger logger;
    
    public static ChunkMD getChunkMD(final AnvilChunkLoader loader, final Minecraft mc, final ChunkPos coord, final boolean forceRetain) {
        try {
            if (RegionLoader.getRegionFile(mc, coord.field_77276_a, coord.field_77275_b).exists()) {
                final World world = Journeymap.clientWorld();
                if (loader.chunkExists(world, coord.field_77276_a, coord.field_77275_b)) {
                    final Chunk chunk = loader.func_75815_a(world, coord.field_77276_a, coord.field_77275_b);
                    if (chunk != null) {
                        if (!chunk.func_177410_o()) {
                            chunk.func_177417_c(true);
                        }
                        return new ChunkMD(chunk, forceRetain);
                    }
                    ChunkLoader.logger.warn("AnvilChunkLoader returned null for chunk: " + coord);
                }
            }
            else {
                ChunkLoader.logger.warn("Region doesn't exist for chunk: " + coord);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static ChunkMD getChunkMdFromMemory(final World world, final int chunkX, final int chunkZ) {
        if (world != null) {
            final IChunkProvider provider = world.func_72863_F();
            if (provider != null) {
                final Chunk theChunk = provider.func_186026_b(chunkX, chunkZ);
                if (theChunk != null && theChunk.func_177410_o() && !(theChunk instanceof EmptyChunk)) {
                    return new ChunkMD(theChunk);
                }
            }
        }
        return null;
    }
    
    static {
        ChunkLoader.logger = Journeymap.getLogger();
    }
}
