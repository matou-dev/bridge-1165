package fr.iamacat.bridge.forge;

import fr.iamacat.bridge.model.BeastModel;
import fr.iamacat.spi.hit.BoneBox;
import fr.iamacat.spi.hit.Hittable;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

/**
 * Registration landing (see hub decisions/SPAWN.md, custom entity
 * tranche): the one generic Forge beast every content mob registers as.
 * Pig shape, AI and sounds are reused verbatim — the vanilla pig
 * renderer is mapped to this class until the custom-renderer tranche,
 * never a renderer per content. One generic subclass, never one per
 * content, never a shadow field. The census, the join veto, the
 * reconcile poll and the kill hook all match this class: vanilla pigs
 * are a different species now (ignored by the census, never vetoed).
 * The content {@code hp} lands on the beast's max-health attribute at
 * every landing ({@code MatouBridgeMod.landBeast}, hp tranche — read
 * back tripwired, never a silent default); the vanilla pig renderer
 * mapping stays until the custom-renderer tranche.
 *
 * <p>Mob identity (per-mob tranche, hub
 * {@code decisions/VIRTUAL_HITBOXES.md}): every beast carries its short
 * content mob name ({@code my_beast} — the combat-table key, never the
 * qualified cell ref), set by the landing before the spawn and persisted
 * through NBT. Entities without a stored tag (legacy saves, natural
 * paths) adopt the first sealed combat mob on first read with a one-line
 * note — that preserves the current single-mob behaviour for naturals,
 * never silently. Hit-time reads dispatch per mob through
 * {@link #hitWeakspots()}; the inherited {@code Hittable} default
 * {@code weakspotMultiplier} already resolves through it, so no override
 * duplicates that rule here.
 *
 * <p>1.16.5 persist shape (measured via javap + joined.tsrg against the
 * pinned 36.2.42 bytes, never the 1.12 names): the beast overrides the
 * Pig-declared helpers {@code writeAdditional} ({@code func_213281_b})
 * + {@code readAdditional} ({@code func_70037_a}) — never the public
 * {@code writeWithoutTypeId} one level up on {@code Entity} (its super
 * call would emit an unmappable intermediate owner, the lead lesson —
 * hub decisions/VIRTUAL_HITBOXES.md second-beast row). PigEntity widens
 * both helpers to public on the notch bytes, so the overrides stay
 * public (an override may never narrow the superclass access).
 *
 * <p>Model tranche (hub decisions/MATOU_MODEL.md): the beast is a
 * {@code Hittable} over the shipped {@code my_beast.geo.json} shape —
 * world-space bone boxes ride the entity origin (feet), the head stays
 * the 2x weakspot. No combat hook reads them yet (that is the combat
 * tranche); this only serves the authoritative shape both sides will
 * ray-test. 1.16.5 keeps no {@code posX} fields — the origin reads
 * through the {@code getPosX/Y/Z} getters.
 *
 * <p>1.16.5 shape: same superclass as the lead bridge
 * ({@code PigEntity}, pig-like {@code (EntityType, World)} ctor — the
 * spawn seam already lands vanilla pigs through it, live-proven). The
 * factory slot ({@code EntityType.IFactory}) takes this constructor by
 * method ref (see {@code Example1Mod}); registration and rendering go
 * through the version-native calls there, never from here. Only this
 * package may import {@code net.minecraft} /
 * {@code net.minecraftforge}.
 */
public class MatouEntity extends PigEntity implements Hittable {
    /** NBT tag carrying the short content mob name. */
    static final String NBT_MOB = "MatouMob";

    /** Short content mob name ({@code my_beast}), null until set. */
    private String mob;

    /**
     * Args are pre-validated by the registering mod (E_REG_* owns the
     * refusals); the constructor only lands the vanilla shape. The mob
     * identity arrives via {@link #setMob} (landings) or NBT (loads).
     */
    public MatouEntity(EntityType<? extends MatouEntity> type,
            World world) {
        super(type, world);
    }

    /**
     * Seals the short content mob name on this beast (called by the
     * landing before the spawn). Loud on null/empty — an unidentified
     * beast would be a silent census leak. Unknown-at-seal is NOT
     * checked here: the sealed readers ({@code BeastModel}, the spawn
     * seal) refuse unknown mobs loudly at their own choke points, never
     * defaulted.
     */
    public void setMob(String mob) {
        if (mob == null) {
            throw new NullPointerException("E_SPAWN_MOB:null mob "
                    + "(want a sealed short mob name — see "
                    + "BeastModel.combatMobs)");
        }
        if (mob.isEmpty()) {
            throw new IllegalArgumentException("E_SPAWN_MOB:empty mob "
                    + "(want a sealed short mob name — never "
                    + "defaulted)");
        }
        this.mob = mob;
    }

    /** Short content mob name, or null before any set/load/adopt. */
    public String mob() {
        return mob;
    }

    /**
     * Short content mob name, adopting the first sealed combat mob (in
     * seal order) with a one-line note when unset — legacy saves and
     * natural paths keep the current single-mob behaviour, never
     * silently. The adoption memoizes: one line per entity, later reads
     * stay quiet. Loud when combat was never sealed (an unsealed read
     * would be a silent default) — every caller is wire-gated.
     */
    public String mobOrFirst() {
        if (mob == null) {
            mob = BeastModel.combatMobs().iterator().next();
            System.out.println("[MatouBridge] beast adopted mob <"
                    + mob + "> (no stored identity — first sealed mob)");
        }
        return mob;
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        if (mob != null) {
            compound.putString(NBT_MOB, mob);
        }
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        if (compound.contains(NBT_MOB)) {
            setMob(compound.getString(NBT_MOB));
        }
        // No tag (legacy/natural paths): mob stays null until
        // mobOrFirst() adopts with its note — the load path never
        // refuses, the hook path never defaults silently.
    }

    @Override
    public List<BoneBox> hitBoxes() {
        // Owner discipline (same class as the 1122 NoSuchFieldError posX,
        // fixed there in 840507c, hub decisions/LOOT.md): a bare
        // getPosX() call owns MatouEntity, whose reobf walk dies at the
        // vanilla PigEntity link — NoSuchMethodError on the first struck
        // hurt live (measured 2026-09-11 on the 1165 combat leg).
        // Inherited vanilla members go through the declaring stub type
        // (Entity), never the beast.
        Entity self = this;
        return BeastModel.cached().boxesAt(self.getPosX(), self.getPosY(), self.getPosZ());
    }

    @Override
    public Map<String, Float> hitWeakspots() {
        return BeastModel.combatWeakspots(mobOrFirst());
    }
}
