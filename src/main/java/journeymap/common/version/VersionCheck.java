package journeymap.common.version;

import com.google.common.io.CharStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import journeymap.common.Journeymap;
import journeymap.common.thread.JMThreadFactory;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VersionCheck {
    private static volatile ExecutorService executorService;
    private static volatile Boolean updateCheckEnabled;
    private static volatile Boolean versionIsCurrent;
    private static volatile Boolean versionIsChecked;
    private static volatile String versionAvailable;
    private static volatile String downloadUrl;

    static {
        VersionCheck.updateCheckEnabled = Journeymap.proxy.isUpdateCheckEnabled();
        VersionCheck.versionIsCurrent = true;
    }

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
        } else {
            (VersionCheck.executorService = Executors.newSingleThreadExecutor(new JMThreadFactory("VersionCheck"))).submit(() -> {
                HttpURLConnection connection;
                URL uri = null;
                String rawResponse;
                try {
                    uri = URI.create("http://widget.mcf.li/mc-mods/minecraft/journeymap.json").toURL();
                    connection = (HttpURLConnection) uri.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(6000);
                    connection.setConnectTimeout(6000);
                } catch (Exception e) {
                    Journeymap.getLogger().error("Could not get download info: " + e);
                }
                try (InputStreamReader in = new InputStreamReader(uri.openStream())) {
                    rawResponse = CharStreams.toString(in);
                    final String currentVersion = Journeymap.JM_VERSION.toString();
                    final boolean currentIsRelease = Journeymap.JM_VERSION.isRelease();
                    final JsonObject project = new JsonParser().parse(rawResponse.trim()).getAsJsonObject();
                    final JsonElement version = project.get("versions").getAsJsonObject().get("1.12.2");
                    if (version == null) {
                        Journeymap.getLogger().warn("No versions found online for 1.12.2");
                    } else {
                        for (JsonElement jsonElement : version.getAsJsonArray()) {
                            final JsonObject file = jsonElement.getAsJsonObject();
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
                            } catch (Exception e) {
                                Journeymap.getLogger().error("Could not parse download info: " + file, e);
                            }
                        }
                    }
                    if (!VersionCheck.versionIsChecked) {
                        VersionCheck.versionAvailable = currentVersion;
                        VersionCheck.versionIsCurrent = true;
                        VersionCheck.versionIsChecked = true;
                        VersionCheck.downloadUrl = "http://minecraft.curseforge.com/projects/journeymap/files/";
                    }
                } catch (Throwable e2) {
                    Journeymap.getLogger().error("Could not check version URL", e2);
                    VersionCheck.updateCheckEnabled = false;
                }
                if (!VersionCheck.versionIsCurrent) {
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
}
