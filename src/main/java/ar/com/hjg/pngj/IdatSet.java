package ar.com.hjg.pngj;

import java.util.*;
import java.util.zip.*;

public class IdatSet extends DeflatedChunksSet
{
    protected byte[] rowUnfiltered;
    protected byte[] rowUnfilteredPrev;
    protected final ImageInfo imgInfo;
    protected final Deinterlacer deinterlacer;
    final RowInfo rowinfo;
    protected int[] filterUseStat;
    
    public IdatSet(final String id, final ImageInfo iminfo, final Deinterlacer deinterlacer) {
        this(id, iminfo, deinterlacer, null, null);
    }
    
    public IdatSet(final String id, final ImageInfo iminfo, final Deinterlacer deinterlacer, final Inflater inf, final byte[] buffer) {
        super(id, (deinterlacer != null) ? (deinterlacer.getBytesToRead() + 1) : (iminfo.bytesPerRow + 1), iminfo.bytesPerRow + 1, inf, buffer);
        this.filterUseStat = new int[5];
        this.imgInfo = iminfo;
        this.deinterlacer = deinterlacer;
        this.rowinfo = new RowInfo(iminfo, deinterlacer);
    }
    
    public void unfilterRow() {
        this.unfilterRow(this.rowinfo.bytesRow);
    }
    
    protected void unfilterRow(final int nbytes) {
        if (this.rowUnfiltered == null || this.rowUnfiltered.length < this.row.length) {
            this.rowUnfiltered = new byte[this.row.length];
            this.rowUnfilteredPrev = new byte[this.row.length];
        }
        if (this.rowinfo.rowNsubImg == 0) {
            Arrays.fill(this.rowUnfiltered, (byte)0);
        }
        final byte[] tmp = this.rowUnfiltered;
        this.rowUnfiltered = this.rowUnfilteredPrev;
        this.rowUnfilteredPrev = tmp;
        final int ftn = this.row[0];
        final FilterType ft = FilterType.getByVal(ftn);
        if (ft == null) {
            throw new PngjInputException("Filter type " + ftn + " invalid");
        }
        final int[] filterUseStat = this.filterUseStat;
        final int n = ftn;
        ++filterUseStat[n];
        this.rowUnfiltered[0] = this.row[0];
        switch (ft) {
            case FILTER_NONE: {
                this.unfilterRowNone(nbytes);
                break;
            }
            case FILTER_SUB: {
                this.unfilterRowSub(nbytes);
                break;
            }
            case FILTER_UP: {
                this.unfilterRowUp(nbytes);
                break;
            }
            case FILTER_AVERAGE: {
                this.unfilterRowAverage(nbytes);
                break;
            }
            case FILTER_PAETH: {
                this.unfilterRowPaeth(nbytes);
                break;
            }
            default: {
                throw new PngjInputException("Filter type " + ftn + " not implemented");
            }
        }
    }
    
    private void unfilterRowAverage(final int nbytes) {
        for (int j = 1 - this.imgInfo.bytesPixel, i = 1; i <= nbytes; ++i, ++j) {
            final int x = (j > 0) ? (this.rowUnfiltered[j] & 0xFF) : 0;
            this.rowUnfiltered[i] = (byte)(this.row[i] + (x + (this.rowUnfilteredPrev[i] & 0xFF)) / 2);
        }
    }
    
    private void unfilterRowNone(final int nbytes) {
        for (int i = 1; i <= nbytes; ++i) {
            this.rowUnfiltered[i] = this.row[i];
        }
    }
    
    private void unfilterRowPaeth(final int nbytes) {
        for (int j = 1 - this.imgInfo.bytesPixel, i = 1; i <= nbytes; ++i, ++j) {
            final int x = (j > 0) ? (this.rowUnfiltered[j] & 0xFF) : 0;
            final int y = (j > 0) ? (this.rowUnfilteredPrev[j] & 0xFF) : 0;
            this.rowUnfiltered[i] = (byte)(this.row[i] + PngHelperInternal.filterPaethPredictor(x, this.rowUnfilteredPrev[i] & 0xFF, y));
        }
    }
    
    private void unfilterRowSub(final int nbytes) {
        for (int i = 1; i <= this.imgInfo.bytesPixel; ++i) {
            this.rowUnfiltered[i] = this.row[i];
        }
        for (int j = 1, i = this.imgInfo.bytesPixel + 1; i <= nbytes; ++i, ++j) {
            this.rowUnfiltered[i] = (byte)(this.row[i] + this.rowUnfiltered[j]);
        }
    }
    
    private void unfilterRowUp(final int nbytes) {
        for (int i = 1; i <= nbytes; ++i) {
            this.rowUnfiltered[i] = (byte)(this.row[i] + this.rowUnfilteredPrev[i]);
        }
    }
    
    protected void preProcessRow() {
        super.preProcessRow();
        this.rowinfo.update(this.getRown());
        this.unfilterRow();
        this.rowinfo.updateBuf(this.rowUnfiltered, this.rowinfo.bytesRow + 1);
    }
    
    protected int processRowCallback() {
        final int bytesNextRow = this.advanceToNextRow();
        return bytesNextRow;
    }
    
    protected void processDoneCallback() {
    }
    
    public int advanceToNextRow() {
        int bytesNextRow;
        if (this.deinterlacer == null) {
            bytesNextRow = ((this.getRown() >= this.imgInfo.rows - 1) ? 0 : (this.imgInfo.bytesPerRow + 1));
        }
        else {
            final boolean more = this.deinterlacer.nextRow();
            bytesNextRow = (more ? (this.deinterlacer.getBytesToRead() + 1) : 0);
        }
        if (!this.isCallbackMode()) {
            this.prepareForNextRow(bytesNextRow);
        }
        return bytesNextRow;
    }
    
    public boolean isRowReady() {
        return !this.isWaitingForMoreInput();
    }
    
    public byte[] getUnfilteredRow() {
        return this.rowUnfiltered;
    }
    
    public Deinterlacer getDeinterlacer() {
        return this.deinterlacer;
    }
    
    void updateCrcs(final Checksum... idatCrcs) {
        for (final Checksum idatCrca : idatCrcs) {
            if (idatCrca != null) {
                idatCrca.update(this.getUnfilteredRow(), 1, this.getRowFilled() - 1);
            }
        }
    }
    
    public void close() {
        super.close();
        this.rowUnfiltered = null;
        this.rowUnfilteredPrev = null;
    }
    
    public int[] getFilterUseStat() {
        return this.filterUseStat;
    }
}
