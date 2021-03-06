package journeymap.client.service;

import journeymap.client.data.*;
import journeymap.client.model.Waypoint;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraftforge.fml.client.FMLClientHandler;
import se.rupy.http.Event;
import se.rupy.http.Query;

import java.net.URLEncoder;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DataService extends BaseService {
    public static final String combinedPath;
    public static final HashMap<String, Class> providerMap;
    private static final long serialVersionUID = 4412225358529161454L;

    static {
        (providerMap = new HashMap<>(14)).put("/data/all", AllData.class);
        DataService.providerMap.put("/data/image", ImagesData.class);
        DataService.providerMap.put("/data/messages", MessagesData.class);
        DataService.providerMap.put("/data/player", PlayerData.class);
        DataService.providerMap.put("/data/world", WorldData.class);
        DataService.providerMap.put("/data/waypoints", WaypointsData.class);
        final StringBuilder sb = new StringBuilder();
        for (final String key : DataService.providerMap.keySet()) {
            sb.append(key).append(":");
        }
        combinedPath = sb.toString();
    }

    @Override
    public String path() {
        return DataService.combinedPath;
    }

    @Override
    public void filter(final Event event) throws Event, Exception {
        try {
            final Query query = event.query();
            query.parse();
            final String path = query.path();
            if (!path.equals("/data/messages")) {
                if (!Journeymap.getClient().isMapping()) {
                    this.throwEventException(503, "JourneyMap not mapping", event, false);
                } else if (FMLClientHandler.instance().getClient().world == null) {
                    this.throwEventException(503, "World not connected", event, false);
                }
            }
            long since = 0L;
            final Object sinceVal = query.get("images.since");
            if (sinceVal != null) {
                try {
                    since = Long.parseLong(sinceVal.toString());
                } catch (Exception e) {
                    Journeymap.getLogger().warn("Bad value for images.since: " + sinceVal);
                    since = new Date().getTime();
                }
            }
            final Class dpClass = DataService.providerMap.get(path);
            Object data = null;
            if (dpClass == AllData.class) {
                data = DataCache.INSTANCE.getAll(since);
            } else if (dpClass == AnimalsData.class) {
                data = DataCache.INSTANCE.getAnimals(false);
            } else if (dpClass == MobsData.class) {
                data = DataCache.INSTANCE.getMobs(false);
            } else if (dpClass == ImagesData.class) {
                data = new ImagesData(since);
            } else if (dpClass == MessagesData.class) {
                data = DataCache.INSTANCE.getMessages(false);
            } else if (dpClass == PlayerData.class) {
                data = DataCache.INSTANCE.getPlayer(false);
            } else if (dpClass == PlayersData.class) {
                data = DataCache.INSTANCE.getPlayers(false);
            } else if (dpClass == WorldData.class) {
                data = DataCache.INSTANCE.getWorld(false);
            } else if (dpClass == VillagersData.class) {
                data = DataCache.INSTANCE.getVillagers(false);
            } else if (dpClass == WaypointsData.class) {
                final Collection<Waypoint> waypoints = DataCache.INSTANCE.getWaypoints(false);
                final Map<String, Waypoint> wpMap = new HashMap<>();
                for (final Waypoint waypoint : waypoints) {
                    wpMap.put(waypoint.getId(), waypoint);
                }
                data = wpMap;
            }
            final String dataString = DataService.GSON.toJson(data);
            final StringBuilder jsonData = new StringBuilder();
            final boolean useJsonP = query.containsKey("callback");
            if (useJsonP) {
                jsonData.append(URLEncoder.encode(query.get("callback").toString(), DataService.UTF8.name()));
                jsonData.append("(");
            } else {
                jsonData.append("data=");
            }
            jsonData.append(dataString);
            if (useJsonP) {
                jsonData.append(")");
            }
            ResponseHeader.on(event).noCache().contentType(ContentType.jsonp);
            this.gzipResponse(event, jsonData.toString());
        } catch (Event eventEx) {
            throw eventEx;
        } catch (Throwable t) {
            Journeymap.getLogger().error(String.format("Unexpected error in data service: %s", LogFormatter.toString(t)));
            this.throwEventException(500, "Error retrieving " + this.path, event, true);
        }
    }
}
