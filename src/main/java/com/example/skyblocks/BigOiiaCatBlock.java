package com.example.skyblocks;

import com.mojang.serialization.MapCodec;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * The big OIIA cat: what a 3x3x3 cube of OIIA cats turns into. It fills all 27 blocks: each one is a part of this
 * block, with PART_X/Y/Z saying where it sits in the cube. The bottom-centre part (1, 0, 1) is the "master": only it
 * has a block entity, which draws the cat at 3x size (client/OiiaCatRenderer) and holds FACING/POWERED.
 * A redstone signal on any part makes it spin, like the small cat. Breaking any part breaks the whole cat and gives
 * back the 27 small cats.
 */
public class BigOiiaCatBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<BigOiiaCatBlock> CODEC = simpleCodec(BigOiiaCatBlock::new);

    public static final IntegerProperty PART_X = IntegerProperty.create("part_x", 0, 2);
    public static final IntegerProperty PART_Y = IntegerProperty.create("part_y", 0, 2);
    public static final IntegerProperty PART_Z = IntegerProperty.create("part_z", 0, 2);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    /** How many times bigger than the small cat. */
    public static final int SIZE = 3;
    /** Small cats given back when it is broken. */
    public static final int CATS = SIZE * SIZE * SIZE;

    /** Each part's share of the small cat's shape (3..13 x 0..13 x 3..13 pixels), scaled 3x over the cube. */
    private static final VoxelShape[] SHAPES = new VoxelShape[CATS];
    /** The whole big cat's shape seen from each part, so the selection outline shows the whole cat. */
    private static final VoxelShape[] OUTLINES = new VoxelShape[CATS];

    static {
        double lo = 3 * SIZE / 16.0, hiXZ = 13 * SIZE / 16.0, hiY = 13 * SIZE / 16.0; // in blocks, inside the cube
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    double x0 = Math.max(0, lo - x), x1 = Math.min(1, hiXZ - x);
                    double y0 = Math.max(0, -y), y1 = Math.min(1, hiY - y);
                    double z0 = Math.max(0, lo - z), z1 = Math.min(1, hiXZ - z);
                    SHAPES[index(x, y, z)] = x1 > x0 && y1 > y0 && z1 > z0
                            ? Block.box(x0 * 16, y0 * 16, z0 * 16, x1 * 16, y1 * 16, z1 * 16)
                            : Shapes.empty();
                    OUTLINES[index(x, y, z)] = Block.box((lo - x) * 16, -y * 16, (lo - z) * 16,
                            (hiXZ - x) * 16, (hiY - y) * 16, (hiXZ - z) * 16);
                }
            }
        }
    }

    /** True while this class itself removes parts, so their removal doesn't start another break. */
    private static boolean dismantling;

    public BigOiiaCatBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false)
                .setValue(PART_X, 0).setValue(PART_Y, 0).setValue(PART_Z, 0));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, PART_X, PART_Y, PART_Z);
    }

    private static int index(int x, int y, int z) {
        return (x * SIZE + y) * SIZE + z;
    }

    static boolean isMaster(BlockState state) {
        return state.getValue(PART_X) == 1 && state.getValue(PART_Y) == 0 && state.getValue(PART_Z) == 1;
    }

    /** The cube's lowest north-west corner. */
    static BlockPos origin(BlockPos pos, BlockState state) {
        return pos.offset(-state.getValue(PART_X), -state.getValue(PART_Y), -state.getValue(PART_Z));
    }

    static BlockPos master(BlockPos origin) {
        return origin.offset(1, 0, 1);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isMaster(state) ? new BigOiiaCatBlockEntity(pos, state) : null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return isMaster(state) ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.INVISIBLE;
    }

    /** Outline / aiming: the whole cat. */
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINES[index(state.getValue(PART_X), state.getValue(PART_Y), state.getValue(PART_Z))];
    }

    /** Collision: only this part's own share of the cat, inside its block. */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[index(state.getValue(PART_X), state.getValue(PART_Y), state.getValue(PART_Z))];
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(ModItems.OIIA_CAT.get());
    }

    /** Drops are handled below (all 27 cats once, not one per part). */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return Collections.emptyList();
    }

    // ---- forming ----

    /**
     * Called when a small cat is placed: if it completes a 3x3x3 cube of small cats, they become one big cat,
     * facing the same way as the cat just placed (so, the player).
     */
    public static void tryForm(Level level, BlockPos placed) {
        BlockState placedState = level.getBlockState(placed);
        if (!placedState.is(ModBlocks.OIIA_CAT.get())) {
            return;
        }
        for (int ox = 0; ox < SIZE; ox++) {
            for (int oy = 0; oy < SIZE; oy++) {
                for (int oz = 0; oz < SIZE; oz++) {
                    BlockPos origin = placed.offset(-ox, -oy, -oz);
                    if (allSmallCats(level, origin)) {
                        form(level, origin, placedState.getValue(FACING));
                        return;
                    }
                }
            }
        }
    }

    private static boolean allSmallCats(Level level, BlockPos origin) {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    if (!level.getBlockState(origin.offset(x, y, z)).is(ModBlocks.OIIA_CAT.get())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void form(Level level, BlockPos origin, Direction facing) {
        BlockState base = ModBlocks.BIG_OIIA_CAT.get().defaultBlockState().setValue(FACING, facing);
        // Replace all 27 without neighbour updates first, so no part sees a half-built cube...
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    BlockState part = base.setValue(PART_X, x).setValue(PART_Y, y).setValue(PART_Z, z);
                    level.setBlock(origin.offset(x, y, z), part, Block.UPDATE_CLIENTS);
                }
            }
        }
        // ...then tell the neighbours, and pick up any redstone signal already there.
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    level.updateNeighborsAt(origin.offset(x, y, z), ModBlocks.BIG_OIIA_CAT.get());
                }
            }
        }
        updatePower(level, origin);

        double cx = origin.getX() + SIZE / 2.0, cy = origin.getY() + SIZE / 2.0, cz = origin.getZ() + SIZE / 2.0;
        level.playSound(null, master(origin), SoundEvents.CAT_AMBIENT, SoundSource.BLOCKS, 2.0F, 0.5F);
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.POOF, cx, cy, cz, 80, 1.2, 1.2, 1.2, 0.02);
        }
    }

    // ---- redstone ----

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide) {
            updatePower(level, origin(pos, state));
        }
    }

    /** POWERED (on the master) = any part gets a redstone signal. */
    private static void updatePower(Level level, BlockPos origin) {
        boolean powered = false;
        for (int x = 0; x < SIZE && !powered; x++) {
            for (int y = 0; y < SIZE && !powered; y++) {
                for (int z = 0; z < SIZE && !powered; z++) {
                    powered = level.hasNeighborSignal(origin.offset(x, y, z));
                }
            }
        }
        BlockPos master = master(origin);
        BlockState state = level.getBlockState(master);
        if (state.is(ModBlocks.BIG_OIIA_CAT.get()) && isMaster(state) && state.getValue(POWERED) != powered) {
            level.setBlock(master, state.setValue(POWERED, powered), Block.UPDATE_CLIENTS);
        }
    }

    // ---- breaking ----

    /** A player breaks a part: remove the rest, and give back the 27 cats unless creative or the wrong tool. */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            boolean drop = !player.isCreative() && player.hasCorrectToolForDrops(state);
            dismantle(level, pos, state, drop);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /** Any other removal (explosion, command, ...): the same, with the cats dropped. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !dismantling && !newState.is(this)) {
            dismantle(level, pos, state, true);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /**
     * Removes the cube's other parts. The cats are dropped only if the cube was still whole, so a cube already
     * taken apart (for example by the player break just before this part's own removal) drops nothing more.
     */
    private static void dismantle(Level level, BlockPos pos, BlockState state, boolean drop) {
        BlockPos origin = origin(pos, state);
        boolean whole = true;
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    BlockState other = level.getBlockState(origin.offset(x, y, z));
                    if (!isPart(other, x, y, z)) {
                        whole = false;
                    }
                }
            }
        }
        dismantling = true;
        try {
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    for (int z = 0; z < SIZE; z++) {
                        BlockPos p = origin.offset(x, y, z);
                        if (!p.equals(pos) && isPart(level.getBlockState(p), x, y, z)) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        }
                    }
                }
            }
        } finally {
            dismantling = false;
        }
        if (whole && drop) {
            popResource(level, master(origin), new ItemStack(ModItems.OIIA_CAT.get(), CATS));
        }
    }

    private static boolean isPart(BlockState state, int x, int y, int z) {
        return state.is(ModBlocks.BIG_OIIA_CAT.get())
                && state.getValue(PART_X) == x && state.getValue(PART_Y) == y && state.getValue(PART_Z) == z;
    }
}
