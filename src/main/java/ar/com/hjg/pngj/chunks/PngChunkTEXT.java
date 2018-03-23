package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;

public class PngChunkTEXT extends PngChunkTextVar
{
    public static final String ID = "tEXt";
    
    public PngChunkTEXT(final ImageInfo info) {
        super("tEXt", info);
    }
    
    public ChunkRaw createRawChunk() {
        if (this.key == null || this.key.trim().length() == 0) {
            throw new PngjException("Text chunk key must be non empty");
        }
        final byte[] b = ChunkHelper.toBytes(this.key + "\u0000" + this.val);
        final ChunkRaw chunk = this.createEmptyChunk(b.length, false);
        chunk.data = b;
        return chunk;
    }
    
    public void parseFromRaw(final ChunkRaw c) {
        int i;
        for (i = 0; i < c.data.length && c.data[i] != 0; ++i) {}
        this.key = ChunkHelper.toString(c.data, 0, i);
        ++i;
        this.val = ((i < c.data.length) ? ChunkHelper.toString(c.data, i, c.data.length - i) : "");
    }
}
