package com.drppp.gt6addition.smoketest;

import com.drppp.gt6addition.common.material.GT6MachineMaterials;
import com.drppp.gt6addition.common.metatileentity.MetaTileEntityHandler;
import com.drppp.gt6addition.common.metatileentity.single.hu.CrucibleHazardFire;
import com.drppp.gt6addition.common.metatileentity.single.hu.MetaTileEntityCrucible;
import com.mojang.authlib.GameProfile;
import gregtech.api.GTValues;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Field;
import java.util.UUID;

/** Registered materials and actual world side effects; never packaged in the mod JAR. */
final class CrucibleHazardWorldSmoke {
    private static int nextPosition;

    static int run(WorldServer world, MinecraftServer server) {
        FakePlayer player = new FakePlayer(world, new GameProfile(UUID.randomUUID(), "[HazardSmoke]"));
        new NetHandlerPlayServer(server, new NetworkManager(EnumPacketDirection.SERVERBOUND), player) {
            @Override public void sendPacket(Packet<?> packet) {}
        };
        lowDensityAndColdGas(world, player);
        combustionBoundary(world, player);
        meltingExemptions(world, player);
        tinBoiling(world, player);
        corrosion(world, player);
        boilingBeforeCorrosion(world, player);
        explosiveBoundary(world, player);
        shellBoundary(world, player);
        fireSelection(world);
        hollowCollisionAndSelection(world, player);
        hotContactAndRecycling(world, player);
        frostContactBoundary(world, player);
        return 12;
    }

    private static void lowDensityAndColdGas(WorldServer world, FakePlayer player) {
        MetaTileEntityCrucible vessel = place(world, player, 15);
        seed(vessel, 293, 1, Materials.Helium, Materials.Nitrogen, Materials.Iron);
        EntityCow nearby = cow(world, vessel.getPos(), 2);
        float health = nearby.getHealth();
        vessel.update();
        check(amount(vessel, Materials.Helium) == 0 && amount(vessel, Materials.Nitrogen) == 0 &&
                amount(vessel, Materials.Iron) == GTValues.M, "Low density / cold gas cleanup removed wrong contents");
        check(nearby.getHealth() == health, "Cold gas or low-density cleanup dealt heat damage");
        installed(world, vessel);
        nearby.setDead();
        pass("LOW_DENSITY_COLD_GAS");
    }

    private static void combustionBoundary(WorldServer world, FakePlayer player) {
        MetaTileEntityCrucible vessel = place(world, player, 15);
        seed(vessel, 313, 1, Materials.Biomass);
        vessel.update();
        check(amount(vessel, Materials.Biomass) == GTValues.M, "Biomass burned at 313 K");
        temperature(vessel, 314);
        vessel.update();
        check(amount(vessel, Materials.Biomass) == 0, "Biomass did not burn above 313 K");
        installed(world, vessel);
        pass("COMBUSTION_313_314");
    }

    private static void meltingExemptions(WorldServer world, FakePlayer player) {
        MetaTileEntityCrucible vessel = place(world, player, 15);
        seed(vessel, 314, 1, Materials.Coal, Materials.Sugar, Materials.Rubber);
        vessel.update();
        for (Material material : new Material[]{Materials.Coal, Materials.Sugar, Materials.Rubber}) {
            check(amount(vessel, material) == GTValues.M, "MELTING exemption lost " + material);
        }
        installed(world, vessel);
        pass("MELTING_BURN_EXEMPTIONS");
    }

