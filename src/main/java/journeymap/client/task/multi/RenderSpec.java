package journeymap.client.task.multi;

import java.text.*;
import net.minecraft.client.*;
import net.minecraft.entity.player.*;
import journeymap.client.model.*;
import net.minecraft.util.math.*;
import journeymap.common.*;
import journeymap.client.properties.*;
import com.google.common.collect.*;
import journeymap.client.data.*;
import java.util.*;
import java.util.concurrent.*;
import journeymap.client.*;
import journeymap.client.ui.option.*;

public class RenderSpec
{
    private static DecimalFormat decFormat;
    private static volatile RenderSpec lastSurfaceRenderSpec;
    private static volatile RenderSpec lastTopoRenderSpec;
    private static volatile RenderSpec lastUndergroundRenderSpec;
    private static Minecraft minecraft;
    private final EntityPlayer player;
    private final MapType mapType;
    private final int primaryRenderDistance;
    private final int maxSecondaryRenderDistance;
    private final RevealShape revealShape;
    private ListMultimap<Integer, Offset> offsets;
    private ArrayList<ChunkPos> primaryRenderCoords;
    private Comparator<ChunkPos> comparator;
    private int lastSecondaryRenderDistance;
    private ChunkPos lastPlayerCoord;
    private long lastTaskTime;
    private int lastTaskChunks;
    private double lastTaskAvgChunkTime;
    
    private RenderSpec(final Minecraft minecraft, final MapType mapType) {
        this.offsets = null;
        this.player = (EntityPlayer)minecraft.field_71439_g;
        final CoreProperties props = Journeymap.getClient().getCoreProperties();
        final int gameRenderDistance = Math.max(1, minecraft.field_71474_y.field_151451_c - 1);
        final int mapRenderDistanceMin;
        final int mapRenderDistanceMax = mapRenderDistanceMin = (mapType.isUnderground() ? props.renderDistanceCaveMax.get() : props.renderDistanceSurfaceMax.get());
        this.mapType = mapType;
        int rdMin = Math.min(gameRenderDistance, mapRenderDistanceMin);
        final int rdMax = Math.min(gameRenderDistance, Math.max(rdMin, mapRenderDistanceMax));
        if (rdMin + 1 == rdMax) {
            ++rdMin;
        }
        this.primaryRenderDistance = rdMin;
        this.maxSecondaryRenderDistance = rdMax;
        this.revealShape = Journeymap.getClient().getCoreProperties().revealShape.get();
        this.lastPlayerCoord = new ChunkPos(minecraft.field_71439_g.field_70176_ah, minecraft.field_71439_g.field_70164_aj);
        this.lastSecondaryRenderDistance = this.primaryRenderDistance;
    }
    
    private static Double blockDistance(final ChunkPos playerCoord, final ChunkPos coord) {
        final int x = (playerCoord.field_77276_a << 4) + 8 - ((coord.field_77276_a << 4) + 8);
        final int z = (playerCoord.field_77275_b << 4) + 8 - ((coord.field_77275_b << 4) + 8);
        return Math.sqrt(x * x + z * z);
    }
    
    private static Double chunkDistance(final ChunkPos playerCoord, final ChunkPos coord) {
        final int x = playerCoord.field_77276_a - coord.field_77276_a;
        final int z = playerCoord.field_77275_b - coord.field_77275_b;
        return Math.sqrt(x * x + z * z);
    }
    
    static boolean inRange(final ChunkPos playerCoord, final ChunkPos coord, final int renderDistance, final RevealShape revealShape) {
        if (revealShape == RevealShape.Circle) {
            final double distance = blockDistance(playerCoord, coord);
            final double diff = distance - renderDistance * 16;
            return diff <= 8.0;
        }
        final float x = Math.abs(playerCoord.field_77276_a - coord.field_77276_a);
        final float z = Math.abs(playerCoord.field_77275_b - coord.field_77275_b);
        return x <= renderDistance && z <= renderDistance;
    }
    
    private static ListMultimap<Integer, Offset> calculateOffsets(final int minOffset, final int maxOffset, final RevealShape revealShape) {
        final ListMultimap<Integer, Offset> multimap = (ListMultimap<Integer, Offset>)ArrayListMultimap.create();
        int offset = maxOffset;
        final int baseX = 0;
        final int baseZ = 0;
        final ChunkPos baseCoord = new ChunkPos(0, 0);
        while (offset >= minOffset) {
            for (int x = 0 - offset; x <= 0 + offset; ++x) {
                for (int z = 0 - offset; z <= 0 + offset; ++z) {
                    final ChunkPos coord = new ChunkPos(x, z);
                    if (revealShape == RevealShape.Square || inRange(baseCoord, coord, offset, revealShape)) {
                        multimap.put((Object)offset, (Object)new Offset(coord.field_77276_a, coord.field_77275_b));
                    }
                }
            }
            if (offset < maxOffset) {
                final List<Offset> oneUp = (List<Offset>)multimap.get((Object)(offset + 1));
                oneUp.removeAll(multimap.get((Object)offset));
            }
            --offset;
        }
        for (int i = minOffset; i <= maxOffset; ++i) {
            multimap.get((Object)i).sort((o1, o2) -> Double.compare(o1.distance(), o2.distance()));
        }
        return (ListMultimap<Integer, Offset>)new ImmutableListMultimap.Builder().putAll((Multimap)multimap).build();
    }
    
