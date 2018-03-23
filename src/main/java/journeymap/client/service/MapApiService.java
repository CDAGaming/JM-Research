package journeymap.client.service;

import se.rupy.http.*;
import journeymap.common.*;
import journeymap.common.properties.config.*;
import java.util.*;

public class MapApiService extends FileService
{
    private static final String API_KEY = "AIzaSyDeq8K0022T9N1y-7Q7GBYhwoDS2hruB3c";
    
    @Override
    public String path() {
        return "/mapapi";
    }
    
    @Override
    public void filter(final Event event) throws Event, Exception {
        final String domain = Journeymap.getClient().getWebMapProperties().googleMapApiDomain.get();
        final String apiUrl = String.format("http://maps.google%s/maps/api/js?key=%s&libraries=geometry&sensor=false", domain, "AIzaSyDeq8K0022T9N1y-7Q7GBYhwoDS2hruB3c");
        ResponseHeader.on(event).setHeader("Location", apiUrl).noCache();
        event.reply().code("303 See Other");
        throw event;
    }
    
    public static class TopLevelDomains implements StringField.ValuesProvider
    {
        @Override
        public List<String> getStrings() {
            return Arrays.asList(".ae", ".cn", ".com", ".es", ".hu", ".kr", ".nl", ".se");
        }
        
        @Override
        public String getDefaultString() {
            return ".com";
        }
    }
}
