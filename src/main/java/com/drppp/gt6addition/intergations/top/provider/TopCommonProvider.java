package com.drppp.gt6addition.intergations.top.provider;

import com.drppp.gt6addition.Tags;
import com.drppp.gt6addition.api.baseMTile.MetaTileEntityMutiEnergyMachine;
import com.drppp.gt6addition.api.top.IEnergyOutShow;
import com.drppp.gt6addition.api.utils.MachineEnergyAcceptFacing;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCastingBasin;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCombustionchamber;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCombustionchamberLiquid;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCrucible;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityMold;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityTemperatureSensor;
import com.drppp.gt6addition.common.metatileentity.single.item.MetaTileEntityGt6Hopper;
import com.drppp.gt6addition.common.metatileentity.single.ku.MetaTileEntityKineticGearbox;
import com.drppp.gt6addition.common.metatileentity.single.ku.MetaTileEntityKineticSteamEngine;
import com.drppp.gt6addition.common.metatileentity.single.ru.MetaTileEntityDieselEngine;
import com.drppp.gt6addition.common.metatileentity.single.ru.MetaTileEntityRotationPump;
import com.drppp.gt6addition.common.metatileentity.single.ru.MetaTileEntitySteamTurbine;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import java.util.List;

import static com.drppp.gt6addition.intergations.top.provider.TopDisplay.*;

public class TopCommonProvider implements IProbeInfoProvider {
    private static final String COMMON = "gt6addition.top.common.";
    private static final String CRUCIBLE = "gt6addition.top.crucible.";

    @Override
    public String getID() {
        return Tags.MOD_ID + ":top_common_provider";
    }

    /** Suppress only CEu rows already covered by this machine's own TOP presentation. */
    public static boolean replacesDefaultMachineInfo(String providerId, Object machine) {
        if (machine instanceof MetaTileEntityCrucible) {
            // The native HU view exposes a pending buffer with Long.MAX_VALUE capacity.
            // Its default temperature/EHU/percentage rows duplicate or obscure the physical display.
            return "gregtech:heat_container_provider".equals(providerId);
        }
        if (!(machine instanceof MetaTileEntityMutiEnergyMachine)) return false;
        return "gregtech:workable_provider".equals(providerId) ||
                "gregtech:recipe_logic_provider".equals(providerId) ||
                "gregtech:controllable_provider".equals(providerId);
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo info, EntityPlayer player,
                             World world, IBlockState state, IProbeHitData hit) {
        MetaTileEntity machine = GTUtility.getMetaTileEntity(world, hit.getPos());
        boolean details = mode == ProbeMode.EXTENDED || player.isSneaking();
        if (machine instanceof MetaTileEntityMutiEnergyMachine) {
            addProcessingMachine(info, (MetaTileEntityMutiEnergyMachine) machine, details);
            return;
        }

        if (machine instanceof IEnergyOutShow) {
            addEnergyOutput(info, (IEnergyOutShow) machine);
        }

        if (machine instanceof MetaTileEntityCrucible) {
            addCrucible(info, (MetaTileEntityCrucible) machine);
        } else if (machine instanceof MetaTileEntityCastingBasin) {
            MetaTileEntityCastingBasin basin = (MetaTileEntityCastingBasin) machine;
            commonRow(info, "capacity", number(basin.getStoredFluidAmount()) + " / " +
                    number(basin.getCapacityFluidAmount()) + " L");
            commonRow(info, "temperature", number(basin.getTemperature()) + " K");
            String contents = basin.getContentsDisplayName();
            commonRow(info, "contents", contents.isEmpty() ? common("empty") :
                    contents + TextFormatting.GRAY + " · " + common(basin.isContentsMolten() ? "molten" : "solid"));
        } else if (machine instanceof MetaTileEntityMold) {
            addMold(info, (MetaTileEntityMold) machine);
        } else if (machine instanceof MetaTileEntityTemperatureSensor) {
            MetaTileEntityTemperatureSensor sensor = (MetaTileEntityTemperatureSensor) machine;
            bar(info, COMMON + "temperature", sensor.getCurrentTemperature(), sensor.getMaxTemperature(), "K", HEAT);
            commonRow(info, "mode", localize(sensor.getModeTranslationKey()));
            commonRow(info, "set_temperature", number(sensor.getSetTemperature()) + " K");
            commonRow(info, "reading_side", direction(sensor.getTargetFacing().getName()));
            commonRow(info, "redstone_output", number(sensor.getRedstoneOutput()));
        } else if (machine instanceof MetaTileEntityKineticSteamEngine) {
            MetaTileEntityKineticSteamEngine engine = (MetaTileEntityKineticSteamEngine) machine;
            bar(info, COMMON + "energy_buffer", engine.getStoredEnergy(), engine.getEnergyCapacity(), "KU", KINETIC);
            commonRow(info, "steam", number(engine.getSteamAmount()) + " / " + number(engine.getTankCapacity()) + " L");
            commonRow(info, "distilled_water", number(engine.getWaterAmount()) + " / " +
                    number(engine.getTankCapacity()) + " L");
        } else if (machine instanceof MetaTileEntityKineticGearbox) {
            MetaTileEntityKineticGearbox gearbox = (MetaTileEntityKineticGearbox) machine;
            // The existing accessor returns an output-side name, not a gear ratio.
            commonRow(info, "output_side", direction(gearbox.getRatioName()));
        } else if (machine instanceof MetaTileEntityGt6Hopper) {
            addHopper(info, (MetaTileEntityGt6Hopper) machine);
        } else if (machine instanceof MetaTileEntityCombustionchamber) {
            MetaTileEntityCombustionchamber chamber = (MetaTileEntityCombustionchamber) machine;
            addBurningBox(info, chamber.isActive, chamber.outPutHu,
                    chamber.currentItemHasBurnedTime, chamber.currentItemBurnTime, chamber.burnSpeed, details);
            item(info, "fuel", chamber.getImportItems().getStackInSlot(0));
            item(info, "ash", chamber.getExportItems().getStackInSlot(0));
        } else if (machine instanceof MetaTileEntityCombustionchamberLiquid) {
            MetaTileEntityCombustionchamberLiquid chamber = (MetaTileEntityCombustionchamberLiquid) machine;
            addBurningBox(info, chamber.isActive, chamber.outPutHu,
                    chamber.currentItemHasBurnedTime, chamber.currentItemBurnTime, chamber.burnSpeed, details);
            tank(info, "fuel", chamber.getImportFluids().getTankAt(0));
            tank(info, "byproduct", chamber.getExportFluids().getTankAt(0));
        } else if (machine instanceof MetaTileEntitySteamTurbine) {
            MetaTileEntitySteamTurbine turbine = (MetaTileEntitySteamTurbine) machine;
            tank(info, "steam", turbine.getImportFluids().getTankAt(0));
            tank(info, "distilled_water", turbine.getExportFluids().getTankAt(0));
        } else if (machine instanceof MetaTileEntityDieselEngine) {
            MetaTileEntityDieselEngine engine = (MetaTileEntityDieselEngine) machine;
            percentBar(info, COMMON + "fuel_remaining",
                    Math.max(0L, (long) engine.currentItemBurnTime - engine.currentItemHasBurnedTime),
                    engine.currentItemBurnTime, HEAT);
            tank(info, "fuel", engine.getImportFluids().getTankAt(0));
            tank(info, "byproduct", engine.getExportFluids().getTankAt(0));
        } else if (machine instanceof MetaTileEntityRotationPump) {
            status(info, machine.isActive() ? "running" : "idle",
                    machine.isActive() ? TextFormatting.GREEN : TextFormatting.GRAY);
            energyType(info, "RU");
            Object fluidHandler = machine.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
            if (fluidHandler instanceof IFluidTank) tank(info, "output_fluid", (IFluidTank) fluidHandler);
        }
    }

