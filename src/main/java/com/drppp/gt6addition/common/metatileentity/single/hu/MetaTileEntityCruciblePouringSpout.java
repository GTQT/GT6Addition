package com.drppp.gt6addition.common.metatileentity.single.hu;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.api.crucible.ICrucibleMold;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/** GT6 MultiTileEntityFaucet: the dedicated Crucible Pouring Spout, not a fluid faucet. */
public class MetaTileEntityCruciblePouringSpout extends MetaTileEntity implements ICrucibleMold {

    private static final String NBT_AUTO_PULL = "AutoPull";
    private final int color;
    private final boolean acidProof;
    private final float hardness;
    private final float resistance;
    private final long maxTemperature;
    private boolean autoPull;

    public MetaTileEntityCruciblePouringSpout(ResourceLocation id, int color, boolean acidProof,
                                               float hardness, float resistance, long maxTemperature) {
        super(id);
        this.color = color;
        this.acidProof = acidProof;
        this.hardness = hardness;
        this.resistance = resistance;
        this.maxTemperature = maxTemperature;
    }

    @Override public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tile) {
        return new MetaTileEntityCruciblePouringSpout(metaTileEntityId, color, acidProof, hardness, resistance, maxTemperature);
    }

    @Override public void onPlacement(EntityLivingBase placer) {
        super.onPlacement(placer);
        if (getFrontFacing().getAxis().isHorizontal()) setFrontFacing(getFrontFacing().getOpposite());
    }
    @Override public boolean isValidFrontFacing(EnumFacing side) { return side.getAxis().isHorizontal(); }

    @Override public void update() {
        super.update();
        if (!getWorld().isRemote && (autoPull ? getOffsetTimer() % 20 == 5 : getWorld().isBlockPowered(getPos()))) pullOnce();
    }

    @Override public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing side, CuboidRayTraceResult hit) {
        if (!getWorld().isRemote && !pullOnce()) player.sendStatusMessage(new TextComponentTranslation("gt6addition.machine.crucible_pouring_spout.status.empty"), true);
        return true;
    }

    private boolean pullOnce() {
        Object source = GTUtility.getMetaTileEntity(getWorld(), getPos().offset(getFrontFacing()));
        if (source instanceof MetaTileEntityCrucible) {
            return ((MetaTileEntityCrucible) source).fillMoldAtSide(this, getFrontFacing().getOpposite(), getFrontFacing()) > 0L;
        }
        return source instanceof MetaTileEntityCrucibleCrossing &&
                ((MetaTileEntityCrucibleCrossing) source).fillMoldAtSide(this, getFrontFacing().getOpposite(), getFrontFacing());
    }

    @Override public boolean isMoldInputSide(@Nullable EnumFacing side) { return side == getFrontFacing(); }
    @Override public long getMoldMaxTemperature() { return maxTemperature; }
    @Override public long getMoldRequiredMaterialUnits(@Nullable Material material) { return getDownstreamDemand(material); }

    private long getDownstreamDemand(@Nullable Material material) {
        BlockPos cursor = getPos().down();
        while (cursor.getY() > 0) {
            Object target = GTUtility.getMetaTileEntity(getWorld(), cursor);
            if (target instanceof MetaTileEntityCruciblePouringSpout) { cursor = cursor.down(); continue; }
            if (target instanceof ICrucibleMold) return ((ICrucibleMold) target).getMoldRequiredMaterialUnits(material);
            if (!getWorld().isAirBlock(cursor)) return 0L;
            cursor = cursor.down();
        }
        return 0L;
    }

    @Override public long fillMold(Material material, long amount, long temperature, @Nullable EnumFacing side, boolean simulate) {
        if (side != getFrontFacing() || material == null || amount <= 0L ||
                (!acidProof && material.getName().toLowerCase(Locale.ROOT).contains("acid"))) return 0L;
        if (temperature > maxTemperature) {
            if (!simulate) getWorld().setBlockState(getPos(), Blocks.FLOWING_LAVA.getDefaultState(), 3);
            return 0L;
        }
        BlockPos cursor = getPos().down();
        while (cursor.getY() > 0) {
            Object target = GTUtility.getMetaTileEntity(getWorld(), cursor);
            if (target instanceof MetaTileEntityCruciblePouringSpout) { cursor = cursor.down(); continue; }
            if (target instanceof ICrucibleMold) return ((ICrucibleMold) target).fillMold(material, amount, temperature, EnumFacing.UP, simulate);
            if (!getWorld().isAirBlock(cursor)) return 0L;
            cursor = cursor.down();
        }
        return 0L;
    }

    @Override public void renderMetaTileEntity(CCRenderState state, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] coloured = ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(color)));
        for (Cuboid6 box : geometry()) {
            // GT6 deliberately leaves the crucible-facing side open. Rendering
            // the entire cuboid closes that opening and makes the spout bulky.
            for (EnumFacing side : EnumFacing.VALUES) {
                if (side != getFrontFacing()) {
                    Textures.renderFace(state, translation, coloured, side, box,
                            Textures.SOLID_STEEL_CASING.getParticleSprite(), BlockRenderLayer.CUTOUT_MIPPED);
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

    // Exact three render passes from GT6 MultiTileEntityFaucet.
    private Cuboid6[] geometry() {
        switch (getFrontFacing()) {
            case NORTH: return boxes(6,1,0,10,14,4, 5,2,0,11,10,4, 10,2,0,11,10,4);
            case SOUTH: return boxes(6,1,12,10,14,16, 5,2,12,11,10,16, 10,2,12,11,10,16);
            case WEST: return boxes(0,1,6,4,14,10, 0,2,5,4,10,11, 0,2,10,4,10,11);
            default: return boxes(12,1,6,16,14,10, 12,2,5,16,10,11, 12,2,10,16,10,11);
        }
    }
    private Cuboid6[] boxes(int... c) {
        Cuboid6[] result = new Cuboid6[c.length / 6];
        for (int i = 0; i < result.length; i++) { int p = i * 6; result[i] = new Cuboid6(c[p]/16D,c[p+1]/16D,c[p+2]/16D,c[p+3]/16D,c[p+4]/16D,c[p+5]/16D); }
        return result;
    }

    @Override public boolean isOpaqueCube() { return false; }
    @Override public int getLightOpacity() { return 0; }
    @Override public float getBlockHardness() { return hardness; }
    @Override public float getBlockResistance() { return resistance; }
    @Override @SideOnly(Side.CLIENT) public Pair<TextureAtlasSprite, Integer> getParticleTexture() { return Pair.of(Textures.SOLID_STEEL_CASING.getParticleSprite(), color); }
    @Override public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gt6addition.machine.crucible_pouring_spout.tooltip.transfer"));
        tooltip.add(I18n.format("gt6addition.machine.crucible_pouring_spout.tooltip.redstone"));
        if (acidProof) tooltip.add(I18n.format("gt6addition.machine.crucible_pouring_spout.tooltip.acid_proof"));
    }
    @Override public NBTTagCompound writeToNBT(NBTTagCompound data) { super.writeToNBT(data); data.setBoolean(NBT_AUTO_PULL, autoPull); return data; }
    @Override public void readFromNBT(NBTTagCompound data) { super.readFromNBT(data); autoPull = data.getBoolean(NBT_AUTO_PULL); }
}
