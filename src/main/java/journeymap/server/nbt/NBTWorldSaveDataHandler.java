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
    
    public void func_76184_a(final NBTTagCompound compound) {
        this.data = compound.func_74775_l(this.tagName);
    }
    
    public NBTTagCompound func_189551_b(final NBTTagCompound compound) {
        compound.func_74782_a(this.tagName, (NBTBase)this.data);
        return compound;
    }
    
    public NBTTagCompound getData() {
        return this.data;
    }
}
