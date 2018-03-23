package ar.com.hjg.pngj;

import java.util.logging.*;
import java.nio.charset.*;
import java.io.*;

public final class PngHelperInternal
{
    public static final String KEY_LOGGER = "ar.com.pngj";
    public static final Logger LOGGER;
    public static String charsetLatin1name;
    public static Charset charsetLatin1;
    public static String charsetUTF8name;
    public static Charset charsetUTF8;
    private static ThreadLocal<Boolean> DEBUG;
    
    public static byte[] getPngIdSignature() {
        return new byte[] { -119, 80, 78, 71, 13, 10, 26, 10 };
    }
    
    public static int doubleToInt100000(final double d) {
        return (int)(d * 100000.0 + 0.5);
    }
    
    public static double intToDouble100000(final int i) {
        return i / 100000.0;
    }
    
    public static int readByte(final InputStream is) {
        try {
            return is.read();
        }
        catch (IOException e) {
            throw new PngjInputException("error reading byte", e);
        }
    }
    
    public static int readInt2(final InputStream is) {
        try {
            final int b1 = is.read();
            final int b2 = is.read();
            if (b1 == -1 || b2 == -1) {
                return -1;
            }
            return b1 << 8 | b2;
        }
        catch (IOException e) {
            throw new PngjInputException("error reading Int2", e);
        }
    }
    
    public static int readInt4(final InputStream is) {
        try {
            final int b1 = is.read();
            final int b2 = is.read();
            final int b3 = is.read();
            final int b4 = is.read();
            if (b1 == -1 || b2 == -1 || b3 == -1 || b4 == -1) {
                return -1;
            }
            return b1 << 24 | b2 << 16 | (b3 << 8) + b4;
        }
        catch (IOException e) {
            throw new PngjInputException("error reading Int4", e);
        }
    }
    
    public static int readInt1fromByte(final byte[] b, final int offset) {
        return b[offset] & 0xFF;
    }
    
    public static int readInt2fromBytes(final byte[] b, final int offset) {
        return (b[offset] & 0xFF) << 8 | (b[offset + 1] & 0xFF);
    }
    
    public static final int readInt4fromBytes(final byte[] b, final int offset) {
        return (b[offset] & 0xFF) << 24 | (b[offset + 1] & 0xFF) << 16 | (b[offset + 2] & 0xFF) << 8 | (b[offset + 3] & 0xFF);
    }
    
    public static void writeByte(final OutputStream os, final byte b) {
        try {
            os.write(b);
        }
        catch (IOException e) {
            throw new PngjOutputException(e);
        }
    }
    
    public static void writeByte(final OutputStream os, final byte[] bs) {
        try {
            os.write(bs);
        }
        catch (IOException e) {
            throw new PngjOutputException(e);
        }
    }
    
    public static void writeInt2(final OutputStream os, final int n) {
        final byte[] temp = { (byte)(n >> 8 & 0xFF), (byte)(n & 0xFF) };
        writeBytes(os, temp);
    }
    
    public static void writeInt4(final OutputStream os, final int n) {
        final byte[] temp = new byte[4];
        writeInt4tobytes(n, temp, 0);
        writeBytes(os, temp);
    }
    
    public static void writeInt2tobytes(final int n, final byte[] b, final int offset) {
        b[offset] = (byte)(n >> 8 & 0xFF);
        b[offset + 1] = (byte)(n & 0xFF);
    }
    
    public static void writeInt4tobytes(final int n, final byte[] b, final int offset) {
        b[offset] = (byte)(n >> 24 & 0xFF);
        b[offset + 1] = (byte)(n >> 16 & 0xFF);
        b[offset + 2] = (byte)(n >> 8 & 0xFF);
        b[offset + 3] = (byte)(n & 0xFF);
    }
    
    public static void readBytes(final InputStream is, final byte[] b, final int offset, final int len) {
        if (len == 0) {
            return;
        }
        try {
            int n;
            for (int read = 0; read < len; read += n) {
                n = is.read(b, offset + read, len - read);
                if (n < 1) {
                    throw new PngjInputException("error reading bytes, " + n + " !=" + len);
                }
            }
        }
        catch (IOException e) {
            throw new PngjInputException("error reading", e);
        }
    }
    
