package journeymap.client.io;

import journeymap.client.Constants;
import journeymap.common.Journeymap;
import journeymap.common.log.LogFormatter;
import journeymap.common.properties.config.StringField;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.io.File;
import java.util.*;

public class IconSetFileHandler {
    public static final ResourceLocation ASSETS_JOURNEYMAP_ICON_ENTITY;
    public static final String MOB_ICON_SET_DEFAULT = "Default";
    private static final Set<String> modUpdatedSetNames;
    private static final Set<ResourceLocation> entityIconLocations;

    static {
        ASSETS_JOURNEYMAP_ICON_ENTITY = new ResourceLocation("journeymap", "icon/entity");
        modUpdatedSetNames = new HashSet<String>();
        entityIconLocations = new HashSet<ResourceLocation>();
        registerEntityIconDirectory(IconSetFileHandler.ASSETS_JOURNEYMAP_ICON_ENTITY);
    }

    public static void initialize() {
        IconSetFileHandler.modUpdatedSetNames.add("Default");
    }

    public static boolean registerEntityIconDirectory(final ResourceLocation resourceLocation) {
        final boolean valid = addEntityIcons(resourceLocation, "Default", false);
        if (valid) {
            IconSetFileHandler.entityIconLocations.add(resourceLocation);
        }
        return valid;
    }

    public static void ensureEntityIconSet(final String setName) {
        ensureEntityIconSet(setName, false);
    }

    public static void ensureEntityIconSet(final String setName, final boolean overwrite) {
        if (!IconSetFileHandler.modUpdatedSetNames.contains(setName)) {
            for (final ResourceLocation resourceLocation : IconSetFileHandler.entityIconLocations) {
                addEntityIcons(resourceLocation, setName, overwrite);
            }
            IconSetFileHandler.modUpdatedSetNames.add(setName);
        }
        try {
            final ResourcePackRepository rpr = FMLClientHandler.instance().getClient().getResourcePackRepository();
            for (final ResourcePackRepository.Entry entry : rpr.getRepositoryEntries()) {
                final IResourcePack pack = entry.getResourcePack();
                for (final String domain : pack.getResourceDomains()) {
                    final ResourceLocation domainEntityIcons = new ResourceLocation(domain, "textures/entity_icons");
                    if (pack.resourceExists(domainEntityIcons)) {
                        addEntityIcons(domainEntityIcons, setName, true);
                    }
                }
            }
        } catch (Throwable t) {
            Journeymap.getLogger().error(String.format("Can't get entity icon from resource packs: %s", LogFormatter.toString(t)));
        }
    }

    private static boolean addEntityIcons(final ResourceLocation resourceLocation, final String setName, final boolean overwrite) {
        boolean result = false;
        try {
            result = FileHandler.copyResources(getEntityIconDir(), resourceLocation, setName, overwrite);
        } catch (Throwable t) {
            Journeymap.getLogger().error("Error adding entity icons: " + t.getMessage(), t);
        }
        Journeymap.getLogger().info(String.format("Added entity icons from %s. Success: %s", resourceLocation, result));
        return result;
    }

    public static File getEntityIconDir() {
        final File dir = new File(FileHandler.getMinecraftDirectory(), Constants.ENTITY_ICON_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static ArrayList<String> getEntityIconSetNames() {
        return getIconSetNames(getEntityIconDir(), Collections.singletonList("Default"));
    }

    public static ArrayList<String> getIconSetNames(final File parentDir, final List<String> defaultIconSets) {
        try {
            for (final String iconSetName : defaultIconSets) {
                final File iconSetDir = new File(parentDir, iconSetName);
                if (iconSetDir.exists() && !iconSetDir.isDirectory()) {
                    iconSetDir.delete();
                }
                iconSetDir.mkdirs();
            }
        } catch (Throwable t) {
            Journeymap.getLogger().error("Could not prepare iconset directories for " + parentDir + ": " + LogFormatter.toString(t));
        }
        final ArrayList<String> names = new ArrayList<String>();
        for (final File iconSetDir2 : parentDir.listFiles()) {
            if (iconSetDir2.isDirectory()) {
                names.add(iconSetDir2.getName());
            }
        }
        Collections.sort(names);
        return names;
    }

    public static class IconSetValuesProvider implements StringField.ValuesProvider {
        @Override
        public List<String> getStrings() {
            if (FMLClientHandler.instance().getClient() != null) {
                return IconSetFileHandler.getEntityIconSetNames();
            }
            return Collections.singletonList("Default");
        }

        @Override
        public String getDefaultString() {
            return "Default";
        }
    }
}
