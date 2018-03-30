package journeymap.client.io;

import java.io.*;
import journeymap.common.*;
import java.util.*;
import ar.com.hjg.pngj.chunks.*;
import ar.com.hjg.pngj.*;

public class PngjHelper
{
    public static void mergeFiles(final File[] tiles, final File destFile, final int tileColumns, final int tileSize) {
        final int ntiles = tiles.length;
        final int tileRows = (ntiles + tileColumns - 1) / tileColumns;
        final PngReader[] readers = new PngReader[tileColumns];
        final ImageInfo destImgInfo = new ImageInfo(tileSize * tileColumns, tileSize * tileRows, 8, true);
        final PngWriter pngw = new PngWriter(destFile, destImgInfo, true);
        pngw.getMetadata().setText("Author", "JourneyMap" + Journeymap.JM_VERSION);
        pngw.getMetadata().setText("Comment", "http://journeymap.info/");
        final ImageLineInt destLine = new ImageLineInt(destImgInfo);
        final int lineLen = tileSize * 4;
        final int gridColor = 135;
        final boolean showGrid = Journeymap.getClient().getFullMapProperties().showGrid.get();
        int destRow = 0;
        for (int ty = 0; ty < tileRows; ++ty) {
            final int nTilesXcur = (ty < tileRows - 1) ? tileColumns : (ntiles - (tileRows - 1) * tileColumns);
            Arrays.fill(destLine.getScanline(), 0);
            for (int tx = 0; tx < nTilesXcur; ++tx) {
                (readers[tx] = new PngReader(tiles[tx + ty * tileColumns])).setChunkLoadBehaviour(ChunkLoadBehaviour.LOAD_CHUNK_NEVER);
            }
        Label_0511:
            for (int srcRow = 0; srcRow < tileSize; ++srcRow, ++destRow) {
                for (int tx2 = 0; tx2 < nTilesXcur; ++tx2) {
                    final ImageLineInt srcLine = (ImageLineInt)readers[tx2].readRow(srcRow);
                    final int[] src = srcLine.getScanline();
                    if (showGrid) {
                        for (int skip = (srcRow % 16 == 0) ? 4 : 64, i = 0; i <= src.length - skip; i += skip) {
                            src[i] = (src[i] + src[i] + 135) / 3;
                            src[i + 1] = (src[i + 1] + src[i + 1] + 135) / 3;
                            src[i + 2] = (src[i + 2] + src[i + 2] + 135) / 3;
                            src[i + 3] = 255;
                        }
                    }
                    final int[] dest = destLine.getScanline();
                    final int destPos = lineLen * tx2;
                    try {
                        System.arraycopy(src, 0, dest, destPos, lineLen);
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        Journeymap.getLogger().error("Bad image data. Src len=" + src.length + ", dest len=" + dest.length + ", destPos=" + destPos);
                        break Label_0511;
                    }
                }
                pngw.writeRow(destLine, destRow);
            }
            for (int tx = 0; tx < nTilesXcur; ++tx) {
                readers[tx].end();
            }
        }
        pngw.end();
    }
}
