#!/bin/sh
# Dev client helper (NOT a gate): stages a Prism Launcher instance
# (matou-1165-dev, MC 1.16.5 / Forge 36.2.42) with a DEV build of the FAT
# bridge plus content, so the proof can be played and inspected in a real
# game instead of only on the nogui server verdict.
#
# Reuse contract (no duplication of provisioning truth):
#   - Upstream pins + narrow SRG map come from a provisioned E3_DIR
#     (run tools/run-live.sh once; needs network). This script never
#     re-derives them: missing srg-narrow.srg fails loudly.
#   - Build flags mirror run-live.sh steps 3-4 (javac 8, FAT bridge,
#     Reobf, mods.toml stamp). Bytes are DEV bytes (dirty tree allowed):
#     the release path stays run-live.sh BUILD_ONLY.
#   - World verdict reuses tools/live/anvil.py + CellUnion via
#     tools/verify-client-save.sh after you quit the game.
#
# Env (no machine paths hardcoded):
#   CLIENT_DIR work dir (default ${TMPDIR:-/tmp}/matou-e3-client)
#   E3_DIR     provisioned live dir (default ${TMPDIR:-/tmp}/matou-e3-live)
#   PRISM_DIR  Prism data root (default ~/.local/share/PrismLauncher;
#              the instance installs to $PRISM_DIR/instances/matou-1165-dev)
#   PRISM_BIN  launcher binary (default prismlauncher on PATH)
#   JAVA8_HOME Java 8 home (default /usr/lib/jvm/java-8-openjdk)
#   TELLME_JAR optional runtime inspector mod jar (e.g. TellMe 1.16.5 from
#              CurseForge) copied into mods/; TELLME_SHA1 optionally pins it.
#              Unset = bridge only, plus a printed suggestion. The script
#              never downloads unknown bytes on your behalf.
#   EXTRA_MODS_DIR optional dir of extra dev-comfort mod jars (perf inspectors,
#              e.g. LazyDFU + ModernFix + FerriteCore + Embeddium for 1.16.5,
#              downloaded once by you from Modrinth/CurseForge). Every *.jar
#              is copied into mods/; an optional SHA256SUMS file inside is
#              verified first (same practice as the R2 release drop). Unset =
#              bridge only. Perf mods stay dev-only: they never ship in dist/
#              and must never change placed blocks (render/RAM/DFU only) —
#              a verdict drift after adding one fails loudly in the verifier,
#              which is the point. The script never downloads mods itself:
#              unknown bytes are never fetched silently.
#   LAUNCH=1   actually exec prismlauncher --launch (default prints the
#              command; launching needs a display and blocks the shell).
set -eu
cd "$(dirname "$0")/.."
CLIENT_DIR="${CLIENT_DIR:-${TMPDIR:-/tmp}/matou-e3-client}"
E3_DIR="${E3_DIR:-${TMPDIR:-/tmp}/matou-e3-live}"
PRISM_DIR="${PRISM_DIR:-$HOME/.local/share/PrismLauncher}"
PRISM_BIN="${PRISM_BIN:-prismlauncher}"
JAVA8_HOME="${JAVA8_HOME:-/usr/lib/jvm/java-8-openjdk}"
INST="matou-1165-dev"
VERSION="${VERSION:-0.0-dev}"

command -v "$PRISM_BIN" >/dev/null 2>&1 \
  || { echo "FAIL run-client : <$PRISM_BIN> not on PATH (install PrismLauncher 11+)"; exit 1; }
J8="$JAVA8_HOME/bin"
[ -x "$J8/java" ] || { echo "FAIL run-client : no Java 8 at <$JAVA8_HOME>"; exit 1; }
[ -x "$J8/javac" ] || { echo "FAIL run-client : no javac at <$JAVA8_HOME>"; exit 1; }
[ -d ../spi/java/src ] || { echo "FAIL run-client : spi sibling absent"; exit 1; }
[ -d ../example1/java/src ] || { echo "FAIL run-client : example1 sibling absent"; exit 1; }
[ -d ../minimap/java/src ] || { echo "FAIL run-client : minimap sibling absent"; exit 1; }
command -v python3 >/dev/null || { echo "FAIL run-client : python3 required (Reobf/normjar)"; exit 1; }
# Provisioning truth comes from the live pipeline, never re-derived here.
SRG_NARROW="$E3_DIR/srg-narrow.srg"
[ -f "$SRG_NARROW" ] || {
  echo "FAIL run-client : narrow SRG absent ($SRG_NARROW)"
  echo "fix: run tools/run-live.sh once first (provisions + pins upstream), or point E3_DIR at a provisioned dir"
  exit 1; }
