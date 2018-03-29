package journeymap.server;

import com.google.common.base.Joiner;
import journeymap.common.Journeymap;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;

public class Constants {
    public static final File MC_DATA_DIR;
    private static final Joiner path;
    private static final String END;
    public static MinecraftServer SERVER;
    public static String JOURNEYMAP_DIR;
    public static String CONFIG_DIR;

    static {
        Constants.SERVER = FMLCommonHandler.instance().getMinecraftServerInstance();
        path = Joiner.on(File.separator).useForNull("");
        END = null;
        MC_DATA_DIR = Constants.SERVER.getDataDirectory();
        Constants.JOURNEYMAP_DIR = "journeymap";
        Constants.CONFIG_DIR = Constants.path.join(Constants.MC_DATA_DIR, Constants.JOURNEYMAP_DIR, "server", Journeymap.JM_VERSION.toMajorMinorString(), Constants.END);
    }
}
