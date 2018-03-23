package ar.com.hjg.pngj;

public class ImageInfo
{
    public static final int MAX_COLS_ROW = 16777216;
    public final int cols;
    public final int rows;
    public final int bitDepth;
    public final int channels;
    public final boolean alpha;
    public final boolean greyscale;
    public final boolean indexed;
    public final boolean packed;
    public final int bitspPixel;
    public final int bytesPixel;
    public final int bytesPerRow;
    public final int samplesPerRow;
    public final int samplesPerRowPacked;
    private long totalPixels;
    private long totalRawBytes;
    
    public ImageInfo(final int cols, final int rows, final int bitdepth, final boolean alpha) {
        this(cols, rows, bitdepth, alpha, false, false);
    }
    
    public ImageInfo(final int cols, final int rows, final int bitdepth, final boolean alpha, final boolean grayscale, final boolean indexed) {
        this.totalPixels = -1L;
        this.totalRawBytes = -1L;
        this.cols = cols;
        this.rows = rows;
        this.alpha = alpha;
        this.indexed = indexed;
        this.greyscale = grayscale;
        if (this.greyscale && indexed) {
            throw new PngjException("palette and greyscale are mutually exclusive");
        }
        this.channels = ((grayscale || indexed) ? (alpha ? 2 : 1) : (alpha ? 4 : 3));
        this.bitDepth = bitdepth;
        this.packed = (bitdepth < 8);
        this.bitspPixel = this.channels * this.bitDepth;
        this.bytesPixel = (this.bitspPixel + 7) / 8;
        this.bytesPerRow = (this.bitspPixel * cols + 7) / 8;
        this.samplesPerRow = this.channels * this.cols;
        this.samplesPerRowPacked = (this.packed ? this.bytesPerRow : this.samplesPerRow);
        switch (this.bitDepth) {
            case 1:
            case 2:
            case 4: {
                if (!this.indexed && !this.greyscale) {
                    throw new PngjException("only indexed or grayscale can have bitdepth=" + this.bitDepth);
                }
                break;
            }
            case 8: {
                break;
            }
            case 16: {
                if (this.indexed) {
                    throw new PngjException("indexed can't have bitdepth=" + this.bitDepth);
                }
                break;
            }
            default: {
                throw new PngjException("invalid bitdepth=" + this.bitDepth);
            }
        }
        if (cols < 1 || cols > 16777216) {
            throw new PngjException("invalid cols=" + cols + " ???");
        }
        if (rows < 1 || rows > 16777216) {
            throw new PngjException("invalid rows=" + rows + " ???");
        }
        if (this.samplesPerRow < 1) {
            throw new PngjException("invalid image parameters (overflow?)");
        }
    }
    
    public long getTotalPixels() {
        if (this.totalPixels < 0L) {
            this.totalPixels = this.cols * this.rows;
        }
        return this.totalPixels;
    }
    
    public long getTotalRawBytes() {
        if (this.totalRawBytes < 0L) {
            this.totalRawBytes = (this.bytesPerRow + 1) * this.rows;
        }
        return this.totalRawBytes;
    }
    
    public String toString() {
        return "ImageInfo [cols=" + this.cols + ", rows=" + this.rows + ", bitDepth=" + this.bitDepth + ", channels=" + this.channels + ", alpha=" + this.alpha + ", greyscale=" + this.greyscale + ", indexed=" + this.indexed + "]";
    }
    
    public String toStringBrief() {
        return String.valueOf(this.cols) + "x" + this.rows + ((this.bitDepth != 8) ? ("d" + this.bitDepth) : "") + (this.alpha ? "a" : "") + (this.indexed ? "p" : "") + (this.greyscale ? "g" : "");
    }
    
    public String toStringDetail() {
        return "ImageInfo [cols=" + this.cols + ", rows=" + this.rows + ", bitDepth=" + this.bitDepth + ", channels=" + this.channels + ", bitspPixel=" + this.bitspPixel + ", bytesPixel=" + this.bytesPixel + ", bytesPerRow=" + this.bytesPerRow + ", samplesPerRow=" + this.samplesPerRow + ", samplesPerRowP=" + this.samplesPerRowPacked + ", alpha=" + this.alpha + ", greyscale=" + this.greyscale + ", indexed=" + this.indexed + ", packed=" + this.packed + "]";
    }
    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + (this.alpha ? 1231 : 1237);
        result = 31 * result + this.bitDepth;
        result = 31 * result + this.channels;
        result = 31 * result + this.cols;
        result = 31 * result + (this.greyscale ? 1231 : 1237);
        result = 31 * result + (this.indexed ? 1231 : 1237);
        result = 31 * result + this.rows;
        return result;
    }
    
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final ImageInfo other = (ImageInfo)obj;
        return this.alpha == other.alpha && this.bitDepth == other.bitDepth && this.channels == other.channels && this.cols == other.cols && this.greyscale == other.greyscale && this.indexed == other.indexed && this.rows == other.rows;
    }
}
