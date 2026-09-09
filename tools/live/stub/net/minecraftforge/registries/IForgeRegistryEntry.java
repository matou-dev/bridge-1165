package net.minecraftforge.registries;

/**
 * E3 compile stub: shape-only Forge 1.16.5-36.2.42 API (universal jar,
 * never obfuscated). Never runs (compile classpath only). The recursive
 * bound is load-bearing, not decorative: the real
 * {@code IForgeRegistry<V extends IForgeRegistryEntry<V>>} erases
 * {@code getValue} to {@code (...)LIForgeRegistryEntry;}, and a stub
 * with an unbounded {@code V} emits {@code (...)LObject;} instead —
 * the call links at compile time and dies live with NoSuchMethodError
 * (found live in E3, never again silently).
 */
public interface IForgeRegistryEntry<V extends IForgeRegistryEntry<V>> {
}
