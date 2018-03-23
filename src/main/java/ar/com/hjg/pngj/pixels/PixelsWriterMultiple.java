package ar.com.hjg.pngj.pixels;

import java.util.*;
import java.io.*;
import ar.com.hjg.pngj.*;

public class PixelsWriterMultiple extends PixelsWriter
{
    protected LinkedList<byte[]> rows;
    protected CompressorStream[] filterBank;
    protected byte[][] filteredRows;
    protected byte[] filteredRowTmp;
    protected FiltersPerformance filtersPerf;
    protected int rowsPerBand;
    protected int rowsPerBandCurrent;
    protected int rowInBand;
    protected int bandNum;
    protected int firstRowInThisBand;
    protected int lastRowInThisBand;
    private boolean tryAdaptive;
    protected static final int HINT_MEMORY_DEFAULT_KB = 100;
    protected int hintMemoryKb;
    private int hintRowsPerBand;
    private boolean useLz4;
    
    public PixelsWriterMultiple(final ImageInfo imgInfo) {
        super(imgInfo);
        this.filterBank = new CompressorStream[6];
        this.filteredRows = new byte[5][];
        this.rowsPerBand = 0;
        this.rowsPerBandCurrent = 0;
        this.rowInBand = -1;
        this.bandNum = -1;
        this.tryAdaptive = true;
        this.hintMemoryKb = 100;
        this.hintRowsPerBand = 1000;
        this.useLz4 = true;
        this.filtersPerf = new FiltersPerformance(imgInfo);
        this.rows = new LinkedList<byte[]>();
        for (int i = 0; i < 2; ++i) {
            this.rows.add(new byte[this.buflen]);
        }
        this.filteredRowTmp = new byte[this.buflen];
    }
    
    protected void filterAndWrite(final byte[] rowb) {
        if (!this.initdone) {
            this.init();
        }
        if (rowb != this.rows.get(0)) {
            throw new RuntimeException("?");
        }
        this.setBandFromNewRown();
        final byte[] rowbprev = this.rows.get(1);
        for (final FilterType ftype : FilterType.getAllStandardNoneLast()) {
            if (this.currentRow != 0 || ftype == FilterType.FILTER_NONE || ftype == FilterType.FILTER_SUB) {
                final byte[] filtered = this.filterRowWithFilterType(ftype, rowb, rowbprev, this.filteredRows[ftype.val]);
                this.filterBank[ftype.val].write(filtered);
                if (this.currentRow == 0 && ftype == FilterType.FILTER_SUB) {
                    this.filterBank[FilterType.FILTER_PAETH.val].write(filtered);
                    this.filterBank[FilterType.FILTER_AVERAGE.val].write(filtered);
                    this.filterBank[FilterType.FILTER_UP.val].write(filtered);
                }
                if (this.tryAdaptive) {
                    this.filtersPerf.updateFromFiltered(ftype, filtered, this.currentRow);
                }
            }
        }
        this.filteredRows[0] = rowb;
        if (this.tryAdaptive) {
            final FilterType preferredAdaptive = this.filtersPerf.getPreferred();
            this.filterBank[5].write(this.filteredRows[preferredAdaptive.val]);
        }
        if (this.currentRow == this.lastRowInThisBand) {
            final int best = this.getBestCompressor();
            final byte[] filtersAdapt = this.filterBank[best].getFirstBytes();
            for (int r = this.firstRowInThisBand, i = 0, j = this.lastRowInThisBand - this.firstRowInThisBand; r <= this.lastRowInThisBand; ++r, --j, ++i) {
                final int fti = filtersAdapt[i];
                byte[] filtered2 = null;
                if (r != this.lastRowInThisBand) {
                    filtered2 = this.filterRowWithFilterType(FilterType.getByVal(fti), this.rows.get(j), this.rows.get(j + 1), this.filteredRowTmp);
                }
                else {
                    filtered2 = this.filteredRows[fti];
                }
                this.sendToCompressedStream(filtered2);
            }
        }
        if (this.rows.size() > this.rowsPerBandCurrent) {
            this.rows.addFirst(this.rows.removeLast());
        }
        else {
            this.rows.addFirst(new byte[this.buflen]);
        }
    }
    
    public byte[] getRowb() {
        return this.rows.get(0);
    }
    
