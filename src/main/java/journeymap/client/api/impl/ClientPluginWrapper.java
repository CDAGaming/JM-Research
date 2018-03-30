package journeymap.client.api.impl;

import javax.annotation.*;
import journeymap.client.api.*;
import journeymap.client.log.*;
import journeymap.client.api.event.*;
import journeymap.client.render.draw.*;
import journeymap.client.waypoint.*;
import journeymap.common.*;
import journeymap.common.log.*;
import journeymap.client.api.display.*;
import java.util.*;
import journeymap.client.api.util.*;
import com.google.common.collect.*;
import com.google.common.base.*;

@ParametersAreNonnullByDefault
class ClientPluginWrapper
{
    private final IClientPlugin plugin;
    private final String modId;
    private final StatTimer eventTimer;
    private final HashMap<Integer, HashBasedTable<String, Overlay, OverlayDrawStep>> dimensionOverlays;
    private final HashBasedTable<String, Waypoint, Waypoint> waypoints;
    private EnumSet<ClientEvent.Type> subscribedClientEventTypes;
    
    public ClientPluginWrapper(final IClientPlugin plugin) {
        this.dimensionOverlays = new HashMap<Integer, HashBasedTable<String, Overlay, OverlayDrawStep>>();
        this.waypoints = (HashBasedTable<String, Waypoint, Waypoint>)HashBasedTable.create();
        this.subscribedClientEventTypes = EnumSet.noneOf(ClientEvent.Type.class);
        this.modId = plugin.getModId();
        this.plugin = plugin;
        this.eventTimer = StatTimer.get("pluginClientEvent_" + this.modId, 1, 200);
    }
    
    private HashBasedTable<String, Overlay, OverlayDrawStep> getOverlays(final int dimension) {
        HashBasedTable<String, Overlay, OverlayDrawStep> table = this.dimensionOverlays.get(dimension);
        if (table == null) {
            table = (HashBasedTable<String, Overlay, OverlayDrawStep>)HashBasedTable.create();
            this.dimensionOverlays.put(dimension, table);
        }
        return table;
    }
    
    public void show(final Displayable displayable) throws Exception {
        final String displayId = displayable.getId();
        switch (displayable.getDisplayType()) {
            case Polygon: {
                final PolygonOverlay polygon = (PolygonOverlay)displayable;
                this.getOverlays(polygon.getDimension()).put((Object)displayId, (Object)polygon, (Object)new DrawPolygonStep(polygon));
                break;
            }
            case Marker: {
                final MarkerOverlay marker = (MarkerOverlay)displayable;
                this.getOverlays(marker.getDimension()).put((Object)displayId, (Object)marker, (Object)new DrawMarkerStep(marker));
                break;
            }
            case Image: {
                final ImageOverlay imageOverlay = (ImageOverlay)displayable;
                this.getOverlays(imageOverlay.getDimension()).put((Object)displayId, (Object)imageOverlay, (Object)new DrawImageStep(imageOverlay));
                break;
            }
            case Waypoint: {
                final Waypoint waypoint = (Waypoint)displayable;
                WaypointStore.INSTANCE.save(waypoint);
                this.waypoints.put((Object)displayId, (Object)waypoint, (Object)waypoint);
                break;
            }
        }
    }
    
    public void remove(final Displayable displayable) {
        final String displayId = displayable.getId();
        try {
            switch (displayable.getDisplayType()) {
                case Waypoint: {
                    this.remove((Waypoint)displayable);
                    break;
                }
                default: {
                    final Overlay overlay = (Overlay)displayable;
                    final OverlayDrawStep drawStep = (OverlayDrawStep)this.getOverlays(overlay.getDimension()).remove((Object)displayId, (Object)displayable);
                    if (drawStep != null) {
                        drawStep.setEnabled(false);
                        break;
                    }
                    break;
                }
            }
        }
        catch (Throwable t) {
            Journeymap.getLogger().error("Error removing DrawMarkerStep: " + LogFormatter.toString(t));
        }
    }
    
