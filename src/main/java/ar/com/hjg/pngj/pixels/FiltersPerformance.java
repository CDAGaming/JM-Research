package ar.com.hjg.pngj.pixels;

import java.util.*;
import ar.com.hjg.pngj.*;

public class FiltersPerformance
{
    private final ImageInfo iminfo;
    private double memoryA;
    private int lastrow;
    private double[] absum;
    private double[] entropy;
    private double[] cost;
    private int[] histog;
    private int lastprefered;
    private boolean initdone;
    private double preferenceForNone;
    public static final double[] FILTER_WEIGHTS_DEFAULT;
    private double[] filter_weights;
    private static final double LOG2NI;
    
    public FiltersPerformance(final ImageInfo imgInfo) {
        this.memoryA = 0.7;
        this.lastrow = -1;
        this.absum = new double[5];
        this.entropy = new double[5];
        this.cost = new double[5];
        this.histog = new int[256];
        this.lastprefered = -1;
        this.initdone = false;
        this.preferenceForNone = 1.0;
        this.filter_weights = new double[] { -1.0, -1.0, -1.0, -1.0, -1.0 };
        this.iminfo = imgInfo;
    }
    
    private void init() {
        if (this.filter_weights[0] < 0.0) {
            System.arraycopy(FiltersPerformance.FILTER_WEIGHTS_DEFAULT, 0, this.filter_weights, 0, 5);
            double wNone = this.filter_weights[0];
            if (this.iminfo.bitDepth == 16) {
                wNone = 1.2;
            }
            else if (this.iminfo.alpha) {
                wNone = 0.8;
            }
            else if (this.iminfo.indexed || this.iminfo.bitDepth < 8) {
                wNone = 0.4;
            }
            wNone /= this.preferenceForNone;
            this.filter_weights[0] = wNone;
        }
        Arrays.fill(this.cost, 1.0);
        this.initdone = true;
    }
    
    public void updateFromFiltered(final FilterType ftype, final byte[] rowff, final int rown) {
        this.updateFromRawOrFiltered(ftype, rowff, null, null, rown);
    }
    
    public void updateFromRaw(final FilterType ftype, final byte[] rowb, final byte[] rowbprev, final int rown) {
        this.updateFromRawOrFiltered(ftype, null, rowb, rowbprev, rown);
    }
    
    private void updateFromRawOrFiltered(final FilterType ftype, final byte[] rowff, final byte[] rowb, final byte[] rowbprev, final int rown) {
        if (!this.initdone) {
            this.init();
        }
        if (rown != this.lastrow) {
            Arrays.fill(this.absum, Double.NaN);
            Arrays.fill(this.entropy, Double.NaN);
        }
        this.lastrow = rown;
        if (rowff != null) {
            this.computeHistogram(rowff);
        }
        else {
            this.computeHistogramForFilter(ftype, rowb, rowbprev);
        }
        if (ftype == FilterType.FILTER_NONE) {
            this.entropy[ftype.val] = this.computeEntropyFromHistogram();
        }
        else {
            this.absum[ftype.val] = this.computeAbsFromHistogram();
        }
    }
    
    public FilterType getPreferred() {
        int fi = 0;
        double vali = Double.MAX_VALUE;
        double val = 0.0;
        for (int i = 0; i < 5; ++i) {
            if (!Double.isNaN(this.absum[i])) {
                val = this.absum[i];
            }
            else {
                if (Double.isNaN(this.entropy[i])) {
                    continue;
                }
                val = (Math.pow(2.0, this.entropy[i]) - 1.0) * 0.5;
            }
            val *= this.filter_weights[i];
            val = this.cost[i] * this.memoryA + (1.0 - this.memoryA) * val;
            this.cost[i] = val;
            if (val < vali) {
                vali = val;
                fi = i;
            }
        }
        this.lastprefered = fi;
        return FilterType.getByVal(this.lastprefered);
    }
    
