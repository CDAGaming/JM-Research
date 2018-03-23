package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;

public class PngChunkOFFS extends PngChunkSingle
{
    public static final String ID = "oFFs";
    private long posX;
    private long posY;
    private int units;
    
    public PngChunkOFFS(final ImageInfo info) {
        super("oFFs", info);
    }
    
    public ChunkOrderingConstraint getOrderingConstraint() {
        return ChunkOrderingConstraint.BEFORE_IDAT;
    }
    
    public ChunkRaw createRawChunk() {
        final ChunkRaw c = this.createEmptyChunk(9, true);
        PngHelperInternal.writeInt4tobytes((int)this.posX, c.data, 0);
        PngHelperInternal.writeInt4tobytes((int)this.posY, c.data, 4);
        c.data[8] = (byte)this.units;
        return c;
    }
    
    public void parseFromRaw(final ChunkRaw chunk) {
        if (chunk.len != 9) {
            throw new PngjException("bad chunk length " + chunk);
        }
        this.posX = PngHelperInternal.readInt4fromBytes(chunk.data, 0);
        if (this.posX < 0L) {
            this.posX += 4294967296L;
        }
        this.posY = PngHelperInternal.readInt4fromBytes(chunk.data, 4);
        if (this.posY < 0L) {
            this.posY += 4294967296L;
        }
        this.units = PngHelperInternal.readInt1fromByte(chunk.data, 8);
    }
    
    public int getUnits() {
        return this.units;
    }
    
    public void setUnits(final int units) {
        this.units = units;
    }
    
    public long getPosX() {
        return this.posX;
    }
    
    public void setPosX(final long posX) {
        this.posX = posX;
    }
    
    public long getPosY() {
        return this.posY;
    }
    
    public void setPosY(final long posY) {
        this.posY = posY;
    }
}
