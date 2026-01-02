package com.drppp.gt6addition.intergations.top;


import com.drppp.gt6addition.intergations.top.provider.TopCommonProvider;
import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.api.ITheOneProbe;



public class TopInit {
    public static void init() {
        ITheOneProbe oneProbe = TheOneProbe.theOneProbeImp;
        oneProbe.registerProvider(new TopCommonProvider());

    }
}
