package com.drppp.gt6addition.common.metatileentity.single.ru;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.api.capability.CapabilityHandler;
import com.drppp.gt6addition.api.capability.interfaces.IRotationEnergy;
import com.drppp.gt6addition.common.metatileentity.single.ku.KineticRenderHelper;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MetaTileEntityRotationPump extends TieredMetaTileEntity {

    private static final String NBT_ACTIVE = "Gt6Working";
    private static final String NBT_TANK = "FluidTank";
    private static final Cuboid6 FULL_CUBE = new Cuboid6(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final int ACTIVE_HOLD_TICKS = 6;
    private static final int PUMP_INTERVAL = 10;
    private static final int TRANSFER_INTERVAL = 5;
    private static final int MAX_SCAN_DEPTH = 64;
    private static final int MAX_SCAN_BLOCKS = 1024;

    private final int color;
    private final int minRuInput;
    private final int maxRuInput;
    private final int tankCapacity;
    private final int baseOperations;
    private final int maxTransferAmount;
    private final FluidTank fluidTank;

    private final List<BlockPos> pumpTargets = new ArrayList<>();
    private Fluid cachedFluid;
    private BlockPos cachedIntake;
    private int nextPumpIndex;
    private boolean working;
    private int activeTicks;

    public MetaTileEntityRotationPump(ResourceLocation metaTileEntityId, int tier, int color) {
        super(metaTileEntityId, tier);
        this.color = color;
        this.minRuInput = (int) GTValues.V[tier];
        this.maxRuInput = this.minRuInput * 2;
        this.tankCapacity = 16000 << Math.max(0, tier - 1);
        this.baseOperations = 1 << Math.max(0, tier - 1);
        this.maxTransferAmount = Fluid.BUCKET_VOLUME * this.baseOperations;
        this.fluidTank = new FluidTank(this.tankCapacity);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntityRotationPump(metaTileEntityId, getTier(), color);
    }

    @Override
    public void update() {
        super.update();
        if (getWorld() == null || getWorld().isRemote) {
            return;
        }

        if (getOffsetTimer() % TRANSFER_INTERVAL == 0L) {
            pushFluidsOut();
        }

        boolean pumped = false;
        if (getOffsetTimer() % PUMP_INTERVAL == 0L) {
            pumped = pumpWorldFluid();
        }

        if (pumped) {
            activeTicks = ACTIVE_HOLD_TICKS;
        } else if (activeTicks > 0) {
            activeTicks--;
        }
        setWorking(activeTicks > 0);
    }

    private boolean pumpWorldFluid() {
        if (fluidTank.getFluidAmount() >= fluidTank.getCapacity()) {
            return false;
        }

        int inputRu = getInputRu();
        if (inputRu < minRuInput) {
            return false;
        }

        ensurePumpTargets();
        if (pumpTargets.isEmpty()) {
            return false;
        }

        boolean pumped = false;
        int operations = inputRu >= maxRuInput ? baseOperations * 2 : baseOperations;
        for (int i = 0; i < operations; i++) {
            BlockPos target = getNextPumpTarget();
            if (target == null) {
                break;
            }

            FluidStack simulated = drainFluid(target, false);
            if (simulated == null || simulated.amount <= 0 || fluidTank.fill(simulated, false) < simulated.amount) {
                invalidateCachedTargets();
                continue;
            }

            FluidStack drained = drainFluid(target, true);
            if (drained == null || drained.amount <= 0) {
                invalidateCachedTargets();
                continue;
            }

            fluidTank.fill(drained, true);
            pumped = true;
            invalidateCachedTargets();
            if (fluidTank.getFluidAmount() >= fluidTank.getCapacity()) {
                break;
            }
        }
        return pumped;
    }

    private void ensurePumpTargets() {
        BlockPos intake = findIntakePos();
        if (intake == null) {
            clearPumpTargets();
            return;
        }

        Fluid fluid = getFluidAt(intake);
        if (fluid == null) {
            clearPumpTargets();
            return;
        }

        if (intake.equals(cachedIntake) && fluid == cachedFluid && hasValidPumpTarget()) {
            return;
        }

        cachedIntake = intake.toImmutable();
        cachedFluid = fluid;
        rescanFluidBody(intake, fluid);
    }

    private boolean hasValidPumpTarget() {
        if (pumpTargets.isEmpty()) {
            return false;
        }
        for (BlockPos target : pumpTargets) {
            FluidStack drained = drainFluid(target, false);
            if (drained != null && drained.amount > 0) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private BlockPos findIntakePos() {
        BlockPos frontPos = getPos().offset(getFrontFacing());
        for (int depth = 0; depth < MAX_SCAN_DEPTH; depth++) {
            BlockPos candidate = frontPos.down(depth);
            Fluid fluid = getFluidAt(candidate);
            if (fluid != null) {
                return candidate;
            }

            IBlockState state = getWorld().getBlockState(candidate);
            if (!getWorld().isAirBlock(candidate) && !state.getMaterial().isReplaceable()) {
                break;
            }
        }
        return null;
    }

    private void rescanFluidBody(BlockPos startPos, Fluid targetFluid) {
        pumpTargets.clear();
        nextPumpIndex = 0;

        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<Long> visited = new HashSet<>();
        queue.add(startPos);
        visited.add(startPos.toLong());

        while (!queue.isEmpty() && visited.size() <= MAX_SCAN_BLOCKS) {
            BlockPos current = queue.removeFirst();
            if (!isFluidAt(current, targetFluid)) {
                continue;
            }

            if (drainFluid(current, false) != null) {
                pumpTargets.add(current.toImmutable());
            }

            for (EnumFacing side : EnumFacing.VALUES) {
                BlockPos next = current.offset(side);
                long key = next.toLong();
                if (!visited.add(key)) {
                    continue;
                }
                if (isFluidAt(next, targetFluid)) {
                    queue.addLast(next);
                }
            }
        }
    }

    @Nullable
    private BlockPos getNextPumpTarget() {
        if (pumpTargets.isEmpty()) {
            return null;
        }

        for (int checked = 0; checked < pumpTargets.size(); checked++) {
            int index = (nextPumpIndex + checked) % pumpTargets.size();
            BlockPos target = pumpTargets.get(index);
            FluidStack simulated = drainFluid(target, false);
            if (simulated != null && simulated.amount > 0) {
                nextPumpIndex = (index + 1) % pumpTargets.size();
                return target;
            }
        }
        clearPumpTargets();
        return null;
    }

    private void invalidateCachedTargets() {
        pumpTargets.clear();
        nextPumpIndex = 0;
    }

    private void clearPumpTargets() {
        pumpTargets.clear();
        cachedFluid = null;
        cachedIntake = null;
        nextPumpIndex = 0;
    }

    private void pushFluidsOut() {
        FluidStack preview = fluidTank.drain(maxTransferAmount, false);
        if (preview == null || preview.amount <= 0) {
            return;
        }

        EnumFacing back = getFrontFacing().getOpposite();
        for (EnumFacing side : EnumFacing.VALUES) {
            if (side == back) {
                continue;
            }

            TileEntity tileEntity = getWorld().getTileEntity(getPos().offset(side));
            if (tileEntity == null ||
                    !tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite())) {
                continue;
            }

            IFluidHandler handler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                    side.getOpposite());
            if (handler == null) {
                continue;
            }

            int accepted = handler.fill(preview, false);
            if (accepted <= 0) {
                continue;
            }

            FluidStack drained = fluidTank.drain(accepted, true);
            if (drained != null && drained.amount > 0) {
                handler.fill(drained, true);
            }

            preview = fluidTank.drain(maxTransferAmount, false);
            if (preview == null || preview.amount <= 0) {
                return;
            }
        }
    }

    private int getInputRu() {
        EnumFacing back = getFrontFacing().getOpposite();
        TileEntity tileEntity = getWorld().getTileEntity(getPos().offset(back));
        if (tileEntity == null ||
                !tileEntity.hasCapability(CapabilityHandler.CAPABILITY_ROTATION_ENERGY, back.getOpposite())) {
            return 0;
        }
        IRotationEnergy rotationEnergy = tileEntity.getCapability(CapabilityHandler.CAPABILITY_ROTATION_ENERGY,
                back.getOpposite());
        return rotationEnergy == null ? 0 : Math.min(rotationEnergy.getEnergyOutput(), maxRuInput);
    }

    @Nullable
    private Fluid getFluidAt(BlockPos pos) {
        IBlockState state = getWorld().getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IFluidBlock) {
            return ((IFluidBlock) block).getFluid();
        }
        if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
            return FluidRegistry.WATER;
        }
        if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) {
            return FluidRegistry.LAVA;
        }
        return null;
    }

    private boolean isFluidAt(BlockPos pos, Fluid fluid) {
        Fluid candidate = getFluidAt(pos);
        return candidate != null && candidate == fluid;
    }

    @Nullable
    private FluidStack drainFluid(BlockPos pos, boolean doDrain) {
        IBlockState state = getWorld().getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IFluidBlock) {
            return ((IFluidBlock) block).drain(getWorld(), pos, doDrain);
        }
        if ((block == Blocks.WATER || block == Blocks.FLOWING_WATER) &&
                state.getValue(BlockLiquid.LEVEL) == 0) {
            if (doDrain) {
                getWorld().setBlockToAir(pos);
            }
            return new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME);
        }
        if ((block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) &&
                state.getValue(BlockLiquid.LEVEL) == 0) {
            if (doDrain) {
                getWorld().setBlockToAir(pos);
            }
            return new FluidStack(FluidRegistry.LAVA, Fluid.BUCKET_VOLUME);
        }
        return null;
    }

    private void setWorking(boolean working) {
        if (this.working == working) {
            return;
        }
        this.working = working;
        markDirty();
        writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(working));
    }

    @Override
    public boolean isActive() {
        return working;
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing side) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER || capability == CapabilityEnergy.ENERGY) {
            return false;
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return side == null || side != getFrontFacing().getOpposite();
        }
        return super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER || capability == CapabilityEnergy.ENERGY) {
            return null;
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY &&
                (side == null || side != getFrontFacing().getOpposite())) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidTank);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        String overlayRoot = working ?
                "gt6addition:blocks/machines/pump/rotation/overlay_active/" :
                "gt6addition:blocks/machines/pump/rotation/overlay/";
        for (EnumFacing side : EnumFacing.VALUES) {
            String face = getFaceTexture(side);
            KineticRenderHelper.renderFace(renderState, translation, pipeline, side, FULL_CUBE,
                    "gt6addition:blocks/machines/pump/rotation/colored/" + face, color);
            KineticRenderHelper.renderOverlayFace(renderState, translation, pipeline, side, FULL_CUBE,
                    overlayRoot + face);
        }
    }

    private String getFaceTexture(EnumFacing side) {
        if (side == getFrontFacing()) {
            return "front";
        }
        return side == getFrontFacing().getOpposite() ? "back" : "side";
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(KineticRenderHelper.getSprite("gt6addition:blocks/machines/pump/rotation/colored/side"), color);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(working);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        working = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            working = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean(NBT_ACTIVE, working);
        data.setTag(NBT_TANK, fluidTank.writeToNBT(new NBTTagCompound()));
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        working = data.getBoolean(NBT_ACTIVE);
        if (data.hasKey(NBT_TANK)) {
            fluidTank.readFromNBT(data.getCompoundTag(NBT_TANK));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.machine.rotation_pump.tooltip.1"));
        tooltip.add(I18n.format("gt6addition.machine.rotation_pump.tooltip.2", minRuInput, maxRuInput,
                baseOperations * 2000, baseOperations * 4000));
        tooltip.add(I18n.format("gt6addition.machine.rotation_pump.tooltip.3", tankCapacity));
    }
}