    public static void skipBytes(final InputStream is, long len) {
        try {
            while (len > 0L) {
                final long n1 = is.skip(len);
                if (n1 > 0L) {
                    len -= n1;
                }
                else {
                    if (n1 != 0L) {
                        throw new IOException("skip() returned a negative value ???");
                    }
                    if (is.read() == -1) {
                        break;
                    }
                    --len;
                }
            }
        }
        catch (IOException e) {
            throw new PngjInputException(e);
        }
    }
    
    public static void writeBytes(final OutputStream os, final byte[] b) {
        try {
            os.write(b);
        }
        catch (IOException e) {
            throw new PngjOutputException(e);
        }
    }
    
    public static void writeBytes(final OutputStream os, final byte[] b, final int offset, final int n) {
        try {
            os.write(b, offset, n);
        }
        catch (IOException e) {
            throw new PngjOutputException(e);
        }
    }
    
    public static void logdebug(final String msg) {
        if (isDebug()) {
            System.err.println("logdebug: " + msg);
        }
    }
    
    public static int filterRowNone(final int r) {
        return r & 0xFF;
    }
    
    public static int filterRowSub(final int r, final int left) {
        return r - left & 0xFF;
    }
    
    public static int filterRowUp(final int r, final int up) {
        return r - up & 0xFF;
    }
    
    public static int filterRowAverage(final int r, final int left, final int up) {
        return r - (left + up) / 2 & 0xFF;
    }
    
    public static int filterRowPaeth(final int r, final int left, final int up, final int upleft) {
        return r - filterPaethPredictor(left, up, upleft) & 0xFF;
    }
    
    static final int filterPaethPredictor(final int a, final int b, final int c) {
        final int p = a + b - c;
        final int pa = (p >= a) ? (p - a) : (a - p);
        final int pb = (p >= b) ? (p - b) : (b - p);
        final int pc = (p >= c) ? (p - c) : (c - p);
        if (pa <= pb && pa <= pc) {
            return a;
        }
        if (pb <= pc) {
            return b;
        }
        return c;
    }
    
    public static void debug(final Object obj) {
        debug(obj, 1, true);
    }
    
    static void debug(final Object obj, final int offset) {
        debug(obj, offset, true);
    }
    
    public static InputStream istreamFromFile(final File f) {
        FileInputStream is;
        try {
            is = new FileInputStream(f);
        }
        catch (Exception e) {
            throw new PngjInputException("Could not open " + f, e);
        }
        return is;
    }
    
    static OutputStream ostreamFromFile(final File f) {
        return ostreamFromFile(f, true);
    }
    
    static OutputStream ostreamFromFile(final File f, final boolean overwrite) {
        return PngHelperInternal2.ostreamFromFile(f, overwrite);
    }
    
    static void debug(final Object obj, final int offset, final boolean newLine) {
        final StackTraceElement ste = new Exception().getStackTrace()[1 + offset];
        String steStr = ste.getClassName();
        final int ind = steStr.lastIndexOf(46);
        steStr = steStr.substring(ind + 1);
        steStr = steStr + "." + ste.getMethodName() + "(" + ste.getLineNumber() + "): " + ((obj == null) ? null : obj.toString());
        System.err.println(steStr);
    }
    
    public static void setDebug(final boolean b) {
        PngHelperInternal.DEBUG.set(b);
    }
    
    public static boolean isDebug() {
        return PngHelperInternal.DEBUG.get();
    }
    
    public static long getDigest(final PngReader pngr) {
        return pngr.getSimpleDigest();
    }
    
    public static void initCrcForTests(final PngReader pngr) {
        pngr.prepareSimpleDigestComputation();
    }
    
    public static long getRawIdatBytes(final PngReader r) {
        return r.interlaced ? r.getChunkseq().getDeinterlacer().getTotalRawBytes() : r.imgInfo.getTotalRawBytes();
    }
    
    static {
        LOGGER = Logger.getLogger("ar.com.pngj");
        PngHelperInternal.charsetLatin1name = "ISO-8859-1";
        PngHelperInternal.charsetLatin1 = Charset.forName(PngHelperInternal.charsetLatin1name);
        PngHelperInternal.charsetUTF8name = "UTF-8";
        PngHelperInternal.charsetUTF8 = Charset.forName(PngHelperInternal.charsetUTF8name);
        PngHelperInternal.DEBUG = new ThreadLocal<Boolean>() {
            protected Boolean initialValue() {
                return Boolean.FALSE;
            }
        };
    }
}
