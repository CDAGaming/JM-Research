package journeymap.client.cartography;

import journeymap.client.render.*;
import journeymap.client.model.*;

public interface IChunkRenderer
{
    boolean render(final ComparableBufferedImage p0, final ChunkMD p1, final Integer p2);
    
    void setStratumColors(final Stratum p0, final int p1, final Integer p2, final boolean p3, final boolean p4, final boolean p5);
    
    float[] getAmbientColor();
}
