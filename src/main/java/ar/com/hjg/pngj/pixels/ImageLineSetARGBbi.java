package ar.com.hjg.pngj.pixels;

import java.awt.image.*;
import ar.com.hjg.pngj.*;

public class ImageLineSetARGBbi implements IImageLineSet<ImageLineARGBbi>
{
    BufferedImage image;
    private ImageInfo imginfo;
    private ImageLineARGBbi line;
    
    public ImageLineSetARGBbi(final BufferedImage bi, final ImageInfo imginfo) {
        this.image = bi;
        this.imginfo = imginfo;
        this.line = new ImageLineARGBbi(imginfo, bi, ((DataBufferByte)this.image.getRaster().getDataBuffer()).getData());
    }
    
    public ImageLineARGBbi getImageLine(final int n) {
        this.line.setRowNumber(n);
        return this.line;
    }
    
    public boolean hasImageLine(final int n) {
        return n >= 0 && n < this.imginfo.rows;
    }
    
    public int size() {
        return 1;
    }
}
