package com.drppp.gt6addition.common.metatileentity.single.hu;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityCrucibleFaucet extends MetaTileEntity {

    private static final int TRANSFER_AMOUNT = GTValues.L;
    private static final double P1 = 1.0D / 16.0D;
    private static final double P2 = 2.0D / 16.0D;
    private static final double P4 = 4.0D / 16.0D;
    private static final double P5 = 5.0D / 16.0D;
    private static final double P6 = 6.0D / 16.0D;
    private static final double P7 = 7.0D / 16.0D;
    private static final double P8 = 8.0D / 16.0D;
    private static final double P9 = 9.0D / 16.0D;
    private static final double P10 = 10.0D / 16.0D;
    private static final double P11 = 11.0D / 16.0D;
    private static final double P12 = 12.0D / 16.0D;
    private static final double P14 = 14.0D / 16.0D;

    private final int tier;
    private final int casingColor;
    private final boolean acidProof;
    private final float hardness;
    private final float resistance;
    private final long maxTemperature;

    public MetaTileEntityCrucibleFaucet(ResourceLocation metaTileEntityId, int tier, int casingColor,
                                       boolean acidProof, float hardness, float resistance) {
        super(metaTileEntityId);
        this.tier = tier;
        this.casingColor = casingColor;
        this.acidProof = acidProof;
        this.hardness = hardness;
        this.resistance = resistance;
        this.maxTemperature = 1800L + Math.max(0, tier) * 300L;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCrucibleFaucet(metaTileEntityId, tier, casingColor, acidProof, hardness, resistance);
    }

    @Override
    public void onPlacement(EntityLivingBase placer) {
        super.onPlacement(placer);
        if (getFrontFacing().getAxis().isHorizontal()) {
            setFrontFacing(getFrontFacing().getOpposite());
        }
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
        int movedFluid = transferFluidDownOnce();
        if (movedFluid > 0) {
            return movedFluid;
        }
        return 0;
    }

    private int transferFluidDownOnce() {
        IFluidHandler target = getBelowFluidHandler();
        return transferFluid(getAdjacentFluidHandler(getFrontFacing(), getFrontFacing().getOpposite()), target);
    }

    private int transferFluid(IFluidHandler source, IFluidHandler target) {
        if (source == null || target == null) {
            return 0;
        }
        FluidStack preview = source.drain(TRANSFER_AMOUNT, false);
        if (preview == null || preview.amount <= 0) {
            return 0;
        }
        if (preview.getFluid().getTemperature(preview) > maxTemperature) {
            meltDown();
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
        Object source = GTUtility.getMetaTileEntity(getWorld(), getPos().offset(getFrontFacing()));
        ICrucibleMold target = getBelowMold();
        if (source instanceof MetaTileEntityCrucible && target != null) {
            if (((MetaTileEntityCrucible) source).getCurrentTemperature() > maxTemperature) {
                meltDown();
                return 0L;
            }
            return ((MetaTileEntityCrucible) source).fillMoldAtSide(target,
                    getFrontFacing().getOpposite(), EnumFacing.UP);
        }
        return 0L;
    }

    private void meltDown() {
        if (getWorld() != null && !getWorld().isRemote) {
            getWorld().setBlockState(getPos(), Blocks.FLOWING_LAVA.getDefaultState(), 3);
        }
    }

    @Nullable
    private ICrucibleMold getBelowMold() {
        Object target = GTUtility.getMetaTileEntity(getWorld(), getPos().down());
        return target instanceof ICrucibleMold ? (ICrucibleMold) target : null;
    }

    @Nullable
    private IFluidHandler getBelowFluidHandler() {
        TileEntity tileEntity = getWorld().getTileEntity(getPos().down());
        if (tileEntity == null || !tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.UP)) {
            return null;
        }
        return tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.UP);
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
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.SOLID_STEEL_CASING.getParticleSprite(), casingColor);
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
        IVertexOperation[] coloredPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(casingColor & 0xFFFFFF)));
        for (Cuboid6 box : getFaucetBoxes()) {
            Textures.SOLID_STEEL_CASING.render(renderState, translation, coloredPipeline, box);
        }
    }

    private Cuboid6[] getFaucetBoxes() {
        switch (getFrontFacing()) {
            case SOUTH:
                return new Cuboid6[]{
                        new Cuboid6(P5, P5, P14, P11, P11, 1.0D),
                        new Cuboid6(P6, P6, P8, P10, P10, 1.0D),
                        new Cuboid6(P6, 0.0D, P6, P10, P7, P10),
                        new Cuboid6(P7, 0.0D, P7, P9, P2, P9)
                };
            case WEST:
                return new Cuboid6[]{
                        new Cuboid6(0.0D, P5, P5, P2, P11, P11),
                        new Cuboid6(0.0D, P6, P6, P8, P10, P10),
                        new Cuboid6(P6, 0.0D, P6, P10, P7, P10),
                        new Cuboid6(P7, 0.0D, P7, P9, P2, P9)
                };
            case EAST:
                return new Cuboid6[]{
                        new Cuboid6(P14, P5, P5, 1.0D, P11, P11),
                        new Cuboid6(P8, P6, P6, 1.0D, P10, P10),
                        new Cuboid6(P6, 0.0D, P6, P10, P7, P10),
                        new Cuboid6(P7, 0.0D, P7, P9, P2, P9)
                };
            case NORTH:
            default:
                return new Cuboid6[]{
                        new Cuboid6(P5, P5, 0.0D, P11, P11, P2),
                        new Cuboid6(P6, P6, 0.0D, P10, P10, P8),
                        new Cuboid6(P6, 0.0D, P6, P10, P7, P10),
                        new Cuboid6(P7, 0.0D, P7, P9, P2, P9)
                };
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
