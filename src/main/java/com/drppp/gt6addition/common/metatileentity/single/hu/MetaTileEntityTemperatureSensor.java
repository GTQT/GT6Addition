package com.drppp.gt6addition.common.metatileentity.single.hu;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.api.temperature.ITemperatureProvider;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.TextFieldWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityTemperatureSensor extends MetaTileEntity {

    public static final int MODE_DISPLAY = 0;
    public static final int MODE_PERCENT = 1;
    public static final int MODE_GREATER = 2;
    public static final int MODE_EQUAL = 3;
    public static final int MODE_SMALLER = 4;
    public static final int MODE_SCALE = 5;
    public static final int MODE_FULL = 6;
    public static final int MODE_NOT_FULL = 7;

    private static final int DATA_STATE = 203;
    private static final String NBT_TARGET = "Target";
    private static final String NBT_MODE = "Mode";
    private static final String NBT_SET_TEMPERATURE = "SetTemperature";
    private static final String NBT_CURRENT = "CurrentTemperature";
    private static final String NBT_MAX = "MaxTemperature";
    private static final String NBT_REDSTONE = "Redstone";
    private static final int MAX_SET_TEMPERATURE = 99999;
    private static final String[] MODE_KEYS = {
            "gt6addition.machine.temperature_sensor.mode.display",
            "gt6addition.machine.temperature_sensor.mode.percent",
            "gt6addition.machine.temperature_sensor.mode.greater",
            "gt6addition.machine.temperature_sensor.mode.equal",
            "gt6addition.machine.temperature_sensor.mode.smaller",
            "gt6addition.machine.temperature_sensor.mode.scale",
            "gt6addition.machine.temperature_sensor.mode.full",
            "gt6addition.machine.temperature_sensor.mode.not_full"
    };

    private final int casingColor;
    private final float hardness;
    private final float resistance;

    private EnumFacing targetFacing = EnumFacing.DOWN;
    private int mode = MODE_GREATER;
    private int setTemperature = 1200;
    private long currentTemperature;
    private long maxTemperature;
    private int redstoneOutput;
    private boolean initializedTarget;
    private boolean updatingRedstoneSignals;
    private boolean redstoneSignalsInitialized;
    private EnumFacing appliedTargetFacing = EnumFacing.DOWN;
    private int appliedRedstoneOutput = -1;

    public MetaTileEntityTemperatureSensor(ResourceLocation metaTileEntityId, int casingColor,
                                           float hardness, float resistance) {
        super(metaTileEntityId);
        this.casingColor = casingColor;
        this.hardness = hardness;
        this.resistance = resistance;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityTemperatureSensor(metaTileEntityId, casingColor, hardness, resistance);
    }

    @Override
    public void onPlacement(EntityLivingBase placer) {
        super.onPlacement(placer);
        setFrontFacing(getFrontFacing().getOpposite());
        targetFacing = getFrontFacing();
        initializedTarget = true;
        markDirty();
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            ensureDefaultTarget();
            if (getOffsetTimer() % 5 == 0) {
                updateReadingAndRedstone();
            }
        }
    }

    @Override
    public void onNeighborChanged() {
        super.onNeighborChanged();
        if (getWorld() != null && !getWorld().isRemote && !updatingRedstoneSignals) {
            updateReadingAndRedstone();
        }
    }

    private void ensureDefaultTarget() {
        if (!initializedTarget) {
            targetFacing = getFrontFacing();
            initializedTarget = true;
            markDirty();
        }
    }

    private void updateReadingAndRedstone() {
        long oldCurrent = currentTemperature;
        long oldMax = maxTemperature;
        int oldOutput = redstoneOutput;
        TemperatureReading reading = readTargetTemperature();
        currentTemperature = reading.current;
        maxTemperature = reading.max;
        redstoneOutput = calculateRedstoneOutput();
        applyRedstoneSignals();
        if (oldCurrent != currentTemperature || oldMax != maxTemperature || oldOutput != redstoneOutput) {
            markDirty();
            writeCustomData(DATA_STATE, this::writeState);
        }
    }

    private TemperatureReading readTargetTemperature() {
        World world = getWorld();
        if (world == null) {
            return TemperatureReading.EMPTY;
        }
        BlockPos targetPos = getPos().offset(targetFacing);
        Object metaTileEntity = GTUtility.getMetaTileEntity(world, targetPos);
        if (metaTileEntity instanceof ITemperatureProvider) {
            ITemperatureProvider provider = (ITemperatureProvider) metaTileEntity;
            EnumFacing side = targetFacing.getOpposite();
            return new TemperatureReading(
                    Math.max(0L, provider.getTemperatureValue(side)),
                    Math.max(0L, provider.getTemperatureMax(side)));
        }
        return TemperatureReading.EMPTY;
    }

    private int calculateRedstoneOutput() {
        switch (mode) {
            case MODE_DISPLAY:
                return 0;
            case MODE_PERCENT:
                return maxTemperature <= 0L ? 0 : clampRedstone((int) (currentTemperature * 15L / maxTemperature));
            case MODE_GREATER:
                return currentTemperature > setTemperature ? 15 : 0;
            case MODE_EQUAL:
                return currentTemperature == setTemperature ? 15 : 0;
            case MODE_SMALLER:
                return currentTemperature < setTemperature ? 15 : 0;
            case MODE_SCALE:
                return setTemperature <= 0 ? 0 : clampRedstone((int) (currentTemperature * 15L / setTemperature));
            case MODE_FULL:
                return maxTemperature > 0L && currentTemperature >= maxTemperature ? 15 : 0;
            case MODE_NOT_FULL:
                return maxTemperature > 0L && currentTemperature < maxTemperature ? 15 : 0;
            default:
                return 0;
        }
    }

    private void applyRedstoneSignals() {
        if (redstoneSignalsInitialized && appliedTargetFacing == targetFacing &&
                appliedRedstoneOutput == redstoneOutput) {
            return;
        }
        updatingRedstoneSignals = true;
        try {
            for (EnumFacing facing : EnumFacing.VALUES) {
                setOutputRedstoneSignal(facing, facing == targetFacing ? 0 : redstoneOutput);
            }
        } finally {
            appliedTargetFacing = targetFacing;
            appliedRedstoneOutput = redstoneOutput;
            redstoneSignalsInitialized = true;
            updatingRedstoneSignals = false;
        }
    }

    private int clampRedstone(int value) {
        return Math.max(0, Math.min(15, value));
    }

    private void setTargetFacing(EnumFacing facing) {
        if (facing == null || targetFacing == facing) {
            return;
        }
        targetFacing = facing;
        initializedTarget = true;
        markDirty();
        updateReadingAndRedstone();
        if (getWorld() != null && !getWorld().isRemote) {
            writeCustomData(DATA_STATE, this::writeState);
        }
    }

    private void setMode(int mode) {
        this.mode = Math.max(0, Math.min(MODE_KEYS.length - 1, mode));
        markDirty();
        updateReadingAndRedstone();
    }

    private void setSetTemperature(int temperature) {
        setTemperature = Math.max(0, Math.min(MAX_SET_TEMPERATURE, temperature));
        markDirty();
        updateReadingAndRedstone();
    }

    private void setSetTemperature(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        try {
            setSetTemperature(Integer.parseInt(text));
        } catch (NumberFormatException ignored) {
        }
    }

    private void adjustSetTemperature(int amount) {
        setSetTemperature(setTemperature + amount);
    }

    private boolean isValidTemperatureText(String text) {
        return text != null && text.length() <= 5 && (text.isEmpty() || text.matches("[0-9]+"));
    }

    private String getModeKey() {
        return MODE_KEYS[Math.max(0, Math.min(MODE_KEYS.length - 1, mode))];
    }

    public EnumFacing getTargetFacing() {
        return targetFacing;
    }

    public int getMode() {
        return mode;
    }

    public int getSetTemperature() {
        return setTemperature;
    }

    public long getCurrentTemperature() {
        return currentTemperature;
    }

    public long getMaxTemperature() {
        return maxTemperature;
    }

    public int getRedstoneOutput() {
        return redstoneOutput;
    }

    public String getModeDisplayName() {
        return I18n.format(getModeKey());
    }

    @Override
    protected boolean canMachineConnectRedstone(EnumFacing side) {
        return true;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return true;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(176, 124);
        builder.label(8, 8, "gt6addition.machine.temperature_sensor.gui.title");
        builder.dynamicLabel(8, 22, () -> "Current: " + currentTemperature + " / " + maxTemperature + " K", 0xFFFFFF);
        builder.dynamicLabel(8, 34, () -> "Redstone: " + redstoneOutput + "  Target side: " + targetFacing.getName(), 0xFFFFFF);
        builder.label(8, 50, "gt6addition.machine.temperature_sensor.gui.set_temperature");
        builder.widget(new TextFieldWidget(86, 46, 64, 18, GuiTextures.DISPLAY,
                () -> Integer.toString(setTemperature), this::setSetTemperature)
                .setMaxStringLength(5)
                .setValidator(this::isValidTemperatureText));
        builder.widget(new ClickButtonWidget(8, 68, 26, 18, "-100", data -> adjustSetTemperature(-100)).setButtonTexture(GuiTextures.BUTTON));
        builder.widget(new ClickButtonWidget(36, 68, 24, 18, "-10", data -> adjustSetTemperature(-10)).setButtonTexture(GuiTextures.BUTTON));
        builder.widget(new ClickButtonWidget(62, 68, 22, 18, "-1", data -> adjustSetTemperature(-1)).setButtonTexture(GuiTextures.BUTTON));
        builder.widget(new ClickButtonWidget(92, 68, 22, 18, "+1", data -> adjustSetTemperature(1)).setButtonTexture(GuiTextures.BUTTON));
        builder.widget(new ClickButtonWidget(116, 68, 24, 18, "+10", data -> adjustSetTemperature(10)).setButtonTexture(GuiTextures.BUTTON));
        builder.widget(new ClickButtonWidget(142, 68, 26, 18, "+100", data -> adjustSetTemperature(100)).setButtonTexture(GuiTextures.BUTTON));
        builder.label(8, 92, "gt6addition.machine.temperature_sensor.gui.mode");
        builder.widget(new CycleButtonWidget(54, 88, 114, 18, MODE_KEYS, this::getMode, this::setMode)
                .setButtonTexture(GuiTextures.BUTTON));
        builder.label(8, 112, "gt6addition.machine.temperature_sensor.gui.target");
        builder.widget(new CycleButtonWidget(54, 108, 114, 18, EnumFacing.class, this::getTargetFacing, this::setTargetFacing)
                .setButtonTexture(GuiTextures.BUTTON));
        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing side) {
        if (capability == CapabilityEnergy.ENERGY || capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return false;
        }
        return super.hasCapability(capability, side);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityEnergy.ENERGY || capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return null;
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] shellPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(casingColor)));
        Textures.SOLID_STEEL_CASING.render(renderState, translation, shellPipeline, getBodyBox());
        IVertexOperation[] displayPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(redstoneOutput > 0 ? 0xD84B2A : 0x243447)));
        Textures.SOLID_STEEL_CASING.render(renderState, translation, displayPipeline, getDisplayBox());
    }

    private Cuboid6 getBodyBox() {
        switch (getFrontFacing()) {
            case SOUTH:
                return new Cuboid6(0.0D, 0.0D, 0.875D, 1.0D, 1.0D, 1.0D);
            case WEST:
                return new Cuboid6(0.0D, 0.0D, 0.0D, 0.125D, 1.0D, 1.0D);
            case EAST:
                return new Cuboid6(0.875D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
            case UP:
                return new Cuboid6(0.0D, 0.875D, 0.0D, 1.0D, 1.0D, 1.0D);
            case DOWN:
                return new Cuboid6(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);
            case NORTH:
            default:
                return new Cuboid6(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.125D);
        }
    }

    private Cuboid6 getDisplayBox() {
        switch (getFrontFacing()) {
            case SOUTH:
                return new Cuboid6(0.25D, 0.25D, 0.9375D, 0.75D, 0.75D, 1.0D);
            case WEST:
                return new Cuboid6(0.0D, 0.25D, 0.25D, 0.0625D, 0.75D, 0.75D);
            case EAST:
                return new Cuboid6(0.9375D, 0.25D, 0.25D, 1.0D, 0.75D, 0.75D);
            case UP:
                return new Cuboid6(0.25D, 0.9375D, 0.25D, 0.75D, 1.0D, 0.75D);
            case DOWN:
                return new Cuboid6(0.25D, 0.0D, 0.25D, 0.75D, 0.0625D, 0.75D);
            case NORTH:
            default:
                return new Cuboid6(0.25D, 0.25D, 0.0D, 0.75D, 0.75D, 0.0625D);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED;
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
    public BlockFaceShape getFaceShape(EnumFacing side) {
        return side == getFrontFacing().getOpposite() ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    @Override
    public void addCollisionBoundingBox(List<IndexedCuboid6> collisionList) {
        collisionList.add(new IndexedCuboid6(null, getBodyBox()));
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
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.machine.temperature_sensor.tooltip.1"));
        tooltip.add(I18n.format("gt6addition.machine.temperature_sensor.tooltip.2"));
        tooltip.add(I18n.format("gt6addition.machine.temperature_sensor.tooltip.3"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setByte(NBT_TARGET, (byte) targetFacing.ordinal());
        data.setByte(NBT_MODE, (byte) mode);
        data.setInteger(NBT_SET_TEMPERATURE, setTemperature);
        data.setLong(NBT_CURRENT, currentTemperature);
        data.setLong(NBT_MAX, maxTemperature);
        data.setByte(NBT_REDSTONE, (byte) redstoneOutput);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        int targetOrdinal = data.getByte(NBT_TARGET);
        if (targetOrdinal >= 0 && targetOrdinal < EnumFacing.VALUES.length) {
            targetFacing = EnumFacing.VALUES[targetOrdinal];
            initializedTarget = true;
        }
        mode = Math.max(0, Math.min(MODE_KEYS.length - 1, data.getByte(NBT_MODE)));
        setTemperature = Math.max(0, Math.min(MAX_SET_TEMPERATURE, data.getInteger(NBT_SET_TEMPERATURE)));
        currentTemperature = data.getLong(NBT_CURRENT);
        maxTemperature = data.getLong(NBT_MAX);
        redstoneOutput = clampRedstone(data.getByte(NBT_REDSTONE));
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        writeState(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        readState(buf);
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == DATA_STATE) {
            readState(buf);
            scheduleRenderUpdate();
        }
    }

    private void writeState(PacketBuffer buf) {
        buf.writeByte(targetFacing.ordinal());
        buf.writeVarInt(mode);
        buf.writeVarInt(setTemperature);
        buf.writeLong(currentTemperature);
        buf.writeLong(maxTemperature);
        buf.writeVarInt(redstoneOutput);
    }

    private void readState(PacketBuffer buf) {
        int targetOrdinal = buf.readByte();
        if (targetOrdinal >= 0 && targetOrdinal < EnumFacing.VALUES.length) {
            targetFacing = EnumFacing.VALUES[targetOrdinal];
            initializedTarget = true;
        }
        mode = Math.max(0, Math.min(MODE_KEYS.length - 1, buf.readVarInt()));
        setTemperature = Math.max(0, Math.min(MAX_SET_TEMPERATURE, buf.readVarInt()));
        currentTemperature = buf.readLong();
        maxTemperature = buf.readLong();
        redstoneOutput = clampRedstone(buf.readVarInt());
    }

    private static class TemperatureReading {
        private static final TemperatureReading EMPTY = new TemperatureReading(0L, 0L);
        private final long current;
        private final long max;

        private TemperatureReading(long current, long max) {
            this.current = current;
            this.max = max;
        }
    }
}
