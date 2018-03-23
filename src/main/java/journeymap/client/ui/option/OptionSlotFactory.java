package journeymap.client.ui.option;

import java.nio.charset.*;
import journeymap.client.properties.*;
import journeymap.common.*;
import journeymap.common.properties.*;
import journeymap.common.properties.config.*;
import journeymap.client.*;
import journeymap.client.ui.component.*;
import com.google.common.io.*;
import java.util.*;
import java.io.*;
import com.google.common.base.*;

public class OptionSlotFactory
{
    protected static final Charset UTF8;
    protected static BufferedWriter docWriter;
    protected static File docFile;
    protected static boolean generateDocs;
    
    public static List<CategorySlot> getSlots(final Map<Category, List<SlotMetadata>> toolbars) {
        final HashMap<Category, List<SlotMetadata>> mergedMap = new HashMap<Category, List<SlotMetadata>>();
        addSlots(mergedMap, ClientCategory.MiniMap1, Journeymap.getClient().getMiniMapProperties1());
        addSlots(mergedMap, ClientCategory.MiniMap2, Journeymap.getClient().getMiniMapProperties2());
        addSlots(mergedMap, ClientCategory.FullMap, Journeymap.getClient().getFullMapProperties());
        addSlots(mergedMap, ClientCategory.WebMap, Journeymap.getClient().getWebMapProperties());
        addSlots(mergedMap, ClientCategory.Waypoint, Journeymap.getClient().getWaypointProperties());
        addSlots(mergedMap, ClientCategory.Advanced, Journeymap.getClient().getCoreProperties());
        final List<CategorySlot> categories = new ArrayList<CategorySlot>();
        for (final Map.Entry<Category, List<SlotMetadata>> entry : mergedMap.entrySet()) {
            final Category category = entry.getKey();
            final CategorySlot categorySlot = new CategorySlot(category);
            for (final SlotMetadata val : entry.getValue()) {
                categorySlot.add(new ButtonListSlot(categorySlot).add(val));
            }
            if (toolbars.containsKey(category)) {
                final ButtonListSlot toolbarSlot = new ButtonListSlot(categorySlot);
                for (final SlotMetadata toolbar : toolbars.get(category)) {
                    toolbarSlot.add(toolbar);
                }
                categorySlot.add(toolbarSlot);
            }
            categories.add(categorySlot);
        }
        Collections.sort(categories);
        int count = 0;
        for (final CategorySlot categorySlot2 : categories) {
            count += categorySlot2.size();
        }
        if (OptionSlotFactory.generateDocs) {
            ensureDocFile();
            for (final ScrollListPane.ISlot rootSlot : categories) {
                final CategorySlot categorySlot = (CategorySlot)rootSlot;
                if (categorySlot.category == ClientCategory.MiniMap2) {
                    continue;
                }
                doc(categorySlot);
                docTable(true);
                categorySlot.sort();
                for (final SlotMetadata childSlot : categorySlot.getAllChildMetadata()) {
                    doc(childSlot, categorySlot.getCategory() == ClientCategory.Advanced);
                }
                docTable(false);
            }
            endDoc();
        }
        return categories;
    }
    
    protected static void addSlots(final HashMap<Category, List<SlotMetadata>> mergedMap, final Category inheritedCategory, final PropertiesBase properties) {
        final Class<? extends PropertiesBase> propertiesClass = properties.getClass();
        final HashMap<Category, List<SlotMetadata>> slots = buildSlots(null, inheritedCategory, propertiesClass, properties);
        for (final Map.Entry<Category, List<SlotMetadata>> entry : slots.entrySet()) {
            Category category = entry.getKey();
            if (category == Category.Inherit) {
                category = inheritedCategory;
            }
            List<SlotMetadata> slotMetadataList = null;
            if (mergedMap.containsKey(category)) {
                slotMetadataList = mergedMap.get(category);
            }
            else {
                slotMetadataList = new ArrayList<SlotMetadata>();
                mergedMap.put(category, slotMetadataList);
            }
            slotMetadataList.addAll(entry.getValue());
        }
    }
    
