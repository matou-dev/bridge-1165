package net.minecraft.util.math;

/**
 * Spawn compile stub: shape-only 1.16.5 (MCP) vanilla API used by
 * {@code forge/} sources. Never runs (compile classpath only). Only a
 * volume token: the census poll goes through
 * {@code World.getEntitiesWithinAABB} over one box (measured via
 * joined.tsrg + snapshot 20210309 + javap: public 6-double ctor — ctors
 * are never obfuscated, so no narrow-map row).
 */
public class AxisAlignedBB {
    public AxisAlignedBB(double x1, double y1, double z1, double x2,
            double y2, double z2) {
    }
}
