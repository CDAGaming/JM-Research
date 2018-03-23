package ar.com.hjg.pngj;

import ar.com.hjg.pngj.chunks.*;

public class ImageLineHelper
{
    static int[] DEPTH_UNPACK_1;
    static int[] DEPTH_UNPACK_2;
    static int[] DEPTH_UNPACK_4;
    static int[][] DEPTH_UNPACK;
    
    private static void initDepthScale() {
        ImageLineHelper.DEPTH_UNPACK_1 = new int[2];
        for (int i = 0; i < 2; ++i) {
            ImageLineHelper.DEPTH_UNPACK_1[i] = i * 255;
        }
        ImageLineHelper.DEPTH_UNPACK_2 = new int[4];
        for (int i = 0; i < 4; ++i) {
            ImageLineHelper.DEPTH_UNPACK_2[i] = i * 255 / 3;
        }
        ImageLineHelper.DEPTH_UNPACK_4 = new int[16];
        for (int i = 0; i < 16; ++i) {
            ImageLineHelper.DEPTH_UNPACK_4[i] = i * 255 / 15;
        }
        ImageLineHelper.DEPTH_UNPACK = new int[][] { null, ImageLineHelper.DEPTH_UNPACK_1, ImageLineHelper.DEPTH_UNPACK_2, null, ImageLineHelper.DEPTH_UNPACK_4 };
    }
    
    public static void scaleUp(final IImageLineArray line) {
        if (line.getImageInfo().indexed || line.getImageInfo().bitDepth >= 8) {
            return;
        }
        if (ImageLineHelper.DEPTH_UNPACK_1 == null || ImageLineHelper.DEPTH_UNPACK == null) {
            initDepthScale();
        }
        final int[] scaleArray = ImageLineHelper.DEPTH_UNPACK[line.getImageInfo().bitDepth];
        if (line instanceof ImageLineInt) {
            final ImageLineInt iline = (ImageLineInt)line;
            for (int i = 0; i < iline.getSize(); ++i) {
                iline.scanline[i] = scaleArray[iline.scanline[i]];
            }
        }
        else {
            if (!(line instanceof ImageLineByte)) {
                throw new PngjException("not implemented");
            }
            final ImageLineByte iline2 = (ImageLineByte)line;
            for (int i = 0; i < iline2.getSize(); ++i) {
                iline2.scanline[i] = (byte)scaleArray[iline2.scanline[i]];
            }
        }
    }
    
    public static void scaleDown(final IImageLineArray line) {
        if (line.getImageInfo().indexed || line.getImageInfo().bitDepth >= 8) {
            return;
        }
        if (line instanceof ImageLineInt) {
            final int scalefactor = 8 - line.getImageInfo().bitDepth;
            if (line instanceof ImageLineInt) {
                final ImageLineInt iline = (ImageLineInt)line;
                for (int i = 0; i < line.getSize(); ++i) {
                    iline.scanline[i] >>= scalefactor;
                }
            }
            else if (line instanceof ImageLineByte) {
                final ImageLineByte iline2 = (ImageLineByte)line;
                for (int i = 0; i < line.getSize(); ++i) {
                    iline2.scanline[i] = (byte)((iline2.scanline[i] & 0xFF) >> scalefactor);
                }
            }
            return;
        }
        throw new PngjException("not implemented");
    }
    
    public static byte scaleUp(final int bitdepth, final byte v) {
        return (bitdepth < 8) ? ((byte)ImageLineHelper.DEPTH_UNPACK[bitdepth][v]) : v;
    }
    
    public static byte scaleDown(final int bitdepth, final byte v) {
        return (bitdepth < 8) ? ((byte)(v >> 8 - bitdepth)) : v;
    }
    
    public static int[] palette2rgb(final ImageLineInt line, final PngChunkPLTE pal, final PngChunkTRNS trns, final int[] buf) {
        return palette2rgb(line, pal, trns, buf, false);
    }
    
