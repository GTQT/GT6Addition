package com.drppp.gt6addition.common.metatileentity.single.lu;

import com.drppp.gt6addition.api.baseMTile.IMutiEnergyProxy;
import com.drppp.gt6addition.api.baseMTile.MetaTileEntityColorMachine;
import com.drppp.gt6addition.api.recipeLogic.RecipeLogicMutiEnergy;
import com.drppp.gt6addition.api.utils.MachineEnergyAcceptFacing;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityLaserEngraver extends MetaTileEntityColorMachine {

    private final SimpleSidedCubeRenderer baseRenderer;

    public MetaTileEntityLaserEngraver(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, ICubeRenderer renderer,
                                       int tier, MachineEnergyAcceptFacing[] acceptFacing) {
        super(metaTileEntityId, recipeMap, renderer, tier, true, "LU", acceptFacing, 0xFFFFFF);
        this.baseRenderer = new SimpleSidedCubeRenderer("gt6addition:machines/lu_machines/laser_engraver/colored");
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLaserEngraver(metaTileEntityId, workable.getRecipeMap(), renderer, getTier(), acceptFacing);
    }

    @Override
    protected AbstractRecipeLogic createWorkable(RecipeMap<?> recipeMap) {
        return new LaserEngraverRecipeLogic(this, recipeMap, mutiEnergyProxy, parallel);
    }

    @Override
    protected SimpleSidedCubeRenderer getBaseRenderer() {
        return baseRenderer;
    }

    @Override
    public int getPaintingColorForRendering() {
        return 0xFFFFFF;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.lu.engraver.info.1"));
        tooltip.add(I18n.format("gt6addition.lu.engraver.info.2"));
        tooltip.add(I18n.format("gt6addition.lu.engraver.info.3"));
    }

    private static class LaserEngraverRecipeLogic extends RecipeLogicMutiEnergy {

        private LaserEngraverRecipeLogic(MetaTileEntity tileEntity, RecipeMap<?> recipeMap,
                                         IMutiEnergyProxy mutiEnergyProxy, int parallel) {
            super(tileEntity, recipeMap, mutiEnergyProxy, parallel);
        }

        @Override
        protected void setupRecipe(Recipe recipe) {
            super.setupRecipe(recipe);
            setRecipeEUt(Math.max(1L, (long) Math.ceil(getRecipeEUt() * 0.5D)));
            setMaxProgress(Math.max(1, (int) Math.ceil(getMaxProgress() * 0.75D)));
        }
    }
}
