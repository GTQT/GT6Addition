package com.drppp.gt6addition.smoketest;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.chunk.storage.RegionFileCache;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Rewrites only the three isolated smoke-world crucible tags to the prior stored-fluid denominator. */
public final class CrucibleLegacyChunkNbtPatcher {
    private static final int[] X = {2400, 2416, 2432};
    private static final int[] Y = {70, 70, 70};
    private static final int[] Z = {2400, 2400, 2400};
    private static final int LEGACY_FACTOR = 143;

    private CrucibleLegacyChunkNbtPatcher() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Expected isolated world directory");
        File worldDirectory = new File(args[0]).getCanonicalFile();
        if (!worldDirectory.isDirectory() || !worldDirectory.getName().startsWith("parity-smoke-restart-")) {
            throw new IllegalArgumentException("Refusing to patch a non-smoke world: " + worldDirectory);
        }

        int patched = 0;
        try {
            for (int fixture = 0; fixture < X.length; fixture++) {
                int chunkX = X[fixture] >> 4;
                int chunkZ = Z[fixture] >> 4;
                NBTTagCompound chunk;
                try (DataInputStream input = RegionFileCache.getChunkInputStream(worldDirectory, chunkX, chunkZ)) {
                    if (input == null) throw new IOException("Saved smoke chunk missing: " + chunkX + "," + chunkZ);
                    chunk = CompressedStreamTools.read(input);
                }

                NBTTagCompound level = chunk.getCompoundTag("Level");
                NBTTagList tileEntities = level.getTagList("TileEntities", 10);
                NBTTagCompound crucible = null;
                for (int i = 0; i < tileEntities.tagCount(); i++) {
                    NBTTagCompound tile = tileEntities.getCompoundTagAt(i);
                    if (tile.getInteger("x") != X[fixture] || tile.getInteger("y") != Y[fixture] ||
                            tile.getInteger("z") != Z[fixture]) continue;
                    crucible = findCrucibleData(tile);
                    break;
                }
                if (crucible == null) throw new IOException("Saved crucible payload missing at fixture " + fixture);
                if (crucible.getInteger("FluidQuantityVersion") != 3) {
                    throw new IOException("Expected current quantity format at fixture " + fixture);
                }

                NBTTagList contents = crucible.getTagList("Contents", 10);
                for (int i = 0; i < contents.tagCount(); i++) {
                    NBTTagCompound content = contents.getCompoundTagAt(i);
                    long remainder = content.getLong("FluidRemainder");
                    if (remainder < 0 || remainder % LEGACY_FACTOR != 0) {
                        throw new IOException("Fixture " + fixture + " has a fraction not representable by " +
                                "the old denominator: " + content.getString("Material") + " remainder=" + remainder);
                    }
                    content.setInteger("FluidRemainder", Math.toIntExact(remainder / LEGACY_FACTOR));
                }
                crucible.setInteger("FluidQuantityVersion", 1);
                try (DataOutputStream output = RegionFileCache.getChunkOutputStream(worldDirectory, chunkX, chunkZ)) {
                    if (output == null) throw new IOException("Could not open smoke chunk for rewrite: " + chunkX + "," + chunkZ);
                    CompressedStreamTools.write(chunk, output);
                }
                patched++;
            }
        } finally {
            RegionFileCache.clearRegionFileReferences();
        }

        if (patched != X.length) throw new IOException("Incomplete legacy NBT rewrite: " + patched);
        File marker = new File(worldDirectory, "crucible-legacy-nbt-patched.txt");
        try (FileOutputStream output = new FileOutputStream(marker)) {
            output.write("fixtures=3;fluidQuantityVersion=3->1;denominatorFactor=143\n".getBytes(StandardCharsets.UTF_8));
        }
        System.out.println("CRUCIBLE_RESTART_LEGACY_NBT_PATCHED fixtures=" + patched +
                " version=3->1 denominatorFactor=" + LEGACY_FACTOR);
    }

    private static NBTTagCompound findCrucibleData(NBTTagCompound tag) {
        if (tag.hasKey("FluidQuantityVersion") && tag.hasKey("Contents", 9)) return tag;
        for (String key : tag.getKeySet()) {
            NBTBase child = tag.getTag(key);
            if (child instanceof NBTTagCompound) {
                NBTTagCompound found = findCrucibleData((NBTTagCompound) child);
                if (found != null) return found;
            } else if (child instanceof NBTTagList) {
                NBTTagList list = (NBTTagList) child;
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTBase entry = list.get(i);
                    if (entry instanceof NBTTagCompound) {
                        NBTTagCompound found = findCrucibleData((NBTTagCompound) entry);
                        if (found != null) return found;
                    }
                }
            }
        }
        return null;
    }
}