    private void addProcessingMachine(IProbeInfo info, MetaTileEntityMutiEnergyMachine machine, boolean details) {
        AbstractRecipeLogic logic = machine.getCapability(GregtechTileCapabilities.CAPABILITY_RECIPE_LOGIC, null);
        long available = machine.mutiEnergyProxy == null ? 0L : Math.max(0, machine.mutiEnergyProxy.getEnergy());
        long required = logic == null || !logic.isActive() ? 0L : Math.max(0L, logic.getRecipeEUt());
        if (logic != null && !logic.isWorkingEnabled()) {
            status(info, "stopped", TextFormatting.RED);
        } else if (logic != null && logic.isActive() && (logic.isHasNotEnoughEnergy() || available < required)) {
            status(info, "insufficient_energy", TextFormatting.GOLD);
        } else {
            boolean active = logic != null && logic.isActive();
            status(info, active ? "running" : "idle", active ? TextFormatting.GREEN : TextFormatting.GRAY);
        }
        energyType(info, machine.EnergyType);
        energyRate(info, "energy_available", available, machine.EnergyType);
        if (logic != null && logic.isActive()) {
            energyRate(info, "energy_required", required, machine.EnergyType);
            bar(info, COMMON + "progress", logic.getProgress(), logic.getMaxProgress(), "t", GREEN);
        }
        if (details) {
            commonRow(info, "parallel_limit", number(Math.max(1,
                    logic == null ? machine.parallel : logic.getParallelLimit())));
            if (machine.acceptFacing != null && machine.acceptFacing.length > 0) {
                StringBuilder sides = new StringBuilder();
                for (MachineEnergyAcceptFacing side : machine.acceptFacing) {
                    if (side == null) continue;
                    if (sides.length() > 0) sides.append(" / ");
                    sides.append(direction(side.name()));
                }
                if (sides.length() > 0) commonRow(info, "input_side", sides.toString());
            }
        }
    }

