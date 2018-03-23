package ar.com.hjg.pngj;

import java.io.*;
import ar.com.hjg.pngj.pixels.*;

public class PngWriterHc extends PngWriter
{
    public PngWriterHc(final File file, final ImageInfo imgInfo, final boolean allowoverwrite) {
        super(file, imgInfo, allowoverwrite);
        this.setFilterType(FilterType.FILTER_SUPER_ADAPTIVE);
    }
    
    public PngWriterHc(final File file, final ImageInfo imgInfo) {
        super(file, imgInfo);
    }
    
    public PngWriterHc(final OutputStream outputStream, final ImageInfo imgInfo) {
        super(outputStream, imgInfo);
    }
    
    protected PixelsWriter createPixelsWriter(final ImageInfo imginfo) {
        final PixelsWriterMultiple pw = new PixelsWriterMultiple(imginfo);
        return pw;
    }
    
    public PixelsWriterMultiple getPixelWriterMultiple() {
        return (PixelsWriterMultiple)this.pixelsWriter;
    }
}
