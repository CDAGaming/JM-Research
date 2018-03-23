package journeymap.client.model;

import java.util.concurrent.atomic.*;

import com.google.common.base.Objects;
import journeymap.client.log.*;
import java.io.*;
import journeymap.common.properties.config.*;
import journeymap.common.properties.*;
import net.minecraft.client.*;
import net.minecraft.entity.player.*;
import journeymap.common.*;
import journeymap.client.io.*;
import journeymap.client.feature.*;
import journeymap.client.data.*;
import journeymap.common.log.*;
import net.minecraft.world.*;
import net.minecraft.entity.*;
import com.google.common.collect.*;
import com.google.common.base.*;
import journeymap.client.render.map.*;
import journeymap.client.api.impl.*;
import journeymap.client.render.draw.*;
import java.util.*;
import journeymap.client.properties.*;
import journeymap.client.task.multi.*;
import net.minecraft.util.math.*;

public class MapState
{
    public final int minZoom = 0;
    public final int maxZoom = 5;
    public AtomicBoolean follow;
    public String playerLastPos;
    private StatTimer refreshTimer;
    private StatTimer generateDrawStepsTimer;
    private MapType lastMapType;
    private File worldDir;
    private long lastRefresh;
    private long lastMapTypeChange;
    private IntegerField lastSlice;
    private boolean surfaceMappingAllowed;
    private boolean caveMappingAllowed;
    private boolean caveMappingEnabled;
    private boolean topoMappingAllowed;
    private List<DrawStep> drawStepList;
    private List<DrawWayPointStep> drawWaypointStepList;
    private String playerBiome;
    private InGameMapProperties lastMapProperties;
    private List<EntityDTO> entityList;
    private int lastPlayerChunkX;
    private int lastPlayerChunkY;
    private int lastPlayerChunkZ;
    private boolean highQuality;
    
    public MapState() {
        this.follow = new AtomicBoolean(true);
        this.playerLastPos = "0,0";
        this.refreshTimer = StatTimer.get("MapState.refresh");
        this.generateDrawStepsTimer = StatTimer.get("MapState.generateDrawSteps");
        this.worldDir = null;
        this.lastRefresh = 0L;
        this.lastMapTypeChange = 0L;
        this.lastSlice = new IntegerField(Category.Hidden, "", 0, 15, 4);
        this.surfaceMappingAllowed = false;
        this.caveMappingAllowed = false;
        this.caveMappingEnabled = false;
        this.topoMappingAllowed = false;
        this.drawStepList = new ArrayList<DrawStep>();
        this.drawWaypointStepList = new ArrayList<DrawWayPointStep>();
        this.playerBiome = "";
        this.lastMapProperties = null;
        this.entityList = new ArrayList<EntityDTO>(32);
        this.lastPlayerChunkX = 0;
        this.lastPlayerChunkY = 0;
        this.lastPlayerChunkZ = 0;
    }
    
    public void refresh(final Minecraft mc, final EntityPlayer player, final InGameMapProperties mapProperties) {
        final World world = (World)mc.world;
        if (world == null || world.provider == null) {
            return;
        }
        this.refreshTimer.start();
        try {
            final CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
            this.lastMapProperties = mapProperties;
            this.worldDir = FileHandler.getJMWorldDir(mc);
            if (world != null && world.getActualHeight() != 256 && this.lastSlice.getMaxValue() != 15) {
                final int maxSlice = world.getActualHeight() / 16 - 1;
                final int seaLevel = Math.round(world.getSeaLevel() / 16);
                final int currentSlice = this.lastSlice.get();
                (this.lastSlice = new IntegerField(Category.Hidden, "", 0, maxSlice, seaLevel)).set(currentSlice);
            }
            final boolean hasSurface = !(world.provider instanceof WorldProviderHell);
            this.caveMappingAllowed = FeatureManager.isAllowed(Feature.MapCaves);
            this.caveMappingEnabled = (this.caveMappingAllowed && mapProperties.showCaves.get());
            this.surfaceMappingAllowed = (hasSurface && FeatureManager.isAllowed(Feature.MapSurface));
            this.topoMappingAllowed = (hasSurface && FeatureManager.isAllowed(Feature.MapTopo) && coreProperties.mapTopography.get());
            this.highQuality = coreProperties.tileHighDisplayQuality.get();
            this.lastPlayerChunkX = player.chunkCoordX;
            this.lastPlayerChunkY = player.chunkCoordY;
            this.lastPlayerChunkZ = player.chunkCoordZ;
            final EntityDTO playerDTO = DataCache.getPlayer();
            this.playerBiome = playerDTO.biome;
            if (this.lastMapType != null) {
                if (player.dimension != this.lastMapType.dimension) {
                    this.lastMapType = null;
                }
                else if (this.caveMappingEnabled && this.follow.get() && playerDTO.underground && !this.lastMapType.isUnderground()) {
                    this.lastMapType = null;
                }
                else if (!this.lastMapType.isAllowed()) {
                    this.lastMapType = null;
                }
            }
            this.lastMapType = this.getMapType();
            this.updateLastRefresh();
        }
        catch (Exception e) {
            Journeymap.getLogger().error("Error refreshing MapState: " + LogFormatter.toPartialString(e));
        }
        finally {
            this.refreshTimer.stop();
        }
    }
    
