package com.drppp.gt6addition.api.capability.impl;


import com.drppp.gt6addition.api.capability.interfaces.IHeatEnergy;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;


public class HeatEnergyStore implements Capability.IStorage<IHeatEnergy>{

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IHeatEnergy> capability, IHeatEnergy instance, EnumFacing side) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Hu",instance.getHeat());
        return tag ;
    }

    @Override
    public void readNBT(Capability<IHeatEnergy> capability, IHeatEnergy instance, EnumFacing side, NBTBase nbt) {
        NBTTagCompound tag =  (NBTTagCompound)nbt;
        instance.setHuEnergy(tag.getInteger("Hu"));
    }
}
