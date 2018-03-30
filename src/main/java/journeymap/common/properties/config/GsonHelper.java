package journeymap.common.properties.config;

import java.lang.reflect.*;
import journeymap.common.version.*;
import journeymap.client.model.*;
import journeymap.common.api.feature.*;
import journeymap.common.*;
import java.util.*;
import journeymap.common.properties.*;
import com.google.common.base.*;
import journeymap.client.cartography.color.*;
import java.awt.*;
import net.minecraft.world.*;
import journeymap.common.feature.*;
import com.google.common.collect.*;
import com.google.gson.*;

public abstract class GsonHelper<T extends ConfigField>
{
    protected final boolean verbose;
    
    public static final GsonBuilder BUILDER_COMPACT() {
        return getGsonBuilder(false, false);
    }
    
    public static final GsonBuilder BUILDER_TERSE() {
        return getGsonBuilder(false, true);
    }
    
    public static final GsonBuilder BUILDER_VERBOSE() {
        return getGsonBuilder(true, true);
    }
    
    private static GsonBuilder getGsonBuilder(final boolean verbose, final boolean pretty) {
        final FeatureSerializer fs = new FeatureSerializer();
        final GsonBuilder gb = new GsonBuilder().serializeNulls().registerTypeAdapter((Type)BooleanField.class, (Object)new BooleanFieldSerializer(verbose)).registerTypeAdapter((Type)IntegerField.class, (Object)new IntegerFieldSerializer(verbose)).registerTypeAdapter((Type)StringField.class, (Object)new StringFieldSerializer(verbose)).registerTypeAdapter((Type)EnumField.class, (Object)new EnumFieldSerializer(verbose)).registerTypeAdapter((Type)CategorySet.class, (Object)new CategorySetSerializer(verbose)).registerTypeAdapter((Type)Version.class, (Object)new VersionSerializer(verbose)).registerTypeAdapter((Type)GridSpec.class, (Object)new GridSpecSerializer(verbose)).registerTypeAdapter((Type)Feature.Action.class, (Object)fs).registerTypeAdapter((Type)Feature.Display.class, (Object)fs).registerTypeAdapter((Type)Feature.Radar.class, (Object)fs).registerTypeAdapter((Type)Feature.MapType.class, (Object)fs).registerTypeAdapter((Type)Feature.class, (Object)fs).registerTypeAdapter((Type)DimensionPolicies.class, (Object)new DimensionPoliciesSerializer(verbose)).registerTypeAdapter((Type)PolicyTable.class, (Object)new PolicyTableSerializer(verbose));
        if (pretty) {
            gb.setPrettyPrinting();
        }
        return gb;
    }
    
    public GsonHelper(final Boolean verbose) {
        this.verbose = verbose;
    }
    
    public JsonElement serializeAttributes(final ConfigField<?> src, final Type typeOfSrc, final JsonSerializationContext context) {
        if (!this.verbose) {
            return context.serialize((Object)src.getStringAttr("value"));
        }
        final JsonObject jsonObject = new JsonObject();
        for (final String attrName : src.getAttributeNames()) {
            jsonObject.addProperty(attrName, src.getStringAttr(attrName));
        }
        return (JsonElement)jsonObject;
    }
    
    protected T deserializeAttributes(final T result, final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        if (!this.verbose || !json.isJsonObject()) {
            result.put("value", json.getAsString());
        }
        else {
            final Set<Map.Entry<String, JsonElement>> set = (Set<Map.Entry<String, JsonElement>>)json.getAsJsonObject().entrySet();
            for (final Map.Entry<String, JsonElement> entry : set) {
                try {
                    result.put(entry.getKey(), entry.getValue().getAsString());
                }
                catch (Throwable t) {
                    Journeymap.getLogger().warn("Error deserializing %s in %s: %s", (Object)entry, (Object)json, (Object)t);
                }
            }
        }
        return result;
    }
    
    public static class CategorySetSerializer implements JsonSerializer<CategorySet>, JsonDeserializer<CategorySet>
    {
        protected final boolean verbose;
        
        public CategorySetSerializer(final boolean verbose) {
            this.verbose = verbose;
        }
        
        public JsonElement serialize(final CategorySet src, final Type typeOfSrc, final JsonSerializationContext context) {
            if (!this.verbose) {
                return null;
            }
            final Category[] array = new Category[src.size()];
            return context.serialize((Object)src.toArray(array));
        }
        