    static int[] lineToARGB32(final ImageLineByte line, final PngChunkPLTE pal, final PngChunkTRNS trns, int[] buf) {
        final boolean alphachannel = line.imgInfo.alpha;
        final int cols = line.getImageInfo().cols;
        if (buf == null || buf.length < cols) {
            buf = new int[cols];
        }
        if (line.getImageInfo().indexed) {
            final int nindexesWithAlpha = (trns != null) ? trns.getPalletteAlpha().length : 0;
            for (int c = 0; c < cols; ++c) {
                final int index = line.scanline[c] & 0xFF;
                final int rgb = pal.getEntry(index);
                final int alpha = (index < nindexesWithAlpha) ? trns.getPalletteAlpha()[index] : 255;
                buf[c] = (alpha << 24 | rgb);
            }
        }
        else if (line.imgInfo.greyscale) {
            final int ga = (trns != null) ? trns.getGray() : -1;
            int c2 = 0;
            int c3 = 0;
            while (c2 < cols) {
                final int g = line.scanline[c3++] & 0xFF;
                final int alpha = alphachannel ? (line.scanline[c3++] & 0xFF) : ((g != ga) ? 255 : 0);
                buf[c2] = (alpha << 24 | g | g << 8 | g << 16);
                ++c2;
            }
        }
        else {
            final int ga = (trns != null) ? trns.getRGB888() : -1;
            int c2 = 0;
            int c3 = 0;
            while (c2 < cols) {
                final int rgb = (line.scanline[c3++] & 0xFF) << 16 | (line.scanline[c3++] & 0xFF) << 8 | (line.scanline[c3++] & 0xFF);
                final int alpha = alphachannel ? (line.scanline[c3++] & 0xFF) : ((rgb != ga) ? 255 : 0);
                buf[c2] = (alpha << 24 | rgb);
                ++c2;
            }
        }
        return buf;
    }
    
    static byte[] lineToRGBA8888(final ImageLineByte line, final PngChunkPLTE pal, final PngChunkTRNS trns, byte[] buf) {
        final boolean alphachannel = line.imgInfo.alpha;
        final int cols = line.imgInfo.cols;
        final int bytes = cols * 4;
        if (buf == null || buf.length < bytes) {
            buf = new byte[bytes];
        }
        if (line.imgInfo.indexed) {
            final int nindexesWithAlpha = (trns != null) ? trns.getPalletteAlpha().length : 0;
            int c = 0;
            int b = 0;
            while (c < cols) {
                final int index = line.scanline[c] & 0xFF;
                final int rgb = pal.getEntry(index);
                buf[b++] = (byte)(rgb >> 16 & 0xFF);
                buf[b++] = (byte)(rgb >> 8 & 0xFF);
                buf[b++] = (byte)(rgb & 0xFF);
                buf[b++] = (byte)((index < nindexesWithAlpha) ? trns.getPalletteAlpha()[index] : 255);
                ++c;
            }
        }
        else if (line.imgInfo.greyscale) {
            byte val;
            for (int ga = (trns != null) ? trns.getGray() : -1, c2 = 0, b2 = 0; b2 < bytes; buf[b2++] = val, buf[b2++] = val, buf[b2++] = val, buf[b2++] = (byte)(alphachannel ? line.scanline[c2++] : (((val & 0xFF) == ga) ? 0 : -1))) {
                val = line.scanline[c2++];
            }
        }
        else if (alphachannel) {
            System.arraycopy(line.scanline, 0, buf, 0, bytes);
        }
        else {
            int c2 = 0;
            int b2 = 0;
            while (b2 < bytes) {
                buf[b2++] = line.scanline[c2++];
                buf[b2++] = line.scanline[c2++];
                buf[b2++] = line.scanline[c2++];
                buf[b2++] = -1;
                if (trns != null && buf[b2 - 3] == (byte)trns.getRGB()[0] && buf[b2 - 2] == (byte)trns.getRGB()[1] && buf[b2 - 1] == (byte)trns.getRGB()[2]) {
                    buf[b2 - 1] = 0;
                }
            }
        }
        return buf;
    }
    
    static byte[] lineToRGB888(final ImageLineByte line, final PngChunkPLTE pal, byte[] buf) {
        final boolean alphachannel = line.imgInfo.alpha;
        final int cols = line.imgInfo.cols;
        final int bytes = cols * 3;
        if (buf == null || buf.length < bytes) {
            buf = new byte[bytes];
        }
        final int[] rgb = new int[3];
        if (line.imgInfo.indexed) {
            int c = 0;
            int b = 0;
            while (c < cols) {
                pal.getEntryRgb(line.scanline[c] & 0xFF, rgb);
                buf[b++] = (byte)rgb[0];
                buf[b++] = (byte)rgb[1];
                buf[b++] = (byte)rgb[2];
                ++c;
            }
        }
        else if (line.imgInfo.greyscale) {
            int c = 0;
            int b = 0;
            while (b < bytes) {
                final byte val = line.scanline[c++];
                buf[b++] = val;
                buf[b++] = val;
                buf[b++] = val;
                if (alphachannel) {
                    ++c;
                }
            }
        }
        else if (!alphachannel) {
            System.arraycopy(line.scanline, 0, buf, 0, bytes);
        }
        else {
            for (int c = 0, b = 0; b < bytes; buf[b++] = line.scanline[c++], buf[b++] = line.scanline[c++], buf[b++] = line.scanline[c++], ++c) {}
        }
        return buf;
    }
    
