package journeymap.common.version;

import journeymap.common.*;
import journeymap.common.thread.*;
import java.util.concurrent.*;
import java.net.*;
import org.apache.http.client.config.*;
import org.apache.http.impl.client.*;
import org.apache.http.client.methods.*;
import com.google.common.io.*;
import java.io.*;
import org.apache.http.client.*;
import org.apache.http.*;
import com.google.gson.*;
import java.util.*;

public class VersionCheck
{
    private static volatile ExecutorService executorService;
    private static volatile Boolean updateCheckEnabled;
    private static volatile Boolean versionIsCurrent;
    private static volatile Boolean versionIsChecked;
    private static volatile String versionAvailable;
    private static volatile String downloadUrl;
    
    public static Boolean getVersionIsCurrent() {
        if (VersionCheck.versionIsChecked == null) {
            checkVersion();
        }
        return VersionCheck.versionIsCurrent;
    }
    
    public static Boolean getVersionIsChecked() {
        if (VersionCheck.versionIsChecked == null) {
            checkVersion();
        }
        return VersionCheck.versionIsChecked;
    }
    
    public static String getVersionAvailable() {
        if (VersionCheck.versionIsChecked == null) {
            checkVersion();
        }
        return VersionCheck.versionAvailable;
    }
    
    public static String getDownloadUrl() {
        if (VersionCheck.versionIsChecked == null) {
            checkVersion();
        }
        return VersionCheck.downloadUrl;
    }
    
    private static synchronized void checkVersion() {
        VersionCheck.versionIsChecked = false;
        VersionCheck.versionIsCurrent = true;
        VersionCheck.versionAvailable = "0";
        if (!VersionCheck.updateCheckEnabled) {
            Journeymap.getLogger().info("Update check disabled in properties file.");
        }
        else {
            (VersionCheck.executorService = Executors.newSingleThreadExecutor(new JMThreadFactory("VersionCheck"))).submit(new Runnable() {
                @Override
                public void run() {
                    final String currentVersion = Journeymap.JM_VERSION.toString();
                    final boolean currentIsRelease = Journeymap.JM_VERSION.isRelease();
                    InputStreamReader in = null;
                    try {
                        final URI uri = URI.create("https://api.cfwidget.com/minecraft/mc-mods/journeymap");
                        final RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(6000).setSocketTimeout(6000).setRedirectsEnabled(true).build();
                        final HttpClient httpClient = (HttpClient)HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
                        final HttpResponse response = httpClient.execute((HttpUriRequest)new HttpGet(uri));
                        if (response.getStatusLine().getStatusCode() / 200 == 1) {
                            in = new InputStreamReader(response.getEntity().getContent());
                            final String rawResponse = CharStreams.toString((Readable)in);
                            final JsonObject project = new JsonParser().parse(rawResponse).getAsJsonObject();
                            final JsonElement version = project.get("versions").getAsJsonObject().get("1.12.2");
                            if (version == null) {
                                Journeymap.getLogger().warn("No versions found online for 1.12.2");
                            }
                            else {
                                final Iterator<JsonElement> files = (Iterator<JsonElement>)version.getAsJsonArray().iterator();
                                while (files.hasNext()) {
                                    final JsonObject file = files.next().getAsJsonObject();
                                    try {
                                        final JsonElement type = file.get("type");
                                        if (currentIsRelease && !"release".equals(type.getAsString())) {
                                            continue;
                                        }
                                        String name = file.get("name").getAsString();
                                        if (!name.contains("1.12.2")) {
                                            continue;
                                        }
                                        name = name.split("1.12.2")[1];
                                        if (!name.contains("-")) {
                                            continue;
                                        }
                                        final String fileVersion = name.split("-")[1];
                                        final String url = "http://minecraft.curseforge.com/projects/journeymap/files/" + file.get("id").getAsString();
                                        if (!isCurrent(currentVersion, fileVersion)) {
                                            VersionCheck.downloadUrl = url;
                                            VersionCheck.versionAvailable = fileVersion;
                                            VersionCheck.versionIsCurrent = false;
                                            VersionCheck.versionIsChecked = true;
                                            Journeymap.getLogger().info(String.format("Newer version online: JourneyMap %s for Minecraft %s on %s", VersionCheck.versionAvailable, "1.12.2", VersionCheck.downloadUrl));
                                            break;
                                        }
                                        continue;
                                    }
                                    catch (Exception e) {
                                        Journeymap.getLogger().error("Could not parse download info: " + file, (Throwable)e);
                                    }
                                }
                            }
                            if (!VersionCheck.versionIsChecked) {
                                VersionCheck.versionAvailable = currentVersion;
                                VersionCheck.versionIsCurrent = true;
                                VersionCheck.versionIsChecked = true;
                                VersionCheck.downloadUrl = "http://minecraft.curseforge.com/projects/journeymap/files/";
                            }
                        }
                        else {
                            Journeymap.getLogger().error(String.format("Version check to %s returned: %s ", uri, response.getStatusLine()));
                        }
                    }
                    catch (Throwable e2) {
                        Journeymap.getLogger().error("Could not check version URL", e2);
                        VersionCheck.updateCheckEnabled = false;
                    }
                    finally {
                        if (in != null) {
                            try {
                                in.close();
                                VersionCheck.executorService.shutdown();
                                VersionCheck.executorService = null;
                            }
                            catch (IOException ex) {}
                        }
                    }
                    if (!VersionCheck.versionIsCurrent) {}
                }
            });
        }
    }
    
    private static boolean isCurrent(final String thisVersionStr, final String availableVersionStr) {
        if (thisVersionStr.equals(availableVersionStr)) {
            return true;
        }
        final Version thisVersion = Version.from(thisVersionStr, null);
        final Version availableVersion = Version.from(availableVersionStr, null);
        return !availableVersion.isNewerThan(thisVersion);
    }
    
    static {
        VersionCheck.updateCheckEnabled = Journeymap.proxy.isUpdateCheckEnabled();
        VersionCheck.versionIsCurrent = true;
    }
}
