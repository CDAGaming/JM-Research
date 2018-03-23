package journeymap.common.properties.config;

import java.lang.reflect.*;
import journeymap.common.*;
import java.util.*;
import journeymap.common.properties.*;
import com.google.gson.*;
import journeymap.common.version.*;
import journeymap.client.model.*;
import com.google.common.base.*;
import journeymap.client.cartography.color.*;
import java.awt.*;

public abstract class GsonHelper<T extends ConfigField>
{
    protected final boolean verbose;
    
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
}
