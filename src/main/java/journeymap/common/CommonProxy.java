package journeymap.common;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;

public interface CommonProxy {
    void preInitialize(final FMLPreInitializationEvent p0) throws Throwable;

    void initialize(final FMLInitializationEvent p0) throws Throwable;

    void postInitialize(final FMLPostInitializationEvent p0) throws Throwable;

    boolean checkModLists(final Map<String, String> p0, final Side p1);

    boolean isUpdateCheckEnabled();

    void handleWorldIdMessage(final String p0, final EntityPlayerMP p1);
}
