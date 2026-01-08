package com.drppp.gt6addition.api.baseMTile;

import com.drppp.gt6addition.api.utils.MachineEnergyAcceptFacing;
import com.drppp.gt6addition.client.Gt6AdditionTextures;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import net.minecraft.util.ResourceLocation;

public class MetaTileEntityColorMachine extends MetaTileEntityMutiEnergyMachine{
    public int color;
    public MetaTileEntityColorMachine(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, ICubeRenderer renderer, int tier, boolean hasFrontFacing, String type, MachineEnergyAcceptFacing[] acceptFacing, int color) {
        super(metaTileEntityId, recipeMap, renderer, tier, hasFrontFacing, type, acceptFacing);
        this.color=color;
    }
    public MetaTileEntityColorMachine(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, ICubeRenderer renderer, int tier, boolean hasFrontFacing, String type, MachineEnergyAcceptFacing[] acceptFacing, int color, int parallel) {
        super(metaTileEntityId, recipeMap, renderer, tier, hasFrontFacing, type, acceptFacing,parallel);
        this.color=color;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityColorMachine(metaTileEntityId, workable.getRecipeMap(), renderer, getTier(),
                this.hasFrontFacing,this.EnergyType,this.acceptFacing,this.color,this.parallel);
    }

    @Override
    protected SimpleSidedCubeRenderer getBaseRenderer() {
        return Gt6AdditionTextures.BASE_NULL_TEXTURE;
    }

    @Override
    public int getPaintingColorForRendering() {
        return this.color;
    }

}
