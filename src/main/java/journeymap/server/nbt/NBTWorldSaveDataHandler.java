package journeymap.server.nbt;

import net.minecraft.world.storage.*;
import net.minecraft.nbt.*;

public class NBTWorldSaveDataHandler extends WorldSavedData
{
    private NBTTagCompound data;
    private String tagName;
    
    public NBTWorldSaveDataHandler(final String tagName) {
        super(tagName);
        this.data = new NBTTagCompound();
        this.tagName = tagName;
    }
    
    public void readFromNBT(final NBTTagCompound compound) {
        this.data = compound.getCompoundTag(this.tagName);
    }
    
    public NBTTagCompound writeToNBT(final NBTTagCompound compound) {
        compound.setTag(this.tagName, (NBTBase)this.data);
        return compound;
    }
    
    public NBTTagCompound getData() {
        return this.data;
    }
}
