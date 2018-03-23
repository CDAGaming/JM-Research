package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;
import java.util.*;

public class PngChunkTIME extends PngChunkSingle
{
    public static final String ID = "tIME";
    private int year;
    private int mon;
    private int day;
    private int hour;
    private int min;
    private int sec;
    
    public PngChunkTIME(final ImageInfo info) {
        super("tIME", info);
    }
    
    public ChunkOrderingConstraint getOrderingConstraint() {
        return ChunkOrderingConstraint.NONE;
    }
    
    public ChunkRaw createRawChunk() {
        final ChunkRaw c = this.createEmptyChunk(7, true);
        PngHelperInternal.writeInt2tobytes(this.year, c.data, 0);
        c.data[2] = (byte)this.mon;
        c.data[3] = (byte)this.day;
        c.data[4] = (byte)this.hour;
        c.data[5] = (byte)this.min;
        c.data[6] = (byte)this.sec;
        return c;
    }
    
    public void parseFromRaw(final ChunkRaw chunk) {
        if (chunk.len != 7) {
            throw new PngjException("bad chunk " + chunk);
        }
        this.year = PngHelperInternal.readInt2fromBytes(chunk.data, 0);
        this.mon = PngHelperInternal.readInt1fromByte(chunk.data, 2);
        this.day = PngHelperInternal.readInt1fromByte(chunk.data, 3);
        this.hour = PngHelperInternal.readInt1fromByte(chunk.data, 4);
        this.min = PngHelperInternal.readInt1fromByte(chunk.data, 5);
        this.sec = PngHelperInternal.readInt1fromByte(chunk.data, 6);
    }
    
    public void setNow(final int secsAgo) {
        final Calendar d = Calendar.getInstance();
        d.setTimeInMillis(System.currentTimeMillis() - 1000L * secsAgo);
        this.year = d.get(1);
        this.mon = d.get(2) + 1;
        this.day = d.get(5);
        this.hour = d.get(11);
        this.min = d.get(12);
        this.sec = d.get(13);
    }
    
    public void setYMDHMS(final int yearx, final int monx, final int dayx, final int hourx, final int minx, final int secx) {
        this.year = yearx;
        this.mon = monx;
        this.day = dayx;
        this.hour = hourx;
        this.min = minx;
        this.sec = secx;
    }
    
    public int[] getYMDHMS() {
        return new int[] { this.year, this.mon, this.day, this.hour, this.min, this.sec };
    }
    
    public String getAsString() {
        return String.format("%04d/%02d/%02d %02d:%02d:%02d", this.year, this.mon, this.day, this.hour, this.min, this.sec);
    }
}
