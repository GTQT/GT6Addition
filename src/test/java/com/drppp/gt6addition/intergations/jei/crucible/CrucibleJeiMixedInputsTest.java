package com.drppp.gt6addition.intergations.jei.crucible;

import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrucibleJeiMixedInputsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        net.minecraft.init.Bootstrap.register();
    }

    @Test
    void internalOutputKeepsExactAmountAndRegistersOnlyAOneItemPreview() {
        ItemStack icon = new ItemStack(new Item(), 10);
        CrucibleJeiRecipe recipe = new CrucibleJeiRecipe(Collections.emptyList(), Collections.emptyList(),
                "internal material", 10L * gregtech.api.GTValues.M, icon, 4263, true, Collections.emptyList());
        assertTrue(recipe.hasInternalOutput());
        assertEquals(10L * gregtech.api.GTValues.M, recipe.getInternalOutputAmount());
        assertEquals(null, recipe.getOutputFluid());
        assertEquals(1, recipe.getOutputPreview().getCount());
        assertEquals(10, icon.getCount());
        ItemStack copy = recipe.getOutputPreview();
        copy.setCount(7);
        assertEquals(1, recipe.getOutputPreview().getCount());
        Map<Class<?>, Object> outputs = new HashMap<>();
        IIngredients ingredients = (IIngredients) Proxy.newProxyInstance(IIngredients.class.getClassLoader(),
                new Class<?>[]{IIngredients.class}, (proxy, method, args) -> {
                    if ("setOutput".equals(method.getName())) outputs.put((Class<?>) args[0], args[1]);
                    return null;
                });
        recipe.getIngredients(ingredients);
        assertEquals(1, outputs.size());
        assertEquals(1, ((ItemStack) outputs.get(ItemStack.class)).getCount());
    }

    @Test
    void internalMaterialWithoutAnIconStillKeepsItsRecipeAndQuantity() {
        CrucibleJeiRecipe recipe = new CrucibleJeiRecipe(Collections.emptyList(), Collections.emptyList(),
                "internal material", 1, ItemStack.EMPTY, 1000, false, Collections.emptyList());
        assertTrue(recipe.hasInternalOutput());
        assertTrue(recipe.getOutputPreview().isEmpty());
        assertEquals(1, recipe.getInternalOutputAmount());
        assertThrows(IllegalArgumentException.class, () -> new CrucibleJeiRecipe(
                Collections.emptyList(), Collections.emptyList(), "internal material", 0,
                ItemStack.EMPTY, 1000, false, Collections.emptyList()));
    }

    @Test
    void mixedRowsKeepComponentOrderWhileSearchInputsExcludeEmptyPlaceholders() {
        ItemStack first = new ItemStack(new Item());
        ItemStack last = new ItemStack(new Item());
        Fluid inputFluid = new Fluid("crucible_jei_input_test", null, null);
        assertTrue(net.minecraftforge.fluids.FluidRegistry.registerFluid(inputFluid));
        FluidStack fluid = new FluidStack(inputFluid, 1296);
        CrucibleJeiRecipe recipe = new CrucibleJeiRecipe(
                Arrays.asList(Collections.singletonList(first), Collections.emptyList(),
                        Collections.singletonList(last)),
                Arrays.asList(Collections.emptyList(), Collections.singletonList(fluid),
                        Collections.emptyList()), fluid, 1300, true,
                Arrays.asList("first", "fluid", "last"));
        assertEquals(3, recipe.getInputCount());
        assertSame(first, recipe.getItemInputs(0).get(0));
        assertTrue(recipe.getItemInputs(1).isEmpty());
        assertSame(fluid, recipe.getFluidInputs(1).get(0));
        assertSame(last, recipe.getItemInputs(2).get(0));
        assertEquals("fluid", recipe.getComponentInfo().get(1));

        Map<Class<?>, List<?>> registered = new HashMap<>();
        IIngredients ingredients = (IIngredients) Proxy.newProxyInstance(IIngredients.class.getClassLoader(),
                new Class<?>[]{IIngredients.class}, (proxy, method, args) -> {
                    if ("setInputLists".equals(method.getName())) {
                        registered.put((Class<?>) args[0], (List<?>) args[1]);
                    }
                    return null;
                });
        recipe.getIngredients(ingredients);
        assertEquals(2, registered.get(ItemStack.class).size());
        assertEquals(1, registered.get(FluidStack.class).size());
        assertSame(fluid, ((List<?>) registered.get(FluidStack.class).get(0)).get(0));
    }

    @Test
    void legacyItemOnlyConstructorCreatesMatchingEmptyFluidRows() {
        CrucibleJeiRecipe recipe = new CrucibleJeiRecipe(
                Arrays.asList(Collections.emptyList(), Collections.emptyList()), null, 1000, true);
        assertEquals(2, recipe.getInputCount());
        assertTrue(recipe.getFluidInputs(0).isEmpty());
        assertTrue(recipe.getFluidInputs(1).isEmpty());
    }

    @Test
    void mismatchedParallelRowsAreRejectedBeforeLayout() {
        assertThrows(IllegalArgumentException.class, () -> new CrucibleJeiRecipe(
                Collections.singletonList(Collections.emptyList()), Collections.emptyList(),
                null, 1000, true, Collections.emptyList()));
    }
}
