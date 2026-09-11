package fr.iamacat.bridge.forge;

import fr.iamacat.bridge.model.BeastModel;
import fr.iamacat.spi.hit.BoneBox;
import fr.iamacat.spi.hit.Hittable;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
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
    /**
     * Args are pre-validated by the registering mod (E_REG_* owns the
     * refusals); the constructor only lands the vanilla shape.
     */
    public MatouEntity(EntityType<? extends MatouEntity> type,
            World world) {
        super(type, world);
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
        return BeastModel.WEAKSPOTS;
    }
}