    public static RenderSpec getSurfaceSpec() {
        if (RenderSpec.lastSurfaceRenderSpec == null || RenderSpec.lastSurfaceRenderSpec.lastPlayerCoord.field_77276_a != RenderSpec.minecraft.field_71439_g.field_70176_ah || RenderSpec.lastSurfaceRenderSpec.lastPlayerCoord.field_77275_b != RenderSpec.minecraft.field_71439_g.field_70164_aj) {
            final RenderSpec newSpec = new RenderSpec(RenderSpec.minecraft, MapType.day(DataCache.getPlayer()));
            newSpec.copyLastStatsFrom(RenderSpec.lastSurfaceRenderSpec);
            RenderSpec.lastSurfaceRenderSpec = newSpec;
        }
        return RenderSpec.lastSurfaceRenderSpec;
    }
    
    public static RenderSpec getTopoSpec() {
        if (RenderSpec.lastTopoRenderSpec == null || RenderSpec.lastTopoRenderSpec.lastPlayerCoord.field_77276_a != RenderSpec.minecraft.field_71439_g.field_70176_ah || RenderSpec.lastTopoRenderSpec.lastPlayerCoord.field_77275_b != RenderSpec.minecraft.field_71439_g.field_70164_aj) {
            final RenderSpec newSpec = new RenderSpec(RenderSpec.minecraft, MapType.topo(DataCache.getPlayer()));
            newSpec.copyLastStatsFrom(RenderSpec.lastTopoRenderSpec);
            RenderSpec.lastTopoRenderSpec = newSpec;
        }
        return RenderSpec.lastTopoRenderSpec;
    }
    
    public static RenderSpec getUndergroundSpec() {
        if (RenderSpec.lastUndergroundRenderSpec == null || RenderSpec.lastUndergroundRenderSpec.lastPlayerCoord.field_77276_a != RenderSpec.minecraft.field_71439_g.field_70176_ah || RenderSpec.lastUndergroundRenderSpec.lastPlayerCoord.field_77275_b != RenderSpec.minecraft.field_71439_g.field_70164_aj) {
            final RenderSpec newSpec = new RenderSpec(RenderSpec.minecraft, MapType.underground(DataCache.getPlayer()));
            newSpec.copyLastStatsFrom(RenderSpec.lastUndergroundRenderSpec);
            RenderSpec.lastUndergroundRenderSpec = newSpec;
        }
        return RenderSpec.lastUndergroundRenderSpec;
    }
    
    public static void resetRenderSpecs() {
        RenderSpec.lastUndergroundRenderSpec = null;
        RenderSpec.lastSurfaceRenderSpec = null;
        RenderSpec.lastTopoRenderSpec = null;
    }
    
    protected List<ChunkPos> getRenderAreaCoords() {
        if (this.offsets == null) {
            this.offsets = calculateOffsets(this.primaryRenderDistance, this.maxSecondaryRenderDistance, this.revealShape);
        }
        final DataCache dataCache = DataCache.INSTANCE;
        if (this.lastPlayerCoord == null || this.lastPlayerCoord.field_77276_a != this.player.field_70176_ah || this.lastPlayerCoord.field_77275_b != this.player.field_70164_aj) {
            this.primaryRenderCoords = null;
            this.lastSecondaryRenderDistance = this.primaryRenderDistance;
        }
        this.lastPlayerCoord = new ChunkPos(RenderSpec.minecraft.field_71439_g.field_70176_ah, RenderSpec.minecraft.field_71439_g.field_70164_aj);
        if (this.primaryRenderCoords == null || this.primaryRenderCoords.isEmpty()) {
            final List<Offset> primaryOffsets = (List<Offset>)this.offsets.get((Object)this.primaryRenderDistance);
            this.primaryRenderCoords = new ArrayList<ChunkPos>(primaryOffsets.size());
            for (final Offset offset : primaryOffsets) {
                final ChunkPos primaryCoord = offset.from(this.lastPlayerCoord);
                this.primaryRenderCoords.add(primaryCoord);
                dataCache.getChunkMD(primaryCoord);
            }
        }
        if (this.maxSecondaryRenderDistance == this.primaryRenderDistance) {
            return new ArrayList<ChunkPos>(this.primaryRenderCoords);
        }
        if (this.lastSecondaryRenderDistance == this.maxSecondaryRenderDistance) {
            this.lastSecondaryRenderDistance = this.primaryRenderDistance;
        }
        ++this.lastSecondaryRenderDistance;
        final List<Offset> secondaryOffsets = (List<Offset>)this.offsets.get((Object)this.lastSecondaryRenderDistance);
        final ArrayList<ChunkPos> renderCoords = new ArrayList<ChunkPos>(this.primaryRenderCoords.size() + secondaryOffsets.size());
        for (final Offset offset2 : secondaryOffsets) {
            final ChunkPos secondaryCoord = offset2.from(this.lastPlayerCoord);
            renderCoords.add(secondaryCoord);
            dataCache.getChunkMD(secondaryCoord);
        }
        renderCoords.addAll(0, this.primaryRenderCoords);
        return renderCoords;
    }
    
