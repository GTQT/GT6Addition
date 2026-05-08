package com.drppp.gt6addition.common.metatileentity.single.hu;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.api.crucible.ICrucibleMold;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityCrucibleFaucet extends MetaTileEntity {

    private static final int TRANSFER_AMOUNT = GTValues.L;
    private static final Cuboid6 CENTER = new Cuboid6(0.3125, 0.3125, 0.3125, 0.6875, 0.6875, 0.6875);
    private static final Cuboid6 PIPE_X = new Cuboid6(0.0, 0.375, 0.375, 1.0, 0.625, 0.625);
    private static final Cuboid6 PIPE_Z = new Cuboid6(0.375, 0.375, 0.0, 0.625, 0.625, 1.0);
    private static final Cuboid6 NOZZLE_NORTH = new Cuboid6(0.40625, 0.125, 0.0, 0.59375, 0.375, 0.1875);
    private static final Cuboid6 NOZZLE_SOUTH = new Cuboid6(0.40625, 0.125, 0.8125, 0.59375, 0.375, 1.0);
    private static final Cuboid6 NOZZLE_WEST = new Cuboid6(0.0, 0.125, 0.40625, 0.1875, 0.375, 0.59375);
    private static final Cuboid6 NOZZLE_EAST = new Cuboid6(0.8125, 0.125, 0.40625, 1.0, 0.375, 0.59375);

    private final int tier;
    private final int casingColor;
    private final boolean acidProof;
    private final float hardness;
    private final float resistance;

    public MetaTileEntityCrucibleFaucet(ResourceLocation metaTileEntityId, int tier, int casingColor,
                                       boolean acidProof, float hardness, float resistance) {
        super(metaTileEntityId);
        this.tier = tier;
        this.casingColor = casingColor;
        this.acidProof = acidProof;
        this.hardness = hardness;
        this.resistance = resistance;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCrucibleFaucet(metaTileEntityId, tier, casingColor, acidProof, hardness, resistance);
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && getOffsetTimer() % 10 == 0 && getWorld().isBlockPowered(getPos())) {
            transferOnce();
        }
    }

    @Override
    public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        if (!getWorld().isRemote && !player.isSneaking()) {
            int moved = transferOnce();
            if (moved <= 0) {
                player.sendStatusMessage(new TextComponentTranslation("gt6addition.machine.hu_crucible_faucet.status.empty"), true);
            }
        }
        return true;
    }

    private int transferOnce() {
        long movedMaterial = transferMaterialUnitsOnce();
        if (movedMaterial > 0L) {
            return (int) Math.min(Integer.MAX_VALUE,
                    Math.max(1L, movedMaterial * GTValues.L / GTValues.M));
        }
        IFluidHandler source = getAdjacentFluidHandler(getFrontFacing().getOpposite(), getFrontFacing());
        IFluidHandler target = getAdjacentFluidHandler(getFrontFacing(), getFrontFacing().getOpposite());
        if (source == null || target == null) {
            return 0;
        }
        FluidStack preview = source.drain(TRANSFER_AMOUNT, false);
        if (preview == null || preview.amount <= 0) {
            return 0;
        }
        int accepted = target.fill(preview, false);
        if (accepted <= 0) {
            return 0;
        }
        FluidStack drained = source.drain(accepted, true);
        if (drained == null || drained.amount <= 0) {
            return 0;
        }
        return target.fill(drained, true);
    }

    private long transferMaterialUnitsOnce() {
        Object source = GTUtility.getMetaTileEntity(getWorld(), getPos().offset(getFrontFacing().getOpposite()));
        Object target = GTUtility.getMetaTileEntity(getWorld(), getPos().offset(getFrontFacing()));
        if (!(source instanceof MetaTileEntityCrucible) || !(target instanceof ICrucibleMold)) {
            return 0L;
        }
        return ((MetaTileEntityCrucible) source).fillMoldAtSide((ICrucibleMold) target,
                getFrontFacing(), getFrontFacing().getOpposite());
    }

    @Nullable
    private IFluidHandler getAdjacentFluidHandler(EnumFacing offset, EnumFacing accessSide) {
        TileEntity tileEntity = getWorld().getTileEntity(getPos().offset(offset));
        if (tileEntity == null || !tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, accessSide)) {
            return null;
        }
        return tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, accessSide);
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return facing.getAxis().isHorizontal();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getLightOpacity() {
        return 0;
    }

    @Override
    public float getBlockHardness() {
        return hardness;
    }

    @Override
    public float getBlockResistance() {
        return resistance;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation,
                                     IVertexOperation[] pipeline) {
        Textures.SOLID_STEEL_CASING.render(renderState, translation, pipeline, CENTER);
        Textures.SOLID_STEEL_CASING.render(renderState, translation, pipeline,
                getFrontFacing().getAxis() == EnumFacing.Axis.X ? PIPE_X : PIPE_Z);
        Textures.SOLID_STEEL_CASING.render(renderState, translation, pipeline, getNozzleBox());
    }

    private Cuboid6 getNozzleBox() {
        switch (getFrontFacing()) {
            case SOUTH:
                return NOZZLE_SOUTH;
            case WEST:
                return NOZZLE_WEST;
            case EAST:
                return NOZZLE_EAST;
            case NORTH:
            default:
                return NOZZLE_NORTH;
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.machine.hu_crucible_faucet.tooltip.transfer"));
        tooltip.add(I18n.format("gt6addition.machine.hu_crucible_faucet.tooltip.redstone"));
        if (acidProof) {
            tooltip.add(I18n.format("gt6addition.machine.hu_crucible_faucet.tooltip.acid_proof"));
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing side) {
        if (capability == CapabilityEnergy.ENERGY) {
            return false;
        }
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return false;
        }
        return super.hasCapability(capability, side);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityEnergy.ENERGY) {
            return null;
        }
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return null;
        }
        return super.getCapability(capability, side);
    }
}
