package modinfo.mp.v1;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import modinfo.*;
import org.apache.logging.log4j.*;

public class Client
{
    public static final String ENDPOINT = "http://www.google-analytics.com/collect";
    private final String VERBOSE_PATTERN = "ModInfo (%s): %s";
    private final String trackingId;
    private final UUID clientId;
    private final Config config;
    private final String userAgent;
    private final ExecutorService service;
    private int retries;
    private int connectTimeout;
    private int readTimeout;
    private AtomicInteger messageCount;
    
    public Client(final String trackingId, final UUID clientId, final Config config, final String defaultUserLanguage) {
        this.retries = 5;
        this.connectTimeout = 5000;
        this.readTimeout = 2000;
        this.messageCount = new AtomicInteger(0);
        this.trackingId = trackingId;
        this.clientId = clientId;
        this.config = config;
        this.userAgent = this.createUserAgent(defaultUserLanguage);
        this.service = Executors.newFixedThreadPool(2);
        if (config.isVerbose()) {
            this.showVerboseMessage("User-Agent: " + this.userAgent);
        }
    }
    
    public Future send(final Payload payload) {
        return this.send(payload, null);
    }
    
    public Future send(final Payload payload, final Message.Callback callback) {
        if (this.config.isEnabled()) {
            payload.put(Payload.Parameter.Version, "1");
            payload.put(Payload.Parameter.TrackingId, this.trackingId);
            payload.put(Payload.Parameter.ClientId, this.clientId.toString());
            payload.put(Payload.Parameter.CustomMetric1, Integer.toString(this.messageCount.incrementAndGet()));
            final Message message = new Message("http://www.google-analytics.com/collect", payload, this.userAgent, this.retries, this.connectTimeout, this.readTimeout);
            final FutureTask<Void> future = new FutureTask<Void>(this.getRunnableWrapper(message, payload, callback), null);
            this.service.submit(future);
            return future;
        }
        return null;
    }
    
    private Runnable getRunnableWrapper(final Message message, final Payload payload, final Message.Callback callback) {
        return new Runnable() {
            @Override
            public void run() {
                Object result = null;
                try {
                    result = message.call();
                }
                catch (Throwable t) {
                    ModInfo.LOGGER.log(Level.ERROR, "ModInfo couldn't send message", t);
                }
                try {
                    if (Client.this.config.isVerbose() && Boolean.TRUE.equals(result)) {
                        Client.this.showVerboseMessage(payload.toVerboseString());
                    }
                }
                catch (Throwable t) {
                    ModInfo.LOGGER.log(Level.ERROR, "ModInfo couldn't do verbose output", t);
                }
                try {
                    if (callback != null) {
                        callback.onResult(result);
                    }
                }
                catch (Throwable t) {
                    ModInfo.LOGGER.log(Level.ERROR, "ModInfo couldn't use callback", t);
                }
            }
        };
    }
    
    private String createUserAgent(final String defaultUserLanguage) {
        String agent = null;
        try {
            String os = System.getProperty("os.name");
            if (os == null) {
                os = "";
            }
            String version = System.getProperty("os.version");
            if (version == null) {
                version = "";
            }
            String arch = System.getProperty("os.arch");
            if (arch == null) {
                arch = "";
            }
            if (arch.equals("amd64")) {
                arch = "WOW64";
            }
            String lang = String.format("%s_%s", System.getProperty("user.language"), System.getProperty("user.country"));
            if (lang.contains("null")) {
                lang = defaultUserLanguage;
            }
            if (os.startsWith("Mac")) {
                version = version.replace(".", "_");
                agent = String.format("Mozilla/5.0 (Macintosh; U; Intel Mac OS X %s; %s)", version, lang);
            }
            else if (os.startsWith("Win")) {
                agent = String.format("Mozilla/5.0 (Windows; U; Windows NT %s; %s; %s)", version, arch, lang);
            }
            else if (os.startsWith("Linux")) {
                agent = String.format("Mozilla/5.0 (Linux; U; Linux %s; %s; %s)", version, arch, lang);
            }
            else {
                agent = String.format("Mozilla/5.0 (%s; U; %s %s; %s, %s)", os, os, version, arch, lang);
            }
        }
        catch (Throwable t) {
            ModInfo.LOGGER.log(Level.ERROR, "ModInfo couldn't create useragent string", t);
            agent = "Mozilla/5.0 (Unknown)";
        }
        return agent;
    }
    
    private void showVerboseMessage(final String message) {
        System.out.println(String.format("ModInfo (%s): %s", this.config.getModId(), message));
    }
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (this.service != null) {
            this.service.shutdown();
        }
    }
}
