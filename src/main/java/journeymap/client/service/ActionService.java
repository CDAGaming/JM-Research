package journeymap.client.service;

import journeymap.client.io.FileHandler;
import journeymap.client.io.MapSaver;
import journeymap.client.model.MapType;
import journeymap.client.task.multi.MapRegionTask;
import journeymap.client.task.multi.SaveMapTask;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import se.rupy.http.Event;
import se.rupy.http.Query;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;

public class ActionService extends BaseService {
    public static final String CHARACTER_ENCODING = "UTF-8";
    private static final long serialVersionUID = 4412225358529161454L;
    private static boolean debug;

    static {
        ActionService.debug = true;
    }

    @Override
    public String path() {
        return "/action";
    }

    @Override
    public void filter(final Event event) throws Event, Exception {
        final Query query = event.query();
        query.parse();
        final Minecraft minecraft = FMLClientHandler.instance().getClient();
        final World world = minecraft.world;
        if (world == null) {
            this.throwEventException(503, "World not connected", event, false);
        }
        if (!Journeymap.getClient().isMapping()) {
            this.throwEventException(503, "JourneyMap not mapping", event, false);
        }
        final String type = this.getParameter(query, "type", (String) null);
        if ("savemap".equals(type)) {
            this.saveMap(event);
        } else if ("automap".equals(type)) {
            this.autoMap(event);
        } else {
            final String error = "Bad request: type=" + type;
            this.throwEventException(400, error, event, true);
        }
    }

    private void saveMap(final Event event) throws Event, Exception {
        final Query query = event.query();
        final Minecraft minecraft = FMLClientHandler.instance().getClient();
        final World world = minecraft.world;
        try {
            final File worldDir = FileHandler.getJMWorldDir(minecraft);
            if (!worldDir.exists() || !worldDir.isDirectory()) {
                final String error = "World unknown: " + worldDir.getAbsolutePath();
                this.throwEventException(500, error, event, true);
            }
            Integer vSlice = this.getParameter(query, "depth", (Integer) null);
            final int dimension = this.getParameter(query, "dim", 0);
            final String mapTypeString = this.getParameter(query, "mapType", MapType.Name.day.name());
            MapType mapType;
            MapType.Name mapTypeName = null;
            try {
                mapTypeName = MapType.Name.valueOf(mapTypeString);
            } catch (Exception e) {
                final String error2 = "Bad request: mapType=" + mapTypeString;
                this.throwEventException(400, error2, event, true);
            }
            if (mapTypeName != MapType.Name.underground) {
                vSlice = null;
            }
            mapType = MapType.from(mapTypeName, vSlice, dimension);
            final Boolean hardcore = world.getWorldInfo().isHardcoreModeEnabled();
            if (mapType.isUnderground() && hardcore) {
                final String error2 = "Cave mapping on hardcore servers is not allowed";
                this.throwEventException(403, error2, event, true);
            }
            final MapSaver mapSaver = new MapSaver(worldDir, mapType);
            if (!mapSaver.isValid()) {
                this.throwEventException(403, "No image files to save.", event, true);
            }
            Journeymap.getClient().toggleTask(SaveMapTask.Manager.class, true, mapSaver);
            final Properties response = new Properties();
            (response).put("filename", mapSaver.getSaveFileName());
            this.respondJson(event, response);
        } catch (NumberFormatException e2) {
            this.reportMalformedRequest(event);
        } catch (Event eventEx) {
            throw eventEx;
        } catch (Throwable t) {
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
        } else if (!enabled) {
            final boolean doAll = "all".equals(scope);
            Journeymap.getClient().toggleTask(MapRegionTask.Manager.class, true, doAll);
            responseObj.put("message", "automap_started");
        } else {
            responseObj.put("message", "automap_already_started");
        }
        this.respondJson(event, responseObj);
    }
}
