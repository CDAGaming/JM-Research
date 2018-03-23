package ar.com.hjg.pngj.chunks;

import java.util.zip.*;
import ar.com.hjg.pngj.*;
import java.io.*;
import java.util.*;

public class ChunkHelper
{
    public static final String IHDR = "IHDR";
    public static final String PLTE = "PLTE";
    public static final String IDAT = "IDAT";
    public static final String IEND = "IEND";
    public static final String cHRM = "cHRM";
    public static final String gAMA = "gAMA";
    public static final String iCCP = "iCCP";
    public static final String sBIT = "sBIT";
    public static final String sRGB = "sRGB";
    public static final String bKGD = "bKGD";
    public static final String hIST = "hIST";
    public static final String tRNS = "tRNS";
    public static final String pHYs = "pHYs";
    public static final String sPLT = "sPLT";
    public static final String tIME = "tIME";
    public static final String iTXt = "iTXt";
    public static final String tEXt = "tEXt";
    public static final String zTXt = "zTXt";
    public static final byte[] b_IHDR;
    public static final byte[] b_PLTE;
    public static final byte[] b_IDAT;
    public static final byte[] b_IEND;
    private static byte[] tmpbuffer;
    
    public static byte[] toBytes(final String x) {
        try {
            return x.getBytes(PngHelperInternal.charsetLatin1name);
        }
        catch (UnsupportedEncodingException e) {
            throw new PngBadCharsetException(e);
        }
    }
    
    public static String toString(final byte[] x) {
        try {
            return new String(x, PngHelperInternal.charsetLatin1name);
        }
        catch (UnsupportedEncodingException e) {
            throw new PngBadCharsetException(e);
        }
    }
    
    public static String toString(final byte[] x, final int offset, final int len) {
        try {
            return new String(x, offset, len, PngHelperInternal.charsetLatin1name);
        }
        catch (UnsupportedEncodingException e) {
            throw new PngBadCharsetException(e);
        }
    }
    
    public static byte[] toBytesUTF8(final String x) {
        try {
            return x.getBytes(PngHelperInternal.charsetUTF8name);
        }
        catch (UnsupportedEncodingException e) {
            throw new PngBadCharsetException(e);
        }
    }
    
    public static String toStringUTF8(final byte[] x) {
        try {
            return new String(x, PngHelperInternal.charsetUTF8name);
        }
        catch (UnsupportedEncodingException e) {
            throw new PngBadCharsetException(e);
        }
    }
    
    public static String toStringUTF8(final byte[] x, final int offset, final int len) {
        try {
            return new String(x, offset, len, PngHelperInternal.charsetUTF8name);
        }
        catch (UnsupportedEncodingException e) {
            throw new PngBadCharsetException(e);
        }
    }
    
    public static boolean isCritical(final String id) {
        return Character.isUpperCase(id.charAt(0));
    }
    
    public static boolean isPublic(final String id) {
        return Character.isUpperCase(id.charAt(1));
    }
    
    public static boolean isSafeToCopy(final String id) {
        return !Character.isUpperCase(id.charAt(3));
    }
    
    public static boolean isUnknown(final PngChunk c) {
        return c instanceof PngChunkUNKNOWN;
    }
    
    public static int posNullByte(final byte[] b) {
        for (int i = 0; i < b.length; ++i) {
            if (b[i] == 0) {
                return i;
            }
        }
        return -1;
    }
    
    public static boolean shouldLoad(final String id, final ChunkLoadBehaviour behav) {
        if (isCritical(id)) {
            return true;
        }
        switch (behav) {
            case LOAD_CHUNK_ALWAYS: {
                return true;
            }
            case LOAD_CHUNK_IF_SAFE: {
                return isSafeToCopy(id);
            }
            case LOAD_CHUNK_NEVER: {
                return false;
            }
            default: {
                return false;
            }
        }
    }
    
    public static final byte[] compressBytes(final byte[] ori, final boolean compress) {
        return compressBytes(ori, 0, ori.length, compress);
    }
    
    public static byte[] compressBytes(final byte[] ori, final int offset, final int len, final boolean compress) {
        try {
            final ByteArrayInputStream inb = new ByteArrayInputStream(ori, offset, len);
            final InputStream in = compress ? inb : new InflaterInputStream(inb);
            final ByteArrayOutputStream outb = new ByteArrayOutputStream();
            final OutputStream out = compress ? new DeflaterOutputStream(outb) : outb;
            shovelInToOut(in, out);
            in.close();
            out.close();
            return outb.toByteArray();
        }
        catch (Exception e) {
            throw new PngjException(e);
        }
    }
    
    private static void shovelInToOut(final InputStream in, final OutputStream out) throws IOException {
        synchronized (ChunkHelper.tmpbuffer) {
            int len;
            while ((len = in.read(ChunkHelper.tmpbuffer)) > 0) {
                out.write(ChunkHelper.tmpbuffer, 0, len);
            }
        }
    }
    
    public static List<PngChunk> filterList(final List<PngChunk> target, final ChunkPredicate predicateKeep) {
        final List<PngChunk> result = new ArrayList<PngChunk>();
        for (final PngChunk element : target) {
            if (predicateKeep.match(element)) {
                result.add(element);
            }
        }
        return result;
    }
    
    public static int trimList(final List<PngChunk> target, final ChunkPredicate predicateRemove) {
        final Iterator<PngChunk> it = target.iterator();
        int cont = 0;
        while (it.hasNext()) {
            final PngChunk c = it.next();
            if (predicateRemove.match(c)) {
                it.remove();
                ++cont;
            }
        }
        return cont;
    }
    
    public static final boolean equivalent(final PngChunk c1, final PngChunk c2) {
        if (c1 == c2) {
            return true;
        }
        if (c1 == null || c2 == null || !c1.id.equals(c2.id)) {
            return false;
        }
        if (c1.crit) {
            return false;
        }
        if (c1.getClass() != c2.getClass()) {
            return false;
        }
        if (!c2.allowsMultiple()) {
            return true;
        }
        if (c1 instanceof PngChunkTextVar) {
            return ((PngChunkTextVar)c1).getKey().equals(((PngChunkTextVar)c2).getKey());
        }
        return c1 instanceof PngChunkSPLT && ((PngChunkSPLT)c1).getPalName().equals(((PngChunkSPLT)c2).getPalName());
    }
    
    public static boolean isText(final PngChunk c) {
        return c instanceof PngChunkTextVar;
    }
    
    static {
        b_IHDR = toBytes("IHDR");
        b_PLTE = toBytes("PLTE");
        b_IDAT = toBytes("IDAT");
        b_IEND = toBytes("IEND");
        ChunkHelper.tmpbuffer = new byte[4096];
    }
}
