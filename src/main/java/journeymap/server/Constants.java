package journeymap.server;

import net.minecraft.server.*;
import com.google.common.base.*;
import java.io.*;
import net.minecraftforge.fml.common.*;
import journeymap.common.*;

public class Constants
{
    public static MinecraftServer SERVER;
    private static final Joiner path;
    private static final String END;
    public static final File MC_DATA_DIR;
    public static String JOURNEYMAP_DIR;
    public static String CONFIG_DIR;
    
    static {
        Constants.SERVER = FMLCommonHandler.instance().getMinecraftServerInstance();
        path = Joiner.on(File.separator).useForNull("");
        END = null;
        MC_DATA_DIR = Constants.SERVER.func_71238_n();
        Constants.JOURNEYMAP_DIR = "journeymap";
        Constants.CONFIG_DIR = Constants.path.join((Object)Constants.MC_DATA_DIR, (Object)Constants.JOURNEYMAP_DIR, new Object[] { "server", Journeymap.JM_VERSION.toMajorMinorString(), Constants.END });
    }
}
