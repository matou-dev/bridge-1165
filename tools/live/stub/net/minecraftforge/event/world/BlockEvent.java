package net.minecraftforge.event.world;

import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Event;

/**
 * Loot compile stub: 36.2.42 {@code BlockEvent} carries the world behind
 * {@code getWorld} (measured via javap against the pinned 36.2.42
 * universal — private final {@code IWorld} field, public getters; the
 * 1.7.10 public-field shape does not port). Only the members forge reads
 * are stubbed. The 1.16.5 harvest signal is {@link BreakEvent}: there is
 * no {@code HarvestDropsEvent} on 1.16.5 (measured absent from the
 * universal — the 1.12/1.7.10 shape does not port), so the loot hook
 * records ore breaks instead of harvested drop lists (fortune/silk stay
 * explicit non-goals either way — hub decisions/LOOT.md). Never runs.
 */
public class BlockEvent extends Event {
    private final IWorld world;
    private final BlockPos pos;
    private final BlockState state;

    protected BlockEvent(IWorld world, BlockPos pos, BlockState state) {
        this.world = world;
        this.pos = pos;
        this.state = state;
    }

    public IWorld getWorld() {
        return world;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    public static class BreakEvent extends BlockEvent {
        public BreakEvent(World world, BlockPos pos, BlockState state,
                PlayerEntity player) {
            super(world, pos, state);
        }

        public PlayerEntity getPlayer() {
            return null;
        }

        public int getExpToDrop() {
            return 0;
        }

        public void setExpToDrop(int exp) {
        }
    }
}
