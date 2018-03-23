package journeymap.client.mod;

import org.apache.logging.log4j.*;
import journeymap.common.*;
import journeymap.client.mod.vanilla.*;
import journeymap.client.mod.impl.*;
import net.minecraftforge.fml.common.*;
import journeymap.common.log.*;
import java.util.*;
import journeymap.client.model.*;

public enum ModBlockDelegate
{
    INSTANCE;
    
    private final Logger logger;
    private final HashMap<String, Class<? extends IModBlockHandler>> handlerClasses;
    private final HashMap<String, IModBlockHandler> handlers;
    private VanillaBlockHandler commonBlockHandler;
    private IBlockColorProxy defaultBlockColorProxy;
    private IBlockSpritesProxy defaultBlockSpritesProxy;
    
    private ModBlockDelegate() {
        this.logger = Journeymap.getLogger();
        this.handlerClasses = new HashMap<String, Class<? extends IModBlockHandler>>();
        this.handlers = new HashMap<String, IModBlockHandler>(10);
        this.reset();
    }
    
    public void reset() {
        this.commonBlockHandler = new VanillaBlockHandler();
        this.defaultBlockColorProxy = new VanillaBlockColorProxy();
        this.defaultBlockSpritesProxy = new VanillaBlockSpriteProxy();
        this.handlerClasses.clear();
        this.handlerClasses.put("BiblioCraft", Bibliocraft.class);
        this.handlerClasses.put("BiomesOPlenty", BiomesOPlenty.class);
        this.handlerClasses.put("terrafirmacraft", TerraFirmaCraft.class);
        this.handlerClasses.put("tfc2", TerraFirmaCraft.class);
        for (final Map.Entry<String, Class<? extends IModBlockHandler>> entry : this.handlerClasses.entrySet()) {
            String modId = entry.getKey();
            final Class<? extends IModBlockHandler> handlerClass = entry.getValue();
            if (Loader.isModLoaded(modId) || Loader.isModLoaded(modId.toLowerCase())) {
                modId = modId.toLowerCase();
                try {
                    this.handlers.put(modId, (IModBlockHandler)handlerClass.newInstance());
                    this.logger.info("Custom modded block handling enabled for " + modId);
                }
                catch (Exception e) {
                    this.logger.error(String.format("Couldn't initialize modded block handler for %s: %s", modId, LogFormatter.toPartialString(e)));
                }
            }
        }
    }
    
    public void initialize(final BlockMD blockMD) {
        if (this.commonBlockHandler == null) {
            this.reset();
        }
        blockMD.setBlockSpritesProxy(this.defaultBlockSpritesProxy);
        blockMD.setBlockColorProxy(this.defaultBlockColorProxy);
        this.initialize(this.commonBlockHandler, blockMD);
        final IModBlockHandler modBlockHandler = this.handlers.get(blockMD.getBlockDomain().toLowerCase());
        if (modBlockHandler != null) {
            modBlockHandler.initialize(blockMD);
        }
        this.commonBlockHandler.postInitialize(blockMD);
    }
    
    private void initialize(final IModBlockHandler handler, final BlockMD blockMD) {
        try {
            handler.initialize(blockMD);
        }
        catch (Throwable t) {
            this.logger.error(String.format("Couldn't initialize IModBlockHandler '%s' for %s: %s", handler.getClass(), blockMD, LogFormatter.toPartialString(t)));
        }
    }
    
    public IModBlockHandler getCommonBlockHandler() {
        return this.commonBlockHandler;
    }
    
    public IBlockSpritesProxy getDefaultBlockSpritesProxy() {
        return this.defaultBlockSpritesProxy;
    }
    
    public IBlockColorProxy getDefaultBlockColorProxy() {
        return this.defaultBlockColorProxy;
    }
}
