package journeymap.client.render.map;

public class TilePos implements Comparable<TilePos>
{
    public final int deltaX;
    public final int deltaZ;
    final double startX;
    final double startZ;
    final double endX;
    final double endZ;
    
    TilePos(final int deltaX, final int deltaZ) {
        this.deltaX = deltaX;
        this.deltaZ = deltaZ;
        this.startX = deltaX * 512;
        this.startZ = deltaZ * 512;
        this.endX = this.startX + 512.0;
        this.endZ = this.startZ + 512.0;
    }
    
    @Override
    public int hashCode() {
        final int prime = 37;
        int result = 1;
        result = 37 * result + this.deltaX;
        result = 37 * result + this.deltaZ;
        return result;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final TilePos other = (TilePos)obj;
        return this.deltaX == other.deltaX && this.deltaZ == other.deltaZ;
    }
    
    @Override
    public String toString() {
        return "TilePos [" + this.deltaX + "," + this.deltaZ + "]";
    }
    
    @Override
    public int compareTo(final TilePos o) {
        int result = new Integer(this.deltaZ).compareTo(Integer.valueOf(o.deltaZ));
        if (result == 0) {
            result = new Integer(this.deltaX).compareTo(Integer.valueOf(o.deltaX));
        }
        return result;
    }
}
