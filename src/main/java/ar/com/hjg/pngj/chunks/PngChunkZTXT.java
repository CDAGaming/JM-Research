package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;
import java.io.*;

public class PngChunkZTXT extends PngChunkTextVar
{
    public static final String ID = "zTXt";
    
    public PngChunkZTXT(final ImageInfo info) {
        super("zTXt", info);
    }
    
    public ChunkRaw createRawChunk() {
        if (this.key == null || this.key.trim().length() == 0) {
            throw new PngjException("Text chunk key must be non empty");
        }
        try {
            final ByteArrayOutputStream ba = new ByteArrayOutputStream();
            ba.write(ChunkHelper.toBytes(this.key));
            ba.write(0);
            ba.write(0);
            final byte[] textbytes = ChunkHelper.compressBytes(ChunkHelper.toBytes(this.val), true);
            ba.write(textbytes);
            final byte[] b = ba.toByteArray();
            final ChunkRaw chunk = this.createEmptyChunk(b.length, false);
            chunk.data = b;
            return chunk;
        }
        catch (IOException e) {
            throw new PngjException(e);
        }
    }
    
    public void parseFromRaw(final ChunkRaw c) {
        int nullsep = -1;
        for (int i = 0; i < c.data.length; ++i) {
            if (c.data[i] == 0) {
                nullsep = i;
                break;
            }
        }
        if (nullsep < 0 || nullsep > c.data.length - 2) {
            throw new PngjException("bad zTXt chunk: no separator found");
        }
        this.key = ChunkHelper.toString(c.data, 0, nullsep);
        final int compmet = c.data[nullsep + 1];
        if (compmet != 0) {
            throw new PngjException("bad zTXt chunk: unknown compression method");
        }
        final byte[] uncomp = ChunkHelper.compressBytes(c.data, nullsep + 2, c.data.length - nullsep - 2, false);
        this.val = ChunkHelper.toString(uncomp);
    }
}
