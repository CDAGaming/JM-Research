package journeymap.client.service;

import java.nio.charset.*;
import journeymap.common.*;
import journeymap.client.log.*;
import org.apache.logging.log4j.*;
import java.net.*;
import javax.imageio.*;
import java.awt.image.*;
import com.google.gson.*;
import java.text.*;
import se.rupy.http.*;
import java.util.zip.*;
import java.io.*;
import java.util.*;
import journeymap.client.*;

public abstract class BaseService extends Service
{
    public static final Charset UTF8;
    public static final String CALLBACK_PARAM = "callback";
    protected static final Gson GSON;
    protected String path;
    
    protected void throwEventException(final int code, final String message, final Event event, final boolean isError) throws Event, Exception {
        final Logger logger = Journeymap.getLogger();
        final String out = code + " " + message;
        if (isError) {
            Exception ex = null;
            if (code != 404) {
                ex = new Exception(this.debugRequestHeaders(event));
            }
            JMLogger.logOnce(this.getClass().getName() + ": " + out, ex);
        }
        else if (logger.isTraceEnabled()) {
            Journeymap.getLogger().trace(out);
        }
        try {
            if (code == 404) {
                event.reply().code("404 Not Found");
            }
            else {
                event.reply().code(out);
            }
        }
        catch (IOException e) {
            logger.warn("Can't set response code: " + out);
        }
        throw event;
    }
    
    protected String debugRequestHeaders(final Event event) throws Exception {
        final StringBuffer sb = new StringBuffer("HTTP Request:");
        if (event.query() != null) {
            event.query().parse();
            sb.append("\n request=").append(event.query().path());
            if (event.query().parameters() != null) {
                sb.append("?").append(event.query().parameters());
            }
            final HashMap headers = event.query().header();
            for (final Object name : headers.keySet()) {
                final Object value = headers.get(name);
                sb.append("\n ").append(name).append("=").append(value);
            }
            sb.append("\n Remote Address:").append(event.remote());
        }
        return sb.toString();
    }
    
    protected void reportMalformedRequest(final Event event) throws Event, Exception {
        final String error = "Bad Request: " + event.query().path();
        Journeymap.getLogger().error(error);
        this.throwEventException(400, error, event, false);
    }
    
    protected String getParameter(final Query map, final String key, final String defaultValue) {
        final Object val = map.get(key);
        return (val != null) ? val.toString() : defaultValue;
    }
    
    protected Integer getParameter(final Map<String, String[]> map, final String key, final Integer defaultValue) {
        final Object val = map.get(key);
        Integer intVal = null;
        if (val != null) {
            try {
                intVal = Integer.parseInt((String)val);
            }
            catch (NumberFormatException e) {
                Journeymap.getLogger().warn("Didn't get numeric query parameter for '" + key + "': " + val);
            }
        }
        return (intVal != null) ? intVal : defaultValue;
    }
    
    protected Long getParameter(final Map<String, String[]> map, final String key, final Long defaultValue) {
        final Object val = map.get(key);
        Long longVal = null;
        if (val != null) {
            try {
                longVal = Long.parseLong((String)val);
            }
            catch (NumberFormatException e) {
                Journeymap.getLogger().warn("Didn't get numeric query parameter for '" + key + "': " + val);
            }
        }
        return (longVal != null) ? longVal : defaultValue;
    }
    
    protected void gzipResponse(final Event event, final String data) throws Exception {
        byte[] bytes = this.gzip(data);
        if (bytes != null) {
            ResponseHeader.on(event).setHeader("Content-encoding", "gzip");
        }
        else {
            bytes = data.getBytes(BaseService.UTF8);
        }
        ResponseHeader.on(event).contentLength(bytes.length);
        event.output().write(bytes);
    }
    
    protected void gzipResponse(final Event event, final byte[] data) throws Exception {
        byte[] bytes = this.gzip(data);
        if (bytes != null) {
            ResponseHeader.on(event).setHeader("Content-encoding", "gzip");
        }
        else {
            bytes = data;
        }
        ResponseHeader.on(event).contentLength(bytes.length);
        event.output().write(bytes);
    }
    
    protected void respondJson(final Event event, final Map responseObj) throws Exception {
        final Query query = event.query();
        final StringBuffer jsonData = new StringBuffer();
        final boolean useJsonP = query.containsKey("callback");
        if (useJsonP) {
            try {
                jsonData.append(URLEncoder.encode(query.get("callback").toString(), BaseService.UTF8.name()));
            }
            catch (UnsupportedEncodingException e) {
                jsonData.append(query.get("callback").toString());
            }
            jsonData.append("(");
        }
        else {
            jsonData.append("data=");
        }
        jsonData.append(BaseService.GSON.toJson((Object)responseObj));
        if (useJsonP) {
            jsonData.append(")");
        }
        ResponseHeader.on(event).noCache().contentType(ContentType.jsonp);
        this.gzipResponse(event, jsonData.toString());
    }
    
