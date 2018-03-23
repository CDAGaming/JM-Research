package ar.com.hjg.pngj;

public class ImageLineByte implements IImageLine, IImageLineArray
{
    public final ImageInfo imgInfo;
    final byte[] scanline;
    final byte[] scanline2;
    protected FilterType filterType;
    final int size;
    
    public ImageLineByte(final ImageInfo imgInfo) {
        this(imgInfo, null);
    }
    
    public ImageLineByte(final ImageInfo imgInfo, final byte[] sci) {
        this.imgInfo = imgInfo;
        this.filterType = FilterType.FILTER_UNKNOWN;
        this.size = imgInfo.samplesPerRow;
        this.scanline = ((sci != null && sci.length >= this.size) ? sci : new byte[this.size]);
        this.scanline2 = (byte[])((imgInfo.bitDepth == 16) ? new byte[this.size] : null);
    }
    
    public static IImageLineFactory<ImageLineByte> getFactory(final ImageInfo iminfo) {
        return new IImageLineFactory<ImageLineByte>() {
            public ImageLineByte createImageLine(final ImageInfo iminfo) {
                return new ImageLineByte(iminfo);
            }
        };
    }
    
    public FilterType getFilterUsed() {
        return this.filterType;
    }
    
    public byte[] getScanlineByte() {
        return this.scanline;
    }
    
    public byte[] getScanlineByte2() {
        return this.scanline2;
    }
    
    public String toString() {
        return " cols=" + this.imgInfo.cols + " bpc=" + this.imgInfo.bitDepth + " size=" + this.scanline.length;
    }
    
    public void readFromPngRaw(final byte[] raw, final int len, final int offset, final int step) {
        this.filterType = FilterType.getByVal(raw[0]);
        final int len2 = len - 1;
        final int step2 = (step - 1) * this.imgInfo.channels;
        if (this.imgInfo.bitDepth == 8) {
            if (step == 1) {
                System.arraycopy(raw, 1, this.scanline, 0, len2);
            }
            else {
                int s = 1;
                int c = 0;
                for (int i = offset * this.imgInfo.channels; s <= len2; ++s, ++i) {
                    this.scanline[i] = raw[s];
                    if (++c == this.imgInfo.channels) {
                        c = 0;
                        i += step2;
                    }
                }
            }
        }
        else if (this.imgInfo.bitDepth == 16) {
            if (step == 1) {
                int j = 0;
                int s2 = 1;
                while (j < this.imgInfo.samplesPerRow) {
                    this.scanline[j] = raw[s2++];
                    this.scanline2[j] = raw[s2++];
                    ++j;
                }
            }
            else {
                int s = 1;
                int c = 0;
                int i = (offset != 0) ? (offset * this.imgInfo.channels) : 0;
                while (s <= len2) {
                    this.scanline[i] = raw[s++];
                    this.scanline2[i] = raw[s++];
                    if (++c == this.imgInfo.channels) {
                        c = 0;
                        i += step2;
                    }
                    ++i;
                }
            }
        }
        else {
            final int bd = this.imgInfo.bitDepth;
            final int mask0 = ImageLineHelper.getMaskForPackedFormats(bd);
            int k = offset * this.imgInfo.channels;
            int r = 1;
            int c2 = 0;
            while (r < len) {
                int mask2 = mask0;
                int shi = 8 - bd;
                do {
                    this.scanline[k] = (byte)((raw[r] & mask2) >> shi);
                    mask2 >>= bd;
                    shi -= bd;
                    ++k;
                    if (++c2 == this.imgInfo.channels) {
                        c2 = 0;
                        k += step2;
                    }
                } while (mask2 != 0 && k < this.size);
                ++r;
            }
        }
    }
    
    public void writeToPngRaw(final byte[] raw) {
        raw[0] = (byte)this.filterType.val;
        if (this.imgInfo.bitDepth == 8) {
            System.arraycopy(this.scanline, 0, raw, 1, this.size);
            for (int i = 0; i < this.size; ++i) {
                raw[i + 1] = this.scanline[i];
            }
        }
        else if (this.imgInfo.bitDepth == 16) {
            int i = 0;
            int s = 1;
            while (i < this.size) {
                raw[s++] = this.scanline[i];
                raw[s++] = this.scanline2[i];
                ++i;
            }
        }
        else {
            final int bd = this.imgInfo.bitDepth;
            int shi = 8 - bd;
            int v = 0;
            int j = 0;
            int r = 1;
            while (j < this.size) {
                v |= this.scanline[j] << shi;
                shi -= bd;
                if (shi < 0 || j == this.size - 1) {
                    raw[r++] = (byte)v;
                    shi = 8 - bd;
                    v = 0;
                }
                ++j;
            }
        }
    }
    
    public void endReadFromPngRaw() {
    }
    
    public int getSize() {
        return this.size;
    }
    
    public int getElem(final int i) {
        return (this.scanline2 == null) ? (this.scanline[i] & 0xFF) : ((this.scanline[i] & 0xFF) << 8 | (this.scanline2[i] & 0xFF));
    }
    
    public byte[] getScanline() {
        return this.scanline;
    }
    
    public ImageInfo getImageInfo() {
        return this.imgInfo;
    }
    
    public FilterType getFilterType() {
        return this.filterType;
    }
}
