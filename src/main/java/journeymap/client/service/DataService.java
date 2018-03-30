package journeymap.client.service;

import journeymap.common.*;
import journeymap.client.data.*;
import journeymap.client.api.display.*;
import journeymap.client.waypoint.*;
import java.net.*;
import journeymap.common.log.*;
import se.rupy.http.*;
import java.util.*;

public class DataService extends BaseService
{
    public static final String combinedPath;
    public static final HashMap<String, Class> providerMap;
    private static final long serialVersionUID = 4412225358529161454L;
    
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
                }
                else if (Journeymap.clientWorld() == null) {
                    this.throwEventException(503, "World not connected", event, false);
                }
            }
            long since = 0L;
            final Object sinceVal = query.get("images.since");
            if (sinceVal != null) {
                try {
                    since = Long.parseLong(sinceVal.toString());
                }
                catch (Exception e) {
                    Journeymap.getLogger().warn("Bad value for images.since: " + sinceVal);
                    since = new Date().getTime();
                }
            }
            final Class dpClass = DataService.providerMap.get(path);
            Object data = null;
            if (dpClass == AllData.class) {
                data = DataCache.INSTANCE.getAll(since);
            }
            else if (dpClass == PassiveMobsData.class) {
                data = DataCache.INSTANCE.getPassiveMobs(false);
            }
            else if (dpClass == HostileMobsData.class) {
                data = DataCache.INSTANCE.getHostileMobs(false);
            }
            else if (dpClass == ImagesData.class) {
                data = new ImagesData(since);
            }
            else if (dpClass == MessagesData.class) {
                data = DataCache.INSTANCE.getMessages(false);
            }
            else if (dpClass == PlayerData.class) {
                data = DataCache.INSTANCE.getPlayer(false);
            }
            else if (dpClass == PlayersData.class) {
                data = DataCache.INSTANCE.getPlayers(false);
            }
            else if (dpClass == WorldData.class) {
                data = DataCache.INSTANCE.getWorld(false);
            }
            else if (dpClass == NpcsData.class) {
                data = DataCache.INSTANCE.getNpcs(false);
            }
            else if (dpClass == NpcsData.class) {
                data = DataCache.INSTANCE.getNpcs(false);
            }
            else if (dpClass == WaypointsData.class) {
                final Map<String, Waypoint> wpMap = new HashMap<String, Waypoint>();
                final int playerDim = Journeymap.clientPlayer().field_71093_bK;
                for (final Waypoint waypoint : WaypointStore.INSTANCE.getAll(playerDim)) {
                    wpMap.put(waypoint.getId(), waypoint);
                }
                data = wpMap;
            }
            final String dataString = DataService.GSON.toJson(data);
            final StringBuffer jsonData = new StringBuffer();
            final boolean useJsonP = query.containsKey("callback");
            if (useJsonP) {
                jsonData.append(URLEncoder.encode(query.get("callback").toString(), DataService.UTF8.name()));
                jsonData.append("(");
            }
            else {
                jsonData.append("data=");
            }
            jsonData.append(dataString);
            if (useJsonP) {
                jsonData.append(")");
            }
            ResponseHeader.on(event).noCache().contentType(ContentType.jsonp);
            this.gzipResponse(event, jsonData.toString());
        }
        catch (Event eventEx) {
            throw eventEx;
        }
        catch (Throwable t) {
            Journeymap.getLogger().error(String.format("Unexpected error in data service: %s", LogFormatter.toString(t)));
            this.throwEventException(500, "Error retrieving " + this.path, event, true);
        }
    }
    
    static {
        (providerMap = new HashMap<String, Class>(14)).put("/data/all", AllData.class);
        DataService.providerMap.put("/data/image", ImagesData.class);
        DataService.providerMap.put("/data/messages", MessagesData.class);
        DataService.providerMap.put("/data/player", PlayerData.class);
        DataService.providerMap.put("/data/world", WorldData.class);
        DataService.providerMap.put("/data/waypoints", WaypointsData.class);
        final StringBuffer sb = new StringBuffer();
        for (final String key : DataService.providerMap.keySet()) {
            sb.append(key).append(":");
        }
        combinedPath = sb.toString();
    }
}
