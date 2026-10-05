package com.drppp.gt6addition.common.metatileentity.single.hu;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;

/** Explicit GT6 Smeltery.java:625-650 death products; no guessed entity drops. */
final class CrucibleEntityRecycling {
    private CrucibleEntityRecycling() {}

    static boolean shouldRecycleDeath(boolean wasAlive, boolean isAlive, long temperature) {
        return wasAlive && !isAlive && temperature > 320L;
    }

    static Rule rule(Class<? extends EntityLivingBase> type, String playerName) {
        if (EntityVillager.class.isAssignableFrom(type) || EntityWitch.class.isAssignableFrom(type)) {
            return new Rule(310, "soylentgreen", 2);
        }
        if (EntitySnowman.class.isAssignableFrom(type)) return new Rule(263, "snow", 4);
        if (EntityIronGolem.class.isAssignableFrom(type)) return new Rule(-1, "iron", 4);
        // GT6 MT.BoneWither is explicitly an alias of MT.Bone (MT.java:1307).
        if (EntityWitherSkeleton.class.isAssignableFrom(type)) {
            return new Rule(-1, new String[]{"bone", "coal"}, new int[]{1, 1}, 1);
        }
        if (EntitySkeleton.class.isAssignableFrom(type)) return new Rule(-1, "bone", 1);
        if (EntityZombie.class.isAssignableFrom(type)) return new Rule(-1, "meatrotten", 1);
        if (EntityCow.class.isAssignableFrom(type) || isLegacyHorse(type)) {
            return new Rule(310, "meatraw", 3); // Mooshroom inherits Cow.
        }
        if (EntityPig.class.isAssignableFrom(type) || EntitySheep.class.isAssignableFrom(type) ||
                EntityWolf.class.isAssignableFrom(type) || EntitySquid.class.isAssignableFrom(type)) {
            return new Rule(310, "meatraw", 2);
        }
        if (EntityChicken.class.isAssignableFrom(type) || EntityOcelot.class.isAssignableFrom(type) ||
                EntitySpider.class.isAssignableFrom(type) || EntitySilverfish.class.isAssignableFrom(type)) {
            return new Rule(310, "meatraw", 1);
        }
        if (EntityCreeper.class.isAssignableFrom(type)) return new Rule(293, "gunpowder", 1);
        if (EntityEnderman.class.isAssignableFrom(type)) return new Rule(293, "enderpearl", 1);
        if (EntityPlayer.class.isAssignableFrom(type) && "GregoriusT".equalsIgnoreCase(playerName)) {
            // GT6 admits each of sixteen single-U portions independently.
            return new Rule(293, new String[]{"technetium"}, new int[]{1}, 16);
        }
        return null;
    }

    private static boolean isLegacyHorse(Class<? extends EntityLivingBase> type) {
        // Vanilla HorseSplit migrates old EntityHorse Type 0..4 into these classes.
        // GT6's crucible did not distinguish those types (unlike its loot handler).
        // AbstractHorse would also include Llama, which has no GT6 equivalent here.
        return EntityHorse.class.isAssignableFrom(type) || EntityDonkey.class.isAssignableFrom(type) ||
                EntityMule.class.isAssignableFrom(type) || EntityZombieHorse.class.isAssignableFrom(type) ||
                EntitySkeletonHorse.class.isAssignableFrom(type);
    }

    static final class Rule {
        final int temperature; // -1 means ambient temperature.
        final String[] materials;
        final int[] units;
        final int repetitions;

        Rule(int temperature, String material, int units) {
            this(temperature, new String[]{material}, new int[]{units}, 1);
        }

        Rule(int temperature, String[] materials, int[] units, int repetitions) {
            this.temperature = temperature;
            this.materials = materials;
            this.units = units;
            this.repetitions = repetitions;
        }
    }
}
