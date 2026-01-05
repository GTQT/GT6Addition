package com.drppp.gt6addition.common.metatileentity.single.hu;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.api.capability.CapabilityHandler;
import com.drppp.gt6addition.api.capability.impl.HeatEnergyHandler;
import com.drppp.gt6addition.api.capability.interfaces.IHeatEnergy;
import com.drppp.gt6addition.api.utils.CraftingGetItemUtils;
import com.drppp.gt6addition.client.Gt6AdditionTextures;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IHeatable;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.common.pipelike.heat.tile.TileEntityHeatConductor;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class MetaTileEntityCombustionchamberLiquid extends MetaTileEntity {
    public final int color;
    public final double efficiency;
    public final int outPutHu;
    public final int burnSpeed;
    protected final ICubeRenderer rendererBASE = Gt6AdditionTextures.HU_BASE_BURRING_BOX_LIQUID;
    protected final SimpleOverlayRenderer renderer = Gt6AdditionTextures.HU_BURRING_BOX_SIDE_OVERLAY;
    protected final SimpleOverlayRenderer renderer_full = Gt6AdditionTextures.HU_BURRING_BOX_SIDE_FULL_OVERLAY;
    private final Random random = new Random();
    public int currentItemBurnTime = 0;
    public int currentItemHasBurnedTime = 0;
    public boolean isActive;
    public boolean isDense;
    IHeatEnergy hu = new HeatEnergyHandler();
    protected FluidTank inputFluidTank;
    protected FluidTank outputFluidTank;
    public MetaTileEntityCombustionchamberLiquid(ResourceLocation metaTileEntityId, int color, double efficiency, int outPutHu, boolean isDense) {
        super(metaTileEntityId);
        this.color = color;
        this.efficiency = efficiency;
        this.outPutHu = outPutHu;
        this.isDense = isDense;
        this.burnSpeed = (int) (this.outPutHu / this.efficiency * 0.6);
        this.initializeInventory();
    }
    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.importFluids = this.createImportFluidHandler();
        this.exportFluids = this.createExportFluidHandler();
        this.fluidInventory = new FluidHandlerProxy(this.importFluids, this.exportFluids);
    }
    @Override
    protected FluidTankList createImportFluidHandler() {
        this.inputFluidTank = new FliterFluidTank(1000);
        return new FluidTankList(false, this.inputFluidTank);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        this.outputFluidTank = new FluidTank(1000);
        return new FluidTankList(false,  this.outputFluidTank);
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            this.markDirty();
            if (getWorld() != null && !getWorld().isRemote) {
                this.writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, (buf) -> {
                    buf.writeBoolean(active);
                });
            }
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityCombustionchamberLiquid(this.metaTileEntityId, this.color, this.efficiency, this.outPutHu, this.isDense);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @SideOnly(Side.CLIENT)
    protected SimpleSidedCubeRenderer getBaseRenderer() {
        return Gt6AdditionTextures.BASE_BURRING_BOX_TEXTURE;
    }

    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Gt6AdditionTextures.HU_BURRING_BOX_SIDE_FULL_OVERLAY.getParticleSprite(), this.color);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(this.getPaintingColorForRendering())));
        IVertexOperation[] pipeline1 = ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(this.color)));
        this.getBaseRenderer().render(renderState, translation, colouredPipeline);
        this.renderer_full.renderSided(EnumFacing.UP, renderState, translation, pipeline1);
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            this.renderer.renderSided(facing, renderState, translation, pipeline1);
        }
        this.rendererBASE.renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), isActive, isActive);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("isActive", isActive);
        data.setInteger("currentItemBurnTime", currentItemBurnTime);
        data.setInteger("currentItemHasBurnedTime", currentItemHasBurnedTime);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        isActive = data.getBoolean("isActive");
        currentItemBurnTime = data.getInteger("currentItemBurnTime");
        currentItemHasBurnedTime = data.getInteger("currentItemHasBurnedTime");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isActive);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        isActive = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            isActive = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.hu.generator.info.7"));
        tooltip.add(I18n.format("gt6addition.hu.generator.info.2", this.efficiency * 100 + "%"));
        tooltip.add(I18n.format("gt6addition.hu.generator.info.3", this.outPutHu));
        tooltip.add(I18n.format("gt6addition.hu.generator.info.4"));
        tooltip.add(I18n.format("gt6addition.hu.generator.info.5"));
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote && getOffsetTimer() % 10 == 0 && isActive) {
            net.minecraft.util.math.BlockPos pos = getPos();
            double d0 = (double) pos.getX() + 0.5D;
            double d1 = (double) pos.getY() + random.nextDouble() * 6.0D / 16.0D;
            double d2 = (double) pos.getZ() + 0.5D;
            double d3 = 0.52D;
            double d4 = random.nextDouble() * 0.6D - 0.3D;
            if (random.nextDouble() < 0.1D) {
                getWorld().playSound((double) pos.getX() + 0.5D, pos.getY(), (double) pos.getZ() + 0.5D, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }

            switch (getFrontFacing()) {
                case WEST:
                    getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 - 0.52D, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
                    getWorld().spawnParticle(EnumParticleTypes.FLAME, d0 - 0.52D, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
                    break;
                case EAST:
                    getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + 0.52D, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
                    getWorld().spawnParticle(EnumParticleTypes.FLAME, d0 + 0.52D, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
                    break;
                case NORTH:
                    getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d4, d1, d2 - 0.52D, 0.0D, 0.0D, 0.0D);
                    getWorld().spawnParticle(EnumParticleTypes.FLAME, d0 + d4, d1, d2 - 0.52D, 0.0D, 0.0D, 0.0D);
                    break;
                case SOUTH:
                    getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d4, d1, d2 + 0.52D, 0.0D, 0.0D, 0.0D);
                    getWorld().spawnParticle(EnumParticleTypes.FLAME, d0 + d4, d1, d2 + 0.52D, 0.0D, 0.0D, 0.0D);
            }
        }
        if (!getWorld().isRemote) {
            if (this.getOffsetTimer() % 20L == 0L && isActive) {
                {
                    List<EntityPlayer> list = getWorld().getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(getPos().up()));
                    for (EntityPlayer e : list)
                        attackPlayer(e);
                }
            }
            if (isActive()) {
                if(!canActive())
                    clearOut();
                this.hu.setHuEnergy(outPutHu);
                this.currentItemHasBurnedTime += this.burnSpeed;
                if (this.currentItemHasBurnedTime >= this.currentItemBurnTime && canActive()) {
                    if (getWorld().getBlockState(getPos().offset(getFrontFacing())).getBlock() != Blocks.AIR) {
                        clearOut();
                    }
                    this.currentItemHasBurnedTime = 0;

                    int mlHu = LiquidBurringInfo.getMlHu(getSelfFluid());//16     64
                    if(mlHu>this.burnSpeed)
                    {
                        this.currentItemBurnTime = mlHu;
                        this.inputFluidTank.drain(1,true);
                        this.outputFluidTank.fill(Materials.CarbonDioxide.getFluid(1), true);
                    }else {
                        int amount = this.burnSpeed/mlHu + this.burnSpeed%mlHu;
                        this.currentItemBurnTime = mlHu *amount;
                        this.inputFluidTank.drain(amount,true);
                        this.outputFluidTank.fill(Materials.CarbonDioxide.getFluid(amount),true);
                    }

                }
                var te = getWorld().getTileEntity(getPos().up());
                if(te!=null && te instanceof TileEntityHeatConductor)
                {
                    TileEntityHeatConductor heat = (TileEntityHeatConductor)te;
                    if(heat.isConnected(EnumFacing.DOWN))
                        heat.applyHeat(this.hu.getHeat());
                }
//                if (GTUtility.getMetaTileEntity(getWorld(), this.getPos().offset(EnumFacing.UP)) instanceof MetaTileEntityHeatHatch hatch) {
//                    hatch.addHeat((int)(this.hu.getHeat() * 2.2), this.isDense ? 7 : 5);
//                }
            }
            if(this.currentItemHasBurnedTime>0 && !isActive)
            {
                this.currentItemHasBurnedTime=0;
            }

        }
    }

    private void attackPlayer(EntityPlayer playerIn) {
        DamageSource damageSource = DamageSource.IN_FIRE;
        playerIn.attackEntityFrom(damageSource, 2);
    }

    private void clearOut() {
        this.currentItemHasBurnedTime = 0;
        this.currentItemBurnTime = 0;
        this.hu.setHuEnergy(0);
        setActive(false);
    }
    private FluidStack getSelfFluid()
    {
        return this.inputFluidTank.getFluid();
    }
    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult)
    {
        if (isActive)
            attackPlayer(playerIn);
        if (!getWorld().isRemote && !playerIn.isSneaking() && facing == getFrontFacing())
        {
            ItemStack filleditem = CraftingGetItemUtils.getItemStackFromCrt("<forge:bucketfilled>",0);
            if (!playerIn.getHeldItem(hand).isEmpty() && LiquidBurringInfo.ContainsFuel(playerIn.getHeldItem(hand)) && playerIn.getHeldItem(hand).getItem()!=filleditem.getItem())
            {
                net.minecraftforge.fluids.capability.IFluidHandlerItem item = playerIn.getHeldItem(hand).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY,null);
                GTTransferUtils.transferFluids(item,this.importFluids);
                return true;
            }
            if (!playerIn.getHeldItem(hand).isEmpty())
            {
                ItemStack item = playerIn.getHeldItem(hand);
                if (item.getItem() == Items.FLINT_AND_STEEL)
                {
                    item.damageItem(1, playerIn);
                    if (random.nextInt(4) == 1 && canActive())
                    {
                        this.setActive(true);
                    }
                }
                if(  playerIn.getHeldItem(hand).getItem()!=filleditem.getItem() && playerIn.getHeldItem(hand).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY,null))
                {
                    net.minecraftforge.fluids.capability.IFluidHandlerItem cap = playerIn.getHeldItem(hand).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY,null);
                    if (this.outputFluidTank.getFluid()!=null && outputFluidTank.getFluidAmount()>0)
                    {
                        int fluidInserted = cap.fill(getSelfFluid(), true);
                        outputFluidTank.drain(fluidInserted,true);
                    }
                    else if (this.inputFluidTank.getFluid()!=null && this.inputFluidTank.getFluidAmount()>0)
                    {
                        int fluidInserted = cap.fill(getSelfFluid(), true);
                        this.inputFluidTank.drain(fluidInserted,true);
                    }
                }
                return true;
            }
        }
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }
    public boolean canActive()
    {
        if(this.inputFluidTank.getFluidAmount()==0)
            return false;
        return this.inputFluidTank.getFluidAmount() * LiquidBurringInfo.getMlHu(this.inputFluidTank.getFluid()) >= this.outPutHu;
    }
    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityHandler.CAPABILITY_HEAT_ENERGY && facing == EnumFacing.UP)
            return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityHandler.CAPABILITY_HEAT_ENERGY && side == EnumFacing.UP)
            return CapabilityHandler.CAPABILITY_HEAT_ENERGY.cast(hu);
        return super.getCapability(capability, side);
    }




    private class FliterFluidTank extends  FluidTank{

        public FliterFluidTank(int capacity) {
            super(capacity);
        }
        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            if(!LiquidBurringInfo.ContainsFuel(fluid))
                return false;
            return super.canFillFluidType(fluid);
        }

    }
}