ASM=$(find "$E3_DIR/server/libraries/org/ow2/asm/asm/9.6" -name "asm-9.6.jar" 2>/dev/null | head -n 1 || true)
ASM_COMMONS=$(find "$E3_DIR/server/libraries/org/ow2/asm/asm-commons/9.6" -name "asm-commons-9.6.jar" 2>/dev/null | head -n 1 || true)
[ -n "$ASM" ] && [ -n "$ASM_COMMONS" ] \
  || { echo "FAIL run-client : ASM 9.6 absent under <$E3_DIR/server/libraries> (run tools/run-live.sh once first)"; exit 1; }

# 1. DEV build (same flags as run-live.sh steps 3-4; dirty tree allowed).
BLD="$CLIENT_DIR/build"
rm -rf "$BLD" \
  || { echo "FAIL run-client : cannot clear <$BLD>"; exit 1; }
mkdir -p "$BLD/spi" "$BLD/ex1" "$BLD/mini" "$BLD/forge" "$BLD/jars" "$BLD/modstoml/META-INF"
"$J8/javac" -source 8 -target 8 -nowarn -d "$BLD/spi" $(find ../spi/java/src -name '*.java')
"$J8/javac" -source 8 -target 8 -nowarn -cp "$BLD/spi" -d "$BLD/ex1" $(find ../example1/java/src -name '*.java')
"$J8/javac" -source 8 -target 8 -nowarn -cp "$BLD/spi" -d "$BLD/mini" $(find ../minimap/java/src -name '*.java')
"$J8/javac" -source 8 -target 8 -nowarn -cp "$BLD/spi:$BLD/ex1" -d "$BLD/forge" $(find tools/live/stub forge/src -name '*.java')
sed "s/@VERSION@/$VERSION/g" forge/src/META-INF/mods.toml > "$BLD/modstoml/META-INF/mods.toml"
EPOCH="$(git log -1 --format=%ct 2>/dev/null || date +%s)"
printf 'Manifest-Version: 1.0\nImplementation-Version: %s\n' "$VERSION" > "$BLD/MANIFEST.MF"
normjar() {
  python3 - "$1" "$EPOCH" <<'EOF'
import sys, zipfile, datetime
path, epoch = sys.argv[1], int(sys.argv[2])
# fromtimestamp(tz=utc): same instant as the gate's utcfromtimestamp, minus
# the host-Python 3.12 DeprecationWarning noise in task output.
dt = datetime.datetime.fromtimestamp(epoch, datetime.timezone.utc).timetuple()[:6]
zin = zipfile.ZipFile(path)
items = [(i, zin.read(i.filename)) for i in zin.infolist()]
zin.close()
zout = zipfile.ZipFile(path + ".norm", "w", zipfile.ZIP_DEFLATED)
for info, data in items:
    info.date_time = dt
    info.create_system = 0
    zout.writestr(info, data)
zout.close()
EOF
  mv "$1.norm" "$1"
}
mkjar() {
  out="$1"; stage="$2"
  files=$(cd "$stage" && find . -type f | LC_ALL=C sort)
  (cd "$stage" && "$J8/jar" cfm "$out" "$BLD/MANIFEST.MF" $files)
  normjar "$out"
}
mkjar "$BLD/jars/matou-spi.jar" "$BLD/spi"
mkjar "$BLD/jars/matou-example1.jar" "$BLD/ex1"
mkjar "$BLD/jars/matou-minimap.jar" "$BLD/mini"
rm -rf "$BLD/bridgemod" && mkdir -p "$BLD/bridgemod"
cp -r "$BLD/forge/"* "$BLD/bridgemod/"
rm -rf "$BLD/bridgemod/net" "$BLD/bridgemod/META-INF"
[ -e "$BLD/bridgemod/net" ] && { echo "FAIL run-client : stub leak into mod jar"; exit 1; }
cp -r "$BLD/spi/"* "$BLD/ex1/"* "$BLD/bridgemod/"
mkdir -p "$BLD/bridgemod/META-INF"
cp "$BLD/modstoml/META-INF/mods.toml" "$BLD/bridgemod/META-INF/mods.toml"
mkjar "$BLD/jars/matoubridge.jar" "$BLD/bridgemod"
"$J8/javac" -nowarn -cp "$ASM:$ASM_COMMONS" -d "$BLD" tools/live/Reobf.java
"$J8/java" -cp "$BLD:$ASM:$ASM_COMMONS" Reobf "$SRG_NARROW" "$BLD/jars/matoubridge.jar" "$BLD/jars/matoubridge-reobf.jar"
normjar "$BLD/jars/matoubridge-reobf.jar"
# Keep CellUnion compiled: tools/verify-client-save.sh reuses it, so the
# union logic is never duplicated between server verdict and client verify.
"$J8/javac" -nowarn -cp "$BLD/spi:$BLD/ex1" -d "$BLD" tools/live/CellUnion.java
echo "ok run-client : dev jars built (VERSION=$VERSION, DEV bytes, not release)"

