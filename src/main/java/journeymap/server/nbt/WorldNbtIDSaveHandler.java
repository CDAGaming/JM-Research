package journeymap.server.nbt;

import net.minecraft.world.*;
import journeymap.server.*;
import net.minecraft.world.storage.*;
import java.util.*;

public class WorldNbtIDSaveHandler
{
    private static final String LEGACY_DAT_FILE = "JourneyMapWorldID";
    private static final String LEGACY_WORLD_ID_KEY = "JourneyMapWorldID";
    private static final String DAT_FILE = "WorldUUID";
    private static final String WORLD_ID_KEY = "world_uuid";
    private NBTWorldSaveDataHandler data;
    private NBTWorldSaveDataHandler legacyData;
    private World world;
    
    public WorldNbtIDSaveHandler() {
        this.world = Constants.SERVER.func_130014_f_();
        this.legacyData = (NBTWorldSaveDataHandler)this.world.getPerWorldStorage().func_75742_a((Class)NBTWorldSaveDataHandler.class, "JourneyMapWorldID");
        this.data = (NBTWorldSaveDataHandler)this.world.getPerWorldStorage().func_75742_a((Class)NBTWorldSaveDataHandler.class, "WorldUUID");
    }
    
    public String getWorldID() {
        return this.getNBTWorldID();
    }
    
    public void setWorldID(final String worldID) {
        this.saveWorldID(worldID);
    }
    
    private String getNBTWorldID() {
        if (this.legacyData != null && this.legacyData.getData().func_74764_b("JourneyMapWorldID")) {
            final String worldId = this.legacyData.getData().func_74779_i("JourneyMapWorldID");
            this.legacyData.getData().func_82580_o("JourneyMapWorldID");
            this.legacyData.func_76185_a();
            this.data = new NBTWorldSaveDataHandler("WorldUUID");
            this.world.getPerWorldStorage().func_75745_a("world_uuid", (WorldSavedData)this.data);
            this.saveWorldID(worldId);
            return worldId;
        }
        if (this.data == null) {
            return this.createNewWorldID();
        }
        if (this.data.getData().func_74764_b("world_uuid")) {
            return this.data.getData().func_74779_i("world_uuid");
        }
        return "noWorldIDFound";
    }
    
    private String createNewWorldID() {
        final String worldID = UUID.randomUUID().toString();
        this.data = new NBTWorldSaveDataHandler("WorldUUID");
        this.world.getPerWorldStorage().func_75745_a("world_uuid", (WorldSavedData)this.data);
        this.saveWorldID(worldID);
        return worldID;
    }
    
    private void saveWorldID(final String worldID) {
        if (this.data != null) {
            this.data.getData().func_74778_a("world_uuid", worldID);
            this.data.func_76185_a();
        }
    }
}
