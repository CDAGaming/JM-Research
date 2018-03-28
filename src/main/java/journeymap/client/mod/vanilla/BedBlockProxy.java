package journeymap.client.mod.vanilla;

import journeymap.client.cartography.color.RGB;
import journeymap.client.mod.IBlockColorProxy;
import journeymap.client.mod.ModBlockDelegate;
import journeymap.client.model.BlockMD;
import journeymap.client.model.ChunkMD;
import journeymap.client.world.JmBlockAccess;
import net.minecraft.block.BlockBed;
import net.minecraft.block.properties.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.util.math.BlockPos;

public enum BedBlockProxy implements IBlockColorProxy {
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
                final int bedColor = ((TileEntityBed) tileentity).getColor().getColorValue();
                if (blockMD.getBlockState().getValue((IProperty) BlockBed.PART) == BlockBed.EnumPartType.FOOT) {
                    return RGB.multiply(13421772, bedColor);
                }
                return RGB.multiply(16777215, bedColor);
            }
        }
        return ModBlockDelegate.INSTANCE.getDefaultBlockColorProxy().getBlockColor(chunkMD, blockMD, blockPos);
    }
}