# 2. Prism instance (MultiMC format, as proven by local Prism 11 instances:
#    instance.cfg + mmc-pack.json + minecraft/ game dir).
IDIR="$PRISM_DIR/instances/$INST"
mkdir -p "$IDIR/minecraft/mods" "$IDIR/minecraft/config/matoubridge"
cat > "$IDIR/mmc-pack.json" <<'EOF'
{
    "components": [
        {
            "cachedName": "Minecraft",
            "important": true,
            "uid": "net.minecraft",
            "version": "1.16.5"
        },
        {
            "cachedName": "Forge",
            "uid": "net.minecraftforge",
            "version": "36.2.42"
        }
    ],
    "formatVersion": 1
}
EOF
# Minimal instance.cfg: Prism fills component metadata on first launch.
# Java 8 pinned explicitly (E3 runtime); memory modest for a flat test world.
cat > "$IDIR/instance.cfg" <<EOF
[General]
ConfigVersion=1.3
InstanceType=OneSix
JavaPath=$JAVA8_HOME/bin/java
ManagedPack=false
MaxMemAlloc=4096
MinMemAlloc=1024
OverrideJavaLocation=true
OverrideMemory=true
iconKey=default
name=$INST
notes=matou-dev bridge-1165 dev client (run-client.sh; DEV bytes, not release)
EOF
cp "$BLD/jars/matoubridge-reobf.jar" "$IDIR/minecraft/mods/matoubridge.jar"
rm -rf "$IDIR/minecraft/matou-content" && cp -r ../example1/content "$IDIR/minecraft/matou-content"
GDIR="$IDIR/minecraft"
# packs.cfg: written once, then KEPT. Re-staging must never clobber a dev's
# alias bindings (e.g. hut_wall=oak_planks for a varied hut) back to the
# stone proof defaults — that made "whatever happens it's stone".
if [ -f "$IDIR/minecraft/config/matoubridge/packs.cfg" ]; then
  echo "note run-client : keeping existing packs.cfg (delete it to reset to stone proof defaults):"
  grep -v "^#" "$IDIR/minecraft/config/matoubridge/packs.cfg" || true
else
  printf 'fr.iamacat.example1.ExamplePack 63 minecraft:stone ownedFile=%s/matou-content/owned.matou scatterFile=%s/matou-content/additive.matou structureFile=%s/matou-content/structure.matou block.example1.structures:hut_wall=minecraft:stone block.example1.structures:hut_roof=minecraft:stone\n' "$GDIR" "$GDIR" "$GDIR" > "$IDIR/minecraft/config/matoubridge/packs.cfg"
