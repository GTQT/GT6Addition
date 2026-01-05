package com.drppp.gt6addition.api.recipeLogic;

import com.drppp.gt6addition.api.baseMTile.IMutiEnergyProxy;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.OCParams;
import gregtech.api.recipes.logic.OCResult;
import gregtech.api.recipes.properties.RecipePropertyStorage;
import org.jetbrains.annotations.NotNull;

import static gregtech.api.recipes.logic.OverclockingLogic.subTickNonParallelOC;

public class RecipeLogicMutiEnergy extends AbstractRecipeLogic {
    IMutiEnergyProxy mutiEnergyProxy;
    public RecipeLogicMutiEnergy(MetaTileEntity tileEntity, RecipeMap<?> recipeMap, IMutiEnergyProxy mutiEnergyProxy) {
        super(tileEntity, recipeMap);
        this.mutiEnergyProxy = mutiEnergyProxy;
        setMaximumOverclockVoltage(getMaxVoltage());
    }

    public RecipeLogicMutiEnergy(MetaTileEntity tileEntity, RecipeMap<?> recipeMap, boolean hasPerfectOC, IMutiEnergyProxy mutiEnergyProxy) {
        super(tileEntity, recipeMap, hasPerfectOC);
        this.mutiEnergyProxy = mutiEnergyProxy;
        this.setParallelLimit(1);
    }
    @Override
    protected long getEnergyInputPerSecond() {
        if(mutiEnergyProxy==null)
            return 0;
        return mutiEnergyProxy.getEnergy()*GTValues.V[this.mutiEnergyProxy.getTire()];
    }

    @Override
    protected long getEnergyStored() {
        if(mutiEnergyProxy==null)
            return 0;
        return mutiEnergyProxy.getEnergy()*GTValues.V[this.mutiEnergyProxy.getTire()];
    }

    @Override
    protected long getEnergyCapacity() {
        if(mutiEnergyProxy==null)
            return 0;
        return mutiEnergyProxy.getEnergy()*GTValues.V[this.mutiEnergyProxy.getTire()];
    }

    @Override
    protected boolean drawEnergy(long recipeEUt, boolean b) {
        if(mutiEnergyProxy==null)
            return false;
        if(mutiEnergyProxy.getEnergy()>=recipeEUt)
            return true;
        return false;
    }
    @Override
    protected boolean hasEnoughPower(long eut, int duration) {
        long recipeEUt = eut;
        if (recipeEUt >= 0) {
            return this.mutiEnergyProxy.getEnergy() >= recipeEUt;
        }
        return false;
    }

    @Override
    public long getMaxVoltage() {
        return  GTValues.V[mutiEnergyProxy.getTire()];
    }

    @Override
    public void setParallelRecipesPerformed(int amount) {
        super.setParallelRecipesPerformed(amount);
    }

    @Override
    public void setParallelLimit(int amount) {
        super.setParallelLimit(amount);
    }

    @Override
    protected void updateRecipeProgress() {
        if (this.canRecipeProgress && this.drawEnergy(this.recipeEUt, true)) {
            if (++this.progressTime > this.getMaxProgress()) {
                this.completeRecipe();
            }
            if (this.hasNotEnoughEnergy && this.getEnergyInputPerSecond() >this.recipeEUt) {
                this.hasNotEnoughEnergy = false;
            }
        } else if (this.recipeEUt > 0) {
            this.hasNotEnoughEnergy = true;
            this.decreaseProgress();
        }

    }
    @Override
    protected void modifyOverclockPre(@NotNull OCParams ocParams, @NotNull RecipePropertyStorage storage) {

    }

    @Override
    public int getMaxProgress() {
        return super.getMaxProgress();
    }
}
