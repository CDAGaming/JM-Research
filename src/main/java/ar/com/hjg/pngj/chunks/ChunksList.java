package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;
import java.util.*;

public class ChunksList
{
    public static final int CHUNK_GROUP_0_IDHR = 0;
    public static final int CHUNK_GROUP_1_AFTERIDHR = 1;
    public static final int CHUNK_GROUP_2_PLTE = 2;
    public static final int CHUNK_GROUP_3_AFTERPLTE = 3;
    public static final int CHUNK_GROUP_4_IDAT = 4;
    public static final int CHUNK_GROUP_5_AFTERIDAT = 5;
    public static final int CHUNK_GROUP_6_END = 6;
    List<PngChunk> chunks;
    final ImageInfo imageInfo;
    boolean withPlte;
    
    public ChunksList(final ImageInfo imfinfo) {
        this.chunks = new ArrayList<PngChunk>();
        this.withPlte = false;
        this.imageInfo = imfinfo;
    }
    
    public List<PngChunk> getChunks() {
        return this.chunks;
    }
    
    protected static List<PngChunk> getXById(final List<PngChunk> list, final String id, final String innerid) {
        if (innerid == null) {
            return ChunkHelper.filterList(list, new ChunkPredicate() {
                public boolean match(final PngChunk c) {
                    return c.id.equals(id);
                }
            });
        }
        return ChunkHelper.filterList(list, new ChunkPredicate() {
            public boolean match(final PngChunk c) {
                return c.id.equals(id) && (!(c instanceof PngChunkTextVar) || ((PngChunkTextVar)c).getKey().equals(innerid)) && (!(c instanceof PngChunkSPLT) || ((PngChunkSPLT)c).getPalName().equals(innerid));
            }
        });
    }
    
    public void appendReadChunk(final PngChunk chunk, final int chunkGroup) {
        chunk.setChunkGroup(chunkGroup);
        this.chunks.add(chunk);
        if (chunk.id.equals("PLTE")) {
            this.withPlte = true;
        }
    }
    
    public List<? extends PngChunk> getById(final String id) {
        return this.getById(id, null);
    }
    
    public List<? extends PngChunk> getById(final String id, final String innerid) {
        return getXById(this.chunks, id, innerid);
    }
    
    public PngChunk getById1(final String id) {
        return this.getById1(id, false);
    }
    
    public PngChunk getById1(final String id, final boolean failIfMultiple) {
        return this.getById1(id, null, failIfMultiple);
    }
    
    public PngChunk getById1(final String id, final String innerid, final boolean failIfMultiple) {
        final List<? extends PngChunk> list = this.getById(id, innerid);
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() > 1 && (failIfMultiple || !((PngChunk)list.get(0)).allowsMultiple())) {
            throw new PngjException("unexpected multiple chunks id=" + id);
        }
        return (PngChunk)list.get(list.size() - 1);
    }
    
    public List<PngChunk> getEquivalent(final PngChunk c2) {
        return ChunkHelper.filterList(this.chunks, new ChunkPredicate() {
            public boolean match(final PngChunk c) {
                return ChunkHelper.equivalent(c, c2);
            }
        });
    }
    
    public String toString() {
        return "ChunkList: read: " + this.chunks.size();
    }
    
    public String toStringFull() {
        final StringBuilder sb = new StringBuilder(this.toString());
        sb.append("\n Read:\n");
        for (final PngChunk chunk : this.chunks) {
            sb.append(chunk).append(" G=" + chunk.getChunkGroup() + "\n");
        }
        return sb.toString();
    }
}
