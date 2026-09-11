package net.minecraft.nbt;

/**
 * Spawn-identity compile stub: the 36.2.42 persist surface the bridge
 * beast extends with its short mob name ({@code MatouEntity} writes/reads
 * one string tag — owner discipline, hub decisions/LOOT.md). MCP names
 * measured, not recalled, against the pinned bytes (joined.tsrg +
 * snapshot 20210309 + javap: {@code contains} is {@code func_74764_b},
 * {@code getString} is {@code func_74779_i}, {@code putString} is
 * {@code func_74778_a} — the 1.12 {@code hasKey/setString} names do not
 * port). Never runs (compile classpath only).
 */
public class CompoundNBT {
    public boolean contains(String key) {
        return false;
    }

    public String getString(String key) {
        return "";
    }

    public void putString(String key, String value) {
    }
}
