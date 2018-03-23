package ar.com.hjg.pngj.chunks;

import java.io.*;
import ar.com.hjg.pngj.*;

public abstract class PngChunk
{
    public final String id;
    public final boolean crit;
    public final boolean pub;
    public final boolean safe;
    protected final ImageInfo imgInfo;
    protected ChunkRaw raw;
    private boolean priority;
    protected int chunkGroup;
    
    public PngChunk(final String id, final ImageInfo imgInfo) {
        this.priority = false;
        this.chunkGroup = -1;
        this.id = id;
        this.imgInfo = imgInfo;
        this.crit = ChunkHelper.isCritical(id);
        this.pub = ChunkHelper.isPublic(id);
        this.safe = ChunkHelper.isSafeToCopy(id);
    }
    
    protected final ChunkRaw createEmptyChunk(final int len, final boolean alloc) {
        final ChunkRaw c = new ChunkRaw(len, ChunkHelper.toBytes(this.id), alloc);
        return c;
    }
    
    public final int getChunkGroup() {
        return this.chunkGroup;
    }
    
    final void setChunkGroup(final int chunkGroup) {
        this.chunkGroup = chunkGroup;
    }
    
    public boolean hasPriority() {
        return this.priority;
    }
    
    public void setPriority(final boolean priority) {
        this.priority = priority;
    }
    
    final void write(final OutputStream os) {
        if (this.raw == null || this.raw.data == null) {
            this.raw = this.createRawChunk();
        }
        if (this.raw == null) {
            throw new PngjExceptionInternal("null chunk ! creation failed for " + this);
        }
        this.raw.writeChunk(os);
    }
    
    protected abstract ChunkRaw createRawChunk();
    
    protected abstract void parseFromRaw(final ChunkRaw p0);
    
    protected abstract boolean allowsMultiple();
    
    public ChunkRaw getRaw() {
        return this.raw;
    }
    
    void setRaw(final ChunkRaw raw) {
        this.raw = raw;
    }
    
    public int getLen() {
        return (this.raw != null) ? this.raw.len : -1;
    }
    
    public long getOffset() {
        return (this.raw != null) ? this.raw.getOffset() : -1L;
    }
    
    public void invalidateRawData() {
        this.raw = null;
    }
    
    public abstract ChunkOrderingConstraint getOrderingConstraint();
    
    public String toString() {
        return "chunk id= " + this.id + " (len=" + this.getLen() + " offset=" + this.getOffset() + ")";
    }
    
    public enum ChunkOrderingConstraint
    {
        NONE, 
        BEFORE_PLTE_AND_IDAT, 
        AFTER_PLTE_BEFORE_IDAT, 
        AFTER_PLTE_BEFORE_IDAT_PLTE_REQUIRED, 
        BEFORE_IDAT, 
        NA;
        
        public boolean mustGoBeforePLTE() {
            return this == ChunkOrderingConstraint.BEFORE_PLTE_AND_IDAT;
        }
        
        public boolean mustGoBeforeIDAT() {
            return this == ChunkOrderingConstraint.BEFORE_IDAT || this == ChunkOrderingConstraint.BEFORE_PLTE_AND_IDAT || this == ChunkOrderingConstraint.AFTER_PLTE_BEFORE_IDAT;
        }
        
        public boolean mustGoAfterPLTE() {
            return this == ChunkOrderingConstraint.AFTER_PLTE_BEFORE_IDAT || this == ChunkOrderingConstraint.AFTER_PLTE_BEFORE_IDAT_PLTE_REQUIRED;
        }
        
        public boolean isOk(final int currentChunkGroup, final boolean hasplte) {
            if (this == ChunkOrderingConstraint.NONE) {
                return true;
            }
            if (this == ChunkOrderingConstraint.BEFORE_IDAT) {
                return currentChunkGroup < 4;
            }
            if (this == ChunkOrderingConstraint.BEFORE_PLTE_AND_IDAT) {
                return currentChunkGroup < 2;
            }
            return this == ChunkOrderingConstraint.AFTER_PLTE_BEFORE_IDAT && (hasplte ? (currentChunkGroup < 4) : (currentChunkGroup < 4 && currentChunkGroup > 2));
        }
    }
}