    protected static HashMap<Category, List<SlotMetadata>> buildSlots(HashMap<Category, List<SlotMetadata>> map, final Category inheritedCategory, final Class<? extends PropertiesBase> propertiesClass, final PropertiesBase properties) {
        if (map == null) {
            map = new HashMap<Category, List<SlotMetadata>>();
        }
        for (final ConfigField configField : properties.getConfigFields().values()) {
            if (configField.getCategory() == Category.Hidden) {
                continue;
            }
            SlotMetadata slotMetadata = null;
            if (configField instanceof BooleanField) {
                slotMetadata = getBooleanSlotMetadata((BooleanField)configField);
            }
            else if (configField instanceof IntegerField) {
                slotMetadata = getIntegerSlotMetadata((IntegerField)configField);
            }
            else if (configField instanceof StringField) {
                slotMetadata = getStringSlotMetadata((StringField)configField);
            }
            else if (configField instanceof EnumField) {
                slotMetadata = getEnumSlotMetadata((EnumField)configField);
            }
            if (slotMetadata != null) {
                slotMetadata.setOrder(configField.getSortOrder());
                Category category = configField.getCategory();
                if (category == Category.Inherit) {
                    category = inheritedCategory;
                }
                List<SlotMetadata> list = map.get(category);
                if (list == null) {
                    list = new ArrayList<SlotMetadata>();
                    map.put(category, list);
                }
                list.add(slotMetadata);
            }
            else {
                Journeymap.getLogger().warn(String.format("Unable to create config gui for %s in %s", properties.getClass().getSimpleName(), configField));
            }
        }
        return map;
    }
    
    static String getTooltip(final ConfigField configField) {
        final String tooltipKey = configField.getKey() + ".tooltip";
        String tooltip = Constants.getString(tooltipKey);
        if (tooltipKey.equals(tooltip)) {
            tooltip = null;
        }
        return tooltip;
    }
    
    static SlotMetadata<Boolean> getBooleanSlotMetadata(final BooleanField field) {
        final String name = Constants.getString(field.getKey());
        final String tooltip = getTooltip(field);
        final String defaultTip = Constants.getString("jm.config.default", field.getDefaultValue());
        final boolean advanced = field.getCategory() == ClientCategory.Advanced;
        final CheckBox button = new CheckBox(name, field);
        final SlotMetadata<Boolean> slotMetadata = new SlotMetadata<Boolean>(button, name, tooltip, defaultTip, field.getDefaultValue(), advanced);
        slotMetadata.setMasterPropertyForCategory(field.isCategoryMaster());
        if (field.isCategoryMaster()) {
            button.setLabelColors(65535, null, null);
        }
        return slotMetadata;
    }
    
    static SlotMetadata<Integer> getIntegerSlotMetadata(final IntegerField field) {
        final String name = Constants.getString(field.getKey());
        final String tooltip = getTooltip(field);
        final String defaultTip = Constants.getString("jm.config.default_numeric", field.getMinValue(), field.getMaxValue(), field.getDefaultValue());
        final boolean advanced = field.getCategory() == ClientCategory.Advanced;
        final IntSliderButton button = new IntSliderButton(field, name + " : ", "", field.getMinValue(), field.getMaxValue(), true);
        button.setDefaultStyle(false);
        button.setDrawBackground(false);
        final SlotMetadata<Integer> slotMetadata = new SlotMetadata<Integer>(button, name, tooltip, defaultTip, (int)field.getDefaultValue(), advanced);
        return slotMetadata;
    }
    
