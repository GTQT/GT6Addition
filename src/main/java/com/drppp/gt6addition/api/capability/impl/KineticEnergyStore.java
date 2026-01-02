package com.drppp.gt6addition.api.capability.impl;


import com.drppp.gt6addition.api.capability.interfaces.IKineticEnergy;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;


public class KineticEnergyStore implements Capability.IStorage<IKineticEnergy>{

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IKineticEnergy> capability, IKineticEnergy instance, EnumFacing side) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Ku",instance.getKinetic());
        return tag ;
    }

    @Override
    public void readNBT(Capability<IKineticEnergy> capability, IKineticEnergy instance, EnumFacing side, NBTBase nbt) {
        NBTTagCompound tag =  (NBTTagCompound)nbt;
        instance.setKineticEnergy(tag.getInteger("Ku"));
    }
}
