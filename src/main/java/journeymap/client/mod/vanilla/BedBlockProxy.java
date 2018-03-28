package journeymap.client.mod.vanilla;

import journeymap.client.mod.*;
import journeymap.client.model.*;
import net.minecraft.util.math.*;
import net.minecraft.block.*;
import journeymap.client.world.*;
import net.minecraft.block.properties.*;
import journeymap.client.cartography.color.*;
import net.minecraft.tileentity.*;

public enum BedBlockProxy implements IBlockColorProxy
{
    INSTANCE;
    
    @Override
    public int deriveBlockColor(final BlockMD blockMD) {
        return ModBlockDelegate.INSTANCE.getDefaultBlockColorProxy().deriveBlockColor(blockMD);
    }
    
    @Override
    public int getBlockColor(final ChunkMD chunkMD, final BlockMD blockMD, final BlockPos blockPos) {
        if (blockMD.getBlock() instanceof BlockBed) {
            final TileEntity tileentity = JmBlockAccess.INSTANCE.getTileEntity(blockPos);
            if (tileentity instanceof TileEntityBed) {
                final int bedColor = ((TileEntityBed)tileentity).getColor().getColorValue();
                if (blockMD.getBlockState().getValue((IProperty)BlockBed.PART) == BlockBed.EnumPartType.FOOT) {
                    return RGB.multiply(13421772, bedColor);
                }
                return RGB.multiply(16777215, bedColor);
            }
        }
        return ModBlockDelegate.INSTANCE.getDefaultBlockColorProxy().getBlockColor(chunkMD, blockMD, blockPos);
    }
}
