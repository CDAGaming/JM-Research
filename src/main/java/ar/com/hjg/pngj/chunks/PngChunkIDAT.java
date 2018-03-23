package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;

public class PngChunkIDAT extends PngChunkMultiple
{
    public static final String ID = "IDAT";
    
    public PngChunkIDAT(final ImageInfo i) {
        super("IDAT", i);
    }
    
    public ChunkOrderingConstraint getOrderingConstraint() {
        return ChunkOrderingConstraint.NA;
    }
    
    public ChunkRaw createRawChunk() {
        return null;
    }
    
    public void parseFromRaw(final ChunkRaw c) {
    }
}
