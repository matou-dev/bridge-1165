package fr.iamacat.bridge.forge;

import fr.iamacat.bridge.Packs;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

/**
 * E1 Forge wiring (36.2.42): FML world tick in, pure SPI decide,
 * bridge-owned apply. Packs come from {@code config/matoubridge/packs.cfg}
 * (one {@code <class> <y> <block> [k=v ...]} per line); a missing file
 * means no packs, staying passive (Q1 cohabitation). Malformed config or
 * unloadable pack fails fast at construction — a half-wired bridge never
 * ticks.
 *
 * <p>Only this package may touch MC/Forge; the decide/apply seam
 * ({@code fr.iamacat.bridge}) ships from {@code matou-spi} (see
 * {@code SPI_PIN}).
 */
@Mod(MatouBridgeMod.MODID)
public final class MatouBridgeMod {
    public static final String MODID = "matoubridge";
    static final String PACKS_PATH = "config/matoubridge/packs.cfg";

    private final List<PackWire> wires = new ArrayList<PackWire>();
    private long tick;

    public MatouBridgeMod() {
        MinecraftForge.EVENT_BUS.register(this);
        File cfg = new File(PACKS_PATH);
        if (!cfg.isFile()) {
            return;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(cfg.toPath(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("E_FORGE_PACKS:unreadable <"
                    + PACKS_PATH + "> (" + e.getMessage() + ")", e);
        }
        for (Packs.PackSpec spec : Packs.parseLines(lines)) {
            wires.add(PackWire.bind(spec));
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.side != LogicalSide.SERVER
                || event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!World.OVERWORLD.equals(event.world.getDimensionKey())) {
            return;
        }
        for (PackWire wire : wires) {
            wire.applyTo(event.world, tick);
        }
        tick++;
    }
}
