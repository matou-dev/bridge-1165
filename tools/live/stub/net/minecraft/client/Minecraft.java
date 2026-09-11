package net.minecraft.client;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;

/**
 * Shape-only compile stub for Minecraft 1.16.5 client.
 * Never runs (compile classpath only). The single Minecraft stub — the
 * former second one under tools/autoplay/stub is merged here (same
 * duplicate-class trouvaille as the 1122 visual tranche and the 1710
 * port: two stubs, one class, javac refuses). Two consumers share it:
 * the client-only forge renderer needs getInstance/world/
 * getRenderViewEntity (first executed by the direct client proof, loud
 * at runtime until then), the dev-only autoplay companion needs
 * getInstance/loadWorld/shutdown (pinned by the AUTOPLAY derive,
 * tools/autoplay/want.txt).
 *
 * <p>Renderer anchors are narrow-map-derived, never recalled (same
 * derive discipline as every 1165 row): {@code getInstance} is
 * {@code func_71410_x} (static, measured {@code ()Ldjz} on the pinned
 * vanilla client jar), {@code world} is the ClientWorld-typed
 * {@code field_71441_e} (measured {@code dwt}, not World — the 1122
 * WorldClient trap in 1.16.5 spelling), {@code getRenderViewEntity} is
 * {@code func_175606_aa} (measured {@code ()Laqa} — Entity-typed, the
 * 1710 EntityLivingBase trap is absent here; the same-sounding
 * {@code func_216773_g} lives on ActiveRenderInfo and is not this).
 * All pinned by tools/run-live.sh.
 */
public class Minecraft {
    // True type is ClientWorld — a World-typed fieldref would die linking
    // (JVM field resolution matches the descriptor exactly). The renderer
    // reads getAllEntities through the declaring ClientWorld type (owner
    // discipline: stubs never ship, a World-owned ref walks nowhere in
    // Reobf and passes through to die linking live — 1122 fix 6988515).
    public ClientWorld world;

    public static Minecraft getInstance() {
        return null;
    }

    public Entity getRenderViewEntity() {
        return null;
    }

    public void loadWorld(String worldName) {
    }

    public void shutdown() {
    }
}
