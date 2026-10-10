package com.drppp.gt6addition.common.metatileentity.single.hu;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.client.Gt6AdditionTextures;
import com.drppp.gt6addition.common.fluid.GT6CalciteFluid;
import com.drppp.gt6addition.common.metatileentity.single.ku.KineticRenderHelper;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import net.minecraft.world.World;
import java.util.List;

/**
 * GT6 fluidized-bed burning box. Uses the legacy molten.calcite fluid at GT6's
 * 72 mB per dust-unit rate without adding a Calcite Material to GTCEu.
 */
public class MetaTileEntityFluidizedBed extends MetaTileEntityCombustionchamber {
    private static final int CALCITE_TANK_CAPACITY = 1000;
    private static final int CALCITE_PER_FUEL_DUST = GT6CalciteFluid.MILLIBUCKETS_PER_DUST;
    private static final int CALCITE_PER_FUEL_BLOCK = CALCITE_PER_FUEL_DUST * 9;

    private FluidTank calciteFluidTank;

    public MetaTileEntityFluidizedBed(ResourceLocation id, int color, double efficiency,
                                      int outputHu, boolean dense, int baseTier) {
        super(id, color, efficiency, outputHu, dense, baseTier);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        // Keep the old second slot so worlds made before the Calcite-fluid migration
        // can convert their stored dust instead of silently discarding it.
        return new FluidizedBedItemHandler(this);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        this.calciteFluidTank = new CalciteFluidTank(CALCITE_TANK_CAPACITY);
        return new FluidTankList(false, this.calciteFluidTank);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFluidizedBed(metaTileEntityId, color, efficiency, outPutHu, isDense, baseTier);
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) convertStoredCalciteDust();
        ItemStack fuelBefore = importItems.getStackInSlot(0).copy();
        super.update();
        if (!getWorld().isRemote) {
            int consumedFuelCount = fuelBefore.getCount() - importItems.getStackInSlot(0).getCount();
            if (consumedFuelCount > 0) {
                int calciteCost = getCalciteCostPerFuel(fuelBefore) * consumedFuelCount;
                FluidStack drained = calciteFluidTank.drain(calciteCost, true);
                if (drained == null || drained.amount != calciteCost) {
                    // Fuel is only consumed after canStartFuelCycle validated the fluid amount.
                    setActive(false);
                    currentItemHasBurnedTime = 0;
                    hu.setHuEnergy(0);
                }
            }
        }
    }

    @Override
    protected boolean canStartFuelCycle() {
        ItemStack fuel = importItems.getStackInSlot(0);
        if (fuel.isEmpty() || TileEntityFurnace.getItemBurnTime(fuel) <= 0 || calciteFluidTank == null) return false;
        FluidStack calcite = calciteFluidTank.getFluid();
        return calcite != null && calcite.getFluid() == GT6CalciteFluid.getFluid() &&
                calcite.amount >= getCalciteCostPerFuel(fuel);
    }

    @Override
    public boolean igniteFromAutomaticIgniter() {
        return canStartFuelCycle() && super.igniteFromAutomaticIgniter();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.hu.fluidized_bed.info"));
    }

    @Override
    public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hit) {
        ItemStack held = player.getHeldItem(hand);
        if (!getWorld().isRemote && !player.isSneaking() && facing == getFrontFacing() &&
                !held.isEmpty() && held.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            IFluidHandlerItem container = held.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (container != null) {
                FluidStack contained = container.drain(Integer.MAX_VALUE, false);
                if (contained != null) {
                    int transferred = GTTransferUtils.transferFluids(container, importFluids, Integer.MAX_VALUE,
                            fluid -> fluid != null && fluid.getFluid() == GT6CalciteFluid.getFluid());
                    if (transferred > 0) {
                        replaceHeldContainer(player, hand, held, container.getContainer());
                        return true;
                    }
                } else if (!isActive && calciteFluidTank.getFluid() != null) {
                    int accepted = container.fill(calciteFluidTank.getFluid().copy(), true);
                    if (accepted > 0) {
                        calciteFluidTank.drain(accepted, true);
                        replaceHeldContainer(player, hand, held, container.getContainer());
                        return true;
                    }
                }
            }
        }
        return super.onRightClick(player, hand, facing, hit);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState state, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] coloured = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(color)));
        String root = "gt6addition:blocks/machines/generators/burning_fluidbed/";
        KineticRenderHelper.renderGt6SixSidedCube(state, translation, coloured, getFrontFacing(),
                new codechicken.lib.vec.Cuboid6(0, 0, 0, 1, 1, 1), root + "colored/");
        KineticRenderHelper.renderGt6SixSidedCube(state, translation, pipeline, getFrontFacing(),
                new codechicken.lib.vec.Cuboid6(0, 0, 0, 1, 1, 1),
                root + (isActive() ? "overlay_active/" : "overlay/"));
    }

    @Override
    @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(KineticRenderHelper.getSprite(
                "gt6addition:blocks/machines/generators/burning_fluidbed/colored/top"), color);
    }

    private static void replaceHeldContainer(EntityPlayer player, EnumHand hand, ItemStack original,
                                             ItemStack replacement) {
        if (replacement == null || replacement.isEmpty()) return;
        replacement = replacement.copy();
        replacement.setCount(1);
        if (original.getCount() <= 1) {
            player.setHeldItem(hand, replacement);
        } else {
            original.shrink(1);
            if (!player.inventory.addItemStackToInventory(replacement)) {
                player.dropItem(replacement, false);
            }
        }
    }

    private static int getCalciteCostPerFuel(ItemStack fuel) {
        return fuel.getItem() == Item.getItemFromBlock(Blocks.COAL_BLOCK) ||
                OreDictUnifier.getPrefix(fuel) == OrePrefix.block ?
                CALCITE_PER_FUEL_BLOCK : CALCITE_PER_FUEL_DUST;
    }

    private void convertStoredCalciteDust() {
        ItemStack storedDust = importItems.getStackInSlot(1);
        if (!isCalcite(storedDust) || calciteFluidTank == null) return;

        int availableDustUnits = Math.max(0,
                (calciteFluidTank.getCapacity() - calciteFluidTank.getFluidAmount()) / CALCITE_PER_FUEL_DUST);
        int dustToConvert = Math.min(storedDust.getCount(), availableDustUnits);
        if (dustToConvert <= 0) return;

        int amount = dustToConvert * CALCITE_PER_FUEL_DUST;
        FluidStack calcite = GT6CalciteFluid.getFluidStack(amount);
        if (calcite != null && calciteFluidTank.fill(calcite, true) == amount) {
            importItems.extractItem(1, dustToConvert, false);
        }
    }

    private static boolean isCalcite(ItemStack stack) {
        if (stack.isEmpty()) return false;
        MaterialStack material = OreDictUnifier.getMaterial(stack);
        return material != null && material.material == Materials.Calcite;
    }

    private static final class CalciteFluidTank extends FluidTank {
        private CalciteFluidTank(int capacity) {
            super(capacity);
        }

        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return fluid != null && fluid.getFluid() == GT6CalciteFluid.getFluid() && super.canFillFluidType(fluid);
        }
    }

    private static final class FluidizedBedItemHandler extends GTItemStackHandler {
        private FluidizedBedItemHandler(MetaTileEntityFluidizedBed machine) {
            super(machine, 2);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            // Slot 1 is retained only for old saved inventories; new Calcite is fluid-only.
            if (slot == 1) return stack;
            return super.insertItem(slot, stack, simulate);
        }
    }
}
