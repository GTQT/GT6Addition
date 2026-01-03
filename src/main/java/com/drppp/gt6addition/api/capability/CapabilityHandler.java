package com.drppp.gt6addition.api.capability;

import com.drppp.gt6addition.Tags;
import com.drppp.gt6addition.api.capability.impl.*;
import com.drppp.gt6addition.api.capability.interfaces.IHeatEnergy;
import com.drppp.gt6addition.api.capability.interfaces.IKineticEnergy;
import com.drppp.gt6addition.api.capability.interfaces.IRotationEnergy;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class CapabilityHandler {
    @CapabilityInject(IHeatEnergy.class)
    public static Capability<IHeatEnergy> CAPABILITY_HEAT_ENERGY= null;
    public static Capability<IHeatEnergy> CAPABILITY_ROTATION_ENERGY= null;
    public static Capability<IHeatEnergy> CAPABILITY_KINETIC_ENERGY= null;

    public static <T> void registerCapabilityWithNoDefault(Class<T> capabilityClass) {
        CapabilityManager.INSTANCE.register(capabilityClass, new Capability.IStorage<T>() {
            public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
                throw new UnsupportedOperationException("Not supported");
            }

            public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {

                throw new UnsupportedOperationException("Not supported");
            }
        }, () -> {
            throw new UnsupportedOperationException("This capability has no default implementation");
        });
    }
    public static void init()
    {
        CapabilityManager.INSTANCE.register(IHeatEnergy.class,new HeatEnergyStore(),  HeatEnergyHandler::new);
        CapabilityManager.INSTANCE.register(IRotationEnergy.class,new RotationEnergyStore(),  RotationEnergyHandler::new);
        CapabilityManager.INSTANCE.register(IKineticEnergy.class,new KineticEnergyStore(),  KineticEnergyHandler::new);
    }
}
