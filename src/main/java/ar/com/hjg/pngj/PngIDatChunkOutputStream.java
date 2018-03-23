package ar.com.hjg.pngj;

import java.io.*;
import ar.com.hjg.pngj.chunks.*;

public class PngIDatChunkOutputStream extends ProgressiveOutputStream
{
    private static final int SIZE_DEFAULT = 32768;
    private final OutputStream outputStream;
    private byte[] prefix;
    
    public PngIDatChunkOutputStream(final OutputStream outputStream) {
        this(outputStream, 0);
    }
    
    public PngIDatChunkOutputStream(final OutputStream outputStream, final int size) {
        super((size > 0) ? size : 32768);
        this.prefix = null;
        this.outputStream = outputStream;
    }
    
    protected final void flushBuffer(final byte[] b, final int len) {
        final int len2 = (this.prefix == null) ? len : (len + this.prefix.length);
        final ChunkRaw c = new ChunkRaw(len2, ChunkHelper.b_IDAT, false);
        if (len == len2) {
            c.data = b;
        }
        c.writeChunk(this.outputStream);
    }
    
    void setPrefix(final byte[] pref) {
        if (pref == null) {
            this.prefix = null;
        }
        else {
            System.arraycopy(pref, 0, this.prefix = new byte[pref.length], 0, pref.length);
        }
    }
}
