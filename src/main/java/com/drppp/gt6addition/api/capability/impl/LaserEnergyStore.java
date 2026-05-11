package com.drppp.gt6addition.api.capability.impl;

import com.drppp.gt6addition.api.capability.interfaces.ILaserEnergy;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Nullable;

public class LaserEnergyStore implements Capability.IStorage<ILaserEnergy> {

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<ILaserEnergy> capability, ILaserEnergy instance, EnumFacing side) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Lu", instance.getEnergyOutput());
        tag.setBoolean("IsOut", instance.isOutPut());
        return tag;
    }

    @Override
    public void readNBT(Capability<ILaserEnergy> capability, ILaserEnergy instance, EnumFacing side, NBTBase nbt) {
        NBTTagCompound tag = (NBTTagCompound) nbt;
        instance.setLuEnergy(tag.getInteger("Lu"));
        instance.setOutPut(tag.getBoolean("IsOut"));
    }
}
