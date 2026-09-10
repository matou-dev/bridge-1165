package net.minecraftforge.registries;

import net.minecraft.util.ResourceLocation;

/**
 * E3 compile stub: shape-only Forge 1.16.5-36.2.42 API (universal jar,
 * never obfuscated). Never runs (compile classpath only). Member
 * {@code getValue} is pinned by tools/run-live.sh (E3) — drift fails
 * loudly. The {@code IForgeRegistryEntry} bound mirrors the real
 * interface so the erased descriptor matches at runtime (see
 * {@code IForgeRegistryEntry}). {@code containsKey} is the only
 * presence probe: {@code getValue} returns the registry default (air
 * for blocks) for unknown names, never null — a null check would cry
 * DUP on a correct config and, worse, resolve typos to air silently
 * (found live, first registration run).
 */
public interface IForgeRegistry<V extends IForgeRegistryEntry<V>> {
    V getValue(ResourceLocation name);

    boolean containsKey(ResourceLocation name);
}
