package fr.iamacat.bridge.forge;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

/**
 * Registration landing (see hub decisions/REGISTRATION.md): the one
 * generic Forge block every content block registers as. Physics lands
 * through the version-native {@code Properties} builder (1.16.5 MCP
 * names {@code AbstractBlock}, single-arg
 * {@code hardnessAndResistance} sets hardness plus matching resistance
 * — both measured on the provisioned 36.2.42 bytes, not recalled) —
 * never hardcoded per content, never a subclass per content. Opacity
 * has no boolean slot on 1.16.5, only a behaviour-predicate builder,
 * and a project-side predicate or override keeps its MCP name through
 * this repo's reobfuscator ({@code tools/live/Reobf.java} only remaps
 * {@code net/minecraft/*} owners — measured, not assumed), so it would
 * link against nothing at runtime: translucent specs refuse loudly at
 * registration time, never default. Only this package may touch
 * {@code net.minecraft} / {@code net.minecraftforge}.
 */
public final class MatouBlock extends Block {
    /**
     * Args are pre-validated by the registering mod (E_REG_* owns the
     * refusals); the constructor only lands them.
     */
    public MatouBlock(float hardness) {
        super(AbstractBlock.Properties.create(Material.ROCK)
                .hardnessAndResistance(hardness));
    }
}
