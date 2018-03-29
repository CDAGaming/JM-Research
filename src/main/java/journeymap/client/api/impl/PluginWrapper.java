package journeymap.client.api.impl;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.*;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.util.UIState;
import journeymap.client.log.StatTimer;
import journeymap.client.render.draw.DrawImageStep;
import journeymap.client.render.draw.DrawMarkerStep;
import journeymap.client.render.draw.DrawPolygonStep;
import journeymap.client.render.draw.OverlayDrawStep;
import journeymap.client.waypoint.WaypointStore;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

@ParametersAreNonnullByDefault
class PluginWrapper {
    private final IClientPlugin plugin;
    private final String modId;
    private final StatTimer eventTimer;
    private final HashMap<Integer, HashBasedTable<String, Overlay, OverlayDrawStep>> dimensionOverlays;
    private final HashBasedTable<String, Waypoint, journeymap.client.model.Waypoint> waypoints;
    private EnumSet<ClientEvent.Type> subscribedClientEventTypes;

    public PluginWrapper(final IClientPlugin plugin) {
        this.dimensionOverlays = new HashMap<>();
        this.waypoints = HashBasedTable.create();
        this.subscribedClientEventTypes = EnumSet.noneOf(ClientEvent.Type.class);
        this.modId = plugin.getModId();
        this.plugin = plugin;
        this.eventTimer = StatTimer.get("pluginClientEvent_" + this.modId, 1, 200);
    }

    private HashBasedTable<String, Overlay, OverlayDrawStep> getOverlays(final int dimension) {
        HashBasedTable<String, Overlay, OverlayDrawStep> table = this.dimensionOverlays.get(dimension);
        if (table == null) {
            table = HashBasedTable.create();
            this.dimensionOverlays.put(dimension, table);
        }
        return table;
    }

    public void show(final Displayable displayable) throws Exception {
        final String displayId = displayable.getId();
        switch (displayable.getDisplayType()) {
            case Polygon: {
                final PolygonOverlay polygon = (PolygonOverlay) displayable;
                this.getOverlays(polygon.getDimension()).put(displayId, polygon, new DrawPolygonStep(polygon));
                break;
            }
            case Marker: {
                final MarkerOverlay marker = (MarkerOverlay) displayable;
                this.getOverlays(marker.getDimension()).put(displayId, marker, new DrawMarkerStep(marker));
                break;
            }
            case Image: {
                final ImageOverlay imageOverlay = (ImageOverlay) displayable;
                this.getOverlays(imageOverlay.getDimension()).put(displayId, imageOverlay, new DrawImageStep(imageOverlay));
                break;
            }
            case Waypoint: {
                final Waypoint modWaypoint = (Waypoint) displayable;
                final journeymap.client.model.Waypoint waypoint = new journeymap.client.model.Waypoint(modWaypoint);
                WaypointStore.INSTANCE.save(waypoint);
                this.waypoints.put(displayId, modWaypoint, waypoint);
                break;
            }
        }
    }

    public void remove(final Displayable displayable) {
        final String displayId = displayable.getId();
        try {
            switch (displayable.getDisplayType()) {
                case Waypoint: {
                    this.remove((Waypoint) displayable);
                    break;
                }
                default: {
                    final Overlay overlay = (Overlay) displayable;
                    final OverlayDrawStep drawStep = this.getOverlays(overlay.getDimension()).remove(displayId, displayable);
                    if (drawStep != null) {
                        drawStep.setEnabled(false);
                        break;
                    }
                    break;
                }
            }
        } catch (Throwable t) {
            Journeymap.getLogger().error("Error removing DrawMarkerStep: " + t, LogFormatter.toString(t));
        }
    }

    public void remove(final Waypoint modWaypoint) {
        final String displayId = modWaypoint.getId();
        journeymap.client.model.Waypoint waypoint = this.waypoints.remove(displayId, modWaypoint);
        if (waypoint == null) {
            waypoint = new journeymap.client.model.Waypoint(modWaypoint);
        }
        WaypointStore.INSTANCE.remove(waypoint);
    }

    public void removeAll(final DisplayType displayType) {
        if (displayType == DisplayType.Waypoint) {
            final List<Waypoint> list = new ArrayList<>(this.waypoints.columnKeySet());
            for (final Waypoint modWaypoint : list) {
                this.remove(modWaypoint);
            }
        } else {
            for (final HashBasedTable<String, Overlay, OverlayDrawStep> overlays : this.dimensionOverlays.values()) {
                final List<Displayable> list2 = new ArrayList<>(overlays.columnKeySet());
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
            final List<Waypoint> list = new ArrayList<>(this.waypoints.columnKeySet());
            for (final Waypoint modWaypoint : list) {
                this.remove(modWaypoint);
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
                return this.waypoints.containsRow(displayId);
            }
            default: {
                if (displayable instanceof Overlay) {
                    final int dimension = ((Overlay) displayable).getDimension();
                    return this.getOverlays(dimension).containsRow(displayId);
                }
                return false;
            }
        }
    }

    public void getDrawSteps(final List<OverlayDrawStep> list, final UIState uiState) {
        final HashBasedTable<String, Overlay, OverlayDrawStep> table = this.getOverlays(uiState.dimension);
        for (final Table.Cell<String, Overlay, OverlayDrawStep> cell : table.cellSet()) {
            if (cell.getColumnKey().isActiveIn(uiState)) {
                list.add(cell.getValue());
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
            } catch (Throwable t) {
                Journeymap.getLogger().error(String.format("Plugin %s errored during event: %s", this, clientEvent.type), t);
            } finally {
                this.eventTimer.stop();
                if (this.eventTimer.hasReachedElapsedLimit()) {
                    Journeymap.getLogger().warn(String.format("Plugin %s too slow handling event: %s", this, clientEvent.type));
                }
            }
        } catch (Throwable t2) {
            Journeymap.getLogger().error(String.format("Plugin %s error during event: %s", this, clientEvent.type), t2);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PluginWrapper)) {
            return false;
        }
        final PluginWrapper that = (PluginWrapper) o;
        return Objects.equal(this.modId, that.modId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.modId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.plugin).add("modId", this.modId).toString();
    }
}
