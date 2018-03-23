package ar.com.hjg.pngj;

import java.io.*;

public interface IPngWriterFactory
{
    PngWriter createPngWriter(final OutputStream p0, final ImageInfo p1);
}
