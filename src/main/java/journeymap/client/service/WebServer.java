package journeymap.client.service;

import journeymap.common.*;
import org.apache.logging.log4j.*;
import journeymap.common.log.*;
import journeymap.client.log.*;
import journeymap.client.properties.*;
import java.nio.channels.*;
import java.net.*;
import java.io.*;
import se.rupy.http.*;
import journeymap.common.thread.*;
import java.util.concurrent.*;
import java.util.*;

public class WebServer
{
    private static final int MAXPORT = 9990;
    private static final int MAXFAILS = 5;
    private static volatile WebServer instance;
    private final Logger logger;
    private Daemon rupy;
    private int port;
    private boolean ready;
    
    private WebServer() {
        this.logger = Journeymap.getLogger();
        this.ready = false;
        this.port = Journeymap.getClient().getWebMapProperties().port.get();
        this.validatePort();
    }
    
    public static void setEnabled(Boolean enable, final boolean forceAnnounce) {
        final WebMapProperties webMapProperties = Journeymap.getClient().getWebMapProperties();
        webMapProperties.enabled.set(enable);
        webMapProperties.save();
        if (WebServer.instance != null) {
            try {
                WebServer.instance.stop();
            }
            catch (Throwable e) {
                Journeymap.getLogger().log(Level.ERROR, LogFormatter.toString(e));
            }
        }
        if (enable) {
            try {
                WebServer.instance = new WebServer();
                if (WebServer.instance.isReady()) {
                    WebServer.instance.start();
                }
                else {
                    enable = false;
                }
            }
            catch (Throwable e) {
                Journeymap.getLogger().log(Level.ERROR, LogFormatter.toString(e));
                enable = false;
            }
            if (!enable) {
                Journeymap.getLogger().error("Unexpected error, JMServer couldn't be started.");
            }
        }
        if (forceAnnounce) {
            ChatLog.enableAnnounceMod = true;
        }
        ChatLog.announceMod();
    }
    
    public static WebServer getInstance() {
        return WebServer.instance;
    }
    
    private void validatePort() {
        int hardFails = 0;
        int testPort = this.port;
        final int maxPort = Math.max(9990, this.port + 1000);
        boolean validPort = false;
        while (!validPort && hardFails <= 5 && testPort <= maxPort) {
            ServerSocketChannel server = null;
            try {
                server = ServerSocketChannel.open();
                server.socket().bind(new InetSocketAddress(testPort));
                validPort = true;
            }
            catch (BindException e) {
                this.logger.warn("Port " + testPort + " already in use");
                testPort += 10;
            }
            catch (Throwable t) {
                this.logger.error("Error when testing port " + testPort + ": " + t);
                ++hardFails;
            }
            finally {
                if (server != null) {
                    try {
                        server.close();
                    }
                    catch (IOException ex) {}
                }
            }
        }
        this.ready = validPort;
        if (this.ready && this.port != testPort) {
            this.logger.info("Webserver will use port " + testPort + " for this session");
            this.port = testPort;
        }
        if (!this.ready && hardFails > 5) {
            this.logger.error("Gave up finding a port for webserver after " + hardFails + " failures to test ports!");
        }
        if (!this.ready && testPort > 9990) {
            this.logger.error("Gave up finding a port for webserver after testing ports " + this.port + " - " + maxPort + " without finding one open!");
        }
    }
    
    public boolean isReady() {
        return this.ready;
    }
    
    public int getPort() {
        return this.port;
    }
    
    public void start() throws Exception {
        if (!this.ready) {
            throw new IllegalStateException("Initialization failed");
        }
        final Properties props = new Properties();
        ((Hashtable<String, String>)props).put("port", Integer.toString(this.port));
        ((Hashtable<String, String>)props).put("delay", Integer.toString(5000));
        ((Hashtable<String, String>)props).put("timeout", Integer.toString(0));
        ((Hashtable<String, String>)props).put("threads", Integer.toString(5));
        final Level logLevel = Level.toLevel(Journeymap.getClient().getCoreProperties().logLevel.get(), Level.INFO);
        if (logLevel.intLevel() >= Level.TRACE.intLevel()) {
            ((Hashtable<String, String>)props).put("debug", Boolean.TRUE.toString());
        }
        if (logLevel.intLevel() >= Level.TRACE.intLevel()) {
            ((Hashtable<String, String>)props).put("verbose", Boolean.TRUE.toString());
        }
        (this.rupy = new Daemon(props)).add(new DataService());
        this.rupy.add(new LogService());
        this.rupy.add(new TileService());
        this.rupy.add(new ActionService());
        this.rupy.add(new FileService());
        this.rupy.add(new PropertyService());
        this.rupy.add(new DebugService());
        this.rupy.add(new MapApiService());
        this.rupy.init();
        final JMThreadFactory tf = new JMThreadFactory("svr");
        final ExecutorService es = Executors.newSingleThreadExecutor(tf);
        es.execute(this.rupy);
        Runtime.getRuntime().addShutdownHook(tf.newThread(new Runnable() {
            @Override
            public void run() {
                WebServer.this.stop();
            }
        }));
        this.logger.info("Started webserver on port " + this.port);
    }
    
    public void stop() {
        try {
            if (this.rupy.isAlive()) {
                this.rupy.stop();
                this.logger.info("Stopped webserver without errors");
            }
        }
        catch (Throwable t) {
            this.logger.info("Stopped webserver with error: " + t);
        }
    }
}
