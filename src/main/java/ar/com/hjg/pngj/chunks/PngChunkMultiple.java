package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;

public abstract class PngChunkMultiple extends PngChunk
{
    protected PngChunkMultiple(final String id, final ImageInfo imgInfo) {
        super(id, imgInfo);
    }
    
    public final boolean allowsMultiple() {
        return true;
    }
}