    private static void tinBoiling(WorldServer world, FakePlayer player) {
        MetaTileEntityCrucible vessel = place(world, player, 15);
        seed(vessel, 2874, 16, Materials.Tin);
        vessel.update();
        check(amount(vessel, Materials.Tin) == 16L * GTValues.M, "Tin boiled below GT6's 2875 K");
        foundation(world, vessel.getPos());
        EntityCow ordinary = cow(world, vessel.getPos(), 2);
        EntityCow protectedCow = cow(world, vessel.getPos(), -2);
        protectedCow.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 200));
        temperature(vessel, 2875);
        world.rand.setSeed(12345L);
        vessel.update();
        check(amount(vessel, Materials.Tin) == 0, "Tin survived its GT6 boiling boundary");
        check(ordinary.getHealth() == 0 && protectedCow.getHealth() == protectedCow.getMaxHealth(),
                "Tin vapor heat damage or fire resistance failed");
        check(fires(world, vessel.getPos()) > 0, "High boiling-point vapor did not start supported fire");
        installed(world, vessel);
        ordinary.setDead();
        protectedCow.setDead();
        pass("TIN_BOILING_DAMAGE_FIRE");
    }

    private static void corrosion(WorldServer world, FakePlayer player) {
        MetaTileEntityCrucible resistant = place(world, player, 11);
        seed(resistant, 300, 1, Materials.SulfuricAcid);
        resistant.update();
        check(amount(resistant, Materials.SulfuricAcid) == GTValues.M, "Chrome lost nonboiling acid");
        installed(world, resistant);
        for (Material acid : new Material[]{Materials.SulfuricAcid, GT6MachineMaterials.FLUORITE}) {
            MetaTileEntityCrucible ordinary = place(world, player, 15);
            // The solid acid must destroy its shell before conversion can neutralize it.
            seed(ordinary, acid == Materials.SulfuricAcid ? 300 : 1633, 1, acid);
            ordinary.update();
            check(world.isAirBlock(ordinary.getPos()) && world.getTileEntity(ordinary.getPos()) == null,
                    "Non-acid-proof shell survived or produced hot-removal lava: " + acid);
            check(save(ordinary).getTagList("Contents", 10).isEmpty(), "Acid left processed materials");
            noMachineDrop(world, ordinary);
        }
        pass("ACID_PROOF_AND_SOLID_CORROSION");
    }

    private static void boilingBeforeCorrosion(WorldServer world, FakePlayer player) {
        MetaTileEntityCrucible vessel = place(world, player, 15);
        // GT6 HCl boils at 200 K: vapor removal precedes its ACID tag at 300 K.
        seed(vessel, 300, 1, Materials.HydrochloricAcid);
        vessel.update();
        check(amount(vessel, Materials.HydrochloricAcid) == 0, "Boiling acid was not removed");
        installed(world, vessel);
        pass("BOILING_BEFORE_ACID");
    }

    private static void explosiveBoundary(WorldServer world, FakePlayer player) {
        for (int units : new int[]{1, 16}) {
            MetaTileEntityCrucible vessel = place(world, player, 15);
            seed(vessel, 313, units, Materials.Gunpowder);
            vessel.update();
            check(amount(vessel, Materials.Gunpowder) == units * GTValues.M, "Gunpowder exploded at 313 K");
            ExplosionObservation observation = new ExplosionObservation(world, vessel.getPos());
            MinecraftForge.EVENT_BUS.register(observation);
            try {
                temperature(vessel, 314);
                vessel.update();
            } finally {
                MinecraftForge.EVENT_BUS.unregister(observation);
            }
            check(observation.starts == 1 && observation.detonations == 1 && observation.removedBeforeBlast,
                    "Actual explosion did not run after shell removal");
            check(observation.strength == (units == 1 ? 1F : 6F), "GT6 content explosion strength mismatch");
            check(world.getTileEntity(vessel.getPos()) == null &&
                    world.getBlockState(vessel.getPos()).getBlock() != Blocks.FLOWING_LAVA,
                    "Explosion triggered a second hot-removal hazard");
            check(save(vessel).getTagList("Contents", 10).isEmpty(), "Explosion preserved processed contents");
            noMachineDrop(world, vessel);
        }
        pass("EXPLOSIVE_BOUNDARY_AND_STRENGTH");
    }

    private static void shellBoundary(WorldServer world, FakePlayer player) {
        MetaTileEntityCrucible vessel = place(world, player, 15);
        seed(vessel, vessel.getMaxTemperature(), 1, Materials.Osmium);
        vessel.update();
        installed(world, vessel);
        foundation(world, vessel.getPos());
        EntityCow ordinary = cow(world, vessel.getPos(), 2);
        EntityCow protectedCow = cow(world, vessel.getPos(), -2);
        protectedCow.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 200));
        temperature(vessel, (long) vessel.getMaxTemperature() + 1);
        world.rand.setSeed(24680L);
        vessel.update();
        check(world.getBlockState(vessel.getPos()).getBlock() == Blocks.FLOWING_LAVA &&
                world.getBlockState(vessel.getPos()).getValue(BlockLiquid.LEVEL) == 1,
                "Shell overflow did not create non-source flowing lava");
        check(world.getTileEntity(vessel.getPos()) == null && save(vessel).getTagList("Contents", 10).isEmpty(),
                "Melted shell preserved machine or contents");
        check(ordinary.getHealth() == 0 && protectedCow.getHealth() == protectedCow.getMaxHealth(),
                "Meltdown heat damage or fire resistance failed");
        check(fires(world, vessel.getPos()) > 0, "Shell meltdown did not start supported fire");
        noMachineDrop(world, vessel);
        ordinary.setDead();
        protectedCow.setDead();
        pass("SHELL_MAX_AND_OVERFLOW");
    }

    private static void fireSelection(WorldServer world) {
        BlockPos pos = new BlockPos(1800 + 32 * nextPosition++, 70, 800);
        world.setBlockState(pos.down(), Blocks.STONE.getDefaultState(), 3);
        check(!CrucibleHazardFire.tryIgnite(world, pos, true) && world.isAirBlock(pos),
                "Checked fire ignited next to nonflammable stone alone");
        check(CrucibleHazardFire.tryIgnite(world, pos, false) && world.getBlockState(pos).getBlock() == Blocks.FIRE,
                "Unchecked GT6 fire refused a supported empty position");
        check(!CrucibleHazardFire.tryIgnite(world, pos, false), "Existing fire was rewritten");
        world.setBlockState(pos, Blocks.STONE.getDefaultState(), 3);
        check(!CrucibleHazardFire.tryIgnite(world, pos, false), "Fire replaced a solid collision block");
        world.setBlockState(pos, Blocks.CARPET.getDefaultState(), 3);
        check(CrucibleHazardFire.tryIgnite(world, pos, true) && world.getBlockState(pos).getBlock() == Blocks.FIRE,
                "GT6 flammable carpet exception failed");
        world.setBlockToAir(pos);
        world.setBlockState(pos.east(), Blocks.CHEST.getDefaultState(), 3);
        check(CrucibleHazardFire.tryIgnite(world, pos, true), "GT6 adjacent chest exception failed");
        world.setBlockState(pos.east(), Blocks.PLANKS.getDefaultState(), 3);
        world.setBlockToAir(pos);
        check(CrucibleHazardFire.tryIgnite(world, pos, true), "Adjacent flammable face did not ignite air");
        world.setBlockState(pos, Blocks.LAVA.getDefaultState(), 3);
        check(!CrucibleHazardFire.tryIgnite(world, pos, false) && world.getBlockState(pos).getBlock() == Blocks.LAVA,
                "Fire replaced lava");
        pass("FIRE_BLOCK_SELECTION");
    }

    private static void hollowCollisionAndSelection(WorldServer world, FakePlayer player) {
        MetaTileEntityCrucible vessel = place(world, player, 15);
        BlockPos pos = vessel.getPos();
        check(world.getCollisionBoxes(null, new AxisAlignedBB(.25, .126, .25, .75, .99, .75).offset(pos)).isEmpty(),
                "Hollow interior collides as a full block");
        check(!world.getCollisionBoxes(null, new AxisAlignedBB(.25, .01, .25, .75, .12, .75).offset(pos)).isEmpty(),
                "Crucible bottom has no collision");
        double[][] walls = {{.01, .2, .25, .12, .8, .75}, {.88, .2, .25, .99, .8, .75},
                {.25, .2, .01, .75, .8, .12}, {.25, .2, .88, .75, .8, .99}};
        for (double[] wall : walls) {
            check(!world.getCollisionBoxes(null, new AxisAlignedBB(wall[0], wall[1], wall[2],
                    wall[3], wall[4], wall[5]).offset(pos)).isEmpty(), "Crucible wall has no collision");
        }
        Vec3d center = new Vec3d(pos).add(.5, .5, .5);
        RayTraceResult top = world.rayTraceBlocks(center.add(0, 1.5, 0), center.add(0, -1.5, 0));
        check(top != null && pos.equals(top.getBlockPos()) && top.sideHit == EnumFacing.UP &&
                Math.abs(top.hitVec.y - (pos.getY() + .125)) < 1E-7,
                "Top ray hit an invisible full-block lid instead of the bottom");
        RayTraceResult side = world.rayTraceBlocks(center.add(-1.5, 0, 0), center);
        check(side != null && pos.equals(side.getBlockPos()) && side.sideHit == EnumFacing.WEST &&
                Math.abs(side.hitVec.x - pos.getX()) < 1E-7, "Side ray failed to select the shell");
        check(world.rayTraceBlocks(new Vec3d(pos).add(.25, .5, .25),
                new Vec3d(pos).add(.75, .5, .75)) == null, "Interior ray selected invisible geometry");
        pass("HOLLOW_COLLISION_AND_RAY_SELECTION");
    }

    private static void hotContactAndRecycling(WorldServer world, FakePlayer player) {
        MetaTileEntityCrucible vessel = place(world, player, 15);
        temperature(vessel, 320);
        EntityCow survivor = cow(world, vessel.getPos(), 0);
        vessel.update();
        check(survivor.getHealth() == survivor.getMaxHealth(), "Contact hurt at the safe 320 K boundary");
        temperature(vessel, 321);
        vessel.update();
        check(survivor.getHealth() == survivor.getMaxHealth() - 1F,
                "321 K contact missed GT6's minimum-one damage");
        check(save(vessel).getTagList("Contents", 10).isEmpty(), "Living contact produced recycled meat");
        survivor.setDead();
        EntityCow fatal = cow(world, vessel.getPos(), 0);
        fatal.setHealth(.5F);
        temperature(vessel, 321);
        vessel.update();
        check(fatal.getHealth() == 0 && amount(vessel, GT6MachineMaterials.MEAT_RAW) == 3L * GTValues.M,
                "Fatal hot cow contact did not insert exactly GT6's three raw meat units");
        check(amount(vessel, GT6MachineMaterials.MEAT_COOKED) == 0, "Cow contact inserted cooked rather than raw meat");
        fatal.setDead();
        // Full contents refuse the whole death-product batch without displacing existing material.
        MetaTileEntityCrucible full = place(world, player, 15);
        seed(full, 321, 16, Materials.Iron);
        EntityCow refused = cow(world, full.getPos(), 0);
        refused.setHealth(.5F);
        full.update();
        check(refused.getHealth() == 0 && amount(full, Materials.Iron) == 16L * GTValues.M &&
                amount(full, GT6MachineMaterials.MEAT_RAW) == 0, "Fatal contact overfilled or displaced full contents");
        refused.setDead();
        MetaTileEntityCrucible protectedVessel = place(world, player, 15);
        temperature(protectedVessel, 321);
        EntityCow resistant = cow(world, protectedVessel.getPos(), 0);
        resistant.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 200));
        protectedVessel.update();
        check(resistant.getHealth() == resistant.getMaxHealth() &&
                save(protectedVessel).getTagList("Contents", 10).isEmpty(), "Protected heat contact dealt damage or recycled");
        resistant.setDead();
        pass("HOT_CONTACT_AND_DEATH_RECYCLING");
    }

    private static void frostContactBoundary(WorldServer world, FakePlayer player) {
        MetaTileEntityCrucible vessel = place(world, player, 15);
        temperature(vessel, 260);
        EntityCow ordinary = cow(world, vessel.getPos(), 0);
        vessel.update();
        check(ordinary.getHealth() == ordinary.getMaxHealth(), "Contact hurt at the safe 260 K boundary");
        ordinary.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 200));
        temperature(vessel, 259);
        vessel.update();
        check(ordinary.getHealth() == ordinary.getMaxHealth() - 1F,
                "GT6 frost contact damage was rounded away or wrongly blocked by fire resistance");
        check(save(vessel).getTagList("Contents", 10).isEmpty(), "Frost contact produced hot death products");
        ordinary.setDead();
        EntityCow fatal = cow(world, vessel.getPos(), 0);
        fatal.setHealth(.5F);
        temperature(vessel, 259);
        vessel.update();
        check(fatal.getHealth() == 0 && save(vessel).getTagList("Contents", 10).isEmpty(),
                "Fatal frost contact generated meat despite GT6's hot-only death recycling");
        fatal.setDead();
        pass("FROST_CONTACT_BOUNDARY_AND_NO_RECYCLING");
    }

    private static MetaTileEntityCrucible place(WorldServer world, FakePlayer player, int material) {
        BlockPos pos = new BlockPos(1800 + 32 * nextPosition++, 70, 800);
        ItemStack stack = MetaTileEntityHandler.CRUCIBLE_HU[material].getStackForm();
        MachineItemBlock block = (MachineItemBlock) stack.getItem();
        check(world.isAirBlock(pos) && block.placeBlockAt(stack, player, world, pos, EnumFacing.UP,
                .5F, .5F, .5F, block.getBlock().getDefaultState()), "Hazard fixture placement failed");
        MetaTileEntityCrucible vessel = (MetaTileEntityCrucible) GTUtility.getMetaTileEntity(world, pos);
        check(vessel != null, "Missing hazard fixture holder");
        return vessel;
    }

    private static void seed(MetaTileEntityCrucible vessel, long temperature, int units, Material... materials) {
        NBTTagCompound data = save(vessel);
        data.setLong("Temperature", temperature);
        data.setLong("OldTemperature", temperature);
        NBTTagList contents = new NBTTagList();
        for (Material material : materials) {
            NBTTagCompound content = new NBTTagCompound();
            content.setString("Material", material.getRegistryName());
            content.setLong("Amount", units * GTValues.M);
            contents.appendTag(content);
        }
        data.setTag("Contents", contents);
        vessel.readFromNBT(data);
    }

    private static void temperature(MetaTileEntityCrucible vessel, long value) {
        NBTTagCompound data = save(vessel);
        data.setLong("Temperature", value);
        data.setLong("OldTemperature", value);
        vessel.readFromNBT(data);
    }

    private static long amount(MetaTileEntityCrucible vessel, Material material) {
        NBTTagList list = save(vessel).getTagList("Contents", 10);
        long amount = 0;
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            if (material.getRegistryName().equals(entry.getString("Material"))) amount += entry.getLong("Amount");
        }
        return amount;
    }

    private static void installed(WorldServer world, MetaTileEntityCrucible vessel) {
        check(GTUtility.getMetaTileEntity(world, vessel.getPos()) == vessel, "Unexpected shell destruction");
    }

    private static void foundation(WorldServer world, BlockPos center) {
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) {
            world.setBlockState(center.add(x, -2, z), Blocks.BEDROCK.getDefaultState(), 3);
        }
    }

    private static int fires(WorldServer world, BlockPos center) {
        int count = 0;
        for (BlockPos pos : BlockPos.getAllInBox(center.add(-3, -1, -3), center.add(3, 3, 3))) {
            if (world.getBlockState(pos).getBlock() == Blocks.FIRE) count++;
        }
        return count;
    }

    private static EntityCow cow(WorldServer world, BlockPos pos, int offset) {
        EntityCow cow = new EntityCow(world);
        cow.setPosition(pos.getX() + .5 + offset, pos.getY() + .5, pos.getZ() + .5);
        check(world.spawnEntity(cow), "Hazard cow spawn failed");
        return cow;
    }

    private static void noMachineDrop(WorldServer world, MetaTileEntityCrucible vessel) {
        for (EntityItem drop : world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(vessel.getPos()).grow(4))) {
            check(!(drop.getItem().getItem() instanceof MachineItemBlock), "Danger destruction dropped machine item");
        }
    }

    public static final class ExplosionObservation {
        private final WorldServer world;
        private final BlockPos pos;
        int starts, detonations;
        float strength;
        boolean removedBeforeBlast;
        ExplosionObservation(WorldServer world, BlockPos pos) { this.world = world; this.pos = pos; }
        private boolean matches(ExplosionEvent event) {
            return event.getWorld() == world && pos.equals(new BlockPos(event.getExplosion().getPosition()));
        }
        @SubscribeEvent public void start(ExplosionEvent.Start event) {
            if (!matches(event)) return;
            starts++;
            removedBeforeBlast = world.isAirBlock(pos) && world.getTileEntity(pos) == null;
            try {
                // Confirmed MCP field in this development source set, not a production reflection hook.
                Field size = Explosion.class.getDeclaredField("size");
                size.setAccessible(true);
                strength = size.getFloat(event.getExplosion());
            } catch (ReflectiveOperationException failure) { throw new AssertionError(failure); }
        }
        @SubscribeEvent public void detonate(ExplosionEvent.Detonate event) {
            if (matches(event)) detonations++;
        }
    }

    private static NBTTagCompound save(MetaTileEntityCrucible vessel) { return vessel.writeToNBT(new NBTTagCompound()); }
    private static void pass(String name) { LogManager.getLogger("CrucibleParitySmoke").info("CRUCIBLE_WORLD_HAZARD_{} passed", name); }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
