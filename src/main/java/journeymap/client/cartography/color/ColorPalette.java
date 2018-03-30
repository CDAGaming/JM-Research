package journeymap.client.cartography.color;

import java.nio.charset.*;
import org.apache.logging.log4j.*;
import com.google.gson.annotations.*;
import com.google.common.collect.*;
import net.minecraftforge.registries.*;
import journeymap.client.*;
import journeymap.common.log.*;
import net.minecraftforge.fml.client.*;
import journeymap.client.io.*;
import net.minecraft.client.*;
import com.google.common.io.*;
import journeymap.client.log.*;
import java.util.regex.*;
import journeymap.client.model.*;
import javax.annotation.*;
import java.io.*;
import journeymap.common.*;
import java.lang.reflect.*;
import java.util.stream.*;
import java.util.*;
import com.google.gson.*;

public class ColorPalette
{
    public static final String HELP_PAGE = "http://journeymap.info/Color_Palette";
    public static final String SAMPLE_STANDARD_PATH = ".minecraft/journeymap/";
    public static final String SAMPLE_WORLD_PATH = ".minecraft/journeymap/data/*/worldname/";
    public static final String JSON_FILENAME = "colorpalette.json";
    public static final String HTML_FILENAME = "colorpalette.html";
    public static final String VARIABLE = "var colorpalette=";
    public static final Charset UTF8;
    public static final double VERSION = 5.49;
    private static final Logger logger;
    private static final Gson GSON;
    @Since(3.0)
    double version;
    @Since(1.0)
    String name;
    @Since(1.0)
    String generated;
    @Since(1.0)
    String[] description;
    @Since(1.0)
    boolean permanent;
    @Since(1.0)
    String resourcePacks;
    @Since(2.0)
    String modNames;
    @Since(5.49)
    HashBasedTable<String, String, BlockStateColor> table;
    private transient File origin;
    private transient boolean dirty;
    
    ColorPalette() {
        this.table = (HashBasedTable<String, String, BlockStateColor>)HashBasedTable.create(GameData.getBlockStateIDMap().func_186804_a(), 16);
    }
    
    private ColorPalette(final String resourcePacks, final String modNames) {
        this.version = 5.49;
        this.name = Constants.getString("jm.colorpalette.file_title");
        this.generated = String.format("Generated using %s for %s on %s", JourneymapClient.MOD_NAME, "1.12.2", new Date());
        this.resourcePacks = resourcePacks;
        this.modNames = modNames;
        final ArrayList<String> lines = new ArrayList<String>();
        lines.add(Constants.getString("jm.colorpalette.file_header_1"));
        lines.add(Constants.getString("jm.colorpalette.file_header_2", "colorpalette.html"));
        lines.add(Constants.getString("jm.colorpalette.file_header_3", "colorpalette.json", ".minecraft/journeymap/data/*/worldname/"));
        lines.add(Constants.getString("jm.colorpalette.file_header_4", "colorpalette.json", ".minecraft/journeymap/"));
        lines.add(Constants.getString("jm.config.file_header_5", "http://journeymap.info/Color_Palette"));
        this.description = lines.toArray(new String[4]);
        this.table = (HashBasedTable<String, String, BlockStateColor>)HashBasedTable.create(GameData.getBlockStateIDMap().func_186804_a(), 16);
    }
    
