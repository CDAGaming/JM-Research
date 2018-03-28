package journeymap.client.api.impl;

import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.event.DeathWaypointEvent;
import journeymap.client.api.event.DisplayUpdateEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;

@ParametersAreNonnullByDefault
public class ClientEventManager {
    private final DisplayUpdateEventThrottle displayUpdateEventThrottle;
    private final Collection<PluginWrapper> plugins;
    private EnumSet<ClientEvent.Type> subscribedClientEventTypes;

    public ClientEventManager(final Collection<PluginWrapper> plugins) {
        this.displayUpdateEventThrottle = new DisplayUpdateEventThrottle();
        this.subscribedClientEventTypes = EnumSet.noneOf(ClientEvent.Type.class);
        this.plugins = plugins;
    }

    public void updateSubscribedTypes() {
        this.subscribedClientEventTypes = EnumSet.noneOf(ClientEvent.Type.class);
        for (final PluginWrapper wrapper : this.plugins) {
            this.subscribedClientEventTypes.addAll((Collection<? extends ClientEvent.Type>) wrapper.getSubscribedClientEventTypes());
        }
    }

    public boolean canFireClientEvent(final ClientEvent.Type type) {
        return this.subscribedClientEventTypes.contains(type);
    }

    public void fireMappingEvent(final boolean started, final int dimension) {
        final ClientEvent.Type type = started ? ClientEvent.Type.MAPPING_STARTED : ClientEvent.Type.MAPPING_STOPPED;
        if (this.plugins.isEmpty() || !this.subscribedClientEventTypes.contains(type)) {
            return;
        }
        final ClientEvent clientEvent = new ClientEvent(type, dimension);
        for (final PluginWrapper wrapper : this.plugins) {
            try {
                wrapper.notify(clientEvent);
            } catch (Throwable t) {
                ClientAPI.INSTANCE.logError("Error in fireMappingEvent(): " + clientEvent, t);
            }
        }
    }

    public void fireDeathpointEvent(final DeathWaypointEvent clientEvent) {
        if (this.plugins.isEmpty() || !this.subscribedClientEventTypes.contains(ClientEvent.Type.DEATH_WAYPOINT)) {
            return;
        }
        for (final PluginWrapper wrapper : this.plugins) {
            try {
                wrapper.notify(clientEvent);
            } catch (Throwable t) {
                ClientAPI.INSTANCE.logError("Error in fireDeathpointEvent(): " + clientEvent, t);
            }
        }
    }

    public void fireDisplayUpdateEvent(final DisplayUpdateEvent clientEvent) {
        if (this.plugins.size() == 0 || !this.subscribedClientEventTypes.contains(ClientEvent.Type.DISPLAY_UPDATE)) {
            return;
        }
        try {
            this.displayUpdateEventThrottle.add(clientEvent);
        } catch (Throwable t) {
            ClientAPI.INSTANCE.logError("Error in fireDisplayUpdateEvent(): " + clientEvent, t);
        }
    }

    public void fireNextClientEvents() {
        if (!this.plugins.isEmpty() && this.displayUpdateEventThrottle.isReady()) {
            final Iterator<DisplayUpdateEvent> iterator = this.displayUpdateEventThrottle.iterator();
            while (iterator.hasNext()) {
                final DisplayUpdateEvent clientEvent = iterator.next();
                iterator.remove();
                for (final PluginWrapper wrapper : this.plugins) {
                    try {
                        wrapper.notify(clientEvent);
                    } catch (Throwable t) {
                        ClientAPI.INSTANCE.logError("Error in fireDeathpointEvent(): " + clientEvent, t);
                    }
                }
            }
        }
    }

    void purge() {
        this.plugins.clear();
        this.subscribedClientEventTypes.clear();
    }
}