    public Boolean isUnderground() {
        return this.mapType.isUnderground();
    }
    
    public Boolean isTopo() {
        return this.mapType.isTopo();
    }
    
    public Boolean getSurface() {
        return this.mapType.isSurface();
    }
    
    public int getPrimaryRenderDistance() {
        return this.primaryRenderDistance;
    }
    
    public int getMaxSecondaryRenderDistance() {
        return this.maxSecondaryRenderDistance;
    }
    
    public int getLastSecondaryRenderDistance() {
        return this.lastSecondaryRenderDistance;
    }
    
    public RevealShape getRevealShape() {
        return this.revealShape;
    }
    
    public int getLastSecondaryRenderSize() {
        if (this.primaryRenderDistance == this.maxSecondaryRenderDistance) {
            return 0;
        }
        return (this.offsets == null) ? 0 : this.offsets.get((Object)this.lastSecondaryRenderDistance).size();
    }
    
    public int getPrimaryRenderSize() {
        return (this.offsets == null) ? 0 : this.offsets.get((Object)this.primaryRenderDistance).size();
    }
    
    public void setLastTaskInfo(final int chunks, final long elapsedNs) {
        this.lastTaskChunks = chunks;
        this.lastTaskTime = TimeUnit.NANOSECONDS.toMillis(elapsedNs);
        this.lastTaskAvgChunkTime = elapsedNs / Math.max(1, chunks) / 1000000.0;
    }
    
    public int getLastTaskChunks() {
        return this.lastTaskChunks;
    }
    
    public void copyLastStatsFrom(final RenderSpec other) {
        if (other != null) {
            this.lastTaskChunks = other.lastTaskChunks;
            this.lastTaskTime = other.lastTaskTime;
            this.lastTaskAvgChunkTime = other.lastTaskAvgChunkTime;
        }
    }
    
    public String getDebugStats() {
        String debugString;
        if (this.isUnderground()) {
            debugString = "jm.common.renderstats_debug_cave";
        }
        else if (this.isTopo()) {
            debugString = "jm.common.renderstats_debug_topo";
        }
        else {
            debugString = "jm.common.renderstats_debug_surface";
        }
        debugString += "_simple";
        return Constants.getString(debugString, this.primaryRenderDistance, this.lastTaskChunks, this.lastTaskTime, RenderSpec.decFormat.format(this.lastTaskAvgChunkTime));
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final RenderSpec that = (RenderSpec)o;
        return this.maxSecondaryRenderDistance == that.maxSecondaryRenderDistance && this.primaryRenderDistance == that.primaryRenderDistance && this.revealShape == that.revealShape && this.mapType.equals(that.mapType);
    }
    
    @Override
    public int hashCode() {
        int result = this.mapType.hashCode();
        result = 31 * result + this.primaryRenderDistance;
        result = 31 * result + this.maxSecondaryRenderDistance;
        result = 31 * result + this.revealShape.hashCode();
        return result;
    }
    
    static {
        RenderSpec.decFormat = new DecimalFormat("##.#");
        RenderSpec.minecraft = Minecraft.func_71410_x();
    }
    
    public enum RevealShape implements KeyedEnum
    {
        Square("jm.minimap.shape_square"), 
        Circle("jm.minimap.shape_circle");
        
        public final String key;
        
        private RevealShape(final String key) {
            this.key = key;
        }
        
        @Override
        public String getKey() {
            return this.key;
        }
        
        @Override
        public String toString() {
            return Constants.getString(this.key);
        }
    }
    
    private static class Offset
    {
        final int x;
        final int z;
        
        private Offset(final int x, final int z) {
            this.x = x;
            this.z = z;
        }
        
        ChunkPos from(final ChunkPos coord) {
            return new ChunkPos(coord.field_77276_a + this.x, coord.field_77275_b + this.z);
        }
        
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final Offset offset = (Offset)o;
            return this.x == offset.x && this.z == offset.z;
        }
        
        public double distance() {
            return Math.sqrt(this.x * this.x + this.z * this.z);
        }
        
        @Override
        public int hashCode() {
            int result = this.x;
            result = 31 * result + this.z;
            return result;
        }
    }
}
