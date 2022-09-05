package xyz.brassgoggledcoders.dailyresources.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FluidResourceStorageBlockEntity extends ResourceStorageBlockEntity {
    public FluidResourceStorageBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
    }
}
