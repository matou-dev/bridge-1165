package net.minecraftforge.eventbus.api;

/**
 * E1 compile stub: shape-only eventbus 4.x API (never obfuscated). Never
 * runs (compile classpath only). Member {@code register} is pinned by
 * tools/run-live.sh (E3) — drift fails loudly. E0 registration adds
 * {@code addListener}: shape measured with javap against the provisioned
 * eventbus 4.0.0 jar (erased descriptor {@code (Consumer)V}), pinned by
 * the live tranche — drift fails loudly.
 */
public interface IEventBus {
    void register(Object obj);

    <T extends Event> void addListener(java.util.function.Consumer<T> listener);
}