    public MapType setMapType(final MapType.Name mapTypeName) {
        return this.setMapType(MapType.from(mapTypeName, DataCache.getPlayer()));
    }
    
    public MapType toggleMapType() {
        final MapType.Name next = this.getNextMapType(this.getMapType().name);
        return this.setMapType(next);
    }
    
    public MapType.Name getNextMapType(final MapType.Name name) {
        final EntityDTO player = DataCache.getPlayer();
        final EntityLivingBase playerEntity = player.entityLivingRef.get();
        if (playerEntity == null) {
            return name;
        }
        final List<MapType.Name> types = new ArrayList<MapType.Name>(4);
        if (this.surfaceMappingAllowed) {
            types.add(MapType.Name.day);
            types.add(MapType.Name.night);
        }
        if (this.caveMappingAllowed && (player.underground || name == MapType.Name.underground)) {
            types.add(MapType.Name.underground);
        }
        if (this.topoMappingAllowed) {
            types.add(MapType.Name.topo);
        }
        if (name == MapType.Name.none && !types.isEmpty()) {
            return types.get(0);
        }
        if (types.contains(name)) {
            final Iterator<MapType.Name> cyclingIterator = Iterables.cycle((Iterable)types).iterator();
            while (cyclingIterator.hasNext()) {
                final MapType.Name current = cyclingIterator.next();
                if (current == name) {
                    return cyclingIterator.next();
                }
            }
        }
        return name;
    }
    
    public MapType setMapType(MapType mapType) {
        if (!mapType.isAllowed()) {
            mapType = MapType.from(this.getNextMapType(mapType.name), DataCache.getPlayer());
            if (!mapType.isAllowed()) {
                mapType = MapType.none();
            }
        }
        final EntityDTO player = DataCache.getPlayer();
        if (player.underground != mapType.isUnderground()) {
            this.follow.set(false);
        }
        if (mapType.isUnderground()) {
            if (player.chunkCoordY != mapType.vSlice) {
                this.follow.set(false);
            }
            this.lastSlice.set(mapType.vSlice);
        }
        else if (mapType.name != MapType.Name.none && this.lastMapProperties.preferredMapType.get() != mapType.name) {
            this.lastMapProperties.preferredMapType.set(mapType.name);
            this.lastMapProperties.save();
        }
        this.setLastMapTypeChange(mapType);
        return this.lastMapType;
    }
    
    public MapType getMapType() {
        if (this.lastMapType == null) {
            final EntityDTO player = DataCache.getPlayer();
            MapType mapType = null;
            try {
                if (this.caveMappingEnabled && player.underground) {
                    mapType = MapType.underground(player);
                }
                else if (this.follow.get() && this.surfaceMappingAllowed && !player.underground) {
                    mapType = MapType.day(player);
                }
                if (mapType == null) {
                    mapType = MapType.from(this.lastMapProperties.preferredMapType.get(), player);
                }
            }
            catch (Exception e) {
                mapType = MapType.day(player);
            }
            this.setMapType(mapType);
        }
        return this.lastMapType;
    }
    
    public long getLastMapTypeChange() {
        return this.lastMapTypeChange;
    }
    
    private void setLastMapTypeChange(final MapType mapType) {
        if (!Objects.equal((Object)mapType, (Object)this.lastMapType)) {
            this.lastMapTypeChange = System.currentTimeMillis();
            this.requireRefresh();
        }
        this.lastMapType = mapType;
    }
    
    public boolean isUnderground() {
        return this.getMapType().isUnderground();
    }
    
    public File getWorldDir() {
        return this.worldDir;
    }
    
    public String getPlayerBiome() {
        return this.playerBiome;
    }
    
    public List<? extends DrawStep> getDrawSteps() {
        return this.drawStepList;
    }
    
    public List<DrawWayPointStep> getDrawWaypointSteps() {
        return this.drawWaypointStepList;
    }
    
