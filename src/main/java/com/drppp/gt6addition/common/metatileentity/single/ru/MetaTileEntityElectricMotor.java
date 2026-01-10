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
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import lombok.var;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class MetaTileEntityElectricMotor extends TieredMetaTileEntity implements IEnergyOutShow {
    public final int color;
    public final double efficiency;
    public final int outPutRu;//最少输出一半  最多输出两倍
    public  int minEuUse;//最少输出一半  最多输出两倍
    public  int maxEuUse;//最少输出一半  最多输出两倍
    protected final ICubeRenderer rendererBASE = Gt6AdditionTextures.RU_ELECTRIC_MOTOR;
    private final Random random = new Random();
    public boolean isActive;
    IRotationEnergy ru = new RotationEnergyHandler();
    public MetaTileEntityElectricMotor(ResourceLocation metaTileEntityId, int tier, int color, double efficiency, int outPutRu) {
        super(metaTileEntityId, tier);
        this.color = color;
        this.efficiency = efficiency;
        this.outPutRu = outPutRu;
        this.minEuUse = (int)(this.outPutRu/this.efficiency);
        this.maxEuUse = (int)(this.outPutRu*2/this.efficiency);
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
    protected long getMaxInputOutputAmperage() {
        return 4L;
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
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        isActive = data.getBoolean("isActive");
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
        if (this.hasFrontFacing() && this.getFrontFacing() == facing) {
            return false;
        }
        return true;
    }
    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityElectricMotor(this.metaTileEntityId,this.getTier(), this.color, this.efficiency, this.outPutRu);
    }
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.ru.em_generator.info.1", this.efficiency * 100 + "%"));
        tooltip.add(I18n.format("gt6addition.ru.em_generator.info.2", this.outPutRu,this.outPutRu/2,this.outPutRu*2));
        tooltip.add(I18n.format("gt6addition.ru.em_generator.info.3"));
        tooltip.add(I18n.format("gt6addition.ru.em_generator.info.4"));
        tooltip.add(I18n.format("gt6addition.ru.em_generator.info.5"));
    }

    @Override
    public void update() {
        super.update();
        if(!getWorld().isRemote)
        {
            if(this.energyContainer.getEnergyStored()<this.minEuUse)
            {
                clearOut();
                return;
            }
            else
                setActive(true);
            if(this.energyContainer.getEnergyStored()>=this.maxEuUse)
            {
                this.energyContainer.removeEnergy(this.maxEuUse);
                this.ru.setRuEnergy(this.outPutRu*2);
            }else
            {
                //消耗所有EU 计算产出
                var amount = this.energyContainer.getEnergyStored();
                this.energyContainer.removeEnergy(this.energyContainer.getEnergyStored());
                this.ru.setRuEnergy((int)(amount*this.efficiency/2));
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
                    float rotationSpeed = 6.0f; // 对应3秒一圈
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

    private void clearOut()
    {
        this.ru.setRuEnergy(0);
        if(this.isActive)
            setActive(false);
    }
    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityHandler.CAPABILITY_ROTATION_ENERGY && facing == this.frontFacing)
            return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityHandler.CAPABILITY_ROTATION_ENERGY && side == this.frontFacing)
            return CapabilityHandler.CAPABILITY_ROTATION_ENERGY.cast(ru);
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
}
