package net.minecraftforge.fml;

import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * E0 registration stub: shape-only Forge 1.16.5-36.2.42 API (universal
 * jar, never obfuscated). Never runs (compile classpath only). The
 * recursive bound is load-bearing, not decorative: the real {@code get}
 * erases to {@code ()LIForgeRegistryEntry;} (same trap as
 * {@code IForgeRegistryEntry}, found live in E3) — an unbounded stub
 * would emit {@code ()Ljava/lang/Object;} and die live with
 * NoSuchMethodError. Bound measured with javap against the provisioned
 * universal jar. Member pinned by tools/run-live.sh (live tranche) —
 * drift fails loudly.
 */
public final class RegistryObject<T extends IForgeRegistryEntry<? super T>> {
    public T get() {
        return null;
    }
}
