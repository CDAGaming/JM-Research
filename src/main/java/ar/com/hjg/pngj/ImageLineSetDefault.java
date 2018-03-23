package ar.com.hjg.pngj;

import java.util.*;

public abstract class ImageLineSetDefault<T extends IImageLine> implements IImageLineSet<T>
{
    protected final ImageInfo imgInfo;
    private final boolean singleCursor;
    private final int nlines;
    private final int offset;
    private final int step;
    protected List<T> imageLines;
    protected T imageLine;
    protected int currentRow;
    
    public ImageLineSetDefault(final ImageInfo imgInfo, final boolean singleCursor, final int nlines, final int noffset, final int step) {
        this.currentRow = -1;
        this.imgInfo = imgInfo;
        this.singleCursor = singleCursor;
        if (singleCursor) {
            this.nlines = 1;
            this.offset = 0;
            this.step = 1;
        }
        else {
            this.nlines = imgInfo.rows;
            this.offset = 0;
            this.step = 1;
        }
        this.createImageLines();
    }
    
    private void createImageLines() {
        if (this.singleCursor) {
            this.imageLine = this.createImageLine();
        }
        else {
            this.imageLines = new ArrayList<T>();
            for (int i = 0; i < this.nlines; ++i) {
                this.imageLines.add(this.createImageLine());
            }
        }
    }
    
    protected abstract T createImageLine();
    
    public T getImageLine(final int n) {
        this.currentRow = n;
        if (this.singleCursor) {
            return this.imageLine;
        }
        return this.imageLines.get(this.imageRowToMatrixRowStrict(n));
    }
    
    public boolean hasImageLine(final int n) {
        return this.singleCursor ? (this.currentRow == n) : (this.imageRowToMatrixRowStrict(n) >= 0);
    }
    
    public int size() {
        return this.nlines;
    }
    
    public int imageRowToMatrixRowStrict(int imrow) {
        imrow -= this.offset;
        final int mrow = (imrow >= 0 && imrow % this.step == 0) ? (imrow / this.step) : -1;
        return (mrow < this.nlines) ? mrow : -1;
    }
    
    public int matrixRowToImageRow(final int mrow) {
        return mrow * this.step + this.offset;
    }
    
    public int imageRowToMatrixRow(final int imrow) {
        final int r = (imrow - this.offset) / this.step;
        return (r < 0) ? 0 : ((r < this.nlines) ? r : (this.nlines - 1));
    }
    
    public static IImageLineSetFactory<ImageLineInt> getFactoryInt() {
        return new IImageLineSetFactory<ImageLineInt>() {
            public IImageLineSet<ImageLineInt> create(final ImageInfo iminfo, final boolean singleCursor, final int nlines, final int noffset, final int step) {
                return new ImageLineSetDefault<ImageLineInt>(iminfo, singleCursor, nlines, noffset, step) {
                    protected ImageLineInt createImageLine() {
                        return new ImageLineInt(this.imgInfo);
                    }
                };
            }
        };
    }
    
    public static IImageLineSetFactory<ImageLineByte> getFactoryByte() {
        return new IImageLineSetFactory<ImageLineByte>() {
            public IImageLineSet<ImageLineByte> create(final ImageInfo iminfo, final boolean singleCursor, final int nlines, final int noffset, final int step) {
                return new ImageLineSetDefault<ImageLineByte>(iminfo, singleCursor, nlines, noffset, step) {
                    protected ImageLineByte createImageLine() {
                        return new ImageLineByte(this.imgInfo);
                    }
                };
            }
        };
    }
}
