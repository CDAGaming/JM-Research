package ar.com.hjg.pngj;

class RowInfo
{
    public final ImageInfo imgInfo;
    public final Deinterlacer deinterlacer;
    public final boolean imode;
    int dY;
    int dX;
    int oY;
    int oX;
    int rowNseq;
    int rowNreal;
    int rowNsubImg;
    int rowsSubImg;
    int colsSubImg;
    int bytesRow;
    int pass;
    byte[] buf;
    int buflen;
    
    public RowInfo(final ImageInfo imgInfo, final Deinterlacer deinterlacer) {
        this.imgInfo = imgInfo;
        this.deinterlacer = deinterlacer;
        this.imode = (deinterlacer != null);
    }
    
    void update(final int rowseq) {
        this.rowNseq = rowseq;
        if (this.imode) {
            this.pass = this.deinterlacer.getPass();
            this.dX = this.deinterlacer.dX;
            this.dY = this.deinterlacer.dY;
            this.oX = this.deinterlacer.oX;
            this.oY = this.deinterlacer.oY;
            this.rowNreal = this.deinterlacer.getCurrRowReal();
            this.rowNsubImg = this.deinterlacer.getCurrRowSubimg();
            this.rowsSubImg = this.deinterlacer.getRows();
            this.colsSubImg = this.deinterlacer.getCols();
            this.bytesRow = (this.imgInfo.bitspPixel * this.colsSubImg + 7) / 8;
        }
        else {
            this.pass = 1;
            final boolean b = true;
            this.dY = (b ? 1 : 0);
            this.dX = (b ? 1 : 0);
            final boolean b2 = false;
            this.oY = (b2 ? 1 : 0);
            this.oX = (b2 ? 1 : 0);
            this.rowNsubImg = rowseq;
            this.rowNreal = rowseq;
            this.rowsSubImg = this.imgInfo.rows;
            this.colsSubImg = this.imgInfo.cols;
            this.bytesRow = this.imgInfo.bytesPerRow;
        }
    }
    
    void updateBuf(final byte[] buf, final int buflen) {
        this.buf = buf;
        this.buflen = buflen;
    }
}