    public static ColorPalette getActiveColorPalette() {
        final String resourcePacks = ColorManager.getResourcePackNames();
        final String modNames = Constants.getModNames();
        final File worldPaletteFile = getWorldPaletteFile();
        if (worldPaletteFile.canRead()) {
            final ColorPalette palette = loadFromFile(worldPaletteFile);
            if (palette != null) {
                if (palette.version >= 5.49) {
                    return palette;
                }
                ColorPalette.logger.warn(String.format("Existing world color palette is obsolete. Required version: %s.  Found version: %s", 5.49, palette.version));
            }
        }
        final File standardPaletteFile = getStandardPaletteFile();
        if (standardPaletteFile.canRead()) {
            ColorPalette palette2 = loadFromFile(standardPaletteFile);
            if (palette2 != null && palette2.version != 5.49) {
                ColorPalette.logger.warn(String.format("Existing color palette is unusable. Required version: %s.  Found version: %s", 5.49, palette2.version));
                standardPaletteFile.renameTo(new File(standardPaletteFile.getParentFile(), standardPaletteFile.getName() + ".v" + palette2.version));
                palette2 = null;
            }
            if (palette2 != null) {
                if (palette2.isPermanent()) {
                    ColorPalette.logger.info("Existing color palette is set to be permanent.");
                    return palette2;
                }
                if (resourcePacks.equals(palette2.resourcePacks)) {
                    if (modNames.equals(palette2.modNames)) {
                        ColorPalette.logger.debug("Existing color palette's resource packs and mod names match current loadout.");
                        return palette2;
                    }
                    ColorPalette.logger.warn("Existing color palette's mods no longer match current loadout.");
                    ColorPalette.logger.info(String.format("WAS: %s\nNOW: %s", palette2.modNames, modNames));
                }
                else {
                    ColorPalette.logger.warn("Existing color palette's resource packs no longer match current loadout.");
                    ColorPalette.logger.info(String.format("WAS: %s\nNOW: %s", palette2.resourcePacks, resourcePacks));
                }
            }
        }
        return null;
    }
    
    public static ColorPalette create(final boolean standard, final boolean permanent) {
        final long start = System.currentTimeMillis();
        ColorPalette palette = null;
        try {
            final String resourcePackNames = ColorManager.getResourcePackNames();
            final String modPackNames = Constants.getModNames();
            palette = new ColorPalette(resourcePackNames, modPackNames);
            palette.setPermanent(permanent);
            palette.writeToFile(standard);
            final long elapsed = System.currentTimeMillis() - start;
            ColorPalette.logger.info(String.format("Color palette file generated for %d blockstates in %dms for: %s", palette.size(), elapsed, palette.getOrigin()));
            return palette;
        }
        catch (Exception e) {
            ColorPalette.logger.error("Couldn't create ColorPalette: " + LogFormatter.toString(e));
            return null;
        }
    }
    
    private static File getWorldPaletteFile() {
        final Minecraft mc = FMLClientHandler.instance().getClient();
        return new File(FileHandler.getJMWorldDir(mc), "colorpalette.json");
    }
    
    private static File getStandardPaletteFile() {
        return new File(FileHandler.getJourneyMapDir(), "colorpalette.json");
    }
    
    private static ColorPalette loadFromFile(final File file) {
        String jsonString = null;
        try {
            jsonString = Files.toString(file, ColorPalette.UTF8).replaceFirst("var colorpalette=", "");
            final ColorPalette palette = (ColorPalette)ColorPalette.GSON.fromJson(jsonString, (Class)ColorPalette.class);
            palette.origin = file;
            palette.getOriginHtml(true, true);
            return palette;
        }
        catch (Throwable e3) {
            ChatLog.announceError(Constants.getString("jm.colorpalette.file_error", file.getPath()));
            try {
                file.renameTo(new File(file.getParentFile(), file.getName() + ".bad"));
            }
            catch (Exception e2) {
                ColorPalette.logger.error("Couldn't rename bad palette file: " + e2);
            }
            return null;
        }
    }
    
    private String substituteValueInContents(final String contents, final String key, final Object... params) {
        final String token = String.format("\\$%s\\$", key);
        return contents.replaceAll(token, Matcher.quoteReplacement(Constants.getString(key, params)));
    }
    
    boolean hasBlockStateColor(final BlockMD blockMD) {
        return this.table.contains((Object)BlockMD.getBlockId(blockMD), (Object)BlockMD.getBlockStateId(blockMD));
    }
    