    public void remove(final Waypoint waypoint) {
        final String displayId = waypoint.getId();
        WaypointStore.INSTANCE.remove(waypoint);
        this.waypoints.remove((Object)displayId, (Object)waypoint);
    }
    
    public void removeAll(final DisplayType displayType) {
        if (displayType == DisplayType.Waypoint) {
            final List<Waypoint> list = new ArrayList<Waypoint>(this.waypoints.columnKeySet());
            for (final Waypoint waypoint : list) {
                this.remove(waypoint);
            }
        }
        else {
            for (final HashBasedTable<String, Overlay, OverlayDrawStep> overlays : this.dimensionOverlays.values()) {
                final List<Displayable> list2 = new ArrayList<Displayable>(overlays.columnKeySet());
                for (final Displayable displayable : list2) {
                    if (displayable.getDisplayType() == displayType) {
                        this.remove(displayable);
                    }
                }
            }
        }
    }
    
    public void removeAll() {
        if (!this.waypoints.isEmpty()) {
            final List<Waypoint> list = new ArrayList<Waypoint>(this.waypoints.columnKeySet());
            for (final Waypoint waypoint : list) {
                this.remove(waypoint);
            }
        }
        if (!this.dimensionOverlays.isEmpty()) {
            this.dimensionOverlays.clear();
        }
    }
    
    public boolean exists(final Displayable displayable) {
        final String displayId = displayable.getId();
        switch (displayable.getDisplayType()) {
            case Waypoint: {
                return this.waypoints.containsRow((Object)displayId);
            }
            default: {
                if (displayable instanceof Overlay) {
                    final int dimension = ((Overlay)displayable).getDimension();
                    return this.getOverlays(dimension).containsRow((Object)displayId);
                }
                return false;
            }
        }
    }
    
    public void getDrawSteps(final List<OverlayDrawStep> list, final UIState uiState) {
        final HashBasedTable<String, Overlay, OverlayDrawStep> table = this.getOverlays(uiState.dimension);
        for (final Table.Cell<String, Overlay, OverlayDrawStep> cell : table.cellSet()) {
            if (((Overlay)cell.getColumnKey()).isActiveIn(uiState)) {
                list.add((OverlayDrawStep)cell.getValue());
            }
        }
    }
    
    public void subscribe(final EnumSet<ClientEvent.Type> enumSet) {
        this.subscribedClientEventTypes = EnumSet.copyOf(enumSet);
    }
    
    public EnumSet<ClientEvent.Type> getSubscribedClientEventTypes() {
        return this.subscribedClientEventTypes;
    }
    
    public void notify(final ClientEvent clientEvent) {
        if (!this.subscribedClientEventTypes.contains(clientEvent.type)) {
            return;
        }
        try {
            final boolean cancelled = clientEvent.isCancelled();
            final boolean cancellable = clientEvent.type.cancellable;
            this.eventTimer.start();
            try {
                this.plugin.onEvent(clientEvent);
                if (cancellable && !cancelled && clientEvent.isCancelled()) {
                    Journeymap.getLogger().debug(String.format("Plugin %s cancelled event: %s", this, clientEvent.type));
                }
            }
            catch (Throwable t) {
                Journeymap.getLogger().error(String.format("Plugin %s errored during event: %s", this, clientEvent.type), t);
            }
            finally {
                this.eventTimer.stop();
                if (this.eventTimer.hasReachedElapsedLimit()) {
                    Journeymap.getLogger().warn(String.format("Plugin %s too slow handling event: %s", this, clientEvent.type));
                }
            }
        }
        catch (Throwable t2) {
            Journeymap.getLogger().error(String.format("Plugin %s error during event: %s", this, clientEvent.type), t2);
        }
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClientPluginWrapper)) {
            return false;
        }
        final ClientPluginWrapper that = (ClientPluginWrapper)o;
        return Objects.equal((Object)this.modId, (Object)that.modId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(new Object[] { this.modId });
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper((Object)this.plugin).add("modId", (Object)this.modId).toString();
    }
}
