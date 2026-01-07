package com.drppp.gt6addition.api.baseMTile;


import com.drppp.gt6addition.api.recipeLogic.RecipeLogicMutiEnergy;
import com.drppp.gt6addition.api.utils.MachineEnergyAcceptFacing;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.multiblock.ICleanroomProvider;
import gregtech.api.metatileentity.multiblock.ICleanroomReceiver;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class WorkableTieredMutiEnergyMetaTileEntity extends TieredMutiEnergyMetaTileEntity
        implements IDataInfoProvider, ICleanroomReceiver {

    protected final AbstractRecipeLogic workable;
    protected final RecipeMap<?> recipeMap;
    protected final ICubeRenderer renderer;

    private final Function<Integer, Integer> tankScalingFunction;

    public final boolean handlesRecipeOutputs;

    @Nullable
    private ICleanroomProvider cleanroom;
    public int parallel=1;
    public WorkableTieredMutiEnergyMetaTileEntity(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap,
                                        ICubeRenderer renderer, int tier,
                                        Function<Integer, Integer> tankScalingFunction,String energyType,MachineEnergyAcceptFacing[] acceptFacing) {
        this(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction, true,energyType,acceptFacing);
    }
    public WorkableTieredMutiEnergyMetaTileEntity(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap,
                                                  ICubeRenderer renderer, int tier,
                                                  Function<Integer, Integer> tankScalingFunction,String energyType,MachineEnergyAcceptFacing[] acceptFacing,int parallel) {

        this(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction, true,energyType,acceptFacing,parallel);
    }
    public WorkableTieredMutiEnergyMetaTileEntity(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap,
                                                  ICubeRenderer renderer, int tier,
                                                  Function<Integer, Integer> tankScalingFunction, boolean handlesRecipeOutputs, String energyType, MachineEnergyAcceptFacing[] acceptFacing) {
        super(metaTileEntityId, tier,energyType,acceptFacing);
        this.renderer = renderer;
        this.handlesRecipeOutputs = handlesRecipeOutputs;
        this.parallel=1;
        this.workable = createWorkable(recipeMap);
        this.recipeMap = recipeMap;
        this.tankScalingFunction = tankScalingFunction;
        initializeInventory();
        reinitializeEnergyContainer();
    }
    public WorkableTieredMutiEnergyMetaTileEntity(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap,
                                                  ICubeRenderer renderer, int tier,
                                                  Function<Integer, Integer> tankScalingFunction, boolean handlesRecipeOutputs, String energyType, MachineEnergyAcceptFacing[] acceptFacing,int parallel) {
        super(metaTileEntityId, tier,energyType,acceptFacing);
        this.renderer = renderer;
        this.handlesRecipeOutputs = handlesRecipeOutputs;
        this.parallel = parallel;
        this.workable = createWorkable(recipeMap);
        this.recipeMap = recipeMap;
        this.tankScalingFunction = tankScalingFunction;
        initializeInventory();
        reinitializeEnergyContainer();
    }
    protected AbstractRecipeLogic createWorkable(RecipeMap<?> recipeMap) {
        return new RecipeLogicMutiEnergy(this, recipeMap, this.mutiEnergyProxy,getMaxParallel());
    }

    private int getMaxParallel()
    {
        return this.parallel;
    }

    @Override
    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[getTier()];
        if (isEnergyEmitter()) {
            this.energyContainer = EnergyContainerHandler.emitterContainer(this,
                    tierVoltage * 64L, tierVoltage, getMaxInputOutputAmperage());
        } else this.energyContainer = new EnergyContainerHandler(this, tierVoltage * 64L, tierVoltage, 2, 0L, 0L) {

            @Override
            public long getInputAmperage() {
                if (getEnergyCapacity() / 2 > getEnergyStored() && workable.isActive()) {
                    return 2;
                }
                return 1;
            }
        };
    }

    @Override
    protected long getMaxInputOutputAmperage() {
        return 2L;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        renderer.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), workable.isActive(),
                workable.isWorkingEnabled());
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        if (workable == null) return new GTItemStackHandler(this, 0);
        return new NotifiableItemStackHandler(this, workable.getRecipeMap().getMaxInputs(), this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        if (workable == null) return new GTItemStackHandler(this, 0);
        return new NotifiableItemStackHandler(this, workable.getRecipeMap().getMaxOutputs(), this, true);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        if (workable == null) return new FluidTankList(false);
        NotifiableFluidTank[] fluidImports = new NotifiableFluidTank[workable.getRecipeMap().getMaxFluidInputs()];
        for (int i = 0; i < fluidImports.length; i++) {
            NotifiableFluidTank filteredFluidHandler = new NotifiableFluidTank(
                    this.tankScalingFunction.apply(this.getTier()), this, false);
            fluidImports[i] = filteredFluidHandler;
        }
        return new FluidTankList(false, fluidImports);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        if (workable == null) return new FluidTankList(false);
        FluidTank[] fluidExports = new FluidTank[workable.getRecipeMap().getMaxFluidOutputs()];
        for (int i = 0; i < fluidExports.length; i++) {
            fluidExports[i] = new NotifiableFluidTank(this.tankScalingFunction.apply(this.getTier()), this, true);
        }
        return new FluidTankList(false, fluidExports);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gt6addition.universal.tooltip.voltage_in", energyContainer.getInputVoltage(),
                this.EnergyType+"/t",GTValues.VNF[getTier()]));
        tooltip.add(
                I18n.format("gt6addition.universal.tooltip.energy_storage_capacity"));
        if (workable.getRecipeMap().getMaxFluidInputs() != 0)
            tooltip.add(I18n.format("gt6addition.universal.tooltip.fluid_storage_capacity",
                    this.tankScalingFunction.apply(getTier())));
    }

    public Function<Integer, Integer> getTankScalingFunction() {
        return tankScalingFunction;
    }

    public boolean isActive() {
        return workable.isActive() && workable.isWorkingEnabled();
    }

    @Override
    public SoundEvent getSound() {
        return workable.getRecipeMap().getSound();
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList<>();

        if (workable != null) {
            list.add(new TextComponentTranslation("behavior.tricorder.workable_progress",
                    new TextComponentTranslation(TextFormattingUtil.formatNumbers(workable.getProgress() / 20))
                            .setStyle(new Style().setColor(TextFormatting.GREEN)),
                    new TextComponentTranslation(TextFormattingUtil.formatNumbers(workable.getMaxProgress() / 20))
                            .setStyle(new Style().setColor(TextFormatting.YELLOW))));

            if (energyContainer != null) {
                list.add(new TextComponentTranslation("gt6addition.tricorder.workable_stored_energy",
                        new TextComponentTranslation(
                                TextFormattingUtil.formatNumbers(this.mutiEnergyProxy.getEnergy()+this.EnergyType))
                                .setStyle(new Style().setColor(TextFormatting.GREEN)))
                );
            }
        }

        return list;
    }

    @Nullable
    @Override
    public ICleanroomProvider getCleanroom() {
        return this.cleanroom;
    }

    @Override
    public void setCleanroom(@NotNull ICleanroomProvider provider) {
        this.cleanroom = provider;
    }

    @Override
    public void unsetCleanroom() {
        this.cleanroom = null;
    }
}