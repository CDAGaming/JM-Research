package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;
import java.io.*;

public class PngChunkSPLT extends PngChunkMultiple
{
    public static final String ID = "sPLT";
    private String palName;
    private int sampledepth;
    private int[] palette;
    
    public PngChunkSPLT(final ImageInfo info) {
        super("sPLT", info);
    }
    
    public ChunkOrderingConstraint getOrderingConstraint() {
        return ChunkOrderingConstraint.BEFORE_IDAT;
    }
    
    public ChunkRaw createRawChunk() {
        try {
            final ByteArrayOutputStream ba = new ByteArrayOutputStream();
            ba.write(ChunkHelper.toBytes(this.palName));
            ba.write(0);
            ba.write((byte)this.sampledepth);
            for (int nentries = this.getNentries(), n = 0; n < nentries; ++n) {
                for (int i = 0; i < 4; ++i) {
                    if (this.sampledepth == 8) {
                        PngHelperInternal.writeByte(ba, (byte)this.palette[n * 5 + i]);
                    }
                    else {
                        PngHelperInternal.writeInt2(ba, this.palette[n * 5 + i]);
                    }
                }
                PngHelperInternal.writeInt2(ba, this.palette[n * 5 + 4]);
            }
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
        int t = -1;
        for (int i = 0; i < c.data.length; ++i) {
            if (c.data[i] == 0) {
                t = i;
                break;
            }
        }
        if (t <= 0 || t > c.data.length - 2) {
            throw new PngjException("bad sPLT chunk: no separator found");
        }
        this.palName = ChunkHelper.toString(c.data, 0, t);
        this.sampledepth = PngHelperInternal.readInt1fromByte(c.data, t + 1);
        t += 2;
        final int nentries = (c.data.length - t) / ((this.sampledepth == 8) ? 6 : 10);
        this.palette = new int[nentries * 5];
        int ne = 0;
        for (int j = 0; j < nentries; ++j) {
            int r;
            int g;
            int b;
            int a;
            if (this.sampledepth == 8) {
                r = PngHelperInternal.readInt1fromByte(c.data, t++);
                g = PngHelperInternal.readInt1fromByte(c.data, t++);
                b = PngHelperInternal.readInt1fromByte(c.data, t++);
                a = PngHelperInternal.readInt1fromByte(c.data, t++);
            }
            else {
                r = PngHelperInternal.readInt2fromBytes(c.data, t);
                t += 2;
                g = PngHelperInternal.readInt2fromBytes(c.data, t);
                t += 2;
                b = PngHelperInternal.readInt2fromBytes(c.data, t);
                t += 2;
                a = PngHelperInternal.readInt2fromBytes(c.data, t);
                t += 2;
            }
            final int f = PngHelperInternal.readInt2fromBytes(c.data, t);
            t += 2;
            this.palette[ne++] = r;
            this.palette[ne++] = g;
            this.palette[ne++] = b;
            this.palette[ne++] = a;
            this.palette[ne++] = f;
        }
    }
    
    public int getNentries() {
        return this.palette.length / 5;
    }
    
    public String getPalName() {
        return this.palName;
    }
    
    public void setPalName(final String palName) {
        this.palName = palName;
    }
    
    public int getSampledepth() {
        return this.sampledepth;
    }
    
    public void setSampledepth(final int sampledepth) {
        this.sampledepth = sampledepth;
    }
    
    public int[] getPalette() {
        return this.palette;
    }
    
    public void setPalette(final int[] palette) {
        this.palette = palette;
    }
}
