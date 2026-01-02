package com.drppp.gt6addition.common.metatileentity.single.hu;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class LiquidBurringInfo {

    public static List<LiquidAndHu> listFuel = new ArrayList<>();
    public static List<LiquidAndHu> listRocketFuel = new ArrayList<>();
    public static void init()
    {
        Collection<RecipeMap> Recipes = new ArrayList<>();
        Recipes.add(RecipeMaps.COMBUSTION_GENERATOR_FUELS);
        Recipes.add(RecipeMaps.GAS_TURBINE_FUELS);
        for(RecipeMap map:Recipes)
        {
            Collection<Recipe> rc = map.getRecipeList();
            rc.forEach(x->LiquidBurringInfo.initFuelsDta(x,false));
        }
    }

    private static void initFuelsDta(Recipe recipe,boolean rocket) {
        if(recipe!=null)
        {
            if(!rocket)
            {
                int eut =(int) recipe.getEUt();
                int time = recipe.getDuration();
                List<gregtech.api.recipes.ingredients.GTRecipeInput> fluid = recipe.getFluidInputs();
                for (gregtech.api.recipes.ingredients.GTRecipeInput item : fluid)
                {
                    if(!ContainsFuel(item.getInputFluidStack()))
                    {
                        int amount = item.getAmount();
                        LiquidBurringInfo.listFuel.add(new LiquidAndHu(item.getInputFluidStack(),eut*time/amount));
                    }
                }
            }
            else
            {
                int eut =(int) recipe.getEUt();
                int time = recipe.getDuration();
                List<gregtech.api.recipes.ingredients.GTRecipeInput> fluid = recipe.getFluidInputs();
                for (gregtech.api.recipes.ingredients.GTRecipeInput item : fluid)
                {
                    if(!ContainsFuel(item.getInputFluidStack()))
                    {
                        int amount = item.getAmount();
                        LiquidBurringInfo.listRocketFuel.add(new LiquidAndHu(item.getInputFluidStack(),eut*time/amount));
                    }
                }
            }
        }
    }

    public static boolean ContainsFuel(FluidStack source)
    {
        for (int i = 0; i < listFuel.size(); i++)
        {
            if(listFuel.get(i).fuel.isFluidEqual(source))
                return true;
        }
        return false;
    }
    public static boolean ContainsRocketFuel(FluidStack source)
    {
        for (int i = 0; i < listRocketFuel.size(); i++)
        {
            if(listRocketFuel.get(i).fuel.isFluidEqual(source))
                return true;
        }
        return false;
    }
    public static boolean ContainsFuel(ItemStack source)
    {
        if(source.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY,null))
        {
            net.minecraftforge.fluids.capability.IFluidHandlerItem item = source.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY,null);
            FluidStack fluidStack = item.drain(Integer.MAX_VALUE, false);
            return ContainsFuel(fluidStack);
        }
        return false;
    }
    public static int getMlHu(FluidStack fuel)
    {
        for (LiquidAndHu s: LiquidBurringInfo.listFuel)
        {
            if(s.fuel.equals(fuel))
                return s.mlHu;
        }
        return -1;
    }
    public static int getRocketMlHu(FluidStack fuel)
    {
        for (LiquidAndHu s: LiquidBurringInfo.listRocketFuel)
        {
            if(s.fuel.equals(fuel))
                return s.mlHu;
        }
        return -1;
    }
    private static class LiquidAndHu{
        public LiquidAndHu(FluidStack fuel, int mlHu) {
            this.fuel = fuel;
            this.mlHu = mlHu;
        }

        public FluidStack fuel;
        public int mlHu;
    }
}
