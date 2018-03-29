package journeymap.client.service;

import journeymap.client.properties.WebMapProperties;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.properties.config.BooleanField;
import se.rupy.http.Event;
import se.rupy.http.Query;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class PropertyService extends BaseService {
    public static final String CALLBACK_PARAM = "callback";
    WebMapProperties webMapProperties;
    HashMap<String, BooleanField> propMap;

    public PropertyService() {
        this.propMap = new HashMap<>();
    }

    @Override
    public String path() {
        return "/properties";
    }

    private void init() {
        if (this.propMap.isEmpty()) {
            this.webMapProperties = Journeymap.getClient().getWebMapProperties();
            this.propMap.put("showCaves", this.webMapProperties.showCaves);
            this.propMap.put("showGrid", this.webMapProperties.showGrid);
            this.propMap.put("showWaypoints", this.webMapProperties.showWaypoints);
        }
    }

    @Override
    public void filter(final Event event) throws Event, Exception {
        try {
            this.init();
            final Query query = event.query();
            query.parse();
            final String path = query.path();
            if (query.method() == 2) {
                this.post(event);
                return;
            }
            if (query.method() != 1) {
                throw new Exception("HTTP method not allowed");
            }
            final StringBuilder jsonData = new StringBuilder();
            final boolean useJsonP = query.containsKey("callback");
            if (useJsonP) {
                jsonData.append(URLEncoder.encode(query.get("callback").toString(), PropertyService.UTF8.name()));
                jsonData.append("(");
            } else {
                jsonData.append("data=");
            }
            final Map<String, Boolean> valMap = new HashMap<>();
            for (final Map.Entry<String, BooleanField> entry : this.propMap.entrySet()) {
                valMap.put(entry.getKey(), entry.getValue().get());
            }
            jsonData.append(PropertyService.GSON.toJson(valMap));
            if (useJsonP) {
                jsonData.append(")");
                ResponseHeader.on(event).noCache().contentType(ContentType.jsonp);
            }
            this.gzipResponse(event, jsonData.toString());
        } catch (Throwable t) {
            Journeymap.getLogger().error(LogFormatter.toString(t));
            this.throwEventException(500, "Error trying " + this.path, event, true);
        }
    }

    public void post(final Event event) throws Event, Exception {
        try {
            final Query query = event.query();
            final String[] param = query.parameters().split("=");
            if (param.length != 2) {
                throw new Exception("Expected single key-value pair");
            }
            final String key = param[0];
            final String value = param[1];
            if (this.propMap.containsKey(key)) {
                final Boolean boolValue = Boolean.parseBoolean(value);
                this.propMap.get(key).set(boolValue);
                this.webMapProperties.save();
            }
        } catch (Throwable t) {
            Journeymap.getLogger().error(LogFormatter.toString(t));
            this.throwEventException(500, "Error trying " + this.path, event, true);
        }
    }
}