    public static int[] palette2rgba(final ImageLineInt line, final PngChunkPLTE pal, final PngChunkTRNS trns, final int[] buf) {
        return palette2rgb(line, pal, trns, buf, true);
    }
    
    public static int[] palette2rgb(final ImageLineInt line, final PngChunkPLTE pal, final int[] buf) {
        return palette2rgb(line, pal, null, buf, false);
    }
    
    private static int[] palette2rgb(final IImageLine line, final PngChunkPLTE pal, final PngChunkTRNS trns, int[] buf, final boolean alphaForced) {
        final boolean isalpha = trns != null;
        final int channels = isalpha ? 4 : 3;
        final ImageLineInt linei = (ImageLineInt)((line instanceof ImageLineInt) ? line : null);
        final ImageLineByte lineb = (ImageLineByte)((line instanceof ImageLineByte) ? line : null);
        final boolean isbyte = lineb != null;
        final int cols = (linei != null) ? linei.imgInfo.cols : lineb.imgInfo.cols;
        final int nsamples = cols * channels;
        if (buf == null || buf.length < nsamples) {
            buf = new int[nsamples];
        }
        final int nindexesWithAlpha = (trns != null) ? trns.getPalletteAlpha().length : 0;
        for (int c = 0; c < cols; ++c) {
            final int index = isbyte ? (lineb.scanline[c] & 0xFF) : linei.scanline[c];
            pal.getEntryRgb(index, buf, c * channels);
            if (isalpha) {
                final int alpha = (index < nindexesWithAlpha) ? trns.getPalletteAlpha()[index] : 255;
                buf[c * channels + 3] = alpha;
            }
        }
        return buf;
    }
    
    public static String infoFirstLastPixels(final ImageLineInt line) {
        return (line.imgInfo.channels == 1) ? String.format("first=(%d) last=(%d)", line.scanline[0], line.scanline[line.scanline.length - 1]) : String.format("first=(%d %d %d) last=(%d %d %d)", line.scanline[0], line.scanline[1], line.scanline[2], line.scanline[line.scanline.length - line.imgInfo.channels], line.scanline[line.scanline.length - line.imgInfo.channels + 1], line.scanline[line.scanline.length - line.imgInfo.channels + 2]);
    }
    
    public static int getPixelRGB8(final IImageLine line, final int column) {
        if (line instanceof ImageLineInt) {
            final int offset = column * ((ImageLineInt)line).imgInfo.channels;
            final int[] scanline = ((ImageLineInt)line).getScanline();
            return scanline[offset] << 16 | scanline[offset + 1] << 8 | scanline[offset + 2];
        }
        if (line instanceof ImageLineByte) {
            final int offset = column * ((ImageLineByte)line).imgInfo.channels;
            final byte[] scanline2 = ((ImageLineByte)line).getScanline();
            return (scanline2[offset] & 0xFF) << 16 | (scanline2[offset + 1] & 0xFF) << 8 | (scanline2[offset + 2] & 0xFF);
        }
        throw new PngjException("Not supported " + line.getClass());
    }
    
    public static int getPixelARGB8(final IImageLine line, final int column) {
        if (line instanceof ImageLineInt) {
            final int offset = column * ((ImageLineInt)line).imgInfo.channels;
            final int[] scanline = ((ImageLineInt)line).getScanline();
            return scanline[offset + 3] << 24 | scanline[offset] << 16 | scanline[offset + 1] << 8 | scanline[offset + 2];
        }
        if (line instanceof ImageLineByte) {
            final int offset = column * ((ImageLineByte)line).imgInfo.channels;
            final byte[] scanline2 = ((ImageLineByte)line).getScanline();
            return (scanline2[offset + 3] & 0xFF) << 24 | (scanline2[offset] & 0xFF) << 16 | (scanline2[offset + 1] & 0xFF) << 8 | (scanline2[offset + 2] & 0xFF);
        }
        throw new PngjException("Not supported " + line.getClass());
    }
    
