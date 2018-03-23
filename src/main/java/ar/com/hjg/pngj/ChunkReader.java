package ar.com.hjg.pngj;

import ar.com.hjg.pngj.chunks.*;

public abstract class ChunkReader
{
    public final ChunkReaderMode mode;
    private final ChunkRaw chunkRaw;
    private boolean crcCheck;
    protected int read;
    private int crcn;
    
    public ChunkReader(final int clen, final String id, final long offsetInPng, final ChunkReaderMode mode) {
        this.read = 0;
        this.crcn = 0;
        if (mode == null || id.length() != 4 || clen < 0) {
            throw new PngjExceptionInternal("Bad chunk paramenters: " + mode);
        }
        this.mode = mode;
        (this.chunkRaw = new ChunkRaw(clen, id, mode == ChunkReaderMode.BUFFER)).setOffset(offsetInPng);
        this.crcCheck = (mode != ChunkReaderMode.SKIP);
    }
    
    public ChunkRaw getChunkRaw() {
        return this.chunkRaw;
    }
    
    public final int feedBytes(final byte[] buf, int off, int len) {
        if (len == 0) {
            return 0;
        }
        if (len < 0) {
            throw new PngjException("negative length??");
        }
        if (this.read == 0 && this.crcn == 0 && this.crcCheck) {
            this.chunkRaw.updateCrc(this.chunkRaw.idbytes, 0, 4);
        }
        int bytesForData = this.chunkRaw.len - this.read;
        if (bytesForData > len) {
            bytesForData = len;
        }
        if (bytesForData > 0 || this.crcn == 0) {
            if (this.crcCheck && this.mode != ChunkReaderMode.BUFFER && bytesForData > 0) {
                this.chunkRaw.updateCrc(buf, off, bytesForData);
            }
            if (this.mode == ChunkReaderMode.BUFFER) {
                if (this.chunkRaw.data != buf && bytesForData > 0) {
                    System.arraycopy(buf, off, this.chunkRaw.data, this.read, bytesForData);
                }
            }
            else if (this.mode == ChunkReaderMode.PROCESS) {
                this.processData(this.read, buf, off, bytesForData);
            }
            this.read += bytesForData;
            off += bytesForData;
            len -= bytesForData;
        }
        int crcRead = 0;
        if (this.read == this.chunkRaw.len) {
            crcRead = 4 - this.crcn;
            if (crcRead > len) {
                crcRead = len;
            }
            if (crcRead > 0) {
                if (buf != this.chunkRaw.crcval) {
                    System.arraycopy(buf, off, this.chunkRaw.crcval, this.crcn, crcRead);
                }
                this.crcn += crcRead;
                if (this.crcn == 4) {
                    if (this.crcCheck) {
                        if (this.mode == ChunkReaderMode.BUFFER) {
                            this.chunkRaw.updateCrc(this.chunkRaw.data, 0, this.chunkRaw.len);
                        }
                        this.chunkRaw.checkCrc();
                    }
                    this.chunkDone();
                }
            }
        }
        return bytesForData + crcRead;
    }
    
    public final boolean isDone() {
        return this.crcn == 4;
    }
    
    public void setCrcCheck(final boolean crcCheck) {
        if (this.read != 0 && crcCheck && !this.crcCheck) {
            throw new PngjException("too late!");
        }
        this.crcCheck = crcCheck;
    }
    
    protected abstract void processData(final int p0, final byte[] p1, final int p2, final int p3);
    
    protected abstract void chunkDone();
    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + ((this.chunkRaw == null) ? 0 : this.chunkRaw.hashCode());
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
        final ChunkReader other = (ChunkReader)obj;
        if (this.chunkRaw == null) {
            if (other.chunkRaw != null) {
                return false;
            }
        }
        else if (!this.chunkRaw.equals(other.chunkRaw)) {
            return false;
        }
        return true;
    }
    
    public String toString() {
        return this.chunkRaw.toString();
    }
    
    public enum ChunkReaderMode
    {
        BUFFER, 
        PROCESS, 
        SKIP;
    }
}
