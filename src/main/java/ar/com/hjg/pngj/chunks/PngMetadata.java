package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;
import java.util.*;

public class PngMetadata
{
    private final ChunksList chunkList;
    private final boolean readonly;
    
    public PngMetadata(final ChunksList chunks) {
        this.chunkList = chunks;
        if (chunks instanceof ChunksListForWrite) {
            this.readonly = false;
        }
        else {
            this.readonly = true;
        }
    }
    
    public void queueChunk(final PngChunk c, final boolean lazyOverwrite) {
        final ChunksListForWrite cl = this.getChunkListW();
        if (this.readonly) {
            throw new PngjException("cannot set chunk : readonly metadata");
        }
        if (lazyOverwrite) {
            ChunkHelper.trimList(cl.getQueuedChunks(), new ChunkPredicate() {
                public boolean match(final PngChunk c2) {
                    return ChunkHelper.equivalent(c, c2);
                }
            });
        }
        cl.queue(c);
    }
    
    public void queueChunk(final PngChunk c) {
        this.queueChunk(c, true);
    }
    
    private ChunksListForWrite getChunkListW() {
        return (ChunksListForWrite)this.chunkList;
    }
    
    public double[] getDpi() {
        final PngChunk c = this.chunkList.getById1("pHYs", true);
        if (c == null) {
            return new double[] { -1.0, -1.0 };
        }
        return ((PngChunkPHYS)c).getAsDpi2();
    }
    
    public void setDpi(final double x) {
        this.setDpi(x, x);
    }
    
    public void setDpi(final double x, final double y) {
        final PngChunkPHYS c = new PngChunkPHYS(this.chunkList.imageInfo);
        c.setAsDpi2(x, y);
        this.queueChunk(c);
    }
    
    public PngChunkTIME setTimeNow(final int secsAgo) {
        final PngChunkTIME c = new PngChunkTIME(this.chunkList.imageInfo);
        c.setNow(secsAgo);
        this.queueChunk(c);
        return c;
    }
    
    public PngChunkTIME setTimeNow() {
        return this.setTimeNow(0);
    }
    
    public PngChunkTIME setTimeYMDHMS(final int yearx, final int monx, final int dayx, final int hourx, final int minx, final int secx) {
        final PngChunkTIME c = new PngChunkTIME(this.chunkList.imageInfo);
        c.setYMDHMS(yearx, monx, dayx, hourx, minx, secx);
        this.queueChunk(c, true);
        return c;
    }
    
    public PngChunkTIME getTime() {
        return (PngChunkTIME)this.chunkList.getById1("tIME");
    }
    
    public String getTimeAsString() {
        final PngChunkTIME c = this.getTime();
        return (c == null) ? "" : c.getAsString();
    }
    
    public PngChunkTextVar setText(final String k, final String val, final boolean useLatin1, final boolean compress) {
        if (compress && !useLatin1) {
            throw new PngjException("cannot compress non latin text");
        }
        PngChunkTextVar c;
        if (useLatin1) {
            if (compress) {
                c = new PngChunkZTXT(this.chunkList.imageInfo);
            }
            else {
                c = new PngChunkTEXT(this.chunkList.imageInfo);
            }
        }
        else {
            c = new PngChunkITXT(this.chunkList.imageInfo);
            ((PngChunkITXT)c).setLangtag(k);
        }
        c.setKeyVal(k, val);
        this.queueChunk(c, true);
        return c;
    }
    
    public PngChunkTextVar setText(final String k, final String val) {
        return this.setText(k, val, false, false);
    }
    
    public List<? extends PngChunkTextVar> getTxtsForKey(final String k) {
        final List c = new ArrayList();
        c.addAll(this.chunkList.getById("tEXt", k));
        c.addAll(this.chunkList.getById("zTXt", k));
        c.addAll(this.chunkList.getById("iTXt", k));
        return (List<? extends PngChunkTextVar>)c;
    }
    
    public String getTxtForKey(final String k) {
        final List<? extends PngChunkTextVar> li = this.getTxtsForKey(k);
        if (li.isEmpty()) {
            return "";
        }
        final StringBuilder t = new StringBuilder();
        for (final PngChunkTextVar c : li) {
            t.append(c.getVal()).append("\n");
        }
        return t.toString().trim();
    }
    
    public PngChunkPLTE getPLTE() {
        return (PngChunkPLTE)this.chunkList.getById1("PLTE");
    }
    
    public PngChunkPLTE createPLTEChunk() {
        final PngChunkPLTE plte = new PngChunkPLTE(this.chunkList.imageInfo);
        this.queueChunk(plte);
        return plte;
    }
    
    public PngChunkTRNS getTRNS() {
        return (PngChunkTRNS)this.chunkList.getById1("tRNS");
    }
    
    public PngChunkTRNS createTRNSChunk() {
        final PngChunkTRNS trns = new PngChunkTRNS(this.chunkList.imageInfo);
        this.queueChunk(trns);
        return trns;
    }
}
