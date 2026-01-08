package com.drppp.gt6addition.api.baseMTile;

import com.drppp.gt6addition.api.recipeLogic.RecipeLogicMutiEnergy;
import com.drppp.gt6addition.api.utils.MachineEnergyAcceptFacing;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.util.ResourceLocation;

public class MetaTileEntityColorOvenMachine extends MetaTileEntityColorMachine {

    public MetaTileEntityColorOvenMachine(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, ICubeRenderer renderer, int tier, boolean hasFrontFacing, String type, MachineEnergyAcceptFacing[] acceptFacing, int color) {
        super(metaTileEntityId, recipeMap, renderer, tier, hasFrontFacing, type, acceptFacing, color);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityColorOvenMachine(metaTileEntityId, workable.getRecipeMap(), renderer, getTier(),
                this.hasFrontFacing,this.EnergyType,this.acceptFacing,this.color);
    }

    @Override
    protected AbstractRecipeLogic createWorkable(RecipeMap<?> recipeMap) {
        return new RecipeLogicOven(this, recipeMap, this.mutiEnergyProxy);
    }

    private static class RecipeLogicOven extends RecipeLogicMutiEnergy
    {

        public RecipeLogicOven(MetaTileEntity tileEntity, RecipeMap<?> recipeMap, IMutiEnergyProxy mutiEnergyProxy) {
            super(tileEntity, recipeMap, mutiEnergyProxy);
        }

        @Override
        public int getMaxProgress() {
            if(this.mutiEnergyProxy==null || this.mutiEnergyProxy.getEnergy()==0)
                return super.getMaxProgress();
            if(this.mutiEnergyProxy.getEnergy()/256 >1)
            {
                this.setParallelLimit(this.mutiEnergyProxy.getEnergy()/256);
            }else
            {
                this.setParallelLimit(1);
            }
            return Math.max(256/this.mutiEnergyProxy.getEnergy(),1);
        }
    }
}
