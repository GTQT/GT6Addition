package com.drppp.gt6addition.api.capability.interfaces;

/** Push input for GT6-style crucibles; no conversion from EU or Forge Energy is implied. */
public interface ICrucibleEnergyReceiver {
    enum Type { HU, CU, KU, VIS_IGNIS }

    /**
     * @param amount positive energy units, not packet count
     * @param simulate true must not change energy, contents, temperature or world state
     * @return accepted units; the caller must debit only this amount from its source
     */
    long receiveCrucibleEnergy(Type type, long amount, boolean simulate);
}
