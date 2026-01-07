package com.drppp.gt6addition.common.metatileentity.single.ku;


import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.api.capability.CapabilityHandler;
import com.drppp.gt6addition.api.capability.impl.KineticEnergyHandler;
import com.drppp.gt6addition.api.capability.impl.RotationEnergyHandler;
import com.drppp.gt6addition.api.capability.interfaces.IKineticEnergy;
import com.drppp.gt6addition.api.capability.interfaces.IRotationEnergy;
import com.drppp.gt6addition.api.top.IEnergyOutShow;
import com.drppp.gt6addition.client.Gt6AdditionTextures;
import com.drppp.gt6addition.common.metatileentity.single.hu.LiquidBurringInfo;
import gregtech.api.capability.GregtechDataCodes;
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
import net.minecraft.tileentity.TileEntity;
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
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityRotationEngine extends MetaTileEntity implements IEnergyOutShow {
    public final int color;
    public final int outPutRu;//最少输出一半  最多输出两倍
    public final int outPutRuMin;//最少输出一半  最多输出两倍
    public final int outPutRuMax;//最少输出一半  最多输出两倍
    protected final ICubeRenderer rendererBASE = Gt6AdditionTextures.RU_KU_ENGINE;
    public boolean isActive;
    IKineticEnergy ku = new KineticEnergyHandler();
    public int AllRu=0;

    //获取流体燃烧时间
    public int currentItemBurnTime = 0;
    public int currentItemHasBurnedTime = 0;

    public MetaTileEntityRotationEngine(ResourceLocation metaTileEntityId, int color, int outPutRu) {
        super(metaTileEntityId);
        this.color = color;
        this.outPutRu = outPutRu;
        this.outPutRuMin = outPutRu/2;
        this.outPutRuMax = outPutRu*2;
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
        return new MetaTileEntityRotationEngine(this.metaTileEntityId, this.color,  this.outPutRu);
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

        this.rendererBASE.renderOrientedState(renderState, translation, pipeline1, this.getFrontFacing(), isActive, isActive);
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
        tooltip.add(I18n.format("gt6addition.ku.re_generator.info.1"));
        tooltip.add(I18n.format("gt6addition.ku.re_generator.info.2", this.outPutRu,this.outPutRuMin,this.outPutRuMax));
        tooltip.add(I18n.format("gt6addition.ku.re_generator.info.3"));
        tooltip.add(I18n.format("gt6addition.ku.re_generator.info.4"));
        tooltip.add(I18n.format("gt6addition.ku.re_generator.info.5"));
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && getOffsetTimer()%5==0) {
            var list = getOtherDirections(getFrontFacing(),getFrontFacing().getOpposite());
            AllRu=0;
            for (var l:list)
            {
                TileEntity te = getWorld().getTileEntity(getPos().offset(l));
                if(te !=null && te.hasCapability(CapabilityHandler.CAPABILITY_ROTATION_ENERGY,l.getOpposite()))
                {
                   IRotationEnergy ru = te.getCapability(CapabilityHandler.CAPABILITY_ROTATION_ENERGY,l.getOpposite());
                    if (ru != null) {
                        AllRu += ru.getEnergyOutput();
                    }
                }
            }
            if(AllRu<outPutRuMin)
                this.ku.setKineticEnergy(0);
            else if(AllRu>this.outPutRuMax)
                this.ku.setKineticEnergy(this.outPutRuMax/2);
            else
                this.ku.setKineticEnergy(AllRu/2);
        }
    }
    public List<EnumFacing> getOtherDirections(EnumFacing headFacing, EnumFacing tailFacing) {
        List<EnumFacing> otherDirections = new ArrayList<>();

        for (EnumFacing facing : EnumFacing.VALUES) {
            if (facing != headFacing && facing != tailFacing) {
                otherDirections.add(facing);
            }
        }
        return otherDirections;
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityHandler.CAPABILITY_ROTATION_ENERGY && (facing == this.frontFacing || facing == this.frontFacing.getOpposite()))
            return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityHandler.CAPABILITY_KINETIC_ENERGY && (side == this.frontFacing || side == this.frontFacing.getOpposite()))
            return CapabilityHandler.CAPABILITY_KINETIC_ENERGY.cast(ku);
        return super.getCapability(capability, side);
    }

    @Override
    public String getEnergyName() {
        return "KU";
    }

    @Override
    public int getEnergyOut() {
        return this.ku.getKinetic();
    }



}