    protected byte[] gzip(final String data) {
        final ByteArrayOutputStream bout = null;
        try {
            return this.gzip(data.getBytes(BaseService.UTF8));
        }
        catch (Exception ex) {
            Journeymap.getLogger().warn("Failed to UTF8 encode: " + data);
            return null;
        }
    }
    
    protected byte[] gzip(final byte[] data) {
        ByteArrayOutputStream bout = null;
        try {
            bout = new ByteArrayOutputStream();
            final GZIPOutputStream output = new GZIPOutputStream(bout);
            output.write(data);
            output.flush();
            output.close();
            bout.flush();
            bout.close();
            return bout.toByteArray();
        }
        catch (Exception ex) {
            Journeymap.getLogger().warn("Failed to gzip encode: " + Arrays.toString(data));
            return null;
        }
    }
    
    protected void serveImage(final Event event, final BufferedImage img) throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        baos.flush();
        final byte[] bytes = baos.toByteArray();
        baos.close();
        event.output().write(bytes);
    }
    
    static {
        UTF8 = Charset.forName("UTF-8");
        GSON = new GsonBuilder().setPrettyPrinting().create();
    }
    
    enum ContentType
    {
        css("text/css; charset=utf-8"), 
        gif("image/gif"), 
        ico("image/x-icon"), 
        htm("text/html; charset=utf-8"), 
        html("text/html; charset=utf-8"), 
        js("application/javascript; charset=utf-8"), 
        json("application/json; charset=utf-8"), 
        jsonp("application/javascript; charset=utf-8"), 
        png("image/png"), 
        jpeg("image/jpeg"), 
        jpg("image/jpeg"), 
        log("text/plain; charset=utf-8"), 
        txt("text/plain; charset=utf-8"), 
        UNKNOWN("application/x-unknown");
        
        static final EnumSet htmlTypes;
        private final String mime;
        
        private ContentType(final String mime) {
            this.mime = mime;
        }
        
        static ContentType fromFileName(final String fileName) {
            final String name = fileName.toLowerCase(Locale.ENGLISH);
            final String ext = name.substring(name.lastIndexOf(46) + 1);
            try {
                return valueOf(ext);
            }
            catch (Exception e) {
                Journeymap.getLogger().warn("No ContentType match for file: " + name);
                return null;
            }
        }
        
        String getMime() {
            return this.mime;
        }
        
        static {
            htmlTypes = EnumSet.of(ContentType.htm, ContentType.html, ContentType.txt);
        }
    }
    
    static class ResponseHeader
    {
        protected static SimpleDateFormat dateFormat;
        private Reply reply;
        
        private ResponseHeader(final Event event) {
            this.reply = event.reply();
        }
        
        static ResponseHeader on(final Event event) {
            return new ResponseHeader(event);
        }
        
        ResponseHeader setHeader(final String name, final String value) {
            if (this.reply == null) {
                throw new IllegalStateException("ResponseHeader builder already cleared.");
            }
            this.reply.header(name, value);
            return this;
        }
        
        ResponseHeader noCache() {
            this.setHeader("Cache-Control", "no-cache");
            this.setHeader("Pragma", "no-cache");
            this.setHeader("Expires", "0");
            return this;
        }
        
        ResponseHeader content(final File file) {
            this.contentType(ContentType.fromFileName(file.getName()));
            this.contentLength(file.length());
            return this.contentModified(file.lastModified());
        }
        
        ResponseHeader content(final ZipEntry zipEntry) {
            this.contentType(ContentType.fromFileName(zipEntry.getName()));
            final long size = zipEntry.getSize();
            final long time = zipEntry.getTime();
            if (size > -1L) {
                this.contentLength(size);
            }
            if (time > -1L) {
                this.contentModified(time);
            }
            return this;
        }
        
        ResponseHeader contentLength(final FileInputStream input) {
            try {
                this.contentLength(input.getChannel().size());
            }
            catch (IOException e) {
                Journeymap.getLogger().warn("Couldn't get content length for FileInputStream");
            }
            return this;
        }
        
        ResponseHeader contentModified(final long timestamp) {
            return this.setHeader("Last-Modified", ResponseHeader.dateFormat.format(new Date(timestamp)));
        }
        
        ResponseHeader contentLength(final long fileSize) {
            return this.setHeader("Content-Length", Long.toString(fileSize));
        }
        
        ResponseHeader expires(final long timestamp) {
            return this.setHeader("Expires", ResponseHeader.dateFormat.format(new Date(timestamp)));
        }
        
        ResponseHeader contentType(final ContentType type) {
            if (type != null) {
                this.reply.type(type.getMime());
                if (ContentType.htmlTypes.contains(type)) {
                    this.contentLanguage(Constants.getLocale());
                }
            }
            return this;
        }
        
        ResponseHeader contentLanguage(final Locale locale) {
            this.setHeader("Content-Language", locale.toString());
            return this;
        }
        
        ResponseHeader inlineFilename(final String name) {
            return this.setHeader("Content-Disposition", "inline; filename=\"" + name + "\"");
        }
        
        void done() {
            this.reply = null;
        }
        
        static {
            (ResponseHeader.dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Constants.getLocale())).setTimeZone(Constants.GMT);
        }
    }
}