    private void setBandFromNewRown() {
        final boolean newBand = this.currentRow == 0 || this.currentRow > this.lastRowInThisBand;
        if (this.currentRow == 0) {
            this.bandNum = -1;
        }
        if (newBand) {
            ++this.bandNum;
            this.rowInBand = 0;
        }
        else {
            ++this.rowInBand;
        }
        if (newBand) {
            this.firstRowInThisBand = this.currentRow;
            this.lastRowInThisBand = this.firstRowInThisBand + this.rowsPerBand - 1;
            final int lastRowInNextBand = this.firstRowInThisBand + 2 * this.rowsPerBand - 1;
            if (lastRowInNextBand >= this.imgInfo.rows) {
                this.lastRowInThisBand = this.imgInfo.rows - 1;
            }
            this.rowsPerBandCurrent = 1 + this.lastRowInThisBand - this.firstRowInThisBand;
            this.tryAdaptive = (this.rowsPerBandCurrent > 3 && (this.rowsPerBandCurrent >= 10 || this.imgInfo.bytesPerRow >= 64));
            this.rebuildFiltersBank();
        }
    }
    
    private void rebuildFiltersBank() {
        final long bytesPerBandCurrent = this.rowsPerBandCurrent * this.buflen;
        final int DEFLATER_COMP_LEVEL = 4;
        for (int i = 0; i <= 5; ++i) {
            CompressorStream cp = this.filterBank[i];
            if (cp == null || cp.totalbytes != bytesPerBandCurrent) {
                if (cp != null) {
                    cp.close();
                }
                if (this.useLz4) {
                    cp = new CompressorStreamLz4(null, this.buflen, bytesPerBandCurrent);
                }
                else {
                    cp = new CompressorStreamDeflater(null, this.buflen, bytesPerBandCurrent, 4, 0);
                }
                this.filterBank[i] = cp;
            }
            else {
                cp.reset();
            }
            cp.setStoreFirstByte(true, this.rowsPerBandCurrent);
        }
    }
    
    private int computeInitialRowsPerBand() {
        int r = (int)(this.hintMemoryKb * 1024.0 / (this.imgInfo.bytesPerRow + 1) - 5.0);
        if (r < 1) {
            r = 1;
        }
        if (this.hintRowsPerBand > 0 && r > this.hintRowsPerBand) {
            r = this.hintRowsPerBand;
        }
        if (r > this.imgInfo.rows) {
            r = this.imgInfo.rows;
        }
        if (r > 2 && r > this.imgInfo.rows / 8) {
            final int k = (this.imgInfo.rows + (r - 1)) / r;
            r = (this.imgInfo.rows + k / 2) / k;
        }
        PngHelperInternal.debug("rows :" + r + "/" + this.imgInfo.rows);
        return r;
    }
    
    private int getBestCompressor() {
        double bestcr = Double.MAX_VALUE;
        int bestb = -1;
        for (int i = this.tryAdaptive ? 5 : 4; i >= 0; --i) {
            final CompressorStream fb = this.filterBank[i];
            final double cr = fb.getCompressionRatio();
            if (cr <= bestcr) {
                bestb = i;
                bestcr = cr;
            }
        }
        return bestb;
    }
    
    protected void initParams() {
        if (this.imgInfo.cols < 3 && !FilterType.isValidStandard(this.filterType)) {
            this.filterType = FilterType.FILTER_DEFAULT;
        }
        if (this.imgInfo.rows < 3 && !FilterType.isValidStandard(this.filterType)) {
            this.filterType = FilterType.FILTER_DEFAULT;
        }
        for (int i = 1; i <= 4; ++i) {
            if (this.filteredRows[i] == null || this.filteredRows[i].length < this.buflen) {
                this.filteredRows[i] = new byte[this.buflen];
            }
        }
        if (this.rowsPerBand == 0) {
            this.rowsPerBand = this.computeInitialRowsPerBand();
        }
    }
    
    public void close() {
        super.close();
        this.rows.clear();
        for (final CompressorStream f : this.filterBank) {
            f.close();
        }
    }
    
    public void setHintMemoryKb(final int hintMemoryKb) {
        this.hintMemoryKb = ((hintMemoryKb <= 0) ? 100 : ((hintMemoryKb > 10000) ? 10000 : hintMemoryKb));
    }
    
    public void setHintRowsPerBand(final int hintRowsPerBand) {
        this.hintRowsPerBand = hintRowsPerBand;
    }
    
    public void setUseLz4(final boolean lz4) {
        this.useLz4 = lz4;
    }
    
    public FiltersPerformance getFiltersPerf() {
        return this.filtersPerf;
    }
}
