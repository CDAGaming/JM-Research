package ar.com.hjg.pngj.pixels;

import java.io.*;
import java.util.zip.*;
import ar.com.hjg.pngj.*;

public class CompressorStreamLz4 extends CompressorStream
{
    private final DeflaterEstimatorLz4 lz4;
    private byte[] buf;
    private final int buffer_size;
    private int inbuf;
    private static final int MAX_BUFFER_SIZE = 16000;
    
    public CompressorStreamLz4(final OutputStream os, final int maxBlockLen, final long totalLen) {
        super(os, maxBlockLen, totalLen);
        this.inbuf = 0;
        this.lz4 = new DeflaterEstimatorLz4();
        this.buffer_size = (int)((totalLen > 16000L) ? 16000L : totalLen);
    }
    
    public CompressorStreamLz4(final OutputStream os, final int maxBlockLen, final long totalLen, final Deflater def) {
        this(os, maxBlockLen, totalLen);
    }
    
    public CompressorStreamLz4(final OutputStream os, final int maxBlockLen, final long totalLen, final int deflaterCompLevel, final int deflaterStrategy) {
        this(os, maxBlockLen, totalLen);
    }
    
    public void mywrite(final byte[] b, int off, int len) {
        if (len == 0) {
            return;
        }
        if (this.done || this.closed) {
            throw new PngjOutputException("write beyond end of stream");
        }
        this.bytesIn += len;
        while (len > 0) {
            if (this.inbuf == 0 && (len >= 16000 || this.bytesIn == this.totalbytes)) {
                this.bytesOut += this.lz4.compressEstim(b, off, len);
                len = 0;
            }
            else {
                if (this.buf == null) {
                    this.buf = new byte[this.buffer_size];
                }
                final int len2 = (this.inbuf + len <= this.buffer_size) ? len : (this.buffer_size - this.inbuf);
                if (len2 > 0) {
                    System.arraycopy(b, off, this.buf, this.inbuf, len2);
                }
                this.inbuf += len2;
                len -= len2;
                off += len2;
                if (this.inbuf != this.buffer_size) {
                    continue;
                }
                this.compressFromBuffer();
            }
        }
    }
    
    void compressFromBuffer() {
        if (this.inbuf > 0) {
            this.bytesOut += this.lz4.compressEstim(this.buf, 0, this.inbuf);
            this.inbuf = 0;
        }
    }
    
    public void done() {
        if (!this.done) {
            this.compressFromBuffer();
            this.done = true;
            this.flush();
        }
    }
    
    public void close() {
        this.done();
        if (!this.closed) {
            super.close();
            this.buf = null;
        }
    }
    
    public void reset() {
        this.done();
        super.reset();
    }
}
