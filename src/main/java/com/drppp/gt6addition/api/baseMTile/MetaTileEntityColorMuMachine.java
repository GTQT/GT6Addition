package com.drppp.gt6addition.api.baseMTile;

import com.drppp.gt6addition.api.recipeLogic.RecipeLogicMutiEnergy;
import com.drppp.gt6addition.api.utils.MachineEnergyAcceptFacing;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.util.ResourceLocation;

public class MetaTileEntityColorMuMachine extends MetaTileEntityColorMachine {

    public MetaTileEntityColorMuMachine(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, ICubeRenderer renderer, int tier, boolean hasFrontFacing, String type, MachineEnergyAcceptFacing[] acceptFacing, int color) {
        super(metaTileEntityId, recipeMap, renderer, tier, hasFrontFacing, type, acceptFacing, color);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityColorMuMachine(metaTileEntityId, workable.getRecipeMap(), renderer, getTier(),
                this.hasFrontFacing,this.EnergyType,this.acceptFacing,this.color);
    }

    @Override
    protected AbstractRecipeLogic createWorkable(RecipeMap<?> recipeMap) {
        return new RecipeLogicMu(this, recipeMap, this.mutiEnergyProxy, getTier() * 2);
    }

    private static class RecipeLogicMu extends RecipeLogicMutiEnergy
    {

        public RecipeLogicMu(MetaTileEntity tileEntity, RecipeMap<?> recipeMap, IMutiEnergyProxy mutiEnergyProxy,int para) {
            super(tileEntity, recipeMap, mutiEnergyProxy,para);
        }

        @Override
        public int getMaxProgress() {
            if(this.mutiEnergyProxy==null || this.mutiEnergyProxy.getEnergy()==0)
                return super.getMaxProgress();
            return Math.max((int)(getMaxProgress()*0.8),1);
        }
    }
}