    public void generateDrawSteps(final Minecraft mc, final GridRenderer gridRenderer, final WaypointDrawStepFactory waypointRenderer, final RadarDrawStepFactory radarRenderer, final InGameMapProperties mapProperties, final boolean checkWaypointDistance) {
        this.generateDrawStepsTimer.start();
        this.lastMapProperties = mapProperties;
        this.drawStepList.clear();
        this.drawWaypointStepList.clear();
        this.entityList.clear();
        ClientAPI.INSTANCE.getDrawSteps(this.drawStepList, gridRenderer.getUIState());
        if (FeatureManager.isAllowed(Feature.RadarAnimals) && (mapProperties.showAnimals.get() || mapProperties.showPets.get())) {
            this.entityList.addAll(DataCache.INSTANCE.getAnimals(false).values());
        }
        if (FeatureManager.isAllowed(Feature.RadarVillagers) && mapProperties.showVillagers.get()) {
            this.entityList.addAll(DataCache.INSTANCE.getVillagers(false).values());
        }
        if (FeatureManager.isAllowed(Feature.RadarMobs) && mapProperties.showMobs.get()) {
            this.entityList.addAll(DataCache.INSTANCE.getMobs(false).values());
        }
        if (FeatureManager.isAllowed(Feature.RadarPlayers) && mapProperties.showPlayers.get()) {
            this.entityList.addAll(DataCache.INSTANCE.getPlayers(false).values());
        }
        if (!this.entityList.isEmpty()) {
            Collections.sort(this.entityList, EntityHelper.entityMapComparator);
            this.drawStepList.addAll(radarRenderer.prepareSteps(this.entityList, gridRenderer, mapProperties));
        }
        if (mapProperties.showWaypoints.get()) {
            final boolean showLabel = mapProperties.showWaypointLabels.get();
            this.drawWaypointStepList.addAll(waypointRenderer.prepareSteps(DataCache.INSTANCE.getWaypoints(false), gridRenderer, checkWaypointDistance, showLabel));
        }
        this.generateDrawStepsTimer.stop();
    }
    
    public boolean zoomIn() {
        return this.lastMapProperties.zoomLevel.get() < 5 && this.setZoom(this.lastMapProperties.zoomLevel.get() + 1);
    }
    
    public boolean zoomOut() {
        return this.lastMapProperties.zoomLevel.get() > 0 && this.setZoom(this.lastMapProperties.zoomLevel.get() - 1);
    }
    
    public boolean setZoom(final int zoom) {
        if (zoom > 5 || zoom < 0 || zoom == this.lastMapProperties.zoomLevel.get()) {
            return false;
        }
        this.lastMapProperties.zoomLevel.set(zoom);
        this.requireRefresh();
        return true;
    }
    
    public int getZoom() {
        return this.lastMapProperties.zoomLevel.get();
    }
    
    public void requireRefresh() {
        this.lastRefresh = 0L;
    }
    
    public void updateLastRefresh() {
        this.lastRefresh = System.currentTimeMillis();
    }
    
    public boolean shouldRefresh(final Minecraft mc, final MapProperties mapProperties) {
        if (ClientAPI.INSTANCE.isDrawStepsUpdateNeeded()) {
            return true;
        }
        if (MapPlayerTask.getlastTaskCompleted() - this.lastRefresh > 500L) {
            return true;
        }
        if (this.lastMapType == null) {
            return true;
        }
        final EntityDTO player = DataCache.getPlayer();
        if (this.getMapType().dimension != player.dimension) {
            return true;
        }
        final double d0 = this.lastPlayerChunkX - player.chunkCoordX;
        final double d2 = this.lastPlayerChunkY - player.chunkCoordY;
        final double d3 = this.lastPlayerChunkZ - player.chunkCoordZ;
        final double diff = MathHelper.sqrt(d0 * d0 + d2 * d2 + d3 * d3);
        return diff > 2.0 || (this.lastMapProperties == null || !this.lastMapProperties.equals(mapProperties));
    }
    
    public boolean isHighQuality() {
        return this.highQuality;
    }
    
    public boolean isCaveMappingAllowed() {
        return this.caveMappingAllowed;
    }
    
    public boolean isCaveMappingEnabled() {
        return this.caveMappingEnabled;
    }
    
    public boolean isSurfaceMappingAllowed() {
        return this.surfaceMappingAllowed;
    }
    
    public boolean isTopoMappingAllowed() {
        return this.topoMappingAllowed;
    }
    
    public int getDimension() {
        return this.getMapType().dimension;
    }
    
    public IntegerField getLastSlice() {
        return this.lastSlice;
    }
    
    public void resetMapType() {
        this.lastMapType = null;
    }
}
