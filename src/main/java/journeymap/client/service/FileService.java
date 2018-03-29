package journeymap.client.service;

import journeymap.client.JourneymapClient;
import journeymap.client.cartography.color.ColorManager;
import journeymap.client.cartography.color.ColorPalette;
import journeymap.client.io.ThemeLoader;
import journeymap.client.log.JMLogger;
import journeymap.client.render.texture.TextureCache;
import journeymap.client.render.texture.TextureImpl;
import journeymap.client.ui.theme.ThemePresets;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import se.rupy.http.Event;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class FileService extends BaseService {
    private static final long serialVersionUID = 2L;
    protected final String resourcePath;
    final String COLOR_PALETTE_JSON = "/colorpalette.json";
    final String COLOR_PALETTE_HTML = "/colorpalette.html";
    final String ENTITY_ICON_PREFIX = "/entity_icon";
    final String ICON_THEME_PATH_PREFIX = "/theme/";
    final String SKIN_PREFIX = "/skin/";
    private boolean useZipEntry;
    private File zipFile;

    public FileService() {
        final URL resourceDir = JourneymapClient.class.getResource("/assets/journeymap/ui");
        String testPath = null;
        if (resourceDir == null) {
            Journeymap.getLogger().error("Can't determine path to webroot!");
        } else {
            testPath = resourceDir.getPath();
            if (testPath.endsWith("/")) {
                testPath = testPath.substring(0, testPath.length() - 1);
            }
            this.useZipEntry = ((resourceDir.getProtocol().equals("file") || resourceDir.getProtocol().equals("jar")) && testPath.contains("!/"));
        }
        if (!this.useZipEntry && "14.23.0.2491".contains("@")) {
            try {
                testPath = new File("../src/main/resources/assets/journeymap/ui").getCanonicalPath();
                Journeymap.getLogger().info("Dev environment detected, serving source files from " + testPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.resourcePath = testPath;
    }

    @Override
    public String path() {
        return null;
    }

    @Override
    public void filter(final Event event) throws Event, Exception {
        String path = null;
        final InputStream in = null;
        try {
            path = event.query().path();
            if (path.startsWith("/skin/")) {
                this.serveSkin(path.split("/skin/")[1], event);
                return;
            }
            InputStream fileStream = null;
            if (path.startsWith("/colorpalette.json")) {
                final ColorPalette colorPalette = ColorManager.INSTANCE.getCurrentPalette();
                if (colorPalette != null) {
                    final File jsonFile = colorPalette.getOrigin();
                    if (jsonFile.canRead()) {
                        ResponseHeader.on(event).contentType(ContentType.js);
                        fileStream = new FileInputStream(jsonFile);
                    }
                }
            } else if (path.startsWith("/colorpalette.html")) {
                final ColorPalette colorPalette = ColorManager.INSTANCE.getCurrentPalette();
                if (colorPalette != null) {
                    final File htmlFile = colorPalette.getOriginHtml(true, false);
                    if (htmlFile.canRead()) {
                        ResponseHeader.on(event).contentType(ContentType.html);
                        fileStream = new FileInputStream(htmlFile);
                    }
                }
            } else if (path.startsWith("/entity_icon")) {
                final String location = event.query().parameters().split("location=")[1];
                final BufferedImage image = TextureCache.resolveImage(new ResourceLocation(location));
                if (image == null) {
                    JMLogger.logOnce("Path not found: " + path, null);
                    this.throwEventException(404, "Unknown: " + path, event, true);
                } else {
                    ResponseHeader.on(event).contentType(ContentType.png).noCache();
                    this.serveImage(event, image);
                }
            } else if (path.startsWith("/theme/")) {
                final String themeIconPath = path.split("/theme/")[1].replace('/', File.separatorChar);
                final File themeDir = new File(ThemeLoader.getThemeIconDir(), ThemePresets.getDefault().directory);
                final File iconFile = new File(themeDir, themeIconPath);
                if (!iconFile.exists()) {
                    final String delim = "\\u005c" + File.separator;
                    final String setName = themeIconPath.split(delim)[0];
                    final String iconPath = themeIconPath.substring(themeIconPath.indexOf(File.separatorChar) + 1);
                    if (event != null) {
                        ResponseHeader.on(event).contentType(ContentType.png);
                    }
                    final String resourcePath = String.format("theme/%s/%s", setName, iconPath);
                    try {
                        final IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
                        final IResource resource = resourceManager.getResource(new ResourceLocation("journeymap", resourcePath));
                        fileStream = resource.getInputStream();
                    } catch (FileNotFoundException e2) {
                        JMLogger.logOnce("Resource not found: " + resourcePath, null);
                        this.throwEventException(404, "Unknown: " + path, event, true);
                    } catch (Exception e) {
                        JMLogger.logOnce("Resource not usable: " + resourcePath, e);
                        this.throwEventException(415, "Not an image: " + path, event, true);
                    }
                } else {
                    if (event != null) {
                        ResponseHeader.on(event).content(iconFile);
                    }
                    fileStream = new FileInputStream(iconFile);
                }
            } else {
                fileStream = this.getStream(path, event);
            }
            if (fileStream == null) {
                JMLogger.logOnce("Path not found: " + path, null);
                this.throwEventException(404, "Unknown: " + path, event, true);
            } else {
                this.serveStream(fileStream, event);
            }
        } catch (Event eventEx) {
            throw eventEx;
        } catch (Throwable t) {
            Journeymap.getLogger().error(LogFormatter.toString(t));
            this.throwEventException(500, "Error: " + path, event, true);
        }
    }

    protected InputStream getStream(final String path, final Event event) {
        InputStream in = null;
        try {
            String requestPath = null;
            if ("/".equals(path)) {
                requestPath = this.resourcePath + "/index.html";
            } else {
                requestPath = this.resourcePath + path;
            }
            if (this.useZipEntry) {
                final String[] tokens = requestPath.split("file:")[1].split("!/");
                if (this.zipFile == null) {
                    this.zipFile = new File(URI.create(tokens[0]).getPath());
                    if (!this.zipFile.canRead()) {
                        throw new RuntimeException("Can't read Zip file: " + this.zipFile + " (originally: " + tokens[0] + ")");
                    }
                }
                final String innerName = tokens[1];
                final FileInputStream fis = new FileInputStream(this.zipFile);
                final ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
                boolean found = false;
                ZipEntry zipEntry;
                while ((zipEntry = zis.getNextEntry()) != null) {
                    if (innerName.equals(zipEntry.getName())) {
                        in = new ZipFile(this.zipFile).getInputStream(zipEntry);
                        if (event != null) {
                            ResponseHeader.on(event).content(zipEntry);
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    zis.close();
                    fis.close();
                    in = null;
                }
            } else {
                final File file = new File(requestPath);
                if (file.exists()) {
                    if (event != null) {
                        ResponseHeader.on(event).content(file);
                    }
                    in = new FileInputStream(file);
                } else {
                    in = null;
                }
            }
        } catch (Throwable t) {
            Journeymap.getLogger().error(LogFormatter.toString(t));
        }
        return in;
    }

    public void serveSkin(final String username, final Event event) throws Exception {
        ResponseHeader.on(event).contentType(ContentType.png);
        final TextureImpl tex = TextureCache.getPlayerSkin(username);
        final BufferedImage img = tex.getImage();
        if (img != null) {
            this.serveImage(event, img);
        } else {
            event.reply().code("404 Not Found");
        }
    }

    public void serveFile(final File sourceFile, final Event event) throws Event, IOException {
        ResponseHeader.on(event).content(sourceFile);
        this.serveStream(new FileInputStream(sourceFile), event);
    }

    public void serveStream(final InputStream input, final Event event) throws Event, IOException {
        ReadableByteChannel inputChannel;
        WritableByteChannel outputChannel;
        try {
            inputChannel = Channels.newChannel(input);
            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            final GZIPOutputStream output = new GZIPOutputStream(bout);
            outputChannel = Channels.newChannel(output);
            final ByteBuffer buffer = ByteBuffer.allocate(65536);
            while (inputChannel.read(buffer) != -1) {
                buffer.flip();
                outputChannel.write(buffer);
                buffer.clear();
            }
            output.flush();
            output.close();
            bout.close();
            final byte[] gzbytes = bout.toByteArray();
            ResponseHeader.on(event).contentLength(gzbytes.length).setHeader("Content-encoding", "gzip");
            event.output().write(gzbytes);
        } catch (IOException e) {
            Journeymap.getLogger().error(LogFormatter.toString(e));
            throw event;
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    @Override
    protected byte[] gzip(final String data) {
        ByteArrayOutputStream bout;
        try {
            bout = new ByteArrayOutputStream();
            final GZIPOutputStream output = new GZIPOutputStream(bout);
            output.write(data.getBytes());
            output.flush();
            output.close();
            bout.close();
            return bout.toByteArray();
        } catch (Exception ex) {
            Journeymap.getLogger().warn("Failed to gzip encode: " + data);
            return null;
        }
    }
}
