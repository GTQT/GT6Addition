package com.drppp.gt6addition.common.metatileentity.single.item;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.drppp.gt6addition.common.metatileentity.single.ku.KineticRenderHelper;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityMortar extends MetaTileEntity {

    private static final double PX = 1.0D / 16.0D;
    private static final Cuboid6 WALL_X_NEG = new Cuboid6(2 * PX, 0.0D, 2 * PX, 4 * PX, 6 * PX, 14 * PX);
    private static final Cuboid6 WALL_X_POS = new Cuboid6(12 * PX, 0.0D, 2 * PX, 14 * PX, 6 * PX, 14 * PX);
    private static final Cuboid6 WALL_Z_NEG = new Cuboid6(2 * PX, 0.0D, 2 * PX, 14 * PX, 6 * PX, 4 * PX);
    private static final Cuboid6 WALL_Z_POS = new Cuboid6(2 * PX, 0.0D, 12 * PX, 14 * PX, 6 * PX, 14 * PX);
    private static final Cuboid6 BOWL_BOTTOM = new Cuboid6(2 * PX, 0.0D, 2 * PX, 14 * PX, PX, 14 * PX);
    private static final Cuboid6 PESTLE = new Cuboid6(6 * PX, 0.0D, 6 * PX, 10 * PX, 9 * PX, 10 * PX);
    private static final Cuboid6 BOUNDS = new Cuboid6(2 * PX, 0.0D, 2 * PX, 14 * PX, 6 * PX, 14 * PX);
    private static final Container DUMMY_CONTAINER = new Container() {
        @Override
        public boolean canInteractWith(@NotNull EntityPlayer playerIn) {
            return false;
        }
    };
    private static final ResourceLocation[] MORTAR_CANDIDATES = {
            new ResourceLocation("gregtech", "mortar"),
            new ResourceLocation("gregtech", "once_mortar"),
            new ResourceLocation("harvestcraft", "mortarandpestleitem"),
            new ResourceLocation("botania", "pestleandmortar")
    };

    private final int bodyColor;
    private final int pestleColor;

    public MetaTileEntityMortar(ResourceLocation metaTileEntityId, int bodyColor, int pestleColor) {
        super(metaTileEntityId);
        this.bodyColor = bodyColor;
        this.pestleColor = pestleColor;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMortar(metaTileEntityId, bodyColor, pestleColor);
    }

    @Override
    public boolean onRightClick(EntityPlayer player, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        if (getWorld().isRemote) {
            return true;
        }
        if (facing != EnumFacing.UP) {
            return true;
        }

        ItemStack heldStack = player.getHeldItem(hand);
        if (heldStack.isEmpty()) {
            return true;
        }

        MortarRecipeMatch match = findMatchingRecipe(heldStack);
        if (match == null || match.output.isEmpty()) {
            return true;
        }

        ItemStack output = match.output.copy();
        if (!player.capabilities.isCreativeMode) {
            heldStack.shrink(match.inputCount);
            player.setHeldItem(hand, heldStack.isEmpty() ? ItemStack.EMPTY : heldStack);
        }
        ItemHandlerHelper.giveItemToPlayer(player, output);
        playMortarSound();
        markDirty();
        return true;
    }

    @Nullable
    private MortarRecipeMatch findMatchingRecipe(ItemStack heldStack) {
        ItemStack singleHeld = GTUtility.copy(1, heldStack);
        if (singleHeld.isEmpty()) {
            return null;
        }

        for (IRecipe recipe : CraftingManager.REGISTRY) {
            if (recipe == null) {
                continue;
            }
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            if (ingredients == null || ingredients.isEmpty()) {
                continue;
            }

            InventoryCrafting inventory = new InventoryCrafting(DUMMY_CONTAINER, 3, 3);
            int heldRequired = 0;
            boolean hasMortar = false;
            boolean valid = true;
            for (int slot = 0; slot < ingredients.size(); slot++) {
                Ingredient ingredient = ingredients.get(slot);
                if (ingredient == Ingredient.EMPTY) {
                    continue;
                }

                ItemStack mortarCandidate = findMatchingMortarTool(ingredient);
                if (!mortarCandidate.isEmpty()) {
                    inventory.setInventorySlotContents(slot, mortarCandidate.copy());
                    hasMortar = true;
                    continue;
                }

                ItemStack matchedHeldIngredient = findMatchingHeldIngredient(ingredient, singleHeld);
                if (matchedHeldIngredient.isEmpty()) {
                    valid = false;
                    break;
                }

                inventory.setInventorySlotContents(slot, matchedHeldIngredient);
                heldRequired++;
            }

            if (!valid || !hasMortar || heldRequired <= 0 || heldStack.getCount() < heldRequired) {
                continue;
            }
            if (!recipe.matches(inventory, getWorld())) {
                continue;
            }

            ItemStack output = recipe.getCraftingResult(inventory);
            if (!output.isEmpty()) {
                return new MortarRecipeMatch(output, heldRequired);
            }
        }
        return null;
    }

    @NotNull
    private ItemStack findMatchingHeldIngredient(Ingredient ingredient, ItemStack heldStack) {
        if (ingredient.apply(heldStack)) {
            return heldStack.copy();
        }
        for (ItemStack candidate : ingredient.getMatchingStacks()) {
            if (stackMatchesIngredient(candidate, heldStack)) {
                return candidate.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    private ItemStack findMatchingMortarTool(Ingredient ingredient) {
        for (ResourceLocation id : MORTAR_CANDIDATES) {
            Item item = Item.REGISTRY.getObject(id);
            if (item == null) {
                continue;
            }
            ItemStack candidate = new ItemStack(item);
            if (ingredient.apply(candidate)) {
                return candidate;
            }
        }
        for (ItemStack stack : ingredient.getMatchingStacks()) {
            if (isMortarStack(stack)) {
                return stack.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean stackMatchesIngredient(ItemStack candidate, ItemStack heldStack) {
        if (candidate.isEmpty() || heldStack.isEmpty() || candidate.getItem() != heldStack.getItem()) {
            return false;
        }
        int candidateMeta = candidate.getMetadata();
        if (candidateMeta != OreDictionary.WILDCARD_VALUE && candidateMeta != heldStack.getMetadata()) {
            return false;
        }
        return !candidate.hasTagCompound() || ItemStack.areItemStackTagsEqual(candidate, heldStack);
    }

    private boolean isMortarStack(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem().getRegistryName() == null) {
            return false;
        }
        String name = stack.getItem().getRegistryName().toString().toLowerCase();
        return name.contains("mortar") || name.contains("pestle");
    }

    private void playMortarSound() {
        SoundEvent soundEvent = SoundEvent.REGISTRY.getObject(new ResourceLocation("gregtech", "use.mortar"));
        if (soundEvent == null) {
            soundEvent = SoundEvents.BLOCK_STONE_HIT;
        }
        getWorld().playSound(null, getPos(), soundEvent, SoundCategory.BLOCKS, 0.8F, 1.0F);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        renderWall(renderState, translation, pipeline, WALL_X_NEG, EnumFacing.WEST);
        renderWall(renderState, translation, pipeline, WALL_X_POS, EnumFacing.EAST);
        renderWall(renderState, translation, pipeline, WALL_Z_NEG, EnumFacing.NORTH);
        renderWall(renderState, translation, pipeline, WALL_Z_POS, EnumFacing.SOUTH);
        renderBase(renderState, translation, pipeline);
        renderPestle(renderState, translation, pipeline);
    }

    @SideOnly(Side.CLIENT)
    private void renderWall(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                            Cuboid6 cuboid, EnumFacing innerFace) {
        for (EnumFacing side : EnumFacing.VALUES) {
            String face;
            if (side == EnumFacing.UP) {
                face = "top";
            } else if (side == innerFace) {
                face = "insides";
            } else if (side == innerFace.getOpposite()) {
                face = "sides";
            } else if (side == EnumFacing.DOWN) {
                continue;
            } else {
                face = "sides";
            }
            KineticRenderHelper.renderFace(renderState, translation, pipeline, side, cuboid,
                    "gt6addition:blocks/machines/tools/mortar/colored/" + face, bodyColor);
            KineticRenderHelper.renderOverlayFace(renderState, translation, pipeline, side, cuboid,
                    "gt6addition:blocks/machines/tools/mortar/overlay/" + face);
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderBase(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        for (EnumFacing side : EnumFacing.VALUES) {
            String face = side == EnumFacing.UP ? "top" : side == EnumFacing.DOWN ? "bottom" : "sides";
            KineticRenderHelper.renderFace(renderState, translation, pipeline, side, BOWL_BOTTOM,
                    "gt6addition:blocks/machines/tools/mortar/colored/" + face, bodyColor);
            KineticRenderHelper.renderOverlayFace(renderState, translation, pipeline, side, BOWL_BOTTOM,
                    "gt6addition:blocks/machines/tools/mortar/overlay/" + face);
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderPestle(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        for (EnumFacing side : EnumFacing.VALUES) {
            String face = side == EnumFacing.UP ? "middletop" : "middleside";
            KineticRenderHelper.renderFace(renderState, translation, pipeline, side, PESTLE,
                    "gt6addition:blocks/machines/tools/mortar/colored/" + face, pestleColor);
            KineticRenderHelper.renderOverlayFace(renderState, translation, pipeline, side, PESTLE,
                    "gt6addition:blocks/machines/tools/mortar/overlay/" + face);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(KineticRenderHelper.getSprite("gt6addition:blocks/machines/tools/mortar/colored/sides"),
                bodyColor);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getLightOpacity() {
        return 1;
    }

    @Override
    public void addCollisionBoundingBox(List<IndexedCuboid6> collisionList) {
        collisionList.add(new IndexedCuboid6(null, WALL_X_NEG));
        collisionList.add(new IndexedCuboid6(null, WALL_X_POS));
        collisionList.add(new IndexedCuboid6(null, WALL_Z_NEG));
        collisionList.add(new IndexedCuboid6(null, WALL_Z_POS));
        collisionList.add(new IndexedCuboid6(null, BOWL_BOTTOM));
        collisionList.add(new IndexedCuboid6(null, PESTLE));
    }

    @Override
    public BlockFaceShape getFaceShape(EnumFacing side) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.machine.mortar.tooltip.1"));
        tooltip.add(I18n.format("gt6addition.machine.mortar.tooltip.2"));
    }

    @Override
    public SoundType getSoundType() {
        return SoundType.STONE;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
    }

    private static final class MortarRecipeMatch {
        private final ItemStack output;
        private final int inputCount;

        private MortarRecipeMatch(ItemStack output, int inputCount) {
            this.output = output;
            this.inputCount = inputCount;
        }
    }
}
