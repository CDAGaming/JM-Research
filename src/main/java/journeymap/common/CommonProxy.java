package journeymap.common;

import net.minecraftforge.fml.common.event.*;
import java.util.*;
import net.minecraftforge.fml.relauncher.*;
import net.minecraft.entity.player.*;

public interface CommonProxy
{
    void preInitialize(final FMLPreInitializationEvent p0) throws Throwable;
    
    void initialize(final FMLInitializationEvent p0) throws Throwable;
    
    void postInitialize(final FMLPostInitializationEvent p0) throws Throwable;
    
    boolean checkModLists(final Map<String, String> p0, final Side p1);
    
    boolean isUpdateCheckEnabled();
    
    void handleWorldIdMessage(final String p0, final EntityPlayerMP p1);
}
