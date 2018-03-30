package journeymap.client.model;

import java.util.concurrent.atomic.*;
import journeymap.client.log.*;
import java.io.*;
import journeymap.common.properties.config.*;
import journeymap.common.properties.*;
import net.minecraft.client.*;
import net.minecraft.entity.player.*;
import journeymap.common.*;
import journeymap.client.io.*;
import journeymap.client.feature.*;
import journeymap.client.*;
import journeymap.common.api.feature.*;
import journeymap.client.data.*;
import journeymap.common.log.*;
import journeymap.common.feature.*;
import net.minecraft.world.*;
import com.google.common.collect.*;
import com.google.common.base.*;
import journeymap.client.render.map.*;
import journeymap.client.api.impl.*;
import journeymap.client.render.draw.*;
import java.util.*;
import journeymap.client.waypoint.*;
import journeymap.client.api.display.*;
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
    private MapView lastMapView;
    private File worldDir;
    private long lastRefresh;
    private long lastMapTypeChange;
    private IntegerField lastSlice;
    private boolean dayMappingAllowed;
    private boolean nightMappingAllowed;
    private boolean caveMappingAllowed;
    private boolean topoMappingAllowed;
    private boolean biomeMappingAllowed;
    private boolean caveMappingEnabled;
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
        this.dayMappingAllowed = false;
        this.nightMappingAllowed = false;
        this.caveMappingAllowed = false;
        this.topoMappingAllowed = false;
        this.biomeMappingAllowed = false;
        this.caveMappingEnabled = false;
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
        final World world = Journeymap.clientWorld();
        if (world == null || world.field_73011_w == null) {
            return;
        }
        this.refreshTimer.start();
        try {
            final CoreProperties coreProperties = Journeymap.getClient().getCoreProperties();
            this.lastMapProperties = mapProperties;
            this.worldDir = FileHandler.getJMWorldDir(mc);
            if (world != null && world.func_72940_L() != 256 && this.lastSlice.getMaxValue() != 15) {
                final int maxSlice = world.func_72940_L() / 16 - 1;
                final int seaLevel = Math.round(world.func_181545_F() / 16);
                final int currentSlice = this.lastSlice.get();
                (this.lastSlice = new IntegerField(Category.Hidden, "", 0, maxSlice, seaLevel)).set(currentSlice);
            }
            final boolean hasSurface = !(world.field_73011_w instanceof WorldProviderHell);
            final int dimension = world.field_73011_w.getDimension();
            final DimensionPolicies policy = ClientFeatures.instance().get(dimension);
            final GameType gameType = JourneymapClient.getGameType();
            this.caveMappingAllowed = policy.isAllowed(gameType, Feature.MapType.Underground);
            this.caveMappingEnabled = (this.caveMappingAllowed && mapProperties.showCaves.get());
            this.dayMappingAllowed = (hasSurface && policy.isAllowed(gameType, Feature.MapType.Day));
            this.nightMappingAllowed = (hasSurface && policy.isAllowed(gameType, Feature.MapType.Night));
            this.topoMappingAllowed = (hasSurface && policy.isAllowed(gameType, Feature.MapType.Topo) && coreProperties.mapTopography.get());
            this.biomeMappingAllowed = policy.isAllowed(gameType, Feature.MapType.Biome);
            this.highQuality = coreProperties.tileHighDisplayQuality.get();
            this.lastPlayerChunkX = player.field_70176_ah;
            this.lastPlayerChunkY = player.field_70162_ai;
            this.lastPlayerChunkZ = player.field_70164_aj;
            final EntityDTO playerDTO = DataCache.getPlayer();
            this.playerBiome = playerDTO.biome;
            if (this.lastMapView != null) {
                if (player.field_71093_bK != this.lastMapView.dimension) {
                    this.lastMapView = null;
                }
                else if (this.caveMappingEnabled && this.follow.get() && playerDTO.underground && !this.lastMapView.isUnderground()) {
                    this.lastMapView = null;
                }
                else if (!this.lastMapView.isAllowed()) {
                    this.lastMapView = null;
                }
            }
            this.lastMapView = this.getMapView();
            this.updateLastRefresh();
        }
        catch (Exception e) {
            Journeymap.getLogger().error("Error refreshing MapState: " + LogFormatter.toPartialString(e));
        }
        finally {
            this.refreshTimer.stop();
        }
    }
    
    public MapView setMapType(final Feature.MapType mapType) {
        if (mapType == null) {
            return this.setMapType(MapView.NONE);
        }
        return this.setMapType(MapView.from(mapType, DataCache.getPlayer()));
    }
    
    public MapView toggleMapType() {
        final Feature.MapType next = this.getNextMapType(this.getMapView().mapType);
        return this.setMapType(next);
    }
    
    public Feature.MapType getNextMapType(final Feature.MapType mapType) {
        final EntityDTO player = DataCache.getPlayer();
        if (player.entityRef.get() == null) {
            return mapType;
        }
        final List<Feature.MapType> types = new ArrayList<Feature.MapType>(4);
        if (this.dayMappingAllowed) {
            types.add(Feature.MapType.Day);
        }
        if (this.nightMappingAllowed) {
            types.add(Feature.MapType.Night);
        }
        if (this.caveMappingAllowed && (player.underground || mapType == Feature.MapType.Underground)) {
            types.add(Feature.MapType.Underground);
        }
        if (this.topoMappingAllowed) {
            types.add(Feature.MapType.Topo);
        }
        if (mapType == null && !types.isEmpty()) {
            return types.get(0);
        }
        if (types.contains(mapType)) {
            final Iterator<Feature.MapType> cyclingIterator = Iterables.cycle((Iterable)types).iterator();
            while (cyclingIterator.hasNext()) {
                final Feature.MapType current = cyclingIterator.next();
                if (current == mapType) {
                    return cyclingIterator.next();
                }
            }
        }
        return mapType;
    }
    
    public MapView setMapType(MapView mapView) {
        if (!mapView.isAllowed()) {
            mapView = MapView.from(this.getNextMapType(mapView.mapType), DataCache.getPlayer());
            if (!mapView.isAllowed()) {
                mapView = MapView.none();
            }
        }
        final EntityDTO player = DataCache.getPlayer();
        if (player.underground != mapView.isUnderground()) {
            this.follow.set(false);
        }
        if (mapView.isUnderground()) {
            if (player.chunkCoordY != mapView.vSlice) {
                this.follow.set(false);
            }
            this.lastSlice.set(mapView.vSlice);
        }
        this.setLastMapTypeChange(mapView);
        return this.lastMapView;
    }
    
    public MapView getMapView() {
        if (this.lastMapView == null) {
            final EntityDTO player = DataCache.getPlayer();
            MapView mapView = null;
            try {
                if (this.caveMappingEnabled && player.underground) {
                    mapView = MapView.underground(player);
                }
                else if (this.follow.get() && !player.underground) {
                    if (this.dayMappingAllowed) {
                        mapView = MapView.day(player);
                    }
                    else if (this.nightMappingAllowed) {
                        mapView = MapView.night(player);
                    }
                    else if (this.topoMappingAllowed) {
                        mapView = MapView.topo(player);
                    }
                }
            }
            catch (Exception e) {
                Journeymap.getLogger().warn("Error determining MapView: ", (Object)LogFormatter.toPartialString(e));
            }
            if (mapView == null) {
                mapView = MapView.day(player);
            }
            this.setMapType(mapView);
        }
        return this.lastMapView;
    }
    
    public long getLastMapTypeChange() {
        return this.lastMapTypeChange;
    }
    
    private void setLastMapTypeChange(final MapView mapView) {
        if (!Objects.equal((Object)mapView, (Object)this.lastMapView)) {
            this.lastMapTypeChange = System.currentTimeMillis();
            this.requireRefresh();
        }
        this.lastMapView = mapView;
    }
    
    public boolean isUnderground() {
        return this.getMapView().isUnderground();
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
        final int dimension = this.getDimension();
        final DimensionPolicies policy = ClientFeatures.instance().get(dimension);
        final GameType gameType = JourneymapClient.getGameType();
        if (policy.isAllowed(gameType, Feature.Radar.Vehicle) && mapProperties.showVehicles.get()) {
            this.entityList.addAll(DataCache.INSTANCE.getVehicles(false).values());
        }
        if (policy.isAllowed(gameType, Feature.Radar.PassiveMob) && (mapProperties.showAnimals.get() || mapProperties.showPets.get())) {
            this.entityList.addAll(DataCache.INSTANCE.getPassiveMobs(false).values());
        }
        if (policy.isAllowed(gameType, Feature.Radar.NPC) && mapProperties.showVillagers.get()) {
            this.entityList.addAll(DataCache.INSTANCE.getNpcs(false).values());
        }
        if (policy.isAllowed(gameType, Feature.Radar.HostileMob) && mapProperties.showMobs.get()) {
            this.entityList.addAll(DataCache.INSTANCE.getHostileMobs(false).values());
        }
        if (policy.isAllowed(gameType, Feature.Radar.Player) && mapProperties.showPlayers.get()) {
            this.entityList.addAll(DataCache.INSTANCE.getPlayers(false).values());
        }
        if (!this.entityList.isEmpty()) {
            Collections.sort(this.entityList, EntityHelper.entityMapComparator);
            this.drawStepList.addAll(radarRenderer.prepareSteps(this.entityList, gridRenderer, mapProperties));
        }
        if (mapProperties.showWaypoints.get() && policy.isAllowed(gameType, Feature.Radar.Waypoint)) {
            final boolean showLabel = mapProperties.showWaypointLabels.get();
            this.drawWaypointStepList.addAll(waypointRenderer.prepareSteps(WaypointStore.INSTANCE.getAll(dimension), gridRenderer, checkWaypointDistance, showLabel));
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
        if (this.lastMapView == null) {
            return true;
        }
        final EntityDTO player = DataCache.getPlayer();
        if (this.getMapView().dimension != player.dimension) {
            return true;
        }
        final double d0 = this.lastPlayerChunkX - player.chunkCoordX;
        final double d2 = this.lastPlayerChunkY - player.chunkCoordY;
        final double d3 = this.lastPlayerChunkZ - player.chunkCoordZ;
        final double diff = MathHelper.func_76133_a(d0 * d0 + d2 * d2 + d3 * d3);
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
    
    public boolean isDayMappingAllowed() {
        return this.dayMappingAllowed;
    }
    
    public boolean isNightMappingAllowed() {
        return this.nightMappingAllowed;
    }
    
    public boolean isTopoMappingAllowed() {
        return this.topoMappingAllowed;
    }
    
    public boolean isBiomeMappingAllowed() {
        return this.biomeMappingAllowed;
    }
    
    public int getDimension() {
        return this.getMapView().dimension;
    }
    
    public IntegerField getLastSlice() {
        return this.lastSlice;
    }
    
    public void resetMapType() {
        this.lastMapView = null;
    }
}
