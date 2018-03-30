package journeymap.client.cartography;

import java.util.concurrent.atomic.*;
import journeymap.client.model.*;
import java.awt.*;
import net.minecraft.util.math.*;

public class Stratum
{
    private static AtomicInteger IDGEN;
    private final int id;
    private ChunkMD chunkMd;
    private BlockMD blockMD;
    private int localX;
    private int y;
    private int localZ;
    private int lightLevel;
    private int lightOpacity;
    private boolean isFluid;
    private Integer dayColor;
    private Integer nightColor;
    private Integer caveColor;
    private float worldAmbientLight;
    private boolean worldHasNoSky;
    private boolean uninitialized;
    
    Stratum() {
        this.uninitialized = true;
        this.id = Stratum.IDGEN.incrementAndGet();
    }
    
    Stratum set(final ChunkMD chunkMd, final BlockMD blockMD, final int localX, final int y, final int localZ, final Integer lightLevel) {
        if (chunkMd == null || blockMD == null) {
            throw new IllegalStateException(String.format("Can't have nulls: %s, %s", chunkMd, blockMD));
        }
        try {
            this.setChunkMd(chunkMd);
            this.setBlockMD(blockMD);
            this.setX(localX);
            this.setY(y);
            this.setZ(localZ);
            this.setFluid(blockMD.isFluid() || blockMD.isFluid());
            if (blockMD.isLava()) {
                this.setLightLevel(14);
            }
            else {
                this.setLightLevel((lightLevel != null) ? ((int)lightLevel) : chunkMd.getSavedLightValue(localX, y + 1, localZ));
            }
            this.setLightOpacity(chunkMd.getLightOpacity(blockMD, localX, y, localZ));
            this.setDayColor(null);
            this.setNightColor(null);
            this.setCaveColor(null);
            this.uninitialized = false;
        }
        catch (RuntimeException t) {
            throw t;
        }
        return this;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Stratum that = (Stratum)o;
        if (this.getY() != that.getY()) {
            return false;
        }
        if (this.getBlockMD() != null) {
            if (this.getBlockMD().equals(that.getBlockMD())) {
                return true;
            }
        }
        else if (that.getBlockMD() == null) {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = (this.getBlockMD() != null) ? this.getBlockMD().hashCode() : 0;
        result = 31 * result + this.getY();
        return result;
    }
    
    @Override
    public String toString() {
        final String common = "Stratum{id=" + this.id + ", uninitialized=" + this.uninitialized + "%s}";
        if (!this.uninitialized) {
            return String.format(common, ", localX=" + this.getX() + ", y=" + this.getY() + ", localZ=" + this.getZ() + ", lightLevel=" + this.getLightLevel() + ", worldAmbientLight=" + this.getWorldAmbientLight() + ", lightOpacity=" + this.getLightOpacity() + ", isFluid=" + this.isFluid() + ", dayColor=" + ((this.getDayColor() == null) ? null : new Color(this.getDayColor())) + ", nightColor=" + ((this.getNightColor() == null) ? null : new Color(this.getNightColor())) + ", caveColor=" + ((this.getCaveColor() == null) ? null : new Color(this.getCaveColor())));
        }
        return String.format(common, "");
    }
    
    public ChunkMD getChunkMd() {
        return this.chunkMd;
    }
    
    public void setChunkMd(final ChunkMD chunkMd) {
        this.chunkMd = chunkMd;
        if (chunkMd != null) {
            this.worldAmbientLight = chunkMd.getWorld().func_72971_b(1.0f) * 15.0f;
            this.worldHasNoSky = chunkMd.hasNoSky();
        }
        else {
            this.worldAmbientLight = 15.0f;
            this.worldHasNoSky = false;
        }
    }
    
    public BlockMD getBlockMD() {
        if (this.blockMD.isFluid()) {}
        return this.blockMD;
    }
    
    public void setBlockMD(final BlockMD blockMD) {
        this.blockMD = blockMD;
    }
    
    public float getWorldAmbientLight() {
        return this.worldAmbientLight;
    }
    
    public boolean getWorldHasNoSky() {
        return this.worldHasNoSky;
    }
    
    public int getX() {
        return this.localX;
    }
    
    public void setX(final int x) {
        this.localX = x;
    }
    
    public int getY() {
        return this.y;
    }
    
    public void setY(final int y) {
        this.y = y;
    }
    
    public int getZ() {
        return this.localZ;
    }
    
    public void setZ(final int z) {
        this.localZ = z;
    }
    
    public int getLightLevel() {
        return this.lightLevel;
    }
    
    public void setLightLevel(final int lightLevel) {
        this.lightLevel = lightLevel;
    }
    
    public int getLightOpacity() {
        return this.lightOpacity;
    }
    
    public void setLightOpacity(final int lightOpacity) {
        this.lightOpacity = lightOpacity;
    }
    
    public boolean isFluid() {
        return this.isFluid;
    }
    
    public void setFluid(final boolean isFluid) {
        this.isFluid = isFluid;
    }
    
    public Integer getDayColor() {
        return this.dayColor;
    }
    
    public void setDayColor(final Integer dayColor) {
        this.dayColor = dayColor;
    }
    
    public Integer getNightColor() {
        return this.nightColor;
    }
    
    public void setNightColor(final Integer nightColor) {
        this.nightColor = nightColor;
    }
    
    public Integer getCaveColor() {
        return this.caveColor;
    }
    
    public void setCaveColor(final Integer caveColor) {
        this.caveColor = caveColor;
    }
    
    public BlockPos getBlockPos() {
        return new BlockPos((Vec3i)this.chunkMd.getBlockPos(this.localX, this.y, this.localZ));
    }
    
    public boolean isUninitialized() {
        return this.uninitialized;
    }
    
    public void clear() {
        this.uninitialized = true;
        this.worldAmbientLight = 15.0f;
        this.worldHasNoSky = false;
        this.setChunkMd(null);
        this.setBlockMD(null);
        this.setX(0);
        this.setY(-1);
        this.setZ(0);
        this.setFluid(false);
        this.setLightLevel(-1);
        this.setLightOpacity(-1);
        this.setDayColor(null);
        this.setNightColor(null);
        this.setCaveColor(null);
    }
    
    static {
        Stratum.IDGEN = new AtomicInteger(0);
    }
}
