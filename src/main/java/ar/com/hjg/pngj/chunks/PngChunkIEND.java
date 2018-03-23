package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;

public class PngChunkIEND extends PngChunkSingle
{
    public static final String ID = "IEND";
    
    public PngChunkIEND(final ImageInfo info) {
        super("IEND", info);
    }
    
    public ChunkOrderingConstraint getOrderingConstraint() {
        return ChunkOrderingConstraint.NA;
    }
    
    public ChunkRaw createRawChunk() {
        final ChunkRaw c = new ChunkRaw(0, ChunkHelper.b_IEND, false);
        return c;
    }
    
    public void parseFromRaw(final ChunkRaw c) {
    }
}
