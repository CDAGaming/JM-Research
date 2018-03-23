package ar.com.hjg.pngj.pixels;

import ar.com.hjg.pngj.*;
import java.io.*;

public abstract class CompressorStream extends FilterOutputStream
{
    protected OutputStream os;
    public final int blockLen;
    public final long totalbytes;
    boolean closed;
    protected boolean done;
    protected long bytesIn;
    protected long bytesOut;
    protected int block;
    private byte[] firstBytes;
    protected boolean storeFirstByte;
    
    public CompressorStream(final OutputStream os, int blockLen, long totalbytes) {
        super(os);
        this.closed = false;
        this.done = false;
        this.bytesIn = 0L;
        this.bytesOut = 0L;
        this.block = -1;
        this.storeFirstByte = false;
        if (blockLen < 0) {
            blockLen = 4096;
        }
        if (totalbytes < 0L) {
            totalbytes = Long.MAX_VALUE;
        }
        if (blockLen < 1 || totalbytes < 1L) {
            throw new RuntimeException(" maxBlockLen or totalLen invalid");
        }
        this.os = os;
        this.blockLen = blockLen;
        this.totalbytes = totalbytes;
    }
    
    public void close() {
        this.done();
        this.closed = true;
    }
    
    public abstract void done();
    
    public final void write(final byte[] b, int off, int len) {
        ++this.block;
        if (len <= this.blockLen) {
            this.mywrite(b, off, len);
            if (this.storeFirstByte && this.block < this.firstBytes.length) {
                this.firstBytes[this.block] = b[off];
            }
        }
        else {
            while (len > 0) {
                this.mywrite(b, off, this.blockLen);
                off += this.blockLen;
                len -= this.blockLen;
            }
        }
        if (this.bytesIn >= this.totalbytes) {
            this.done();
        }
    }
    
    protected abstract void mywrite(final byte[] p0, final int p1, final int p2);
    
    public final void write(final byte[] b) {
        this.write(b, 0, b.length);
    }
    
    public void write(final int b) throws IOException {
        throw new PngjOutputException("should not be used");
    }
    
    public void reset() {
        this.reset(this.os);
    }
    
    public void reset(final OutputStream os) {
        if (this.closed) {
            throw new PngjOutputException("cannot reset, discarded object");
        }
        this.done();
        this.bytesIn = 0L;
        this.bytesOut = 0L;
        this.block = -1;
        this.done = false;
        this.os = os;
    }
    
    public final double getCompressionRatio() {
        return (this.bytesOut == 0L) ? 1.0 : (this.bytesOut / this.bytesIn);
    }
    
    public final long getBytesRaw() {
        return this.bytesIn;
    }
    
    public final long getBytesCompressed() {
        return this.bytesOut;
    }
    
    public OutputStream getOs() {
        return this.os;
    }
    
    public void flush() {
        if (this.os != null) {
            try {
                this.os.flush();
            }
            catch (IOException e) {
                throw new PngjOutputException(e);
            }
        }
    }
    
    public boolean isClosed() {
        return this.closed;
    }
    
    public boolean isDone() {
        return this.done;
    }
    
    public byte[] getFirstBytes() {
        return this.firstBytes;
    }
    
    public void setStoreFirstByte(final boolean storeFirstByte, final int nblocks) {
        this.storeFirstByte = storeFirstByte;
        if (this.storeFirstByte) {
            if (this.firstBytes == null || this.firstBytes.length < nblocks) {
                this.firstBytes = new byte[nblocks];
            }
        }
        else {
            this.firstBytes = null;
        }
    }
}
