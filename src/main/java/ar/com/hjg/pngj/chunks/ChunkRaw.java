package ar.com.hjg.pngj.chunks;

import java.util.zip.*;
import ar.com.hjg.pngj.*;
import java.io.*;

public class ChunkRaw
{
    public final int len;
    public final byte[] idbytes;
    public final String id;
    public byte[] data;
    private long offset;
    public byte[] crcval;
    private CRC32 crcengine;
    
    public ChunkRaw(final int len, final String id, final boolean alloc) {
        this.data = null;
        this.offset = 0L;
        this.crcval = new byte[4];
        this.len = len;
        this.id = id;
        this.idbytes = ChunkHelper.toBytes(id);
        for (int i = 0; i < 4; ++i) {
            if (this.idbytes[i] < 65 || this.idbytes[i] > 122 || (this.idbytes[i] > 90 && this.idbytes[i] < 97)) {
                throw new PngjException("Bad id chunk: must be ascii letters " + id);
            }
        }
        if (alloc) {
            this.allocData();
        }
    }
    
    public ChunkRaw(final int len, final byte[] idbytes, final boolean alloc) {
        this(len, ChunkHelper.toString(idbytes), alloc);
    }
    
    public void allocData() {
        if (this.data == null || this.data.length < this.len) {
            this.data = new byte[this.len];
        }
    }
    
    private void computeCrcForWriting() {
        (this.crcengine = new CRC32()).update(this.idbytes, 0, 4);
        if (this.len > 0) {
            this.crcengine.update(this.data, 0, this.len);
        }
        PngHelperInternal.writeInt4tobytes((int)this.crcengine.getValue(), this.crcval, 0);
    }
    
    public void writeChunk(final OutputStream os) {
        this.writeChunkHeader(os);
        if (this.len > 0) {
            if (this.data == null) {
                throw new PngjOutputException("cannot write chunk, raw chunk data is null [" + this.id + "]");
            }
            PngHelperInternal.writeBytes(os, this.data, 0, this.len);
        }
        this.computeCrcForWriting();
        this.writeChunkCrc(os);
    }
    
    public void writeChunkHeader(final OutputStream os) {
        if (this.idbytes.length != 4) {
            throw new PngjOutputException("bad chunkid [" + this.id + "]");
        }
        PngHelperInternal.writeInt4(os, this.len);
        PngHelperInternal.writeBytes(os, this.idbytes);
    }
    
    public void writeChunkCrc(final OutputStream os) {
        PngHelperInternal.writeBytes(os, this.crcval, 0, 4);
    }
    
    public void checkCrc() {
        final int crcComputed = (int)this.crcengine.getValue();
        final int crcExpected = PngHelperInternal.readInt4fromBytes(this.crcval, 0);
        if (crcComputed != crcExpected) {
            throw new PngjBadCrcException("chunk: " + this.toString() + " expected=" + crcExpected + " read=" + crcComputed);
        }
    }
    
    public void updateCrc(final byte[] buf, final int off, final int len) {
        if (this.crcengine == null) {
            this.crcengine = new CRC32();
        }
        this.crcengine.update(buf, off, len);
    }
    
    ByteArrayInputStream getAsByteStream() {
        return new ByteArrayInputStream(this.data);
    }
    
    public long getOffset() {
        return this.offset;
    }
    
    public void setOffset(final long offset) {
        this.offset = offset;
    }
    
    public String toString() {
        return "chunkid=" + ChunkHelper.toString(this.idbytes) + " len=" + this.len;
    }
    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = 31 * result + (int)(this.offset ^ this.offset >>> 32);
        return result;
    }
    
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final ChunkRaw other = (ChunkRaw)obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        }
        else if (!this.id.equals(other.id)) {
            return false;
        }
        return this.offset == other.offset;
    }
}