    public final void computeHistogramForFilter(final FilterType filterType, final byte[] rowb, final byte[] rowbprev) {
        Arrays.fill(this.histog, 0);
        final int imax = this.iminfo.bytesPerRow;
        switch (filterType) {
            case FILTER_NONE: {
                for (int i = 1; i <= imax; ++i) {
                    final int[] histog = this.histog;
                    final int n = rowb[i] & 0xFF;
                    ++histog[n];
                }
                break;
            }
            case FILTER_PAETH: {
                for (int i = 1; i <= imax; ++i) {
                    final int[] histog2 = this.histog;
                    final int filterRowPaeth = PngHelperInternal.filterRowPaeth(rowb[i], 0, rowbprev[i] & 0xFF, 0);
                    ++histog2[filterRowPaeth];
                }
                for (int j = 1, i = this.iminfo.bytesPixel + 1; i <= imax; ++i, ++j) {
                    final int[] histog3 = this.histog;
                    final int filterRowPaeth2 = PngHelperInternal.filterRowPaeth(rowb[i], rowb[j] & 0xFF, rowbprev[i] & 0xFF, rowbprev[j] & 0xFF);
                    ++histog3[filterRowPaeth2];
                }
                break;
            }
            case FILTER_SUB: {
                for (int i = 1; i <= this.iminfo.bytesPixel; ++i) {
                    final int[] histog4 = this.histog;
                    final int n2 = rowb[i] & 0xFF;
                    ++histog4[n2];
                }
                for (int j = 1, i = this.iminfo.bytesPixel + 1; i <= imax; ++i, ++j) {
                    final int[] histog5 = this.histog;
                    final int n3 = rowb[i] - rowb[j] & 0xFF;
                    ++histog5[n3];
                }
                break;
            }
            case FILTER_UP: {
                for (int i = 1; i <= this.iminfo.bytesPerRow; ++i) {
                    final int[] histog6 = this.histog;
                    final int n4 = rowb[i] - rowbprev[i] & 0xFF;
                    ++histog6[n4];
                }
                break;
            }
            case FILTER_AVERAGE: {
                for (int i = 1; i <= this.iminfo.bytesPixel; ++i) {
                    final int[] histog7 = this.histog;
                    final int n5 = (rowb[i] & 0xFF) - (rowbprev[i] & 0xFF) / 2 & 0xFF;
                    ++histog7[n5];
                }
                for (int j = 1, i = this.iminfo.bytesPixel + 1; i <= imax; ++i, ++j) {
                    final int[] histog8 = this.histog;
                    final int n6 = (rowb[i] & 0xFF) - ((rowbprev[i] & 0xFF) + (rowb[j] & 0xFF)) / 2 & 0xFF;
                    ++histog8[n6];
                }
                break;
            }
            default: {
                throw new PngjExceptionInternal("Bad filter:" + filterType);
            }
        }
    }
    
    public void computeHistogram(final byte[] rowff) {
        Arrays.fill(this.histog, 0);
        for (int i = 1; i < this.iminfo.bytesPerRow; ++i) {
            final int[] histog = this.histog;
            final int n = rowff[i] & 0xFF;
            ++histog[n];
        }
    }
    
    public double computeAbsFromHistogram() {
        int s = 0;
        for (int i = 1; i < 128; ++i) {
            s += this.histog[i] * i;
        }
        int i = 128;
        for (int j = 128; j > 0; --j) {
            s += this.histog[i] * j;
            ++i;
        }
        return s / this.iminfo.bytesPerRow;
    }
    
    public final double computeEntropyFromHistogram() {
        final double s = 1.0 / this.iminfo.bytesPerRow;
        final double ls = Math.log(s);
        double h = 0.0;
        for (final int x : this.histog) {
            if (x > 0) {
                h += (Math.log(x) + ls) * x;
            }
        }
        h *= s * FiltersPerformance.LOG2NI;
        if (h < 0.0) {
            h = 0.0;
        }
        return h;
    }
    
    public void setPreferenceForNone(final double preferenceForNone) {
        this.preferenceForNone = preferenceForNone;
    }
    
    public void tuneMemory(final double m) {
        if (m == 0.0) {
            this.memoryA = 0.0;
        }
        else {
            this.memoryA = Math.pow(this.memoryA, 1.0 / m);
        }
    }
    
    public void setFilterWeights(final double[] weights) {
        System.arraycopy(weights, 0, this.filter_weights, 0, 5);
    }
    
    static {
        FILTER_WEIGHTS_DEFAULT = new double[] { 0.73, 1.03, 0.97, 1.11, 1.22 };
        LOG2NI = -1.0 / Math.log(2.0);
    }
}
