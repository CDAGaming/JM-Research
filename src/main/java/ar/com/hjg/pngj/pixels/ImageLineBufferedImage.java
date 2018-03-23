package ar.com.hjg.pngj.pixels;

import java.awt.image.*;
import ar.com.hjg.pngj.*;

public class ImageLineBufferedImage implements IImageLine
{
    public final ImageInfo imgInfo;
    private final BufferedImage image;
    private int rowNumber;
    private boolean hasAlpha;
    private int rowLength;
    private boolean bgrOrder;
    private byte[] bytes;
    
    public ImageLineBufferedImage(final ImageInfo imgInfo, final BufferedImage bi, final byte[] bytesdata) {
        this.rowNumber = -1;
        this.imgInfo = imgInfo;
        this.image = bi;
        this.bytes = bytesdata;
        this.hasAlpha = this.image.getColorModel().hasAlpha();
        if (this.hasAlpha) {
            this.rowLength = this.image.getWidth() * 4;
        }
        else {
            this.rowLength = this.image.getWidth() * 3;
        }
        this.bgrOrder = (((ComponentSampleModel)this.image.getSampleModel()).getBandOffsets()[0] != 0);
    }
    
    public static IImageLineFactory<ImageLineByte> getFactory(final ImageInfo iminfo) {
        return new IImageLineFactory<ImageLineByte>() {
            public ImageLineByte createImageLine(final ImageInfo iminfo) {
                return new ImageLineByte(iminfo);
            }
        };
    }
    
    public void readFromPngRaw(final byte[] raw, final int len, final int offset, final int step) {
        throw new RuntimeException("not implemented");
    }
    
    public void writeToPngRaw(final byte[] raw) {
        if (this.imgInfo.bytesPerRow != this.rowLength) {
            throw new RuntimeException("??");
        }
        if (this.rowNumber < 0 || this.rowNumber >= this.imgInfo.rows) {
            throw new RuntimeException("???");
        }
        int bytesIdx = this.rowLength * this.rowNumber;
        int i = 1;
        if (this.hasAlpha) {
            if (this.bgrOrder) {
                while (i <= this.rowLength) {
                    final byte a = this.bytes[bytesIdx++];
                    final byte b = this.bytes[bytesIdx++];
                    final byte g = this.bytes[bytesIdx++];
                    final byte r = this.bytes[bytesIdx++];
                    raw[i++] = r;
                    raw[i++] = g;
                    raw[i++] = b;
                    raw[i++] = a;
                }
            }
            else {
                while (i <= this.rowLength) {
                    raw[i++] = this.bytes[bytesIdx++];
                    raw[i++] = this.bytes[bytesIdx++];
                    raw[i++] = this.bytes[bytesIdx++];
                    raw[i++] = this.bytes[bytesIdx++];
                }
            }
        }
        else if (this.bgrOrder) {
            while (i <= this.rowLength) {
                final byte b2 = this.bytes[bytesIdx++];
                final byte g2 = this.bytes[bytesIdx++];
                final byte r2 = this.bytes[bytesIdx++];
                raw[i++] = r2;
                raw[i++] = g2;
                raw[i++] = b2;
            }
        }
        else {
            while (i <= this.rowLength) {
                raw[i++] = this.bytes[bytesIdx++];
                raw[i++] = this.bytes[bytesIdx++];
                raw[i++] = this.bytes[bytesIdx++];
            }
        }
    }
    
    public void endReadFromPngRaw() {
        throw new RuntimeException("not implemented");
    }
    
    public int getRowNumber() {
        return this.rowNumber;
    }
    
    public void setRowNumber(final int rowNumber) {
        this.rowNumber = rowNumber;
    }
}
