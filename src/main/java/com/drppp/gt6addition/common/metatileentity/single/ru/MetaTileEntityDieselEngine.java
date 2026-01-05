package com.drppp.gt6addition.common.metatileentity.single.ru;


import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.api.capability.CapabilityHandler;
import com.drppp.gt6addition.api.capability.impl.RotationEnergyHandler;
import com.drppp.gt6addition.api.capability.interfaces.IRotationEnergy;
import com.drppp.gt6addition.api.top.IEnergyOutShow;
import com.drppp.gt6addition.client.Gt6AdditionTextures;
import com.drppp.gt6addition.common.metatileentity.single.hu.LiquidBurringInfo;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IFilter;
import gregtech.api.capability.impl.CommonFluidFilters;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import lombok.var;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityDieselEngine extends MetaTileEntity implements IEnergyOutShow {
    public final int color;
    public final int outPutRu;//最少输出一半  最多输出两倍
    protected final ICubeRenderer rendererBASE = Gt6AdditionTextures.RU_DIESEL_ENGINE;
    public boolean isActive;
    private final int TANK_CAPACITY=1000;
    IRotationEnergy ru = new RotationEnergyHandler();

    protected FluidTank fuelFluidTank;
    protected FluidTank outFluidTank;
    //获取流体燃烧时间
    public int currentItemBurnTime = 0;
    public int currentItemHasBurnedTime = 0;

    public MetaTileEntityDieselEngine(ResourceLocation metaTileEntityId, int color,  int outPutRu) {
        super(metaTileEntityId);
        this.color = color;
        this.outPutRu = outPutRu;
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
        this.fuelFluidTank = new FliterFluidTank(TANK_CAPACITY);
        return new FluidTankList(false, fuelFluidTank);
        

    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        this.outFluidTank = new FliterFluidTank(TANK_CAPACITY);
        return new FluidTankList(false, outFluidTank);
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
        return new MetaTileEntityDieselEngine(this.metaTileEntityId, this.color,  this.outPutRu);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @SideOnly(Side.CLIENT)
    protected SimpleSidedCubeRenderer getBaseRenderer() {
        return Gt6AdditionTextures.BASE_NULL_TEXTURE;
    }

    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Gt6AdditionTextures.BASE_NULL_TEXTURE.getParticleSprite(), this.color);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        IVertexOperation[] pipeline1 = ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(this.color)));
        this.getBaseRenderer().render(renderState, translation, pipeline1);

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
    public boolean isValidFrontFacing(EnumFacing facing) {
        return !this.hasFrontFacing() || this.getFrontFacing() != facing;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.ru.de_generator.info.1", 100 + "%"));
        tooltip.add(I18n.format("gt6addition.ru.de_generator.info.2", this.outPutRu));
        tooltip.add(I18n.format("gt6addition.ru.de_generator.info.3"));
        tooltip.add(I18n.format("gt6addition.ru.de_generator.info.4"));
        tooltip.add(I18n.format("gt6addition.ru.de_generator.info.5"));
    }
    public boolean canActive()
    {
        if(this.fuelFluidTank.getFluidAmount()==0)
            return false;
        return this.fuelFluidTank.getFluidAmount() * LiquidBurringInfo.getMlHu(this.fuelFluidTank.getFluid())*2 >= this.outPutRu;
    }
    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            if(isActive)
            {
                if(this.currentItemHasBurnedTime>= this.currentItemBurnTime)
                {
                    if (!canActive() || this.outFluidTank.getFluidAmount() >= TANK_CAPACITY)
                    {
                        clearOut();
                        return;
                    }
                    //消耗1ml燃料 计算当前燃值
                    var mlhu = LiquidBurringInfo.getMlHu(this.fuelFluidTank.getFluid())*2;
                    if(getWorld().getBlockState(getPos().offset(this.getFrontFacing().getOpposite())).getBlock()!= Blocks.AIR)
                        this.outFluidTank.fill(Materials.CarbonDioxide.getFluid(100),true);
                    if(mlhu>this.outPutRu)
                    {
                        this.currentItemBurnTime = mlhu;
                        this.fuelFluidTank.drain(1,true);
                        this.outFluidTank.fill(Materials.CarbonDioxide.getFluid(100), true);
                    }else {
                        int amount = this.outPutRu/mlhu + this.outPutRu%mlhu;
                        this.currentItemBurnTime = mlhu *amount;
                        this.fuelFluidTank.drain(amount,true);
                        this.outFluidTank.fill(Materials.CarbonDioxide.getFluid(100),true);
                    }
                    this.ru.setRuEnergy(this.outPutRu);
                }else
                {
                    this.currentItemHasBurnedTime += this.outPutRu;
                    this.ru.setRuEnergy(this.outPutRu);
                }

            }else
            {
                if (this.fuelFluidTank.getFluidAmount() <=0 || this.outFluidTank.getFluidAmount() >= TANK_CAPACITY) {
                    clearOut();
                } else
                    setActive(true);
            }

        }else
        {
            //如果朝上 让上面的实体旋转
            if (this.isActive && this.frontFacing == EnumFacing.UP) {
                // 获取方块正上方位置
                BlockPos abovePos = this.getPos().up();
                // 获取方块上方位置内的所有实体
                List<Entity> entities = this.getWorld().getEntitiesWithinAABB(Entity.class,
                        new AxisAlignedBB(abovePos));
                for (Entity entity : entities) {
                    // 每tick旋转的角度 = 360度 / (秒数 * 20 ticks/秒)
                    // 例如：3秒一圈 = 360 / (3 * 20) = 6度/tick
                    float rotationSpeed = 1.0f; // 对应3秒一圈
                    // 获取实体当前旋转角度
                    float currentYaw = entity.rotationYaw;
                    float currentPitch = entity.rotationPitch;
                    // 增加旋转角度
                    float newYaw = currentYaw + rotationSpeed;
                    if (newYaw >= 360.0f) {
                        newYaw -= 360.0f;
                    }
                    // 设置实体新的旋转角度
                    entity.rotationYaw = newYaw;
                    entity.prevRotationYaw = newYaw; // 确保旋转平滑
                }
            }
        }
    }

    private void clearOut() {
        this.ru.setRuEnergy(0);
        if (this.isActive)
            setActive(false);
    }


    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityHandler.CAPABILITY_ROTATION_ENERGY && facing == this.frontFacing)
            return true;
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityHandler.CAPABILITY_ROTATION_ENERGY && side == this.frontFacing)
            return CapabilityHandler.CAPABILITY_ROTATION_ENERGY.cast(ru);
        else if(capability== CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && side==this.frontFacing.getOpposite())
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.outFluidTank);
        else if(capability== CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && side!=this.frontFacing && side!=this.frontFacing.getOpposite())
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.fuelFluidTank);
        else if(capability== CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return null;

        return super.getCapability(capability, side);
    }

    @Override
    public String getEnergyName() {
        return "RU";
    }

    @Override
    public int getEnergyOut() {
        return this.ru.getEnergyOutput();
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