    public static void setPixelsRGB8(final ImageLineInt line, final int[] rgb) {
        int i = 0;
        int j = 0;
        while (i < line.imgInfo.cols) {
            line.scanline[j++] = (rgb[i] >> 16 & 0xFF);
            line.scanline[j++] = (rgb[i] >> 8 & 0xFF);
            line.scanline[j++] = (rgb[i] & 0xFF);
            ++i;
        }
    }
    
    public static void setPixelRGB8(final ImageLineInt line, int col, final int r, final int g, final int b) {
        col *= line.imgInfo.channels;
        line.scanline[col++] = r;
        line.scanline[col++] = g;
        line.scanline[col] = b;
    }
    
    public static void setPixelRGB8(final ImageLineInt line, final int col, final int rgb) {
        setPixelRGB8(line, col, rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
    }
    
    public static void setPixelsRGBA8(final ImageLineInt line, final int[] rgb) {
        int i = 0;
        int j = 0;
        while (i < line.imgInfo.cols) {
            line.scanline[j++] = (rgb[i] >> 16 & 0xFF);
            line.scanline[j++] = (rgb[i] >> 8 & 0xFF);
            line.scanline[j++] = (rgb[i] & 0xFF);
            line.scanline[j++] = (rgb[i] >> 24 & 0xFF);
            ++i;
        }
    }
    
    public static void setPixelRGBA8(final ImageLineInt line, int col, final int r, final int g, final int b, final int a) {
        col *= line.imgInfo.channels;
        line.scanline[col++] = r;
        line.scanline[col++] = g;
        line.scanline[col++] = b;
        line.scanline[col] = a;
    }
    
    public static void setPixelRGBA8(final ImageLineInt line, final int col, final int rgb) {
        setPixelRGBA8(line, col, rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, rgb >> 24 & 0xFF);
    }
    
    public static void setValD(final ImageLineInt line, final int i, final double d) {
        line.scanline[i] = double2int(line, d);
    }
    
    public static int interpol(final int a, final int b, final int c, final int d, final double dx, final double dy) {
        final double e = a * (1.0 - dx) + b * dx;
        final double f = c * (1.0 - dx) + d * dx;
        return (int)(e * (1.0 - dy) + f * dy + 0.5);
    }
    
    public static double int2double(final ImageLineInt line, final int p) {
        return (line.imgInfo.bitDepth == 16) ? (p / 65535.0) : (p / 255.0);
    }
    
    public static double int2doubleClamped(final ImageLineInt line, final int p) {
        final double d = (line.imgInfo.bitDepth == 16) ? (p / 65535.0) : (p / 255.0);
        return (d <= 0.0) ? 0.0 : ((d >= 1.0) ? 1.0 : d);
    }
    
    public static int double2int(final ImageLineInt line, double d) {
        d = ((d <= 0.0) ? 0.0 : ((d >= 1.0) ? 1.0 : d));
        return (line.imgInfo.bitDepth == 16) ? ((int)(d * 65535.0 + 0.5)) : ((int)(d * 255.0 + 0.5));
    }
    
    public static int double2intClamped(final ImageLineInt line, double d) {
        d = ((d <= 0.0) ? 0.0 : ((d >= 1.0) ? 1.0 : d));
        return (line.imgInfo.bitDepth == 16) ? ((int)(d * 65535.0 + 0.5)) : ((int)(d * 255.0 + 0.5));
    }
    
    public static int clampTo_0_255(final int i) {
        return (i > 255) ? 255 : ((i < 0) ? 0 : i);
    }
    
    public static int clampTo_0_65535(final int i) {
        return (i > 65535) ? 65535 : ((i < 0) ? 0 : i);
    }
    
    public static int clampTo_128_127(final int x) {
        return (x > 127) ? 127 : ((x < -128) ? -128 : x);
    }
    
    static int getMaskForPackedFormats(final int bitDepth) {
        if (bitDepth == 4) {
            return 240;
        }
        if (bitDepth == 2) {
            return 192;
        }
        return 128;
    }
    
    static int getMaskForPackedFormatsLs(final int bitDepth) {
        if (bitDepth == 4) {
            return 15;
        }
        if (bitDepth == 2) {
            return 3;
        }
        return 1;
    }
}
