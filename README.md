# matou-dev/bridge-1165 — SPI ↔ Minecraft 1.16.5 translator

The only module allowed to touch MC/Forge 1.16.5 (Forge 36.2.42) on
this side. Translates `matou-spi` into the game (world, registries).

Modid: `matoubridge` (see hub `NAMES.md` — reused from the other bridges,
safe: two bridges never load in the same MC instance).

The decide/apply seam (`fr.iamacat.bridge`: `SpiBridge`, `CellSink`,
`ForgeCells`, `ForgeSnapshot`, `ForgeContent`, `Packs`) ships from
`matou-spi` (see `SPI_PIN`) at identical FQNs — this repo carries only its
Forge side below. Pure coverage lives in SPI (`BridgeCheck`); content E2E
(`ForgeContentCheck` against `../example1`) is wired in E2 below.

## E1 Forge wiring (Forge 36.2.42)

Only `forge/` touches MC/Forge (`World.setBlockState` + `BlockPos` + `BlockState`,
block resolve through `ForgeRegistries.BLOCKS`, overworld y `0..255`):

- `forge/src/fr/iamacat/bridge/forge`: `MatouBridgeMod`
  (`@Mod("matoubridge")`, constructor registers on `EVENT_BUS`,
  `WorldTickEvent` `END` + `OVERWORLD` key → snapshot `matou:tick` →
  pure decide), `PackWire` (reflective bind + block
  resolve + y check, fail fast), `WorldCellSink` (`CellSink` into the
  world, volume cells resolve their block by name, cached, unknown refused
  loudly). Passive until `packs.cfg` exists (Q1 coexistence).
- `tools/live/stub`: shape-only 1.16.5 API used by `forge/` (compile
  classpath only, never runs). Etage 2 compiles `forge/` against it —
  green with no MC jars. `run-live.sh` (E3) asserts these members
  against the provisioned 1.16.5-36.2.42 jars once wired.

Gate: `tools/check.sh` (etage 1 siblings-spi-ex1 compile + pure E2E,
etage 2 forge-vs-stub compile, etage 3 `LIVE=1` runs `tools/run-live.sh`).

## E2 content wiring (packs)

Packs come from `config/matoubridge/packs.cfg`
(`<class> <y> <block> [k=v ...]`, `#` comments, missing file = passive
like E1). Contract in `matou-spi` (`ContentPack` + optional
`ConfigurablePack` for operator args, `ForgeContent` for decide +
apply), `Packs` (strict config parse + reflective `load`) from the
shared seam, E2E `java/test/.../ForgeContentCheck` (real example1 jobs
from the source files + fake world; `ForgeContent.merge ==
AdditiveScatterJob.merge` comparator). Body kept in sync with the other
bridges by convention.

## E3 live proof

`tools/run-live.sh` (LIVE=1, Java 8): provisions Forge 1.16.5-36.2.42
(checksum-verified), derives the narrow MCP→SRG map from pinned bytes
(vanilla server + `joined.tsrg` + MCP snapshot names via `javap`), pins
every stub member against the provisioned jars (vanilla members against
the derived SRG, Forge members against universal/eventbus), builds
versioned jars, reobfuscates the bridge, boots the server 150s
(`forge-1.16.5-36.2.42.jar nogui`, flat world), then proves
world == pure union (stone only).

Production naming (measured, not assumed): SRG classes + SRG members at
runtime (the installer-renamed server jar); `forge/` sources are MCP and
reobfuscate (Reobf, the ForgeGradle reobf equivalent). The snapshot name
lock is load-bearing (World carries three same-type static RegistryKey
fields — descriptor alone cannot pick OVERWORLD). The bridge ships FAT
(spi + example1 embedded — ModLauncher isolates every mods/ jar);
`mods.toml` carries the version stamp. Last green proof: hub STATE.md.

## Dev client (Prism, NOT a gate)

`tools/run-client.sh` stages a Prism Launcher instance
(`matou-1165-dev`, MC 1.16.5 / Forge 36.2.42) with a DEV build of the FAT
bridge plus content, so the proof can be played in a real game. It reuses
the live pipeline truth (narrow SRG + ASM from a provisioned `E3_DIR` —
run `tools/run-live.sh` once first) and the same build flags; bytes are
DEV bytes (dirty tree allowed), the release path stays `BUILD_ONLY`.
`TELLME_JAR` optionally adds a runtime inspector (`/tellme looking-at`
for block NBT); unset installs the bridge only. Launch with Prism
(`--launch "matou-1165-dev"`), create a FLAT world named `matou`, quit,
then `tools/verify-client-save.sh [world]` replays the E3 verdict
(world == pure union) on the client save.
