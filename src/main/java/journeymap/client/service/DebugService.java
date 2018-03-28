package journeymap.client.service;

import com.google.common.io.CharStreams;
import journeymap.client.data.DataCache;
import journeymap.client.log.JMLogger;
import journeymap.client.log.StatTimer;
import journeymap.common.log.LogFormatter;
import se.rupy.http.Event;

import java.io.InputStream;
import java.io.InputStreamReader;

public class DebugService extends FileService {
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
            final String debugHtml = CharStreams.toString((Readable) new InputStreamReader(debugHtmlStream, "UTF-8"));
            debug = debugHtml.replace("<output/>", sb.toString());
        } else {
            debug = sb.toString();
        }
        this.gzipResponse(event, debug);
    }
}