    static SlotMetadata<String> getStringSlotMetadata(final StringField field) {
        try {
            final String name = Constants.getString(field.getKey());
            final String tooltip = getTooltip(field);
            final boolean advanced = field.getCategory() == ClientCategory.Advanced;
            ListPropertyButton<String> button = null;
            String defaultTip = null;
            if (LocationFormat.IdProvider.class.isAssignableFrom(field.getValuesProviderClass())) {
                button = new LocationFormat.Button(field);
                defaultTip = Constants.getString("jm.config.default", ((LocationFormat.Button)button).getLabel(field.getDefaultValue()));
            }
            else {
                button = new ListPropertyButton<String>(field.getValidValues(), name, field);
                defaultTip = Constants.getString("jm.config.default", field.getDefaultValue());
            }
            button.setDefaultStyle(false);
            button.setDrawBackground(false);
            final SlotMetadata<String> slotMetadata = new SlotMetadata<String>(button, name, tooltip, defaultTip, field.getDefaultValue(), advanced);
            slotMetadata.setValueList(field.getValidValues());
            return slotMetadata;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    static SlotMetadata<Enum> getEnumSlotMetadata(final EnumField field) {
        try {
            final String name = Constants.getString(field.getKey());
            final String tooltip = getTooltip(field);
            final boolean advanced = field.getCategory() == ClientCategory.Advanced;
            final ListPropertyButton<Enum> button = new ListPropertyButton<Enum>(field.getValidValues(), name, field);
            final String defaultTip = Constants.getString("jm.config.default", field.getDefaultValue());
            button.setDefaultStyle(false);
            button.setDrawBackground(false);
            final SlotMetadata<Enum> slotMetadata = new SlotMetadata<Enum>(button, name, tooltip, defaultTip, field.getDefaultValue(), advanced);
            slotMetadata.setValueList(Arrays.asList(field.getValidValues()));
            return slotMetadata;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    static void ensureDocFile() {
        if (OptionSlotFactory.docFile == null) {
            OptionSlotFactory.docFile = new File(Constants.JOURNEYMAP_DIR, "journeymap-options-wiki.txt");
            try {
                if (OptionSlotFactory.docFile.exists()) {
                    OptionSlotFactory.docFile.delete();
                }
                Files.createParentDirs(OptionSlotFactory.docFile);
                (OptionSlotFactory.docWriter = Files.newWriter(OptionSlotFactory.docFile, OptionSlotFactory.UTF8)).append((CharSequence)String.format("<!-- Generated %s -->", new Date()));
                OptionSlotFactory.docWriter.newLine();
                OptionSlotFactory.docWriter.append((CharSequence)"=== Overview ===");
                OptionSlotFactory.docWriter.newLine();
                OptionSlotFactory.docWriter.append((CharSequence)"{{version|5.0.0|page}}");
                OptionSlotFactory.docWriter.newLine();
                OptionSlotFactory.docWriter.append((CharSequence)"This page lists all of the available options which can be configured in-game using the JourneyMap [[Options Manager]].");
                OptionSlotFactory.docWriter.append((CharSequence)"(Note: All of this information can also be obtained from the tooltips within the [[Options Manager]] itself.) <br clear/> <br clear/>");
                OptionSlotFactory.docWriter.newLine();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    static void doc(final CategorySlot categorySlot) {
        try {
            OptionSlotFactory.docWriter.newLine();
            OptionSlotFactory.docWriter.append((CharSequence)String.format("==%s==", categorySlot.getCategory().getName().replace("Preset 1", "Preset (1 and 2)")));
            OptionSlotFactory.docWriter.newLine();
            OptionSlotFactory.docWriter.append((CharSequence)String.format("''%s''", categorySlot.getMetadata().iterator().next().tooltip.replace("Preset 1", "Preset (1 and 2)")));
            OptionSlotFactory.docWriter.newLine();
            OptionSlotFactory.docWriter.newLine();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    static void docTable(final boolean start) {
        try {
            if (start) {
                OptionSlotFactory.docWriter.append((CharSequence)"{| class=\"wikitable\" style=\"cellpadding=\"4\"");
                OptionSlotFactory.docWriter.newLine();
                OptionSlotFactory.docWriter.append((CharSequence)"! scope=\"col\" | Option");
                OptionSlotFactory.docWriter.newLine();
                OptionSlotFactory.docWriter.append((CharSequence)"! scope=\"col\" | Purpose");
                OptionSlotFactory.docWriter.newLine();
                OptionSlotFactory.docWriter.append((CharSequence)"! scope=\"col\" | Range / Default Value");
                OptionSlotFactory.docWriter.newLine();
                OptionSlotFactory.docWriter.append((CharSequence)"|-");
                OptionSlotFactory.docWriter.newLine();
            }
            else {
                OptionSlotFactory.docWriter.append((CharSequence)"|}");
                OptionSlotFactory.docWriter.newLine();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    static void doc(final SlotMetadata slotMetadata, final boolean advanced) {
        try {
            final String color = advanced ? "red" : "black";
            OptionSlotFactory.docWriter.append((CharSequence)String.format("| style=\"text-align:right; white-space: nowrap; font-weight:bold; padding:6px; color:%s\" | %s", color, slotMetadata.getName()));
            OptionSlotFactory.docWriter.newLine();
            OptionSlotFactory.docWriter.append((CharSequence)String.format("| %s ", slotMetadata.tooltip));
            if (slotMetadata.getValueList() != null) {
                OptionSlotFactory.docWriter.append((CharSequence)String.format("<br/><em>Choices available:</em> <code>%s</code>", Joiner.on(", ").join((Iterable)slotMetadata.getValueList())));
            }
            OptionSlotFactory.docWriter.newLine();
            OptionSlotFactory.docWriter.append((CharSequence)String.format("| <code>%s</code>", slotMetadata.range.replace("[", "").replace("]", "").trim()));
            OptionSlotFactory.docWriter.newLine();
            OptionSlotFactory.docWriter.append((CharSequence)"|-");
            OptionSlotFactory.docWriter.newLine();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    static void endDoc() {
        try {
            OptionSlotFactory.docFile = null;
            OptionSlotFactory.docWriter.flush();
            OptionSlotFactory.docWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    static {
        UTF8 = Charset.forName("UTF-8");
        OptionSlotFactory.generateDocs = false;
    }
}
