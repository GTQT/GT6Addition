package com.drppp.gt6addition.common.metatileentity.single.hu;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * GT6-style explosion behavior adapted to the Minecraft 1.12 Explosion API.
 * Reference behavior: gregapi.random.ExplosionGT (GT6 source snapshot).
 */
final class CrucibleGt6Explosion {
    private static final float RAY_STEP = 0.3F;
    private static final float RAY_ENERGY_LOSS = RAY_STEP * 0.75F;
    private CrucibleGt6Explosion() {}

    static Explosion explode(World world, double x, double y, double z, float strength,
                             boolean flaming, boolean smoking) {
        Explosion explosion = new Explosion(world, null, x, y, z, strength, flaming, smoking);
        if (world.isRemote || ForgeEventFactory.onExplosionStart(world, explosion)) {
            return explosion;
        }

        calculateAffectedBlocksAndEntityDamage(world, explosion, x, y, z, strength);
        if (world instanceof WorldServer) {
            explosion.doExplosionB(false);
            if (!smoking) {
                explosion.clearAffectedBlockPositions();
            }
            sendExplosionPackets((WorldServer) world, explosion, x, y, z, strength);
        } else {
            explosion.doExplosionB(true);
        }
        return explosion;
    }

    private static void calculateAffectedBlocksAndEntityDamage(World world, Explosion explosion,
                                                                double x, double y, double z,
                                                                float strength) {
        Set<BlockPos> affected = new HashSet<>();
        for (int ix = 0; ix < 16; ix++) {
            for (int iy = 0; iy < 16; iy++) {
                for (int iz = 0; iz < 16; iz++) {
                    if (ix != 0 && ix != 15 && iy != 0 && iy != 15 && iz != 0 && iz != 15) {
                        continue;
                    }
                    double dx = ix / 7.5F - 1.0F;
                    double dy = iy / 7.5F - 1.0F;
                    double dz = iz / 7.5F - 1.0F;
                    double directionLength = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    dx /= directionLength;
                    dy /= directionLength;
                    dz /= directionLength;

                    float remainingPower = strength * (0.7F + world.rand.nextFloat() * 0.6F);
                    double rayX = x;
                    double rayY = y;
                    double rayZ = z;
                    while (remainingPower > 0.0F) {
                        BlockPos blockPos = new BlockPos(rayX, rayY, rayZ);
                        IBlockState state = world.getBlockState(blockPos);
                        Block block = state.getBlock();
                        if (state.getMaterial() != Material.AIR) {
                            float resistance = explosion.exploder != null
                                    ? explosion.exploder.getExplosionResistance(explosion, world, blockPos, state)
                                    : block.getExplosionResistance(world, blockPos, null, explosion);
                            remainingPower -= (resistance + 0.3F) * RAY_STEP;
                        }
                        if (remainingPower > 0.0F &&
                                (explosion.exploder == null || explosion.exploder.canExplosionDestroyBlock(
                                        explosion, world, blockPos, state, remainingPower))) {
                            affected.add(blockPos);
                        }
                        rayX += dx * RAY_STEP;
                        rayY += dy * RAY_STEP;
                        rayZ += dz * RAY_STEP;
                        remainingPower -= RAY_ENERGY_LOSS;
                    }
                }
            }
        }
        explosion.getAffectedBlockPositions().addAll(affected);

        float diameter = strength * 2.0F;
        AxisAlignedBB bounds = new AxisAlignedBB(
                MathHelper.floor(x - diameter - 1.0F), MathHelper.floor(y - diameter - 1.0F),
                MathHelper.floor(z - diameter - 1.0F), MathHelper.floor(x + diameter + 1.0F),
                MathHelper.floor(y + diameter + 1.0F), MathHelper.floor(z + diameter + 1.0F));
        List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(explosion.exploder, bounds);
        ForgeEventFactory.onExplosionDetonate(world, explosion, entities, diameter);
        Vec3d center = new Vec3d(x, y, z);
        float damageMultiplier = CrucibleGt6DamageScaling.scale(1.0F);

        for (Entity entity : entities) {
            if (isExcludedBoss(entity)) {
                continue;
            }
            double normalizedDistance = entity.getDistance(x, y, z) / diameter;
            if (normalizedDistance > 1.0D) {
                continue;
            }

            double knockX = entity.posX - x;
            double knockY = entity.posY + entity.getEyeHeight() - y;
            double knockZ = entity.posZ - z;
            double length = MathHelper.sqrt(knockX * knockX + knockY * knockY + knockZ * knockZ);
            if (length <= 0.0D) {
                continue;
            }
            knockX /= length;
            knockY /= length;
            knockZ /= length;
            double exposure = (1.0D - normalizedDistance) * world.getBlockDensity(center, entity.getEntityBoundingBox());
            double damage = ((int) ((exposure * exposure + exposure) * 4.0D * diameter + 1.0D)) * damageMultiplier;
            entity.attackEntityFrom(DamageSource.causeExplosionDamage(explosion), (float) damage);

            double blastProtection = entity instanceof EntityLivingBase
                    ? EnchantmentProtection.getBlastDamageReduction((EntityLivingBase) entity, exposure)
                    : exposure;
            entity.motionX += knockX * blastProtection;
            entity.motionY += knockY * blastProtection;
            entity.motionZ += knockZ * blastProtection;
            if (entity instanceof EntityPlayer) {
                explosion.getPlayerKnockbackMap().put((EntityPlayer) entity,
                        new Vec3d(knockX * exposure, knockY * exposure, knockZ * exposure));
            }
        }
    }

    private static void sendExplosionPackets(WorldServer world, Explosion explosion,
                                             double x, double y, double z, float strength) {
        for (EntityPlayer player : world.playerEntities) {
            if (player.getDistanceSq(x, y, z) < 4096.0D) {
                Vec3d knockback = explosion.getPlayerKnockbackMap().get(player);
                ((EntityPlayerMP) player).connection.sendPacket(new SPacketExplosion(
                        x, y, z, strength, explosion.getAffectedBlockPositions(), knockback));
            }
        }
    }

    private static boolean isExcludedBoss(Entity entity) {
        if (entity instanceof EntityWither || entity instanceof EntityDragon ||
                (entity instanceof MultiPartEntityPart &&
                        ((MultiPartEntityPart) entity).parent instanceof EntityDragon)) {
            return true;
        }
        return entity.getClass().getName().toLowerCase(Locale.ROOT).contains("boss");
    }

}
