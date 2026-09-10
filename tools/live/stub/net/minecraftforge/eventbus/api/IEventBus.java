package net.minecraftforge.eventbus.api;

/**
 * E1 compile stub: shape-only eventbus 4.x API (never obfuscated). Never
 * runs (compile classpath only). Member {@code register} is pinned by
 * tools/run-live.sh (E3) — drift fails loudly. E0 registration adds
 * {@code addListener}: shape measured with javap against the provisioned
 * eventbus 4.0.0 jar (erased descriptor {@code (Consumer)V}), pinned by
 * the live tranche — drift fails loudly.
 *
 * <p>Companion shape (spawn proof, DEV ONLY): the companion posts its
 * simulated kill through {@code post} (measured via javap against the
 * provisioned eventbus 4.0.0 jar — the bus lives outside the universal,
 * so no universal pin covers it; tools/run-live.sh pins it from the
 * eventbus jar, and the live run proves it, loud on drift like every
 * linkage error).
 */
public interface IEventBus {
    void register(Object obj);

    <T extends Event> void addListener(java.util.function.Consumer<T> listener);

    boolean post(Event event);
}
