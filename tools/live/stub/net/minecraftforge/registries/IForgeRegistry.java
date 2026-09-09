package net.minecraftforge.registries;

import net.minecraft.util.ResourceLocation;

/**
 * E3 compile stub: shape-only Forge 1.16.5-36.2.42 API (universal jar,
 * never obfuscated). Never runs (compile classpath only). Member
 * {@code getValue} is pinned by tools/run-live.sh (E3) — drift fails
 * loudly. The {@code IForgeRegistryEntry} bound mirrors the real
 * interface so the erased descriptor matches at runtime (see
 * {@code IForgeRegistryEntry}).
 */
public interface IForgeRegistry<V extends IForgeRegistryEntry<V>> {
    V getValue(ResourceLocation name);
}
