package com.drppp.gt6addition.common.metatileentity.single.lu;

import com.drppp.gt6addition.api.baseMTile.MetaTileEntityColorMachine;
import com.drppp.gt6addition.api.utils.MachineEnergyAcceptFacing;
import com.drppp.gt6addition.client.Gt6AdditionTextures;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityLaserWelder extends MetaTileEntityColorMachine {



    public MetaTileEntityLaserWelder(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, ICubeRenderer renderer,
                                     int tier, MachineEnergyAcceptFacing[] acceptFacing) {
        super(metaTileEntityId, recipeMap, renderer, tier, true, "LU", acceptFacing, 0xFFFFFF);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLaserWelder(metaTileEntityId, workable.getRecipeMap(), renderer, getTier(), acceptFacing);
    }

    @Override
    protected SimpleSidedCubeRenderer getBaseRenderer() {
        return Gt6AdditionTextures.BASE_RENDERER;
    }

    @Override
    public int getPaintingColorForRendering() {
        return 0xFFFFFF;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.lu.welder.info.1"));
    }
}
