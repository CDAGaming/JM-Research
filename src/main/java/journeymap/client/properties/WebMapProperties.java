package journeymap.client.properties;

import journeymap.client.service.MapApiService;
import journeymap.common.properties.config.BooleanField;
import journeymap.common.properties.config.IntegerField;
import journeymap.common.properties.config.StringField;

public class WebMapProperties extends MapProperties {
    public final BooleanField enabled;
    public final IntegerField port;
    public final StringField googleMapApiDomain;

    public WebMapProperties() {
        this.enabled = new BooleanField(ClientCategory.WebMap, "jm.webmap.enable", false, true);
        this.port = new IntegerField(ClientCategory.WebMap, "jm.advanced.port", 80, 10000, 8080);
        this.googleMapApiDomain = new StringField(ClientCategory.WebMap, "jm.webmap.google_domain", MapApiService.TopLevelDomains.class);
    }

    @Override
    public String getName() {
        return "webmap";
    }
}
