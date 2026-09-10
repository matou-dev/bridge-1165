package net.minecraftforge.fml;

import java.util.function.Supplier;
import net.minecraftforge.api.distmarker.Dist;

/**
 * Custom entity compile stub: shape-only Forge 1.16.5-36.2.42 API
 * (universal jar, never obfuscated). Never runs (compile classpath
 * only). Measured with javap against the provisioned universal jar
 * ({@code runWhenOn(Dist, Supplier<Runnable>)} — the supplier evaluates
 * only on the matching dist, so a dedicated server never loads the
 * client referent). Pinned by tools/run-live.sh — drift fails loudly.
 */
public final class DistExecutor {
    private DistExecutor() {
    }

    public static void runWhenOn(Dist dist,
            Supplier<Runnable> runnable) {
    }
}
