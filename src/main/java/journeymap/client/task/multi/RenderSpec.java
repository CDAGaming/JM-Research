package journeymap.client.task.multi;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import journeymap.client.Constants;
import journeymap.client.data.DataCache;
import journeymap.client.model.MapType;
import journeymap.client.properties.CoreProperties;
import journeymap.client.ui.option.KeyedEnum;
import journeymap.common.Journeymap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.ChunkPos;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RenderSpec {
    private static DecimalFormat decFormat;
    private static volatile RenderSpec lastSurfaceRenderSpec;
    private static volatile RenderSpec lastTopoRenderSpec;
    private static volatile RenderSpec lastUndergroundRenderSpec;
    private static Minecraft minecraft;

    static {
        RenderSpec.decFormat = new DecimalFormat("##.#");
        RenderSpec.minecraft = Minecraft.getMinecraft();
    }

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
        this.player = (EntityPlayer) minecraft.player;
        final CoreProperties props = Journeymap.getClient().getCoreProperties();
        final int gameRenderDistance = Math.max(1, minecraft.gameSettings.renderDistanceChunks - 1);
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
        this.lastPlayerCoord = new ChunkPos(minecraft.player.chunkCoordX, minecraft.player.chunkCoordZ);
        this.lastSecondaryRenderDistance = this.primaryRenderDistance;
    }

    private static Double blockDistance(final ChunkPos playerCoord, final ChunkPos coord) {
        final int x = (playerCoord.x << 4) + 8 - ((coord.x << 4) + 8);
        final int z = (playerCoord.z << 4) + 8 - ((coord.z << 4) + 8);
        return Math.sqrt(x * x + z * z);
    }

    private static Double chunkDistance(final ChunkPos playerCoord, final ChunkPos coord) {
        final int x = playerCoord.x - coord.x;
        final int z = playerCoord.z - coord.z;
        return Math.sqrt(x * x + z * z);
    }

    static boolean inRange(final ChunkPos playerCoord, final ChunkPos coord, final int renderDistance, final RevealShape revealShape) {
        if (revealShape == RevealShape.Circle) {
            final double distance = blockDistance(playerCoord, coord);
            final double diff = distance - renderDistance * 16;
            return diff <= 8.0;
        }
        final float x = Math.abs(playerCoord.x - coord.x);
        final float z = Math.abs(playerCoord.z - coord.z);
        return x <= renderDistance && z <= renderDistance;
    }

    private static ListMultimap<Integer, Offset> calculateOffsets(final int minOffset, final int maxOffset, final RevealShape revealShape) {
        final ListMultimap<Integer, Offset> multimap = ArrayListMultimap.create();
        int offset = maxOffset;
        final int baseX = 0;
        final int baseZ = 0;
        final ChunkPos baseCoord = new ChunkPos(0, 0);
        while (offset >= minOffset) {
            for (int x = 0 - offset; x <= 0 + offset; ++x) {
                for (int z = 0 - offset; z <= 0 + offset; ++z) {
                    final ChunkPos coord = new ChunkPos(x, z);
                    if (revealShape == RevealShape.Square || inRange(baseCoord, coord, offset, revealShape)) {
                        multimap.put(offset, new Offset(coord.x, coord.z));
                    }
                }
            }
            if (offset < maxOffset) {
                final List<Offset> oneUp = (List<Offset>) multimap.get((offset + 1));
                oneUp.removeAll(multimap.get(offset));
            }
            --offset;
        }
        for (int i = minOffset; i <= maxOffset; ++i) {
            multimap.get(i).sort((o1, o2) -> Double.compare(o1.distance(), o2.distance()));
        }
        return (ListMultimap<Integer, Offset>) new ImmutableListMultimap.Builder().putAll((Multimap) multimap).build();
    }

    public static RenderSpec getSurfaceSpec() {
        if (RenderSpec.lastSurfaceRenderSpec == null || RenderSpec.lastSurfaceRenderSpec.lastPlayerCoord.x != RenderSpec.minecraft.player.chunkCoordX || RenderSpec.lastSurfaceRenderSpec.lastPlayerCoord.z != RenderSpec.minecraft.player.chunkCoordZ) {
            final RenderSpec newSpec = new RenderSpec(RenderSpec.minecraft, MapType.day(DataCache.getPlayer()));
            newSpec.copyLastStatsFrom(RenderSpec.lastSurfaceRenderSpec);
            RenderSpec.lastSurfaceRenderSpec = newSpec;
        }
        return RenderSpec.lastSurfaceRenderSpec;
    }

    public static RenderSpec getTopoSpec() {
        if (RenderSpec.lastTopoRenderSpec == null || RenderSpec.lastTopoRenderSpec.lastPlayerCoord.x != RenderSpec.minecraft.player.chunkCoordX || RenderSpec.lastTopoRenderSpec.lastPlayerCoord.z != RenderSpec.minecraft.player.chunkCoordZ) {
            final RenderSpec newSpec = new RenderSpec(RenderSpec.minecraft, MapType.topo(DataCache.getPlayer()));
            newSpec.copyLastStatsFrom(RenderSpec.lastTopoRenderSpec);
            RenderSpec.lastTopoRenderSpec = newSpec;
        }
        return RenderSpec.lastTopoRenderSpec;
    }

    public static RenderSpec getUndergroundSpec() {
        if (RenderSpec.lastUndergroundRenderSpec == null || RenderSpec.lastUndergroundRenderSpec.lastPlayerCoord.x != RenderSpec.minecraft.player.chunkCoordX || RenderSpec.lastUndergroundRenderSpec.lastPlayerCoord.z != RenderSpec.minecraft.player.chunkCoordZ) {
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
        if (this.lastPlayerCoord == null || this.lastPlayerCoord.x != this.player.chunkCoordX || this.lastPlayerCoord.z != this.player.chunkCoordZ) {
            this.primaryRenderCoords = null;
            this.lastSecondaryRenderDistance = this.primaryRenderDistance;
        }
        this.lastPlayerCoord = new ChunkPos(RenderSpec.minecraft.player.chunkCoordX, RenderSpec.minecraft.player.chunkCoordZ);
        if (this.primaryRenderCoords == null || this.primaryRenderCoords.isEmpty()) {
            final List<Offset> primaryOffsets = (List<Offset>) this.offsets.get(this.primaryRenderDistance);
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
        final List<Offset> secondaryOffsets = (List<Offset>) this.offsets.get(this.lastSecondaryRenderDistance);
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
        return (this.offsets == null) ? 0 : this.offsets.get(this.lastSecondaryRenderDistance).size();
    }

    public int getPrimaryRenderSize() {
        return (this.offsets == null) ? 0 : this.offsets.get(this.primaryRenderDistance).size();
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
        } else if (this.isTopo()) {
            debugString = "jm.common.renderstats_debug_topo";
        } else {
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
        final RenderSpec that = (RenderSpec) o;
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

    public enum RevealShape implements KeyedEnum {
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

    private static class Offset {
        final int x;
        final int z;

        private Offset(final int x, final int z) {
            this.x = x;
            this.z = z;
        }

        ChunkPos from(final ChunkPos coord) {
            return new ChunkPos(coord.x + this.x, coord.z + this.z);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final Offset offset = (Offset) o;
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