    @Nullable
    private BlockStateColor getBlockStateColor(final BlockMD blockMD, final boolean createIfMissing) {
        BlockStateColor blockStateColor = (BlockStateColor)this.table.get((Object)BlockMD.getBlockId(blockMD), (Object)BlockMD.getBlockStateId(blockMD));
        if (blockStateColor == null && createIfMissing && blockMD.hasColor()) {
            blockStateColor = new BlockStateColor(blockMD);
            this.table.put((Object)BlockMD.getBlockId(blockMD), (Object)BlockMD.getBlockStateId(blockMD), (Object)blockStateColor);
            this.dirty = true;
        }
        return blockStateColor;
    }
    
    public boolean applyColor(final BlockMD blockMD, final boolean createIfMissing) {
        final boolean preExisting = this.hasBlockStateColor(blockMD);
        final BlockStateColor blockStateColor = this.getBlockStateColor(blockMD, createIfMissing);
        if (blockStateColor == null) {
            return false;
        }
        if (preExisting) {
            if (blockMD.hasTransparency()) {
                blockMD.setAlpha((blockStateColor.alpha != null) ? ((float)blockStateColor.alpha) : 1.0f);
            }
            final int color = RGB.hexToInt(blockStateColor.color);
            blockMD.setColor(color);
        }
        return true;
    }
    
    public int applyColors(final Collection<BlockMD> blockMDs, final boolean createIfMissing) {
        int hit = 0;
        int miss = 0;
        for (final BlockMD blockMD : blockMDs) {
            if (this.applyColor(blockMD, createIfMissing)) {
                ++hit;
            }
            else {
                ++miss;
            }
        }
        if (miss > 0) {
            ColorPalette.logger.debug(miss + " blockstates didn't have a color in the palette");
        }
        return hit;
    }
    
    public void writeToFile() {
        this.writeToFile(this.isStandard());
    }
    
    private boolean writeToFile(final boolean standard) {
        File palleteFile = null;
        try {
            palleteFile = (standard ? getStandardPaletteFile() : getWorldPaletteFile());
            Files.write((CharSequence)("var colorpalette=" + ColorPalette.GSON.toJson((Object)this)), palleteFile, ColorPalette.UTF8);
            this.origin = palleteFile;
            this.dirty = false;
            this.getOriginHtml(true, true);
            return true;
        }
        catch (Exception e) {
            ColorPalette.logger.error(String.format("Can't save color palette file %s: %s", palleteFile, LogFormatter.toString(e)));
            return false;
        }
    }
    
    public File getOrigin() throws IOException {
        return this.origin.getCanonicalFile();
    }
    
    public File getOriginHtml(final boolean createIfMissing, final boolean overwriteExisting) {
        try {
            if (this.origin == null) {
                return null;
            }
            File htmlFile = new File(this.origin.getParentFile(), "colorpalette.html");
            if ((!htmlFile.exists() && createIfMissing) || overwriteExisting) {
                htmlFile = FileHandler.copyColorPaletteHtmlFile(this.origin.getParentFile(), "colorpalette.html");
                String htmlString = Files.toString(htmlFile, ColorPalette.UTF8);
                htmlString = this.substituteValueInContents(htmlString, "jm.colorpalette.file_title", new Object[0]);
                htmlString = this.substituteValueInContents(htmlString, "jm.colorpalette.file_missing_data", "colorpalette.json");
                htmlString = this.substituteValueInContents(htmlString, "jm.colorpalette.resource_packs", new Object[0]);
                htmlString = this.substituteValueInContents(htmlString, "jm.colorpalette.mods", new Object[0]);
                htmlString = this.substituteValueInContents(htmlString, "jm.colorpalette.basic_colors", new Object[0]);
                htmlString = this.substituteValueInContents(htmlString, "jm.colorpalette.biome_colors", new Object[0]);
                Files.write((CharSequence)htmlString, htmlFile, ColorPalette.UTF8);
            }
            return htmlFile;
        }
        catch (Throwable t) {
            ColorPalette.logger.error("Can't write colorpalette.html: " + t);
            return null;
        }
    }
    
    public boolean isPermanent() {
        return this.permanent;
    }
    
    public void setPermanent(final boolean permanent) {
        this.permanent = permanent;
    }
    