        public CategorySet deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            final CategorySet categorySet = new CategorySet();
            if (this.verbose) {
                final JsonArray jsonArray = json.getAsJsonArray();
                for (final JsonElement jsonElement : jsonArray) {
                    categorySet.add((Category)context.deserialize(jsonElement, (Type)Category.class));
                }
            }
            return categorySet;
        }
    }
    
    public static class VersionSerializer implements JsonSerializer<Version>, JsonDeserializer<Version>
    {
        public VersionSerializer(final boolean verbose) {
        }
        
        public JsonElement serialize(final Version src, final Type typeOfSrc, final JsonSerializationContext context) {
            return context.serialize((Object)src.toString());
        }
        
        public Version deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonObject()) {
                final JsonObject jo = json.getAsJsonObject();
                return Version.from(jo.get("major").getAsString(), jo.get("minor").getAsString(), jo.get("micro").getAsString(), jo.get("patch").getAsString(), Journeymap.JM_VERSION);
            }
            return Version.from(json.getAsString(), Journeymap.JM_VERSION);
        }
    }
    
    public static class GridSpecSerializer implements JsonSerializer<GridSpec>, JsonDeserializer<GridSpec>
    {
        public GridSpecSerializer(final boolean verbose) {
        }
        
        public JsonElement serialize(final GridSpec src, final Type typeOfSrc, final JsonSerializationContext context) {
            final String string = Joiner.on(",").join((Object)src.style, (Object)RGB.toHexString(src.getColor()), new Object[] { src.alpha, src.getColorX(), src.getColorY() });
            return context.serialize((Object)string);
        }
        
        public GridSpec deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonObject()) {
                final JsonObject jo = json.getAsJsonObject();
                final GridSpec gridSpec = new GridSpec(Enum.valueOf(GridSpec.Style.class, jo.get("style").getAsString()), jo.get("red").getAsFloat(), jo.get("green").getAsFloat(), jo.get("blue").getAsFloat(), jo.get("alpha").getAsFloat());
                gridSpec.setColorCoords(jo.get("colorX").getAsInt(), jo.get("colorY").getAsInt());
                return gridSpec;
            }
            final String[] parts = json.getAsString().split(",");
            final GridSpec gridSpec = new GridSpec(Enum.valueOf(GridSpec.Style.class, parts[0]), new Color(RGB.hexToInt(parts[1])), Float.parseFloat(parts[2]));
            gridSpec.setColorCoords(Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
            return gridSpec;
        }
    }
    
    public static class BooleanFieldSerializer extends GsonHelper<BooleanField> implements JsonSerializer<BooleanField>, JsonDeserializer<BooleanField>
    {
        public BooleanFieldSerializer(final boolean verbose) {
            super(verbose);
        }
        
        public JsonElement serialize(final BooleanField src, final Type typeOfSrc, final JsonSerializationContext context) {
            return this.serializeAttributes(src, typeOfSrc, context);
        }
        
        public BooleanField deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            return this.deserializeAttributes(new BooleanField(), json, typeOfT, context);
        }
    }
    
    public static class IntegerFieldSerializer extends GsonHelper<IntegerField> implements JsonSerializer<IntegerField>, JsonDeserializer<IntegerField>
    {
        public IntegerFieldSerializer(final boolean verbose) {
            super(verbose);
        }
        
        public JsonElement serialize(final IntegerField src, final Type typeOfSrc, final JsonSerializationContext context) {
            return this.serializeAttributes(src, typeOfSrc, context);
        }
        
        public IntegerField deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            return this.deserializeAttributes(new IntegerField(), json, typeOfT, context);
        }
    }
    
    public static class StringFieldSerializer extends GsonHelper<StringField> implements JsonSerializer<StringField>, JsonDeserializer<StringField>
    {
        public StringFieldSerializer(final boolean verbose) {
            super(verbose);
        }
        
        public JsonElement serialize(final StringField src, final Type typeOfSrc, final JsonSerializationContext context) {
            return this.serializeAttributes(src, typeOfSrc, context);
        }
        
        public StringField deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            return this.deserializeAttributes(new StringField(), json, typeOfT, context);
        }
    }
    
    public static class EnumFieldSerializer extends GsonHelper<EnumField> implements JsonSerializer<EnumField>, JsonDeserializer<EnumField>
    {
        public EnumFieldSerializer(final boolean verbose) {
            super(verbose);
        }
        
        public JsonElement serialize(final EnumField src, final Type typeOfSrc, final JsonSerializationContext context) {
            return this.serializeAttributes(src, typeOfSrc, context);
        }
        
        public EnumField deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            return this.deserializeAttributes(new EnumField(), json, typeOfT, context);
        }
    }
    
    public static class FeatureSerializer implements JsonSerializer<Enum<? extends Feature>>, JsonDeserializer<Feature>
    {
        private final String BASE;
        
        public FeatureSerializer() {
            this.BASE = Feature.class.getCanonicalName() + "$";
        }
        
        public JsonElement serialize(final Enum<? extends Feature> src, final Type typeOfSrc, final JsonSerializationContext context) {
            return context.serialize((Object)String.format("%s.%s", ((Class)typeOfSrc).getSimpleName(), src.name()));
        }
        
        public Feature deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            try {
                final String prim = json.getAsString();
                final int idx = prim.lastIndexOf(46);
                final String className = this.BASE + prim.substring(0, idx);
                final String name = prim.substring(idx + 1);
                final Class<? extends Enum> klass = (Class<? extends Enum>)Class.forName(className);
                return Enum.valueOf((Class<Feature>)klass, name);
            }
            catch (Exception e) {
                throw new JsonParseException("Couldn't parse: " + json + ": " + e.getMessage());
            }
        }
    }
    
    public static class PolicyTableSerializer implements JsonSerializer<PolicyTable>, JsonDeserializer<PolicyTable>
    {
        protected final boolean verbose;
        
        public PolicyTableSerializer(final boolean verbose) {
            this.verbose = verbose;
        }
        
        public JsonElement serialize(final PolicyTable src, final Type typeOfSrc, final JsonSerializationContext context) {
            final HashBasedTable<Feature, GameType, Policy> policies = src.getTable();
            final JsonObject jsonTable = new JsonObject();
            for (final Feature feature : PlayerFeatures.ALL_FEATURES) {
                if (!policies.containsRow((Object)feature)) {
                    continue;
                }
                final String featureString = context.serialize((Object)feature).getAsString();
                final JsonElement jsonPolicies = (JsonElement)(this.verbose ? new JsonArray() : new JsonObject());
                jsonTable.add(featureString, jsonPolicies);
                for (final GameType gameType : PlayerFeatures.VALID_GAME_TYPES) {
                    if (!policies.contains((Object)feature, (Object)gameType)) {
                        continue;
                    }
                    final Policy policy = (Policy)policies.get((Object)feature, (Object)gameType);
                    if (this.verbose) {
                        jsonPolicies.getAsJsonArray().add(context.serialize((Object)policy));
                    }
                    else {
                        jsonPolicies.getAsJsonObject().addProperty(gameType.func_77149_b(), policy.isAllowed());
                    }
                }
            }
            return (JsonElement)jsonTable;
        }
        
        public PolicyTable deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            final JsonObject jsonTable = json.getAsJsonObject();
            final PolicyTable table = new PolicyTable();
            for (final Map.Entry<String, JsonElement> jsonFeatureRow : jsonTable.entrySet()) {
                final Feature feature = (Feature)context.deserialize((JsonElement)new JsonPrimitive((String)jsonFeatureRow.getKey()), (Type)Feature.class);
                final JsonElement value = jsonFeatureRow.getValue();
                if (value.isJsonArray()) {
                    final JsonArray jsonPolicies = value.getAsJsonArray();
                    for (final JsonElement jsonPolicy : jsonPolicies) {
                        final Policy policy = (Policy)context.deserialize(jsonPolicy, (Type)Policy.class);
                        table.getTable().put((Object)feature, (Object)policy.getGameType(), (Object)policy);
                    }
                }
                else {
                    final JsonObject jsonPolicies2 = value.getAsJsonObject();
                    final String origin = "jm.common.server_config";
                    for (final Map.Entry<String, JsonElement> entry : jsonPolicies2.entrySet()) {
                        final GameType gameType = GameType.func_77142_a((String)entry.getKey());
                        if (PlayerFeatures.VALID_GAME_TYPES.contains(gameType)) {
                            final Policy policy2 = Policy.update(origin, gameType, feature, entry.getValue().getAsBoolean());
                            table.getTable().put((Object)feature, (Object)policy2.getGameType(), (Object)policy2);
                        }
                    }
                }
            }
            return table;
        }
    }
    
    public static class DimensionPoliciesSerializer implements JsonSerializer<DimensionPolicies>, JsonDeserializer<DimensionPolicies>
    {
        protected final boolean verbose;
        
        public DimensionPoliciesSerializer(final boolean verbose) {
            this.verbose = verbose;
        }
        
        public JsonElement serialize(final DimensionPolicies src, final Type typeOfSrc, final JsonSerializationContext context) {
            final JsonObject result = new JsonObject();
            result.addProperty("dimension", (Number)src.getDimension());
            result.add("policies", context.serialize((Object)src, (Type)PolicyTable.class));
            return (JsonElement)result;
        }
        
        public DimensionPolicies deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            final JsonObject result = json.getAsJsonObject();
            final int dimension = result.get("dimension").getAsInt();
            final PolicyTable table = (PolicyTable)context.deserialize(result.get("policies"), (Type)PolicyTable.class);
            final DimensionPolicies dimPolicies = new DimensionPolicies(dimension);
            dimPolicies.update(table);
            return dimPolicies;
        }
    }
}
