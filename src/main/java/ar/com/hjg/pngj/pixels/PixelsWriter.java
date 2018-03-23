package ar.com.hjg.pngj.pixels;

import java.io.*;
import ar.com.hjg.pngj.*;

public abstract class PixelsWriter
{
    protected final ImageInfo imgInfo;
    protected final int buflen;
    protected final int bytesPixel;
    protected final int bytesRow;
    private CompressorStream compressorStream;
    protected int deflaterCompLevel;
    protected int deflaterStrategy;
    protected boolean initdone;
    protected FilterType filterType;
    private int[] filtersUsed;
    private OutputStream os;
    protected int currentRow;
    
    public PixelsWriter(final ImageInfo imgInfo) {
        this.deflaterCompLevel = 6;
        this.deflaterStrategy = 0;
        this.initdone = false;
        this.filtersUsed = new int[5];
        this.imgInfo = imgInfo;
        this.bytesRow = imgInfo.bytesPerRow;
        this.buflen = this.bytesRow + 1;
        this.bytesPixel = imgInfo.bytesPixel;
        this.currentRow = -1;
        this.filterType = FilterType.FILTER_DEFAULT;
    }
    
    public final void processRow(final byte[] rowb) {
        if (!this.initdone) {
            this.init();
        }
        ++this.currentRow;
        this.filterAndWrite(rowb);
    }
    
    protected void sendToCompressedStream(final byte[] rowf) {
        this.compressorStream.write(rowf, 0, rowf.length);
        final int[] filtersUsed = this.filtersUsed;
        final byte b = rowf[0];
        ++filtersUsed[b];
    }
    
    protected abstract void filterAndWrite(final byte[] p0);
    
    protected final byte[] filterRowWithFilterType(final FilterType _filterType, final byte[] _rowb, final byte[] _rowbprev, byte[] _rowf) {
        if (_filterType == FilterType.FILTER_NONE) {
            _rowf = _rowb;
        }
        _rowf[0] = (byte)_filterType.val;
        switch (_filterType) {
            case FILTER_NONE: {
                break;
            }
            case FILTER_PAETH: {
                for (int i = 1; i <= this.bytesPixel; ++i) {
                    _rowf[i] = (byte)PngHelperInternal.filterRowPaeth(_rowb[i], 0, _rowbprev[i] & 0xFF, 0);
                }
                for (int j = 1, i = this.bytesPixel + 1; i <= this.bytesRow; ++i, ++j) {
                    _rowf[i] = (byte)PngHelperInternal.filterRowPaeth(_rowb[i], _rowb[j] & 0xFF, _rowbprev[i] & 0xFF, _rowbprev[j] & 0xFF);
                }
                break;
            }
            case FILTER_SUB: {
                for (int i = 1; i <= this.bytesPixel; ++i) {
                    _rowf[i] = _rowb[i];
                }
                for (int j = 1, i = this.bytesPixel + 1; i <= this.bytesRow; ++i, ++j) {
                    _rowf[i] = (byte)(_rowb[i] - _rowb[j]);
                }
                break;
            }
            case FILTER_AVERAGE: {
                for (int i = 1; i <= this.bytesPixel; ++i) {
                    _rowf[i] = (byte)(_rowb[i] - (_rowbprev[i] & 0xFF) / 2);
                }
                for (int j = 1, i = this.bytesPixel + 1; i <= this.bytesRow; ++i, ++j) {
                    _rowf[i] = (byte)(_rowb[i] - ((_rowbprev[i] & 0xFF) + (_rowb[j] & 0xFF)) / 2);
                }
                break;
            }
            case FILTER_UP: {
                for (int i = 1; i <= this.bytesRow; ++i) {
                    _rowf[i] = (byte)(_rowb[i] - _rowbprev[i]);
                }
                break;
            }
            default: {
                throw new PngjOutputException("Filter type not recognized: " + _filterType);
            }
        }
        return _rowf;
    }
    
    public abstract byte[] getRowb();
    
    protected final void init() {
        if (!this.initdone) {
            this.initParams();
            this.initdone = true;
        }
    }
    
    protected void initParams() {
        if (this.compressorStream == null) {
            this.compressorStream = new CompressorStreamDeflater(this.os, this.buflen, this.imgInfo.getTotalRawBytes(), this.deflaterCompLevel, this.deflaterStrategy);
        }
    }
    
    public void close() {
        if (this.compressorStream != null) {
            this.compressorStream.close();
        }
    }
    
    public void setDeflaterStrategy(final Integer deflaterStrategy) {
        this.deflaterStrategy = deflaterStrategy;
    }
    
    public void setDeflaterCompLevel(final Integer deflaterCompLevel) {
        this.deflaterCompLevel = deflaterCompLevel;
    }
    
    public Integer getDeflaterCompLevel() {
        return this.deflaterCompLevel;
    }
    
    public final void setOs(final OutputStream datStream) {
        this.os = datStream;
    }
    
    public OutputStream getOs() {
        return this.os;
    }
    
    public final FilterType getFilterType() {
        return this.filterType;
    }
    
    public final void setFilterType(final FilterType filterType) {
        this.filterType = filterType;
    }
    
    public double getCompression() {
        return this.compressorStream.isDone() ? this.compressorStream.getCompressionRatio() : 1.0;
    }
    
    public void setCompressorStream(final CompressorStream compressorStream) {
        this.compressorStream = compressorStream;
    }
    
    public long getTotalBytesToWrite() {
        return this.imgInfo.getTotalRawBytes();
    }
    
    protected FilterType getDefaultFilter() {
        if (this.imgInfo.indexed || this.imgInfo.bitDepth < 8) {
            return FilterType.FILTER_NONE;
        }
        if (this.imgInfo.getTotalPixels() < 1024L) {
            return FilterType.FILTER_NONE;
        }
        if (this.imgInfo.rows == 1) {
            return FilterType.FILTER_SUB;
        }
        if (this.imgInfo.cols == 1) {
            return FilterType.FILTER_UP;
        }
        return FilterType.FILTER_PAETH;
    }
    
    public final String getFiltersUsed() {
        return String.format("%d,%d,%d,%d,%d", (int)(this.filtersUsed[0] * 100.0 / this.imgInfo.rows + 0.5), (int)(this.filtersUsed[1] * 100.0 / this.imgInfo.rows + 0.5), (int)(this.filtersUsed[2] * 100.0 / this.imgInfo.rows + 0.5), (int)(this.filtersUsed[3] * 100.0 / this.imgInfo.rows + 0.5), (int)(this.filtersUsed[4] * 100.0 / this.imgInfo.rows + 0.5));
    }
}
