package journeymap.client.model;

public class BlockCoordIntPair
{
    public int x;
    public int z;
    
    public BlockCoordIntPair() {
        this.setLocation(0, 0);
    }
    
    public BlockCoordIntPair(final int x, final int z) {
        this.setLocation(x, z);
    }
    
    public void setLocation(final int x, final int z) {
        this.x = x;
        this.z = z;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final BlockCoordIntPair that = (BlockCoordIntPair)o;
        return this.x == that.x && this.z == that.z;
    }
    
    @Override
    public int hashCode() {
        int result = this.x;
        result = 31 * result + this.z;
        return result;
    }
}
