package com.mojang.blaze3d.matrix;

/**
 * Shape-only compile stub for the 1.16.5 client MatrixStack (Mojang
 * class, SRG names at runtime like every MCP-era member).
 * Never runs (compile classpath only). MCP names measured, not recalled,
 * against the pinned 36.2.42 bytes (joined.tsrg + snapshot 20210309 +
 * javap on the pinned vanilla client jar, obf {@code dfm}):
 * {@code getLast} is {@code func_227866_c_} (public {@code ()Ldfm$a}).
 * Pinned by tools/run-live.sh (narrow map) — drift fails loudly. The
 * nested Entry compiles to the runtime binary name
 * {@code MatrixStack$Entry} (obf {@code dfm$a}, {@code getMatrix} is
 * {@code func_227870_a_}).
 */
public class MatrixStack {
    public Entry getLast() {
        return null;
    }

    public static final class Entry {
        public net.minecraft.util.math.vector.Matrix4f getMatrix() {
            return null;
        }
    }
}
