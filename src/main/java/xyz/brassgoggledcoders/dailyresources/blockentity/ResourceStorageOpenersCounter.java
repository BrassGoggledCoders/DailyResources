package xyz.brassgoggledcoders.dailyresources.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.dailyresources.menu.ItemResourceStorageMenu;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

public class ResourceStorageOpenersCounter extends ContainerOpenersCounter {
    private final UUID uniqueId;

    public ResourceStorageOpenersCounter(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void onOpen(Level pLevel, BlockPos pPos, BlockState pState) {
        this.playSound(pState, pPos, pLevel, SoundEvents.BARREL_OPEN);
        if (pState.hasProperty(BlockStateProperties.OPEN)) {
            pLevel.setBlock(pPos, pState.setValue(BlockStateProperties.OPEN, true), Block.UPDATE_ALL);
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void onClose(Level pLevel, BlockPos pPos, BlockState pState) {
        this.playSound(pState, pPos, pLevel, SoundEvents.BARREL_CLOSE);
        if (pState.hasProperty(BlockStateProperties.OPEN)) {
            pLevel.setBlock(pPos, pState.setValue(BlockStateProperties.OPEN, false), Block.UPDATE_ALL);
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void openerCountChanged(Level pLevel, BlockPos pPos, BlockState pState, int pPriorCount, int pOpenCount) {

    }

    @Override
    protected boolean isOwnContainer(@NotNull Player player) {
        if (player.containerMenu instanceof ItemResourceStorageMenu menu) {
            return menu.getUniqueId() == this.uniqueId;
        }

        return false;
    }

    private void playSound(BlockState pState, BlockPos pPos, Level pLevel, SoundEvent pSound) {
        Vec3i vec3i;
        if (pState.hasProperty(BlockStateProperties.FACING)) {
            vec3i = pState.getValue(BlockStateProperties.FACING).getNormal();
        } else {
            vec3i = new Vec3i(0, 0, 0);
        }
        double d0 = (double) pPos.getX() + 0.5D + (double) vec3i.getX() / 2.0D;
        double d1 = (double) pPos.getY() + 0.5D + (double) vec3i.getY() / 2.0D;
        double d2 = (double) pPos.getZ() + 0.5D + (double) vec3i.getZ() / 2.0D;
        pLevel.playSound(null, d0, d1, d2, pSound, SoundSource.BLOCKS, 0.5F, pLevel.random.nextFloat() * 0.1F + 0.9F);
    }
}
