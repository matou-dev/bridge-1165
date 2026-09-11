package net.minecraft.util.math.vector;

import java.nio.FloatBuffer;

/**
 * Shape-only compile stub for the 1.16.5 Matrix4f (Mojang math, SRG
 * names at runtime like every MCP-era member).
 * Never runs (compile classpath only). Only the surface the renderer
 * uploads through: {@code write} is {@code func_195879_b} (public
 * {@code (Ljava/nio/FloatBuffer;)V}, measured via javap on the pinned
 * vanilla client jar — the 16 view/projection floats ride this call,
 * never a hand-rolled field walk). Pinned by tools/run-live.sh
 * (narrow map) — drift fails loudly.
 */
public class Matrix4f {
    public void write(FloatBuffer buf) {
    }
}
