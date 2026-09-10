package fr.iamacat.bridge.forge;

import fr.iamacat.bridge.Packs;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Registration half of example1 (see hub decisions/REGISTRATION.md):
 * a second mod in the bridge jar under example1's own frozen modid
 * ({@code NAMES.md}, never a rename target). 1.16.5 registers blocks
 * through a {@code DeferredRegister} on the mod event bus
 * ({@code FMLJavaModLoadingContext}), so the constructor only queues
 * validated specs as deferred entries and common setup verifies them —
 * the fill lands at registry-event time, always before setup, whatever
 * the mod order. Block-only tranche: no beast path (the single-mob
 * table stays unread, {@code MatouEntity} stays an unwired shell). New
 * refusals stay registration-local ({@code E_REG_*}, never in the
 * shared catalog). Only this package may touch {@code net.minecraft} /
 * {@code net.minecraftforge}.
 *
 * <p>Ordering note: {@link MatouBridgeMod} still binds in its own
 * constructor (unchanged this tranche), which runs before the deferred
 * fill — so a custom wire refuses loudly there until the bind-timing
 * tranche defers it. Registration never silently lags a bind.
 */
@Mod(Example1Mod.MODID)
public final class Example1Mod {
    public static final String MODID = "example1";
    static final String PACKS_PATH = "config/matoubridge/packs.cfg";

    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final Map<String, RegistryObject<MatouBlock>> REGISTERED =
            new HashMap<String, RegistryObject<MatouBlock>>();

    /**
     * Registration: every packs.cfg wire naming a non-vanilla block
     * gets its content {@code BlockSpec} queued here as a deferred
     * entry, before any setup-time bind resolves it. Vanilla wires skip
     * silently — the bind-time resolve owns them, unchanged. A missing
     * packs.cfg stays passive (Q1 cohabitation), same as the bridge
     * init.
     */
    public Example1Mod() {
        File cfg = new File(PACKS_PATH);
        if (cfg.isFile()) {
            List<String> lines;
            try {
                lines = Files.readAllLines(cfg.toPath(),
                        StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException("E_REG_PACKS:unreadable <"
                        + PACKS_PATH + "> (" + e.getMessage() + ")", e);
            }
            for (Packs.PackSpec spec : Packs.parseLines(lines)) {
                queueCustom(spec);
            }
        }
        IEventBus bus =
                FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        bus.addListener((FMLCommonSetupEvent event) -> verifyRegistered());
    }

    /**
     * Verify + announce: the registry is only reliably queryable once
     * loading reaches setup, so the resolve check and the numeric-ID
     * line the verdict greps live here, not in the constructor. The id
     * is the default-state runtime id — dynamic per boot, resolved from
     * this very line at verify time, never frozen.
     */
    private static void verifyRegistered() {
        for (Map.Entry<String, RegistryObject<MatouBlock>> e
                : REGISTERED.entrySet()) {
            Block resolved = ForgeRegistries.BLOCKS.getValue(
                    new ResourceLocation(e.getKey()));
            if (resolved == null || resolved != e.getValue().get()) {
                throw new IllegalStateException(
                        "E_REG_UNRESOLVED:registered but unresolvable <"
                                + e.getKey() + ">");
            }
            System.out.println("[MatouBridge] registered <" + e.getKey()
                    + "> id " + Block.getStateId(
                            e.getValue().get().getDefaultState()));
        }
    }

    private static void queueCustom(Packs.PackSpec spec) {
        String want = spec.blockName;
        int colon = want.indexOf(':');
        if (colon < 0 || want.startsWith("minecraft:")) {
            return;
        }
        if (REGISTERED.containsKey(want)) {
            return;
        }
        if (ForgeRegistries.BLOCKS.getValue(
                new ResourceLocation(want)) != null) {
            throw new IllegalArgumentException(
                    "E_REG_DUP:already registered <" + want + ">");
        }
        String shortName = want.substring(colon + 1);
        String ownedFile = spec.args.get("ownedFile");
        if (ownedFile == null) {
            throw new IllegalArgumentException("E_REG_NOSPEC:no ownedFile "
                    + "for custom <" + want + "> (operator must point at "
                    + "the content declaring it)");
        }
        float hardness = 0.0f;
        boolean found = false;
        for (Object o : loadSpecs(ownedFile)) {
            if (!shortName.equals(specField(o, "name", ownedFile))) {
                continue;
            }
            if (found) {
                throw new IllegalArgumentException("E_REG_SPEC:dup <"
                        + shortName + "> in <" + ownedFile + ">");
            }
            Object h = specField(o, "hardness", ownedFile);
            Object op = specField(o, "opaque", ownedFile);
            if (!(h instanceof Float) || !(op instanceof Boolean)) {
                throw new IllegalArgumentException("E_REG_SPEC:shape <"
                        + ownedFile + "> (bad physics types)");
            }
            if (!((Boolean) op).booleanValue()) {
                throw new IllegalArgumentException("E_REG_SPEC:translucent <"
                        + shortName + "> (no boolean opacity slot on 1.16.5)");
            }
            hardness = ((Float) h).floatValue();
            found = true;
        }
        if (!found) {
            throw new IllegalArgumentException("E_REG_NOSPEC:no block <"
                    + shortName + "> in <" + ownedFile + "> for <" + want
                    + ">");
        }
        // Short name on purpose: the register builds the entry id as
        // (modid, name) from this DeferredRegister's own modid, so short
        // in, example1:my_ore out. Measured on the provisioned 36.2.42
        // bytes, same lesson as the 1.7.10 short-name fix.
        final float landed = hardness;
        try {
            REGISTERED.put(want, BLOCKS.register(shortName,
                    () -> new MatouBlock(landed)));
        } catch (Exception e) {
            throw new IllegalArgumentException("E_REG_BLOCK:refused <"
                    + want + "> (" + e.getMessage() + ")", e);
        }
    }

    /**
     * Content specs, reached reflectively: the bridge stays content-blind
     * at build time (Q2 — same rule as {@code Packs.load}). Every
     * failure is coded E_REG_*, never a silent default.
     */
    private static List<?> loadSpecs(String ownedFile) {
        final Class<?> cls;
        try {
            cls = Class.forName("fr.iamacat.example1.BlockSpec");
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("E_REG_SPEC:missing "
                    + "example1 for <" + ownedFile + "> ("
                    + e.getMessage() + ")", e);
        }
        final Method fromFile;
        try {
            fromFile = cls.getMethod("fromFile", String.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("E_REG_SPEC:shape "
                    + "<fr.iamacat.example1.BlockSpec> ("
                    + e.getMessage() + ")", e);
        }
        try {
            Object out = fromFile.invoke(null, ownedFile);
            if (!(out instanceof List)) {
                throw new IllegalStateException("E_REG_SPEC:shape "
                        + "<fromFile> (want List)");
            }
            return (List<?>) out;
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalArgumentException("E_REG_SPEC:unreadable <"
                    + ownedFile + "> (" + cause.getMessage() + ")", e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("E_REG_SPEC:shape <"
                    + ownedFile + "> (" + e.getMessage() + ")", e);
        }
    }

    private static Object specField(Object spec, String getter,
            String ownedFile) {
        try {
            return spec.getClass().getMethod(getter).invoke(spec);
        } catch (Exception e) {
            throw new IllegalArgumentException("E_REG_SPEC:shape <"
                    + ownedFile + "> (no " + getter + ")", e);
        }
    }
}
