package journeymap.client.service;

import journeymap.common.*;
import se.rupy.http.*;
import net.minecraft.world.*;
import net.minecraftforge.fml.client.*;
import journeymap.common.api.feature.*;
import journeymap.client.model.*;
import journeymap.client.io.*;
import journeymap.common.log.*;
import java.io.*;
import journeymap.client.task.multi.*;
import java.util.*;

public class ActionService extends BaseService
{
    public static final String CHARACTER_ENCODING = "UTF-8";
    private static final long serialVersionUID = 4412225358529161454L;
    private static boolean debug;
    
    @Override
    public String path() {
        return "/action";
    }
    
    @Override
    public void filter(final Event event) throws Event, Exception {
        final Query query = event.query();
        query.parse();
        final World world = Journeymap.clientWorld();
        if (world == null) {
            this.throwEventException(503, "World not connected", event, false);
        }
        if (!Journeymap.getClient().isMapping()) {
            this.throwEventException(503, "JourneyMap not mapping", event, false);
        }
        final String type = this.getParameter(query, "type", null);
        if ("savemap".equals(type)) {
            this.saveMap(event);
        }
        else if ("automap".equals(type)) {
            this.autoMap(event);
        }
        else {
            final String error = "Bad request: type=" + type;
            this.throwEventException(400, error, event, true);
        }
    }
    
    private void saveMap(final Event event) throws Event, Exception {
        final Query query = event.query();
        try {
            final File worldDir = FileHandler.getJMWorldDir(FMLClientHandler.instance().getClient());
            if (!worldDir.exists() || !worldDir.isDirectory()) {
                final String error = "World unknown: " + worldDir.getAbsolutePath();
                this.throwEventException(500, error, event, true);
            }
            Integer vSlice = this.getParameter(query, "depth", (Integer)null);
            final int dimension = this.getParameter(query, "dim", Integer.valueOf(0));
            final String mapTypeString = this.getParameter(query, "mapType", Feature.MapType.Day.name());
            Feature.MapType mapType = null;
            try {
                mapType = Feature.MapType.valueOf(mapTypeString);
            }
            catch (Exception e) {
                final String error2 = "Bad request: mapType=" + mapTypeString;
                this.throwEventException(400, error2, event, true);
            }
            if (mapType != Feature.MapType.Underground) {
                vSlice = null;
            }
            final MapView mapView = MapView.from(mapType, vSlice, dimension);
            if (!mapView.isAllowed()) {
                final String error2 = "Map type is not currently allowed";
                this.throwEventException(403, error2, event, true);
            }
            final MapSaver mapSaver = new MapSaver(worldDir, mapView);
            if (!mapSaver.isValid()) {
                this.throwEventException(403, "No image files to save.", event, true);
            }
            Journeymap.getClient().toggleTask(SaveMapTask.Manager.class, true, mapSaver);
            final Properties response = new Properties();
            ((Hashtable<String, String>)response).put("filename", mapSaver.getSaveFileName());
            this.respondJson(event, response);
        }
        catch (NumberFormatException e2) {
            this.reportMalformedRequest(event);
        }
        catch (Event eventEx) {
            throw eventEx;
        }
        catch (Throwable t) {
            Journeymap.getLogger().error(LogFormatter.toString(t));
            this.throwEventException(500, "Unexpected error handling path: " + this.path, event, true);
        }
    }
    
    private void autoMap(final Event event) throws Event, Exception {
        final boolean enabled = Journeymap.getClient().isTaskManagerEnabled(MapRegionTask.Manager.class);
        final String scope = this.getParameter(event.query(), "scope", "stop");
        final HashMap responseObj = new HashMap();
        if ("stop".equals(scope)) {
            if (enabled) {
                Journeymap.getClient().toggleTask(MapRegionTask.Manager.class, false, Boolean.FALSE);
                responseObj.put("message", "automap_complete");
            }
        }
        else if (!enabled) {
            final boolean doAll = "all".equals(scope);
            Journeymap.getClient().toggleTask(MapRegionTask.Manager.class, true, doAll);
            responseObj.put("message", "automap_started");
        }
        else {
            responseObj.put("message", "automap_already_started");
        }
        this.respondJson(event, responseObj);
    }
    
    static {
        ActionService.debug = true;
    }
}
