package com.drppp.gt6addition.common.cover;

import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.common.covers.CoverDrain;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Loader;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * GT6-style drain behavior adapted to GTCEu's Cover API.
 * It collects fluid sources in front, rain, XP orbs, and fluids from entities
 * that pass over an upward-facing drain cover.
 */
public class CoverGT6Drain extends CoverDrain {

    private static final int WORLD_FLUID_INTERVAL = 20;
    private static final int RAIN_INTERVAL = 100;
    private static final int XP_ORB_INTERVAL = 100;
    private static final Set<String> RIVER_AND_LAKE_BIOMES = new HashSet<>(Arrays.asList(
            "River", "Frozen River", "Lush River", "Estuary", "Twilight Stream", "Tropical River",
            "Riparian Zone", "Sandstone Canyon", "Sandstone Canyon 2", "Creek Bed", "rwg_riverIce",
            "Ice River", "rwg_riverCold", "Cold River", "rwg_riverTemperate", "Temperate River",
            "rwg_riverHot", "Hot River", "rwg_riverWet", "Wet River", "rwg_riverOasis", "River Oasis",
            "Tropical Lake", "Twilight Lake", "Lake", "Oasis", "Woodland Lake", "Woodland Lake Edge"));

    public CoverGT6Drain(CoverDefinition definition, CoverableView coverableView, EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide, Fluid.BUCKET_VOLUME);
    }

    @Override
    public void update() {
        World world = getWorld();
        if (world == null || world.isRemote || !getCoverableView().isValid()) {
            return;
        }

        long timer = getOffsetTimer();
        if (timer % WORLD_FLUID_INTERVAL == 5) {
            IFluidHandler handler = getFluidHandler();
            if (handler != null) {
                drainBlockInFront(handler);
                collectEntityFluids(handler);
            }
        }
        if (timer % 5 == 0 && getAttachedSide() == EnumFacing.UP) {
            IFluidHandler handler = getFluidHandler();
            if (handler != null) {
                collectPlayerExperience(handler);
            }
        }
        if (timer % RAIN_INTERVAL == 10) {
            collectRainwater();
        }
        if (timer % XP_ORB_INTERVAL == 50) {
            collectExperienceOrbs();
        }
    }

    private IFluidHandler getFluidHandler() {
        return getCoverableView().getCapability(
                CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, getAttachedSide());
    }

    private void drainBlockInFront(IFluidHandler handler) {
        World world = getWorld();
        BlockPos sourcePos = getPos().offset(getAttachedSide());
        IBlockState state = world.getBlockState(sourcePos);
        Block block = state.getBlock();

        if (block instanceof IFluidBlock) {
            IFluidBlock fluidBlock = (IFluidBlock) block;
            FluidStack preview = fluidBlock.drain(world, sourcePos, false);
            if (preview == null || preview.amount <= 0 || !canDrainTowardAttachedSide(preview)) {
                return;
            }
            if (fillCompletely(handler, preview)) {
                fluidBlock.drain(world, sourcePos, true);
            }
            return;
        }

        FluidStack source = null;
        boolean infiniteWater = false;
        if ((block == Blocks.WATER || block == Blocks.FLOWING_WATER) &&
                state.getValue(BlockLiquid.LEVEL) == 0) {
            infiniteWater = isInfiniteWaterBiome(sourcePos);
            source = new FluidStack(FluidRegistry.WATER,
                    infiniteWater ? 16 * Fluid.BUCKET_VOLUME : Fluid.BUCKET_VOLUME);
        } else if ((block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) &&
                state.getValue(BlockLiquid.LEVEL) == 0) {
            source = new FluidStack(FluidRegistry.LAVA, Fluid.BUCKET_VOLUME);
        }

        if (source == null || !canDrainTowardAttachedSide(source)) {
            return;
        }
        if (infiniteWater) {
            handler.fill(source, true);
        } else if (fillCompletely(handler, source)) {
            world.setBlockToAir(sourcePos);
        }
    }

    private boolean isInfiniteWaterBiome(BlockPos pos) {
        World world = getWorld();
        Biome biome = world.getBiome(pos);
        int waterLevel = world.provider.getDimension() == 0 ? 62 :
                (world.provider.hasSkyLight() ? 62 : 31);
        return pos.getY() >= waterLevel - 15 && pos.getY() <= waterLevel &&
                (BiomeDictionary.hasType(biome, BiomeDictionary.Type.RIVER) ||
                        RIVER_AND_LAKE_BIOMES.contains(biome.getBiomeName()));
    }

    private boolean canDrainTowardAttachedSide(FluidStack fluid) {
        EnumFacing side = getAttachedSide();
        if (side.getAxis().isHorizontal() || fluid.getFluid().isGaseous()) {
            return true;
        }
        return fluid.getFluid().getDensity() < 0 ? side == EnumFacing.UP : side == EnumFacing.DOWN;
    }

    private boolean fillCompletely(IFluidHandler handler, FluidStack fluid) {
        if (handler.fill(fluid.copy(), false) < fluid.amount) {
            return false;
        }
        return handler.fill(fluid.copy(), true) >= fluid.amount;
    }

    private void collectRainwater() {
        EnumFacing side = getAttachedSide();
        World world = getWorld();
        if (side == EnumFacing.DOWN) {
            return;
        }

        BlockPos frontPos = getPos().offset(side);
        IBlockState frontState = world.getBlockState(frontPos);
        Block frontBlock = frontState.getBlock();
        if (frontBlock instanceof BlockLiquid || frontBlock instanceof IFluidBlock ||
                frontState.isSideSolid(world, frontPos, side.getOpposite()) ||
                frontState.isSideSolid(world, frontPos, EnumFacing.UP) ||
                !world.isRainingAt(frontPos)) {
            return;
        }

        Biome biome = world.getBiome(getPos());
        float rainfall = biome.getRainfall();
        if (rainfall <= 0.0F || biome.getTemperature(getPos()) < 0.2F) {
            return;
        }

        int amount = Math.max(1, (int) (rainfall * 10000.0F));
        if (world.isThundering()) {
            amount *= 2;
        }
        IFluidHandler handler = getFluidHandler();
        if (handler != null) {
            handler.fill(new FluidStack(FluidRegistry.WATER, amount), true);
        }
    }

    private void collectExperienceOrbs() {
        IFluidHandler handler = getFluidHandler();
        if (handler == null) {
            return;
        }
        BlockPos center = getPos().offset(getAttachedSide(), 2);
        AxisAlignedBB bounds = new AxisAlignedBB(center).grow(1.0D, 1.0D, 1.0D);
        for (EntityXPOrb orb : getWorld().getEntitiesWithinAABB(EntityXPOrb.class, bounds)) {
            if (orb.isDead || orb.getXpValue() <= 0) {
                continue;
            }
            Fluid xpJuice = FluidRegistry.getFluid("xpjuice");
            if (xpJuice != null && OpenBlocksXpBridge.isAvailable()) {
                try {
                    int amount = OpenBlocksXpBridge.xpToLiquid(orb.getXpValue());
                    if (amount > 0 && fillCompletely(handler, new FluidStack(xpJuice, amount))) {
                        orb.setDead();
                        continue;
                    }
                } catch (ReflectiveOperationException ignored) {
                    // Fall through to the GT6 mob-essence conversion if OpenBlocks' API is unavailable.
                }
            }
            Fluid mobEssence = FluidRegistry.getFluid("mobessence");
            if (mobEssence != null && fillCompletely(handler,
                    new FluidStack(mobEssence, orb.getXpValue() * 20))) {
                orb.setDead();
            }
        }
    }

    private void collectPlayerExperience(IFluidHandler handler) {
        Fluid xpFluid = FluidRegistry.getFluid("xpjuice");
        if (xpFluid == null || !OpenBlocksXpBridge.isAvailable()) {
            return;
        }

        BlockPos pos = getPos();
        AxisAlignedBB bounds = new AxisAlignedBB(
                pos.getX(), pos.getY() + 0.85D, pos.getZ(),
                pos.getX() + 1.0D, pos.getY() + 1.75D, pos.getZ() + 1.0D);
        for (EntityPlayer player : getWorld().getEntitiesWithinAABB(EntityPlayer.class, bounds)) {
            if (!player.isSneaking()) {
                continue;
            }
            try {
                int playerXp = OpenBlocksXpBridge.getPlayerXp(player);
                int maxFluid = Math.min(1000, OpenBlocksXpBridge.xpToLiquid(playerXp));
                if (maxFluid <= 0) {
                    continue;
                }

                int accepted = handler.fill(new FluidStack(xpFluid, maxFluid), false);
                if (accepted <= 0) {
                    continue;
                }
                int xpToDrain = OpenBlocksXpBridge.liquidToXp(accepted);
                int fluidToFill = OpenBlocksXpBridge.xpToLiquid(xpToDrain);
                if (xpToDrain <= 0 || fluidToFill <= 0 ||
                        !fillCompletely(handler, new FluidStack(xpFluid, fluidToFill))) {
                    continue;
                }
                OpenBlocksXpBridge.addPlayerXp(player, -xpToDrain);
            } catch (ReflectiveOperationException ignored) {
                // OpenBlocks is optional; if its XP conversion API is unavailable, leave player XP untouched.
            }
        }
    }

    private void collectEntityFluids(IFluidHandler handler) {
        if (getAttachedSide() != EnumFacing.UP) {
            return;
        }

        BlockPos pos = getPos();
        AxisAlignedBB bounds = new AxisAlignedBB(
                pos.getX(), pos.getY() + 0.85D, pos.getZ(),
                pos.getX() + 1.0D, pos.getY() + 1.75D, pos.getZ() + 1.0D);
        Fluid sewage = FluidRegistry.getFluid("sewage");
        Fluid squidInk = FluidRegistry.getFluid("squidink");
        Fluid slime = FluidRegistry.getFluid("slime");
        Fluid blueSlime = FluidRegistry.getFluid("slime.blue");
        Fluid pinkSlime = FluidRegistry.getFluid("pinkslime");
        Fluid blaze = FluidRegistry.getFluid("blaze");

        if (sewage == null && squidInk == null && slime == null && blueSlime == null &&
                pinkSlime == null && blaze == null) {
            return;
        }
        for (Entity entity : getWorld().getEntitiesWithinAABB(Entity.class, bounds)) {
            if (entity.isDead) {
                continue;
            }
            String entityName = entity.getClass().getSimpleName();
            if (entity instanceof EntitySquid && squidInk != null) {
                handler.fill(new FluidStack(squidInk, 1), true);
            } else if ("KingBlueSlime".equals(entityName) && blueSlime != null) {
                handler.fill(new FluidStack(blueSlime, 4), true);
            } else if ("BlueSlime".equals(entityName) && blueSlime != null) {
                handler.fill(new FluidStack(blueSlime, 1), true);
            } else if ("EntityPinkSlime".equals(entityName) && pinkSlime != null) {
                handler.fill(new FluidStack(pinkSlime, 1), true);
            } else if (entity.getClass() == EntityMagmaCube.class && blaze != null) {
                int amount = Math.max(1, ((EntityMagmaCube) entity).getSlimeSize());
                handler.fill(new FluidStack(blaze, amount), true);
            } else if ("EntityTFMazeSlime".equals(entityName) && slime != null) {
                int amount = Math.max(1, ((EntitySlime) entity).getSlimeSize());
                handler.fill(new FluidStack(slime, amount), true);
            } else if (entity.getClass() == EntitySlime.class && slime != null) {
                int amount = Math.max(1, ((EntitySlime) entity).getSlimeSize());
                handler.fill(new FluidStack(slime, amount), true);
            } else if (entity instanceof IAnimals && sewage != null &&
                    (!(entity instanceof EntityAgeable) || !((EntityAgeable) entity).isChild())) {
                int amount = Math.max(1, Math.round(20.0F * entity.width * entity.width * entity.height));
                handler.fill(new FluidStack(sewage, amount), true);
            }
        }
    }

    private static final class OpenBlocksXpBridge {

        private static boolean initialized;
        private static Method liquidToXpRatio;
        private static Method xpToLiquidRatio;
        private static Method getPlayerXp;
        private static Method addPlayerXp;

        private static boolean isAvailable() {
            if (initialized) {
                return liquidToXpRatio != null;
            }
            initialized = true;
            if (!Loader.isModLoaded("openblocks")) {
                return false;
            }
            try {
                Class<?> liquidXpUtils = Class.forName("openblocks.common.LiquidXpUtils");
                Class<?> enchantmentUtils = Class.forName("openmods.utils.EnchantmentUtils");
                liquidToXpRatio = liquidXpUtils.getMethod("liquidToXpRatio", int.class);
                xpToLiquidRatio = liquidXpUtils.getMethod("xpToLiquidRatio", int.class);
                getPlayerXp = enchantmentUtils.getMethod("getPlayerXP", EntityPlayer.class);
                addPlayerXp = enchantmentUtils.getMethod("addPlayerXP", EntityPlayer.class, int.class);
                return true;
            } catch (ReflectiveOperationException | LinkageError ignored) {
                liquidToXpRatio = null;
                return false;
            }
        }

        private static int liquidToXp(int amount) throws ReflectiveOperationException {
            return ((Number) liquidToXpRatio.invoke(null, amount)).intValue();
        }

        private static int xpToLiquid(int amount) throws ReflectiveOperationException {
            return ((Number) xpToLiquidRatio.invoke(null, amount)).intValue();
        }

        private static int getPlayerXp(EntityPlayer player) throws ReflectiveOperationException {
            return ((Number) getPlayerXp.invoke(null, player)).intValue();
        }

        private static void addPlayerXp(EntityPlayer player, int amount) throws ReflectiveOperationException {
            addPlayerXp.invoke(null, player, amount);
        }
    }
}