    private void addEnergyOutput(IProbeInfo info, IEnergyOutShow machine) {
        if (machine instanceof MetaTileEntityKineticSteamEngine &&
                ((MetaTileEntityKineticSteamEngine) machine).isStopped()) {
            status(info, "stopped", TextFormatting.RED);
        } else if (machine instanceof MetaTileEntityKineticGearbox &&
                ((MetaTileEntityKineticGearbox) machine).isJammed()) {
            status(info, "jammed", TextFormatting.RED);
        } else {
            boolean active = machine.getEnergyOut() > 0;
            status(info, active ? "running" : "idle", active ? TextFormatting.GREEN : TextFormatting.GRAY);
        }
        energyType(info, machine.getEnergyName());
        energyRate(info, "energy_output", machine.getEnergyOut(), machine.getEnergyName());
    }

    private void addBurningBox(IProbeInfo info, boolean active, int output,
                               int burned, int total, int speed, boolean details) {
        status(info, active ? "running" : "idle", active ? TextFormatting.GREEN : TextFormatting.GRAY);
        energyType(info, "HU");
        energyRate(info, "energy_output", active ? output : 0L, "HU");
        percentBar(info, COMMON + "fuel_remaining", Math.max(0L, (long) total - burned), total, HEAT);
        if (details) {
            energyRate(info, "rated_output", output, "HU");
            commonRow(info, "fuel_consumption", number(active ? speed : 0) + " " + common("fuel_heat_unit") + "/t");
        }
    }

    private void addCrucible(IProbeInfo info, MetaTileEntityCrucible crucible) {
        bar(info, CRUCIBLE + "temperature", crucible.getCurrentTemperature(), crucible.getMaxTemperature(), "K", HEAT);
        row(info, CRUCIBLE + "capacity_equivalent", number(crucible.getStoredFluidAmount()) + " / " +
                number(crucible.getCapacityFluidAmount()) + " L");
        ItemStack input = crucible.getImportItems().getStackInSlot(0).copy();
        row(info, CRUCIBLE + "input_slot", input.isEmpty() ? common("empty") :
                input.getDisplayName() + TextFormatting.GRAY + " ×" + number(input.getCount()));
        row(info, CRUCIBLE + "pending_items", number(crucible.getPendingItemCount()) + " / " +
                number(crucible.getPendingItemCapacity()));
        List<MetaTileEntityCrucible.CrucibleContentInfo> contents = crucible.getTopContents();
        if (contents.isEmpty()) {
            row(info, CRUCIBLE + "contents", common("empty"));
            return;
        }
        info.text(TextFormatting.WHITE.toString() + TextFormatting.BOLD +
                localize(CRUCIBLE + "contents") + TextFormatting.RESET);
        for (MetaTileEntityCrucible.CrucibleContentInfo content : contents) {
            IProbeInfo line = info.horizontal();
            ItemStack stack = content.getDisplayStack();
            if (!stack.isEmpty()) line.item(stack);
            line.text(TextFormatting.GREEN + content.getMaterialName() + TextFormatting.GRAY + " · " +
                    number(content.getFluidAmount()) + " L · " +
                    common(content.isMolten() ? "molten" : "solid") + TextFormatting.RESET);
        }
    }

    private void addMold(IProbeInfo info, MetaTileEntityMold mold) {
        commonRow(info, "mold_grid", "5 × 5");
        commonRow(info, "temperature_limit", number(mold.getMoldMaxTemperature()) + " K");
        if (mold.isCooling()) {
            status(info, "cooling", TextFormatting.AQUA);
            commonRow(info, "contents", mold.getCoolingMaterialName());
            percentBar(info, COMMON + "cooling_progress", mold.getCoolingProgressPercent(), 100L, COLD);
            commonRow(info, "temperature", number(mold.getTemperatureValue(null)) + " K");
            commonRow(info, "solidification_temperature", number(mold.getCoolingTargetTemperature()) + " K");
        } else {
            item(info, "finished_item", mold.getOutputStack());
        }
    }

    private void addHopper(IProbeInfo info, MetaTileEntityGt6Hopper hopper) {
        bar(info, COMMON + "slots", hopper.getUsedSlots(), hopper.getSlotCount(), "", GREEN);
        commonRow(info, "mode", common(hopper.isQueueMode() ? "hopper.fifo" : "hopper.merge"));
        commonRow(info, "batch_size", number(hopper.getStackSizeLimit()) +
                (hopper.isExactMode() ? TextFormatting.GRAY + " · " + common("hopper.exact") : ""));
        commonRow(info, "item_count", number(hopper.getTotalItemCount()));
        ItemStack preview = hopper.getOutputStackPreview();
        if (!preview.isEmpty()) item(info, "pending_output", preview);
    }
}
