package ar.com.hjg.pngj;

public class ImageLineInt implements IImageLine, IImageLineArray
{
    public final ImageInfo imgInfo;
    protected final int[] scanline;
    protected final int size;
    protected FilterType filterType;
    
    public ImageLineInt(final ImageInfo imgInfo) {
        this(imgInfo, null);
    }
    
    public ImageLineInt(final ImageInfo imgInfo, final int[] sci) {
        this.filterType = FilterType.FILTER_UNKNOWN;
        this.imgInfo = imgInfo;
        this.filterType = FilterType.FILTER_UNKNOWN;
        this.size = imgInfo.samplesPerRow;
        this.scanline = ((sci != null && sci.length >= this.size) ? sci : new int[this.size]);
    }
    
    public FilterType getFilterType() {
        return this.filterType;
    }
    
    protected void setFilterType(final FilterType ft) {
        this.filterType = ft;
    }
    
    public String toString() {
        return " cols=" + this.imgInfo.cols + " bpc=" + this.imgInfo.bitDepth + " size=" + this.scanline.length;
    }
    
    public void readFromPngRaw(final byte[] raw, final int len, final int offset, final int step) {
        this.setFilterType(FilterType.getByVal(raw[0]));
        final int len2 = len - 1;
        final int step2 = (step - 1) * this.imgInfo.channels;
        if (this.imgInfo.bitDepth == 8) {
            if (step == 1) {
                for (int i = 0; i < this.size; ++i) {
                    this.scanline[i] = (raw[i + 1] & 0xFF);
                }
            }
            else {
                int s = 1;
                int c = 0;
                for (int j = offset * this.imgInfo.channels; s <= len2; ++s, ++j) {
                    this.scanline[j] = (raw[s] & 0xFF);
                    if (++c == this.imgInfo.channels) {
                        c = 0;
                        j += step2;
                    }
                }
            }
        }
        else if (this.imgInfo.bitDepth == 16) {
            if (step == 1) {
                int i = 0;
                int s2 = 1;
                while (i < this.size) {
                    this.scanline[i] = ((raw[s2++] & 0xFF) << 8 | (raw[s2++] & 0xFF));
                    ++i;
                }
            }
            else {
                int s = 1;
                int c = 0;
                for (int j = (offset != 0) ? (offset * this.imgInfo.channels) : 0; s <= len2; ++s, ++j) {
                    this.scanline[j] = ((raw[s++] & 0xFF) << 8 | (raw[s] & 0xFF));
                    if (++c == this.imgInfo.channels) {
                        c = 0;
                        j += step2;
                    }
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
                    this.scanline[k] = (raw[r] & mask2) >> shi;
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
            for (int i = 0; i < this.size; ++i) {
                raw[i + 1] = (byte)this.scanline[i];
            }
        }
        else if (this.imgInfo.bitDepth == 16) {
            int i = 0;
            int s = 1;
            while (i < this.size) {
                raw[s++] = (byte)(this.scanline[i] >> 8);
                raw[s++] = (byte)(this.scanline[i] & 0xFF);
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
        return this.scanline[i];
    }
    
    public int[] getScanline() {
        return this.scanline;
    }
    
    public ImageInfo getImageInfo() {
        return this.imgInfo;
    }
    
    public static IImageLineFactory<ImageLineInt> getFactory(final ImageInfo iminfo) {
        return new IImageLineFactory<ImageLineInt>() {
            public ImageLineInt createImageLine(final ImageInfo iminfo) {
                return new ImageLineInt(iminfo);
            }
        };
    }
}
