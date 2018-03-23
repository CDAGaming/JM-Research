package modinfo.mp.v1;

import java.net.*;
import modinfo.*;
import org.apache.logging.log4j.*;
import java.util.*;
import java.io.*;

public class Payload
{
    public static final String VERSION = "1";
    public static Comparator<Parameter> ParameterOrdinalSort;
    private TreeMap<Parameter, String> params;
    
    public Payload(final Type type) {
        (this.params = new TreeMap<Parameter, String>(Payload.ParameterOrdinalSort)).put(Parameter.HitType, type.getHitName());
    }
    
    public static String encode(final String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            ModInfo.LOGGER.log(Level.ERROR, "Can't encode: " + value);
            return value;
        }
    }
    
    static String urlClamp(final String value, final Integer maxBytes, final boolean encodeResult) {
        if (maxBytes == null) {
            return encodeResult ? encode(value) : value;
        }
        final StringBuilder sb = new StringBuilder(value);
        String encoded = encode(sb.toString());
        int byteLength = encoded.getBytes().length;
        int offset = 0;
        while (byteLength > maxBytes) {
            offset = (int)Math.max(1.0, Math.floor((byteLength - maxBytes) / 11));
            sb.setLength(sb.length() - offset);
            encoded = encode(sb.toString());
            byteLength = encoded.getBytes().length;
        }
        return encodeResult ? encoded : sb.toString();
    }
    
    public void put(final Parameter param, final String value) {
        this.params.put(param, value);
    }
    
    public Payload add(final Map<Parameter, String> map) {
        this.params.putAll(map);
        return this;
    }
    
    String toUrlEncodedString() {
        final StringBuilder sb = new StringBuilder();
        final Iterator<Map.Entry<Parameter, String>> iter = this.params.entrySet().iterator();
        while (iter.hasNext()) {
            final Map.Entry<Parameter, String> entry = iter.next();
            final Parameter param = entry.getKey();
            sb.append(param.pname).append("=");
            sb.append(urlClamp(entry.getValue(), param.maxBytes, true));
            if (iter.hasNext()) {
                sb.append("&");
            }
        }
        return sb.toString();
    }
    
    String toVerboseString() {
        final StringBuilder sb = new StringBuilder();
        final Iterator<Map.Entry<Parameter, String>> iter = this.params.entrySet().iterator();
        while (iter.hasNext()) {
            final Map.Entry<Parameter, String> entry = iter.next();
            final Parameter param = entry.getKey();
            sb.append(param.pname).append("=");
            String value = entry.getValue();
            if (param == Parameter.TrackingId) {
                value = "UA-XXXXXXXX-1";
            }
            sb.append(urlClamp(value, param.maxBytes, false));
            if (iter.hasNext()) {
                sb.append("&");
            }
        }
        return sb.toString();
    }
    
    static {
        Payload.ParameterOrdinalSort = new Comparator<Parameter>() {
            @Override
            public int compare(final Parameter o1, final Parameter o2) {
                return o1.compareTo(o2);
            }
        };
    }
    
    public enum Parameter
    {
        Version("v"), 
        TrackingId("tid"), 
        ClientId("cid"), 
        HitType("t"), 
        ApplicationName("an", 100), 
        ApplicationVersion("av", 100), 
        NonInteractionHit("ni"), 
        ContentDescription("cd", 2048), 
        ScreenResolution("sr", 20), 
        UserLanguage("ul", 20), 
        ExceptionDescription("exd", 150), 
        ExceptionFatal("exf"), 
        EventCategory("ec", 150), 
        EventAction("ea", 500), 
        EventLabel("el", 500), 
        EventValue("ev"), 
        CustomMetric1("cm1");
        
        private String pname;
        private Integer maxBytes;
        
        private Parameter(final String pname) {
            this.pname = pname;
            this.maxBytes = null;
        }
        
        private Parameter(final String pname, final int maxBytes) {
            this.pname = pname;
            this.maxBytes = maxBytes;
        }
        
        public String getParameterName() {
            return this.pname;
        }
        
        public int getMaxBytes() {
            return this.maxBytes;
        }
    }
    
    public enum Type
    {
        AppView("appview"), 
        Event("event"), 
        Exception("exception");
        
        private String hname;
        
        private Type(final String hname) {
            this.hname = hname;
        }
        
        public String getHitName() {
            return this.hname;
        }
    }
}
