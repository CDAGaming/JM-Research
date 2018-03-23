package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;
import java.io.*;
import java.util.*;

public class ChunksListForWrite extends ChunksList
{
    private final List<PngChunk> queuedChunks;
    private HashMap<String, Integer> alreadyWrittenKeys;
    
    public ChunksListForWrite(final ImageInfo imfinfo) {
        super(imfinfo);
        this.queuedChunks = new ArrayList<PngChunk>();
        this.alreadyWrittenKeys = new HashMap<String, Integer>();
    }
    
    public List<? extends PngChunk> getQueuedById(final String id) {
        return this.getQueuedById(id, null);
    }
    
    public List<? extends PngChunk> getQueuedById(final String id, final String innerid) {
        return ChunksList.getXById(this.queuedChunks, id, innerid);
    }
    
    public PngChunk getQueuedById1(final String id, final String innerid, final boolean failIfMultiple) {
        final List<? extends PngChunk> list = this.getQueuedById(id, innerid);
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() > 1 && (failIfMultiple || !((PngChunk)list.get(0)).allowsMultiple())) {
            throw new PngjException("unexpected multiple chunks id=" + id);
        }
        return (PngChunk)list.get(list.size() - 1);
    }
    
    public PngChunk getQueuedById1(final String id, final boolean failIfMultiple) {
        return this.getQueuedById1(id, null, failIfMultiple);
    }
    
    public PngChunk getQueuedById1(final String id) {
        return this.getQueuedById1(id, false);
    }
    
    public List<PngChunk> getQueuedEquivalent(final PngChunk c2) {
        return ChunkHelper.filterList(this.queuedChunks, new ChunkPredicate() {
            public boolean match(final PngChunk c) {
                return ChunkHelper.equivalent(c, c2);
            }
        });
    }
    
    public boolean removeChunk(final PngChunk c) {
        return c != null && this.queuedChunks.remove(c);
    }
    
    public boolean queue(final PngChunk c) {
        this.queuedChunks.add(c);
        return true;
    }
    
    private static boolean shouldWrite(final PngChunk c, final int currentGroup) {
        if (currentGroup == 2) {
            return c.id.equals("PLTE");
        }
        if (currentGroup % 2 == 0) {
            throw new PngjOutputException("bad chunk group?");
        }
        int minChunkGroup;
        int maxChunkGroup;
        if (c.getOrderingConstraint().mustGoBeforePLTE()) {
            maxChunkGroup = (minChunkGroup = 1);
        }
        else if (c.getOrderingConstraint().mustGoBeforeIDAT()) {
            maxChunkGroup = 3;
            minChunkGroup = (c.getOrderingConstraint().mustGoAfterPLTE() ? 3 : 1);
        }
        else {
            maxChunkGroup = 5;
            minChunkGroup = 1;
        }
        int preferred = maxChunkGroup;
        if (c.hasPriority()) {
            preferred = minChunkGroup;
        }
        if (ChunkHelper.isUnknown(c) && c.getChunkGroup() > 0) {
            preferred = c.getChunkGroup();
        }
        return currentGroup == preferred || (currentGroup > preferred && currentGroup <= maxChunkGroup);
    }
    
    public int writeChunks(final OutputStream os, final int currentGroup) {
        int cont = 0;
        final Iterator<PngChunk> it = this.queuedChunks.iterator();
        while (it.hasNext()) {
            final PngChunk c = it.next();
            if (!shouldWrite(c, currentGroup)) {
                continue;
            }
            if (ChunkHelper.isCritical(c.id) && !c.id.equals("PLTE")) {
                throw new PngjOutputException("bad chunk queued: " + c);
            }
            if (this.alreadyWrittenKeys.containsKey(c.id) && !c.allowsMultiple()) {
                throw new PngjOutputException("duplicated chunk does not allow multiple: " + c);
            }
            c.write(os);
            this.chunks.add(c);
            this.alreadyWrittenKeys.put(c.id, this.alreadyWrittenKeys.containsKey(c.id) ? (this.alreadyWrittenKeys.get(c.id) + 1) : 1);
            c.setChunkGroup(currentGroup);
            it.remove();
            ++cont;
        }
        return cont;
    }
    
    public List<PngChunk> getQueuedChunks() {
        return this.queuedChunks;
    }
    
    public String toString() {
        return "ChunkList: written: " + this.getChunks().size() + " queue: " + this.queuedChunks.size();
    }
    
    public String toStringFull() {
        final StringBuilder sb = new StringBuilder(this.toString());
        sb.append("\n Written:\n");
        for (final PngChunk chunk : this.getChunks()) {
            sb.append(chunk).append(" G=" + chunk.getChunkGroup() + "\n");
        }
        if (!this.queuedChunks.isEmpty()) {
            sb.append(" Queued:\n");
            for (final PngChunk chunk : this.queuedChunks) {
                sb.append(chunk).append("\n");
            }
        }
        return sb.toString();
    }
}
