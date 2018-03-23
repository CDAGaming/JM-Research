package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.*;

public class ChunkCopyBehaviour
{
    public static final int COPY_NONE = 0;
    public static final int COPY_PALETTE = 1;
    public static final int COPY_ALL_SAFE = 4;
    public static final int COPY_ALL = 8;
    public static final int COPY_PHYS = 16;
    public static final int COPY_TEXTUAL = 32;
    public static final int COPY_TRANSPARENCY = 64;
    public static final int COPY_UNKNOWN = 128;
    public static final int COPY_ALMOSTALL = 256;
    
    private static boolean maskMatch(final int v, final int mask) {
        return (v & mask) != 0x0;
    }
    
    public static ChunkPredicate createPredicate(final int copyFromMask, final ImageInfo imgInfo) {
        return new ChunkPredicate() {
            public boolean match(final PngChunk chunk) {
                if (chunk.crit) {
                    if (chunk.id.equals("PLTE")) {
                        if (imgInfo.indexed && maskMatch(copyFromMask, 1)) {
                            return true;
                        }
                        if (!imgInfo.greyscale && maskMatch(copyFromMask, 8)) {
                            return true;
                        }
                    }
                }
                else {
                    final boolean text = chunk instanceof PngChunkTextVar;
                    final boolean safe = chunk.safe;
                    if (maskMatch(copyFromMask, 8)) {
                        return true;
                    }
                    if (safe && maskMatch(copyFromMask, 4)) {
                        return true;
                    }
                    if (chunk.id.equals("tRNS") && maskMatch(copyFromMask, 64)) {
                        return true;
                    }
                    if (chunk.id.equals("pHYs") && maskMatch(copyFromMask, 16)) {
                        return true;
                    }
                    if (text && maskMatch(copyFromMask, 32)) {
                        return true;
                    }
                    if (maskMatch(copyFromMask, 256) && !ChunkHelper.isUnknown(chunk) && !text && !chunk.id.equals("hIST") && !chunk.id.equals("tIME")) {
                        return true;
                    }
                    if (maskMatch(copyFromMask, 128) && ChunkHelper.isUnknown(chunk)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
