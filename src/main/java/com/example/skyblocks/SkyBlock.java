package com.example.skyblocks;

import com.mojang.serialization.MapCodec;
import com.noodlegamer76.shadered.entity.block.SkyblockHolderEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * A Shadered+ skyblock. Like Shadered's own skyblocks it has a block entity, and its renderer hands the
 * block to Shadered's skyblock renderer, which draws the sky (see client/ShaderedSkies).
 */
public class SkyBlock extends Block implements EntityBlock {
    public static final MapCodec<SkyBlock> CODEC = simpleCodec(p -> new SkyBlock(p, 0));

    private final int skyIndex;

    public SkyBlock(Properties properties, int skyIndex) {
        super(properties);
        this.skyIndex = skyIndex;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    /** Which of our skies this block shows (position in ModBlocks.SKY_NAMES). */
    public int getSkyIndex() {
        return skyIndex;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SkyBlockEntity(pos, state);
    }

    /** Same as Shadered: a skyblock filter held in the other hand while placing applies that filter. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide || placer == null
                || !(level.getBlockEntity(pos) instanceof SkyblockHolderEntity entity)) {
            return;
        }
        InteractionHand hand = placer.getMainHandItem() == stack ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        entity.onPlaced(placer, placer.getItemInHand(hand));
    }
}
