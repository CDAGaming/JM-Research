package journeymap.client.service;

import se.rupy.http.*;
import journeymap.client.log.*;
import java.io.*;

public class LogService extends FileService
{
    private static final long serialVersionUID = 1L;
    private static final String CLASSPATH_ROOT = "/";
    private static final String CLASSPATH_WEBROOT = "web";
    private static final String IDE_TEST = "eclipse/Client/bin/";
    private static final String IDE_SOURCEPATH = "../../../src/minecraft/net/techbrew/journeymap/web";
    
    @Override
    public String path() {
        return "/log";
    }
    
    @Override
    public void filter(final Event event) throws Event, Exception {
        final File logFile = JMLogger.getLogFile();
        if (logFile.exists()) {
            ResponseHeader.on(event).contentType(ContentType.txt);
            ResponseHeader.on(event).inlineFilename("journeymap.log");
            this.serveFile(logFile, event);
        }
        else {
            this.throwEventException(404, "Not found: " + logFile.getPath(), event, true);
        }
    }
}