fi
if [ -n "${TELLME_JAR:-}" ]; then
  [ -f "$TELLME_JAR" ] || { echo "FAIL run-client : TELLME_JAR=<$TELLME_JAR> absent"; exit 1; }
  if [ -n "${TELLME_SHA1:-}" ]; then
    echo "$TELLME_SHA1  $TELLME_JAR" | sha1sum -c - >/dev/null 2>&1 \
      || { echo "FAIL run-client : TellMe sha1 drift (want $TELLME_SHA1)"; exit 1; }
  fi
  cp "$TELLME_JAR" "$IDIR/minecraft/mods/"
  echo "ok run-client : TellMe installed ($(basename "$TELLME_JAR"))"
else
  echo "note run-client : no TELLME_JAR (bridge only). Runtime inspector suggestion:"
  echo "  TellMe 1.16.5 (CurseForge) gives /tellme looking-at|holding|batch-run"
  echo "  for NBT/registry dumps; pin its sha1 in TELLME_SHA1 on first download."
fi
if [ -n "${EXTRA_MODS_DIR:-}" ]; then
  [ -d "$EXTRA_MODS_DIR" ] || { echo "FAIL run-client : EXTRA_MODS_DIR=<$EXTRA_MODS_DIR> absent"; exit 1; }
  if [ -f "$EXTRA_MODS_DIR/SHA256SUMS" ]; then
    (cd "$EXTRA_MODS_DIR" && sha256sum -c SHA256SUMS) \
      || { echo "FAIL run-client : extra mods SHA256SUMS mismatch"; exit 1; }
    echo "ok run-client : extra mods pinned (SHA256SUMS verified)"
  else
    echo "note run-client : no SHA256SUMS in <$EXTRA_MODS_DIR> (unverified copy;"
    echo "  create one with (cd dir && sha256sum *.jar > SHA256SUMS) to pin the bytes)"
  fi
  count=$(ls "$EXTRA_MODS_DIR"/*.jar 2>/dev/null | wc -l)
  [ "$count" -gt 0 ] || { echo "FAIL run-client : no jars in <$EXTRA_MODS_DIR>"; exit 1; }
  cp "$EXTRA_MODS_DIR"/*.jar "$IDIR/minecraft/mods/"
  echo "ok run-client : extra mods installed ($count jars)"
  echo "  perf picks for 1.16.5/36.2.42 live here (dev-only, never in dist/):"
  echo "  LazyDFU + ModernFix + FerriteCore + Embeddium — render/RAM/DFU only."
  echo "  If tools/verify-client-save.sh drifts after adding one, the mod"
  echo "  changed placed blocks: drop it loudly, keep the verdict."
fi
echo "ok run-client : instance staged <$IDIR>"

# 3. Play protocol (owned slice, same geometry as the E3 server proof).
cat <<'EOF'
--- play protocol ---
1. Launch:  prismlauncher --launch "matou-1165-dev"   (offline works: --offline MatouDev)
   First launch downloads MC 1.16.5 + Forge 36.2.42 into the instance (network once).
2. Singleplayer: create NEW world named "matou", game mode Creative, FLAT type.
   The wire lands plane cells at y=63 and hut volumes at y=64..65 around the
   origin (chunks 0..1, rows -1..1) — same 1274-cell stone union as E3.
   STAY near spawn ~4 min (4000 ticks) without wandering: every tick decides
   DIFFERENT cells (tick-addressed RNG), so a chunk unloaded mid-run loses
   its early cells forever — the union only accumulates in continuously
   loaded chunks. Wandering first, verifying later always undercounts.
3. Owned check (backend seul): fresh flat world shows the stone hut near
   spawn; nothing else changes. Compat check (additif tardif): open any
   existing vanilla world instead — vanilla builds stay intact, our cells
   only append where absent (never replace, never duplicate).
4. Runtime values: F3 screen for pos/chunk; /tellme looking-at for block NBT
   (if TellMe installed); instance log for E_FORGE/E_BRIDGE/E_EXAMPLE refusals
   (loud, never silent); MinimapJob rows stay server-side proof (M3) until a
   client blit lands them.
5. Quit the game (flush the save), then:
     tools/verify-client-save.sh [world-name]   (default: matou)
   replays the E3 verdict (world == pure union) on the client save.
EOF
if [ "${LAUNCH:-}" = "1" ]; then
  exec "$PRISM_BIN" --launch "$INST"
else
  echo "staged (no launch: LAUNCH=1 to exec $PRISM_BIN --launch $INST)"
fi