    public boolean isStandard() {
        return this.origin != null && this.origin.getParentFile().getAbsoluteFile().equals(FileHandler.getJourneyMapDir().getAbsoluteFile());
    }
    
    public double getVersion() {
        return this.version;
    }
    
    public boolean isDirty() {
        return this.dirty;
    }
    
    public int size() {
        return this.table.size();
    }
    
    @Override
    public String toString() {
        return "ColorPalette[" + this.resourcePacks + "]";
    }
    
    static {
        UTF8 = Charset.forName("UTF-8");
        logger = Journeymap.getLogger();
        GSON = new GsonBuilder().registerTypeAdapter((Type)HashBasedTable.class, (Object)new Serializer()).registerTypeAdapter((Type)HashBasedTable.class, (Object)new Deserializer()).create();
    }
    
    private static class Serializer implements JsonSerializer<HashBasedTable<String, String, BlockStateColor>>
    {
        public JsonElement serialize(final HashBasedTable<String, String, BlockStateColor> src, final Type typeOfSrc, final JsonSerializationContext context) {
            final JsonObject jsonTable = new JsonObject();
            for (final String blockId : (List)src.rowKeySet().stream().sorted().collect(Collectors.toList())) {
                final String[] resource = blockId.split(":");
                final String mod = resource[0];
                final String block = resource[1];
                JsonObject jsonMod = null;
                if (!jsonTable.has(mod)) {
                    jsonMod = new JsonObject();
                    jsonTable.add(mod, (JsonElement)jsonMod);
                }
                else {
                    jsonMod = jsonTable.getAsJsonObject(mod);
                }
                JsonObject jsonBlock = null;
                if (!jsonMod.has(block)) {
                    jsonBlock = new JsonObject();
                    jsonMod.add(block, (JsonElement)jsonBlock);
                }
                else {
                    jsonBlock = jsonMod.getAsJsonObject(block);
                }
                for (final String stateId : (List)src.row((Object)blockId).keySet().stream().sorted().collect(Collectors.toList())) {
                    final BlockStateColor blockStateColor = (BlockStateColor)src.get((Object)blockId, (Object)stateId);
                    final JsonArray bscArray = new JsonArray();
                    bscArray.add((JsonElement)new JsonPrimitive(blockStateColor.color));
                    if (blockStateColor.alpha != null && blockStateColor.alpha != 1.0f) {
                        bscArray.add((JsonElement)new JsonPrimitive((Number)blockStateColor.alpha));
                    }
                    jsonBlock.add(stateId, (JsonElement)bscArray);
                }
            }
            return (JsonElement)jsonTable;
        }
    }
    
    private static class Deserializer implements JsonDeserializer<HashBasedTable<String, String, BlockStateColor>>
    {
        public HashBasedTable<String, String, BlockStateColor> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            final HashBasedTable<String, String, BlockStateColor> result = (HashBasedTable<String, String, BlockStateColor>)HashBasedTable.create(GameData.getBlockStateIDMap().func_186804_a(), 16);
            final JsonObject jsonTable = json.getAsJsonObject();
            for (final Map.Entry<String, JsonElement> jsonMod : jsonTable.entrySet()) {
                final String modId = jsonMod.getKey();
                for (final Map.Entry<String, JsonElement> jsonBlock : jsonMod.getValue().getAsJsonObject().entrySet()) {
                    final String blockId = jsonBlock.getKey();
                    for (final Map.Entry<String, JsonElement> jsonState : jsonBlock.getValue().getAsJsonObject().entrySet()) {
                        final String blockStateId = jsonState.getKey();
                        final JsonArray bscArray = jsonState.getValue().getAsJsonArray();
                        final String color = bscArray.get(0).getAsString();
                        float alpha = 1.0f;
                        if (bscArray.size() > 1) {
                            alpha = bscArray.get(1).getAsFloat();
                        }
                        final BlockStateColor blockStateColor = new BlockStateColor(color, Float.valueOf(alpha));
                        result.put((Object)(modId + ":" + blockId), (Object)blockStateId, (Object)blockStateColor);
                    }
                }
            }
            return result;
        }
    }
}
