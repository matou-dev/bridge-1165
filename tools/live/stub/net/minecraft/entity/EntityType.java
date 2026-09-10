package net.minecraft.entity;

import net.minecraft.entity.passive.PigEntity;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Spawn compile stub (DEV-adjacent): shape-only 1.16.5 (MCP) vanilla API
 * used by {@code forge/} sources. Never runs (compile classpath only).
 * Only a type token: entity-type refs (the T1 pig type) go through this
 * (measured in the pinned joined.tsrg — {@code PIG} is
 * {@code field_200784_X} per snapshot 20210309). Non-final by design (a
 * final object would still be safe, but the Items.DIAMOND precedent keeps
 * registry refs non-final — hub decisions/REPOP_SPIKE.md no-stub-const
 * lesson). Pinned by tools/run-live.sh (narrow map) — drift fails loudly.
 *
 * <p>Custom entity tranche: the Forge binary patch (measured on the
 * official 1.16.x patch file — {@code EntityType} extends
 * {@code ForgeRegistryEntry<EntityType<?>>}, same mechanism as the
 * E3-proven {@code Block} bound) makes this a registry entry, so the
 * interface bound mirrors here (compile shape only — the stub never
 * runs, the runtime carries the real entry). The {@code Builder} and
 * {@code IFactory} shapes are MCP per snapshot 20210309
 * ({@code create}/{@code size}/{@code trackingRange}/{@code build},
 * reobfuscated MCP to SRG at E3 time, pinned in tools/run-live.sh) —
 * the 1.7.10/1.12 {@code EntityRegistry} call does not exist on 1.16.5.
 */
public class EntityType<T extends Entity>
        implements IForgeRegistryEntry<EntityType<?>> {
    public static EntityType<PigEntity> PIG;

    public interface IFactory<F extends Entity> {
        F create(EntityType<F> type, World world);
    }

    public static final class Builder<B extends Entity> {
        public static <N extends Entity> Builder<N> create(
                IFactory<N> factory, EntityClassification classification) {
            return null;
        }

        public Builder<B> size(float width, float height) {
            return null;
        }

        public Builder<B> trackingRange(int range) {
            return null;
        }

        public EntityType<B> build(String id) {
            return null;
        }
    }
}
