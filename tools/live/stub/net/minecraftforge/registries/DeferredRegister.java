package net.minecraftforge.registries;

import java.util.function.Supplier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;

/**
 * E0 registration stub: shape-only Forge 1.16.5-36.2.42 API (universal
 * jar, never obfuscated). Never runs (compile classpath only).
 * Signatures measured with javap against the provisioned universal jar,
 * not recalled: {@code create} takes the live registry plus the owning
 * modid, and the entry id builds as {@code (modid, name)} — so
 * registration names are short paths, never qualified. Members are
 * pinned by tools/run-live.sh (live tranche) — drift fails loudly.
 */
public class DeferredRegister<T extends IForgeRegistryEntry<T>> {
    public static <B extends IForgeRegistryEntry<B>> DeferredRegister<B> create(
            IForgeRegistry<B> registry, String modid) {
        return null;
    }

    public <I extends T> RegistryObject<I> register(String name,
            Supplier<? extends I> supplier) {
        return null;
    }

    public void register(IEventBus bus) {
    }
}
