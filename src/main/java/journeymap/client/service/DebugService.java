package journeymap.client.service;

import se.rupy.http.*;
import journeymap.common.log.*;
import journeymap.client.data.*;
import journeymap.client.log.*;
import com.google.common.io.*;
import java.io.*;

public class DebugService extends FileService
{
    private static final long serialVersionUID = 1L;
    
    @Override
    public String path() {
        return "/debug";
    }
    
    @Override
    public void filter(final Event event) throws Event, Exception {
        ResponseHeader.on(event).contentType(ContentType.html).noCache();
        final StringBuilder sb = new StringBuilder();
        sb.append(LogFormatter.LINEBREAK).append("<div id='accordion'>");
        sb.append(LogFormatter.LINEBREAK).append("<h1>Performance Metrics</h1>");
        sb.append(LogFormatter.LINEBREAK).append("<div><b>Stat Timers:</b><pre>").append(StatTimer.getReport()).append("</pre>");
        sb.append(LogFormatter.LINEBREAK).append(DataCache.INSTANCE.getDebugHtml()).append("</div>");
        sb.append(LogFormatter.LINEBREAK).append("<h1>Properties</h1><div>");
        sb.append(LogFormatter.LINEBREAK).append(JMLogger.getPropertiesSummary().replaceAll(LogFormatter.LINEBREAK, "<p>")).append("</div>");
        sb.append(LogFormatter.LINEBREAK).append("</div> <!-- /accordion -->");
        String debug = null;
        final InputStream debugHtmlStream = this.getStream("/debug.html", null);
        if (debugHtmlStream != null) {
            final String debugHtml = CharStreams.toString((Readable)new InputStreamReader(debugHtmlStream, "UTF-8"));
            debug = debugHtml.replace("<output/>", sb.toString());
        }
        else {
            debug = sb.toString();
        }
        this.gzipResponse(event, debug);
    }
}
