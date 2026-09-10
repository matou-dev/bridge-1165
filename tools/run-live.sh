#!/bin/sh
# E3 live gate: Forge 1.16.5-36.2.42 server run proving PackWire.bind
# (real Block resolve) plus world-tick apply on a real world, then comparing
# the world against the pure decision union (tools/live).
#
# Manual gate (needs network once + Java 8); opt-in from tools/check.sh via
# LIVE=1, never blocking by default. Never silent: any mismatch fails loudly,
# never defaulted.
#
# Env (no machine paths hardcoded):
#   E3_DIR     work dir (default ${TMPDIR:-/tmp}/matou-e3-live; non-owned
#              leftovers refused loudly by the preflight — clean or fresh dir)
#   JAVA8_HOME Java 8 home (default /usr/lib/jvm/java-8-openjdk)
#   FORGE_URL  installer URL (default Maven 36.2.42 installer)
#   MCP_CONFIG_URL  MCP config URL (default Forge Maven mcp_config
#              1.16.5-20210115.111550, carries joined.tsrg, the obf<->SRG map
#              the narrow SRG derives from)
#   MCP_SNAPSHOT_URL  MCP snapshot URL (default Forge Maven mcp_snapshot
#              20210309-1.16.5, carries fields.csv/methods.csv, the SRG<->MCP
#              name lock — required, not documentary: World carries three
#              same-type static RegistryKey fields, so descriptor + static
#              alone cannot pick OVERWORLD)
#   BOOT_SECS  server run time (default 150; short runs fail coverage loudly)
#   E3_OFFLINE=1  never download (fail loudly if cache files missing)
#
# R2 release assembly: BUILD_ONLY=1 VERSION=x.y.z assembles dist/ (versioned
# jars + content + packs.cfg.example + SHA256SUMS) and exits before booting
# the server. Release demands strict X.Y.Z, a clean tree in all 4 code repos,
# and a tokenized mods.toml template; anything else fails loudly, never
# defaulted. SOURCE_DATE_EPOCH pins jar entry timestamps (default: bridge
# HEAD commit time); with a pinned toolchain (tools/live/Dockerfile) the
# same commit always yields the same bytes.
#
# Reproducibility pins: installer / universal / vanilla server / MCP config /
# MCP snapshot / ASM sha1 below. Any upstream drift fails loudly instead of
# running against unknown bytes. 1.16.5 ships no srg-mcp.srg (ForgeGradle is
# not required): the 4-line narrow map derives deterministically from the
# pinned vanilla server + joined.tsrg + snapshot names (see step 2), so the
# pins below are the whole upstream surface.
#
# Production naming (MCP model, same as C3): runtime vanilla is SRG
# (server-1.16.5-*-srg.jar in the provisioned libraries); forge/ sources
# are MCP and reobfuscate MCP->SRG (Reobf, the ForgeGradle reobf
# equivalent). Forge classes are never obfuscated and pass through.
set -eu
cd "$(dirname "$0")/.."
E3_DIR="${E3_DIR:-${TMPDIR:-/tmp}/matou-e3-live}"
JAVA8_HOME="${JAVA8_HOME:-/usr/lib/jvm/java-8-openjdk}"
FORGE_URL="${FORGE_URL:-https://maven.minecraftforge.net/net/minecraftforge/forge/1.16.5-36.2.42/forge-1.16.5-36.2.42-installer.jar}"
MCP_CONFIG_URL="${MCP_CONFIG_URL:-https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_config/1.16.5-20210115.111550/mcp_config-1.16.5-20210115.111550.zip}"
MCP_SNAPSHOT_URL="${MCP_SNAPSHOT_URL:-https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_snapshot/20210309-1.16.5/mcp_snapshot-20210309-1.16.5.zip}"
BOOT_SECS="${BOOT_SECS:-150}"
# Pins: measured 2026-09-09 from the Maven installer + installed universal +
# Mojang vanilla server + MCP config + MCP snapshot + provisioned ASM 9.6.
# Drift = loud failure, never silent upgrade.
INSTALLER_SHA1="e09ecf910e4d5eae12fb3564d9b7de212c1958b2"
UNIVERSAL_SHA1="fd07fe6c2c1cc7d3e403a1db7eadfa935ca7640d"
MC_SERVER_SHA1="1b557e7b033b583cd9f66746b7a9ab1ec1673ced"
MCP_CONFIG_SHA1="a81464c686be42933aacac7898f8c0d50edccf51"
MCP_SNAPSHOT_SHA1="f6273df8817a92d41cf417a1a5935c08e09035ec"
ASM_PIN="asm-9.6.jar"
ASM_SHA1="aa205cf0a06dbd8e04ece91c0b37c3f5d567546a"
ASM_COMMONS_PIN="asm-commons-9.6.jar"
ASM_COMMONS_SHA1="f1a9e5508eff490744144565c47326c8648be309"
J8="$JAVA8_HOME/bin"
[ -x "$J8/java" ] || { echo "FAIL e3-live : no Java 8 at <$JAVA8_HOME>"; exit 1; }
[ -x "$J8/javac" ] || { echo "FAIL e3-live : no javac at <$JAVA8_HOME>"; exit 1; }
[ -x "$J8/javap" ] || { echo "FAIL e3-live : no javap at <$JAVA8_HOME>"; exit 1; }
[ -d ../spi/java/src ] || { echo "FAIL e3-live : spi sibling absent"; exit 1; }
[ -d ../example1/java/src ] || { echo "FAIL e3-live : example1 sibling absent"; exit 1; }
[ -d ../minimap/java/src ] || { echo "FAIL e3-live : minimap sibling absent"; exit 1; }
command -v python3 >/dev/null || { echo "FAIL e3-live : python3 required (narrow derive + anvil verify)"; exit 1; }
# R2 versioning: VERSION stamps manifests + mods.toml. Dev live runs take an
# explicit non-release default; release assembly demands strict X.Y.Z.
VERSION="${VERSION:-0.0-dev}"
if [ "${BUILD_ONLY:-}" = "1" ]; then
  printf '%s' "$VERSION" | grep -Eq '^[0-9]+\.[0-9]+\.[0-9]+$' \
    || { echo "FAIL r2-release : VERSION=<$VERSION> not X.Y.Z (want e.g. 1.0.0)"; exit 1; }
  for r in . ../spi ../example1 ../minimap; do
    git -C "$r" diff --quiet && git -C "$r" diff --cached --quiet \
      || { echo "FAIL r2-release : dirty tree in <$r> (release from clean checkouts only)"; exit 1; }
  done
  grep -q 'version="@VERSION@"' forge/src/META-INF/mods.toml \
    || { echo "FAIL r2-release : mods.toml template hardcoded (keep @VERSION@, bump via VERSION=)"; exit 1; }
fi

# 1. Provision the 36.2.42 server once (idempotent, checksum-verified).
#    E3_OFFLINE=1 never touches the network: missing cache fails loudly.
mkdir -p "$E3_DIR"
SERV="$E3_DIR/server"
mkdir -p "$SERV"
# 1a. E3_DIR preflight: docker runs leave root-owned leftovers (build/,
#     world/, logs/, matou-content/) that a host run cannot clear file by
#     file (rm needs write on the root-owned parent). Fail fast with the fix
#     instead of dying mid-run or reusing stale state silently.
if [ -e "$E3_DIR" ]; then
  BAD_OWNER=$(find "$E3_DIR" ! -user "$(id -un)" -print -quit 2>/dev/null || true)
  if [ -n "$BAD_OWNER" ]; then
    echo "FAIL e3-live : E3_DIR=<$E3_DIR> has non-owned leftovers (e.g. <$BAD_OWNER> from a docker run as root)"
    echo "fix: sudo rm -rf <$E3_DIR/build> <$E3_DIR/server/world> <$E3_DIR/server/logs> <$E3_DIR/server/matou-content> OR E3_DIR=/tmp/matou-e3-clean $0"
    exit 1
  fi
  if [ ! -w "$E3_DIR" ]; then
    echo "FAIL e3-live : E3_DIR=<$E3_DIR> not writable (fix ownership or point E3_DIR at a user-owned dir)"
    exit 1
  fi
fi
if [ ! -f "$E3_DIR/forge-installer.jar" ]; then
  if [ "${E3_OFFLINE:-}" = "1" ]; then
    echo "FAIL e3-live : offline and installer absent ($E3_DIR/forge-installer.jar)"
    exit 1
  fi
  curl -sL -o "$E3_DIR/forge-installer.jar" "$FORGE_URL" \
    || { echo "FAIL e3-live : installer download"; exit 1; }
fi
echo "$INSTALLER_SHA1  $E3_DIR/forge-installer.jar" | sha1sum -c - >/dev/null 2>&1 \
  || { echo "FAIL e3-live : installer sha1 drift (want $INSTALLER_SHA1)"; exit 1; }
UNI="$SERV/libraries/net/minecraftforge/forge/1.16.5-36.2.42/forge-1.16.5-36.2.42-universal.jar"
if [ ! -f "$UNI" ]; then
  (cd "$SERV" && "$J8/java" -jar "$E3_DIR/forge-installer.jar" --installServer >/dev/null 2>&1) \
    || { echo "FAIL e3-live : --installServer"; exit 1; }
fi
echo "$UNIVERSAL_SHA1  $UNI" | sha1sum -c - >/dev/null 2>&1 \
  || { echo "FAIL e3-live : universal sha1 drift (want $UNIVERSAL_SHA1)"; exit 1; }
MCSERV="$SERV/minecraft_server.1.16.5.jar"
[ -f "$MCSERV" ] || { echo "FAIL e3-live : vanilla server absent ($MCSERV, re-run --installServer online)"; exit 1; }
echo "$MC_SERVER_SHA1  $MCSERV" | sha1sum -c - >/dev/null 2>&1 \
  || { echo "FAIL e3-live : vanilla server sha1 drift (want $MC_SERVER_SHA1)"; exit 1; }
BOOT_JAR="$SERV/forge-1.16.5-36.2.42.jar"
[ -f "$BOOT_JAR" ] || { echo "FAIL e3-live : boot jar absent ($BOOT_JAR, re-run --installServer online)"; exit 1; }
ASM=$(find "$SERV/libraries/org/ow2/asm/asm/9.6" -name "$ASM_PIN" | head -n 1)
[ -n "$ASM" ] || { echo "FAIL e3-live : ASM $ASM_PIN missing from server libs"; exit 1; }
echo "$ASM_SHA1  $ASM" | sha1sum -c - >/dev/null 2>&1 \
  || { echo "FAIL e3-live : ASM sha1 drift (want $ASM_SHA1)"; exit 1; }
ASM_COMMONS=$(find "$SERV/libraries/org/ow2/asm/asm-commons/9.6" -name "$ASM_COMMONS_PIN" | head -n 1)
[ -n "$ASM_COMMONS" ] || { echo "FAIL e3-live : ASM commons $ASM_COMMONS_PIN missing from server libs"; exit 1; }
echo "$ASM_COMMONS_SHA1  $ASM_COMMONS" | sha1sum -c - >/dev/null 2>&1 \
  || { echo "FAIL e3-live : ASM commons sha1 drift (want $ASM_COMMONS_SHA1)"; exit 1; }
EVENTBUS=$(find "$SERV/libraries/net/minecraftforge/eventbus" -name "eventbus-4.0.0.jar" | head -n 1)
[ -n "$EVENTBUS" ] || { echo "FAIL e3-live : eventbus 4.0.0 missing from server libs"; exit 1; }
if [ ! -f "$E3_DIR/mcp_config-1.16.5-20210115.111550.zip" ]; then
  if [ "${E3_OFFLINE:-}" = "1" ]; then
    echo "FAIL e3-live : offline and MCP config absent ($E3_DIR/mcp_config-1.16.5-20210115.111550.zip)"
    exit 1
  fi
  curl -sL -o "$E3_DIR/mcp_config-1.16.5-20210115.111550.zip" "$MCP_CONFIG_URL" \
    || { echo "FAIL e3-live : MCP config download"; exit 1; }
fi
echo "$MCP_CONFIG_SHA1  $E3_DIR/mcp_config-1.16.5-20210115.111550.zip" | sha1sum -c - >/dev/null 2>&1 \
  || { echo "FAIL e3-live : MCP config sha1 drift (want $MCP_CONFIG_SHA1)"; exit 1; }
if [ ! -f "$E3_DIR/mcp_snapshot-20210309-1.16.5.zip" ]; then
  if [ "${E3_OFFLINE:-}" = "1" ]; then
    echo "FAIL e3-live : offline and MCP snapshot absent ($E3_DIR/mcp_snapshot-20210309-1.16.5.zip)"
    exit 1
  fi
  curl -sL -o "$E3_DIR/mcp_snapshot-20210309-1.16.5.zip" "$MCP_SNAPSHOT_URL" \
    || { echo "FAIL e3-live : MCP snapshot download"; exit 1; }
fi
echo "$MCP_SNAPSHOT_SHA1  $E3_DIR/mcp_snapshot-20210309-1.16.5.zip" | sha1sum -c - >/dev/null 2>&1 \
  || { echo "FAIL e3-live : MCP snapshot sha1 drift (want $MCP_SNAPSHOT_SHA1)"; exit 1; }
echo "ok e3-live : server provisioned (pins verified)"

# 2. Derive the narrow MCP->SRG map from pinned bytes (no srg-mcp.srg on
#    1.16.5): joined.tsrg gives obf<->SRG per class, the notch server jar
#    disambiguates overloads and static-ness via javap (exactly-one assert
#    per member, loud otherwise), and the MCP snapshot locks SRG<->MCP
#    names. The map covers every vanilla member our forge/ bytecode
#    references (verified by constant-pool scan at E3 time:
#    getDefaultState, setBlockState, getDimensionKey, OVERWORLD, plus the
#    registration tranche: getStateId, Properties.create,
#    hardnessAndResistance, Material.ROCK, plus the loot tranche:
#    Entity/world/getPosX/getPosY/getPosZ, World/isRemote,
#    ServerWorld/addEntity, Items/DIAMOND, Vector3i/getX/getY/getZ,
#    AbstractBlockState/getBlock).
#    The snapshot lock is load-bearing, not documentary: World carries
#    three same-type static RegistryKey fields (OVERWORLD, THE_NETHER,
#    THE_END), so descriptor + static-ness alone cannot pick OVERWORLD —
#    the field resolves snapshot-first (SRG name), then tsrg + javap
#    confirm (obf owner, static, RegistryKey type).
python3 - "$E3_DIR/mcp_config-1.16.5-20210115.111550.zip" "$E3_DIR/mcp_snapshot-20210309-1.16.5.zip" "$MCSERV" "$J8/javap" "$E3_DIR/srg-narrow.srg" <<'EOF'
import re, subprocess, sys, zipfile
mcpcfg, snapshot, server, javap, outpath = sys.argv[1:6]
tsrg = zipfile.ZipFile(mcpcfg).read("config/joined.tsrg").decode("utf-8")
# (owner_srg, srg_name, mcp_name, desc_srg, kind, static?) — the full
# vanilla surface of forge/ (constant-pool truth, E3 time).
WANT = [
    ("net/minecraft/block/Block", "func_176223_P", "getDefaultState", "()Lnet/minecraft/block/BlockState;", "method", False),
    ("net/minecraft/world/World", "func_175656_a", "setBlockState", "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z", "method", False),
    ("net/minecraft/world/World", "func_234923_W_", "getDimensionKey", "()Lnet/minecraft/util/RegistryKey;", "method", False),
    ("net/minecraft/world/World", "field_234918_g_", "OVERWORLD", "Lnet/minecraft/util/RegistryKey;", "field", True),
    ("net/minecraft/block/Block", "func_196246_j", "getStateId", "(Lnet/minecraft/block/BlockState;)I", "method", True),
    ("net/minecraft/block/AbstractBlock$Properties", "func_200945_a", "create", "(Lnet/minecraft/block/material/Material;)Lnet/minecraft/block/AbstractBlock$Properties;", "method", True),
    ("net/minecraft/block/AbstractBlock$Properties", "func_200943_b", "hardnessAndResistance", "(F)Lnet/minecraft/block/AbstractBlock$Properties;", "method", False),
    ("net/minecraft/block/material/Material", "field_151576_e", "ROCK", "Lnet/minecraft/block/material/Material;", "field", True),
    ("net/minecraft/entity/Entity", "field_70170_p", "world", "Lnet/minecraft/world/World;", "field", False),
    ("net/minecraft/entity/Entity", "func_226277_ct_", "getPosX", "()D", "method", False),
    ("net/minecraft/entity/Entity", "func_226278_cu_", "getPosY", "()D", "method", False),
    ("net/minecraft/entity/Entity", "func_226281_cx_", "getPosZ", "()D", "method", False),
    ("net/minecraft/world/World", "field_72995_K", "isRemote", "Z", "field", False),
    ("net/minecraft/world/server/ServerWorld", "func_217376_c", "addEntity", "(Lnet/minecraft/entity/Entity;)Z", "method", False),
    ("net/minecraft/item/Items", "field_151045_i", "DIAMOND", "Lnet/minecraft/item/Item;", "field", True),
    ("net/minecraft/util/math/vector/Vector3i", "func_177958_n", "getX", "()I", "method", False),
    ("net/minecraft/util/math/vector/Vector3i", "func_177956_o", "getY", "()I", "method", False),
    ("net/minecraft/util/math/vector/Vector3i", "func_177952_p", "getZ", "()I", "method", False),
    ("net/minecraft/block/AbstractBlock$AbstractBlockState", "func_177230_c", "getBlock", "()Lnet/minecraft/block/Block;", "method", False),
]
z = zipfile.ZipFile(snapshot)
mcpnames = {}
for row in z.read("methods.csv").decode("utf-8").splitlines()[1:]:
    mcpnames[row.split(",")[0]] = row.split(",")[1]
for row in z.read("fields.csv").decode("utf-8").splitlines()[1:]:
    mcpnames.setdefault(row.split(",")[0], row.split(",")[1])
for owner, srg, mcp, desc, kind, want_static in WANT:
    assert mcpnames.get(srg) == mcp, \
        "E_SRG_DERIVE:snapshot <%s> is <%s>, want <%s>" % (srg, mcpnames.get(srg), mcp)
print("ok e3-live : snapshot names confirm 19/19")
srg2obf, classes = {}, {}
cur = None
for raw in tsrg.splitlines():
    line = raw.strip()
    if not line or line.startswith("#"):
        continue
    if raw[0] in (" ", "\t"):
        classes.setdefault(cur, []).append(line.split())
    else:
        obf, srg = line.split()
        srg2obf[srg] = obf
        cur = srg

def obf_desc(d):
    return re.sub(r"L([^;]+);",
                  lambda m: "L" + srg2obf.get(m.group(1), m.group(1)) + ";", d)

# javap spells primitive field types by name (boolean, ...), never by
# descriptor char — the loot tranche pins World/isRemote (Z), so the
# field-type key maps single-char descriptors (object types keep the
# obf_desc path above; same shape as the 1122 loot tranche).
PRIM = {"Z": "boolean", "B": "byte", "C": "char", "D": "double",
        "F": "float", "I": "int", "J": "long", "S": "short"}

def obf_ftype(d):
    if d in PRIM:
        return PRIM[d]
    return obf_desc(d)[1:-1]

def javap_flags(cls):
    # -> {(name, descriptor-or-F:type): is_static} from the notch server jar.
    out = subprocess.check_output([javap, "-p", "-s", "-cp", server, cls]).decode()
    res, name, static = {}, None, False
    for l in out.splitlines():
        s = l.strip()
        if s.startswith("descriptor:"):
            res[(name, s.split(None, 1)[1])] = static
        elif s and not s.startswith("Compiled"):
            m = re.match(r".*\s([\w$<>]+)\(", s)
            if m:
                static = bool(re.search(r"\bstatic\b", s.split("(")[0]))
                name = m.group(1)
            elif "(" not in s and s.endswith(";") and "{" not in s:
                # Field type class carries `?` for javap-printed wildcards
                # (e.g. `ceg$d<aqe<?>>` in AbstractBlock$Properties —
                # measured, not assumed: the inner class holds a generic
                # optional field, and the pre-fix class failed loud here).
                m2 = re.match(r"(?:(.*)\s)?([\w.$\[\]<>, ?]+?)\s+([\w$]+);", s)
                assert m2, "E_SRG_DERIVE:unparsed javap line <%s> in <%s>" % (s, cls)
                static = bool(re.search(r"\bstatic\b", m2.group(1) or ""))
                name = m2.group(3)
                res[(name, "F:" + re.sub(r"<.*>", "", m2.group(2)))] = static
    return res

lines = []
for row in WANT:
    owner, srg, mcp, desc, kind, want_static = row[:6]
    # SRG anchor (1122 port lesson): several vanilla members share one
    # descriptor (here four hardnessAndResistance overloads), so javap
    # static-ness alone cannot pick — the snapshot SRG name filters
    # first, tsrg + javap still confirm (an anchor missing from the
    # pinned bytes fails loud).
    anchor = srg
    obf_owner = srg2obf[owner]
    members = classes[owner]
    flags = javap_flags(obf_owner)
    if kind == "method":
        od = obf_desc(desc)
        cands = [(m[0], m[2]) for m in members if len(m) == 3 and m[1] == od]
        assert cands, "E_SRG_DERIVE:no tsrg member <%s %s>" % (owner, mcp)
        cands = [(n, s) for n, s in cands if s == anchor]
        assert cands, "E_SRG_DERIVE:no tsrg member <%s %s> (anchor %s absent)" % (owner, mcp, anchor)
        hits = [(n, s) for n, s in cands if flags.get((n, od)) == want_static]
        assert len(hits) == 1, "E_SRG_DERIVE:ambiguous <%s %s> %s" % (owner, mcp, hits)
        assert hits[0][1] == srg, \
            "E_SRG_DERIVE:srg mismatch <%s %s> got %s want %s" % (owner, mcp, hits[0][1], srg)
        lines.append("MD: %s/%s %s %s/%s %s" % (owner, hits[0][1], desc, owner, mcp, desc))
    else:
        tm = [m for m in members if len(m) == 2 and m[1] == srg]
        assert len(tm) == 1, "E_SRG_DERIVE:no tsrg field <%s %s>" % (owner, srg)
        ftype_obf = obf_ftype(desc)
        assert flags.get((tm[0][0], "F:" + ftype_obf)) == want_static, \
            "E_SRG_DERIVE:javap mismatch field <%s %s>" % (owner, srg)
        lines.append("FD: %s/%s %s/%s" % (owner, tm[0][1], owner, mcp))
assert len(lines) == 19, "E_SRG_DERIVE:want 19 lines, got %d" % len(lines)
open(outpath, "w").write("\n".join(lines) + "\n")
print("ok e3-live : narrow SRG derived (%d lines)" % len(lines))
EOF
SRG_NARROW="$E3_DIR/srg-narrow.srg"
# 2b. Pin every derived line: a derivation the SRG does not confirm is a loud
#     failure, never a silent default.
pin_method() {
  grep -q "^MD: [^ ]* [^ ]* $1 $2\$" "$SRG_NARROW" \
    || { echo "FAIL e3-live : stub member unpinned <$1 $2>"; exit 1; }
}
pin_field() {
  grep -q "^FD: [^ ]* $1\$" "$SRG_NARROW" \
    || { echo "FAIL e3-live : stub field unpinned <$1>"; exit 1; }
}
pin_method "net/minecraft/block/Block/getDefaultState" "()Lnet/minecraft/block/BlockState;"
pin_method "net/minecraft/block/Block/getStateId" "(Lnet/minecraft/block/BlockState;)I"
pin_method "net/minecraft/block/AbstractBlock\$Properties/create" "(Lnet/minecraft/block/material/Material;)Lnet/minecraft/block/AbstractBlock\$Properties;"
pin_method "net/minecraft/block/AbstractBlock\$Properties/hardnessAndResistance" "(F)Lnet/minecraft/block/AbstractBlock\$Properties;"
pin_method "net/minecraft/world/World/setBlockState" "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"
pin_method "net/minecraft/world/World/getDimensionKey" "()Lnet/minecraft/util/RegistryKey;"
pin_field "net/minecraft/world/World/OVERWORLD"
pin_field "net/minecraft/block/material/Material/ROCK"
pin_field "net/minecraft/entity/Entity/world"
pin_method "net/minecraft/entity/Entity/getPosX" "()D"
pin_method "net/minecraft/entity/Entity/getPosY" "()D"
pin_method "net/minecraft/entity/Entity/getPosZ" "()D"
pin_field "net/minecraft/world/World/isRemote"
pin_method "net/minecraft/world/server/ServerWorld/addEntity" "(Lnet/minecraft/entity/Entity;)Z"
pin_field "net/minecraft/item/Items/DIAMOND"
pin_method "net/minecraft/util/math/vector/Vector3i/getX" "()I"
pin_method "net/minecraft/util/math/vector/Vector3i/getY" "()I"
pin_method "net/minecraft/util/math/vector/Vector3i/getZ" "()I"
pin_method "net/minecraft/block/AbstractBlock\$AbstractBlockState/getBlock" "()Lnet/minecraft/block/Block;"
[ "$(grep -c . "$SRG_NARROW")" = "19" ] \
  || { echo "FAIL e3-live : narrow map drift (want 19 lines)"; exit 1; }
echo "ok e3-live : stubs pinned to derived SRG"

# 2c. Pin every stubbed Forge member against the provisioned jars. Forge
#     classes are never obfuscated, so names are final — presence is the
#     pin. (Vanilla-typed Forge members reference notch classes in the
#     universal, which is why forge/ compiles against stubs, not it.)
#     FML (LogicalSide, Mod) ships inside the 36.2.42 universal; eventbus
#     (Event, IEventBus, SubscribeEvent) ships its own 4.0.0 jar.
pin_uni() {
  "$J8/javap" -p -cp "$UNI" "$1" 2>/dev/null | grep -q "$2" \
    || { echo "FAIL e3-live : universal pin unmet <$1 :: $2>"; exit 1; }
}
pin_uni 'net.minecraftforge.common.MinecraftForge' 'EVENT_BUS'
pin_uni 'net.minecraftforge.event.TickEvent' 'side'
pin_uni 'net.minecraftforge.event.TickEvent' 'phase'
pin_uni 'net.minecraftforge.event.TickEvent$Phase' 'END'
pin_uni 'net.minecraftforge.event.TickEvent$WorldTickEvent' 'world'
pin_uni 'net.minecraftforge.fml.LogicalSide' 'SERVER'
pin_uni 'net.minecraftforge.fml.common.Mod' 'value('
pin_uni 'net.minecraftforge.registries.ForgeRegistries' 'BLOCKS'
pin_uni 'net.minecraftforge.registries.IForgeRegistry' 'getValue('
pin_uni 'net.minecraftforge.registries.IForgeRegistry' 'containsKey('
pin_uni 'net.minecraftforge.registries.DeferredRegister' 'create('
pin_uni 'net.minecraftforge.registries.DeferredRegister' 'register('
pin_uni 'net.minecraftforge.fml.RegistryObject' 'get('
pin_uni 'net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext' 'get()'
pin_uni 'net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext' 'getModEventBus('
pin_uni 'net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent' 'FMLCommonSetupEvent'
pin_uni 'net.minecraftforge.event.world.BlockEvent' 'getWorld('
pin_uni 'net.minecraftforge.event.world.BlockEvent' 'getPos('
pin_uni 'net.minecraftforge.event.world.BlockEvent' 'getState('
pin_uni 'net.minecraftforge.event.world.BlockEvent$BreakEvent' 'BreakEvent('
pin_uni 'net.minecraftforge.event.entity.living.LivingEvent' 'getEntityLiving('
pin_uni 'net.minecraftforge.event.entity.living.LivingDropsEvent' 'LivingDropsEvent('
# Erased descriptor lock: the real getValue erases V to
# IForgeRegistryEntry, not Object — an unbounded stub would compile and
# die live with NoSuchMethodError (found live in E3). Refuse the drift
# here, at pin time, never at boot.
"$J8/javap" -p -s -cp "$UNI" net.minecraftforge.registries.IForgeRegistry 2>/dev/null \
  | grep -q "(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraftforge/registries/IForgeRegistryEntry;" \
  || { echo "FAIL e3-live : IForgeRegistry.getValue erased descriptor drift"; exit 1; }
pin_eb() {
  "$J8/javap" -p -cp "$EVENTBUS" "$1" 2>/dev/null | grep -q "$2" \
    || { echo "FAIL e3-live : eventbus pin unmet <$1 :: $2>"; exit 1; }
}
pin_eb 'net.minecraftforge.eventbus.api.Event' 'Event'
pin_eb 'net.minecraftforge.eventbus.api.IEventBus' 'register('
pin_eb 'net.minecraftforge.eventbus.api.IEventBus' 'addListener('
pin_eb 'net.minecraftforge.eventbus.api.SubscribeEvent' 'SubscribeEvent'
echo "ok e3-live : forge stubs pinned to provisioned jars"

# 3. Build all mod jars with Java 8. forge/ compiles against the pinned
#    stubs (vanilla shape + Forge shape); the live run is the semantic arbiter.
#    R2: jar entries are sorted with timestamps clamped to EPOCH (same
#    commit + same toolchain == same bytes, see normjar), manifests carry
#    VERSION, the bridge jar embeds mods.toml.
#    These are the exact bytes the live run proves AND the release ships.
#    Bridge-owned pure (java/src: loot store/seal, operator policy) compiles
#    beside the seam and stages into the forge classes (same shape as
#    1122/1710: java/ ships inside the bridge jar, never standalone).
BLD="$E3_DIR/build"
rm -rf "$BLD" \
  || { echo "FAIL e3-live : cannot clear <$BLD> (root-owned docker leftovers? point E3_DIR at a user-owned dir)"; exit 1; }
mkdir -p "$BLD/spi" "$BLD/ex1" "$BLD/mini" "$BLD/forge" "$BLD/jars" "$BLD/modstoml/META-INF" "$BLD/bridge"
"$J8/javac" -source 8 -target 8 -nowarn -d "$BLD/spi" $(find ../spi/java/src -name '*.java')
"$J8/javac" -source 8 -target 8 -nowarn -cp "$BLD/spi" -d "$BLD/ex1" $(find ../example1/java/src -name '*.java')
"$J8/javac" -source 8 -target 8 -nowarn -cp "$BLD/spi" -d "$BLD/mini" $(find ../minimap/java/src -name '*.java')
"$J8/javac" -source 8 -target 8 -nowarn -cp "$BLD/spi:$BLD/ex1" -d "$BLD/bridge" $(find java/src -name '*.java')
"$J8/javac" -source 8 -target 8 -nowarn -cp "$BLD/spi:$BLD/ex1:$BLD/bridge" -d "$BLD/forge" $(find tools/live/stub forge/src -name '*.java')
# Bridge-owned pure stages into the forge classes (ships in the bridge jar).
cp -r "$BLD/bridge/"* "$BLD/forge/"
sed "s/@VERSION@/$VERSION/g" forge/src/META-INF/mods.toml > "$BLD/modstoml/META-INF/mods.toml"
grep -q "version=\"$VERSION\"" "$BLD/modstoml/META-INF/mods.toml" \
  || { echo "FAIL e3-live : mods.toml stamp lost (want version $VERSION)"; exit 1; }
EPOCH="${SOURCE_DATE_EPOCH:-$(git log -1 --format=%ct)}"
printf 'Manifest-Version: 1.0\nImplementation-Version: %s\n' "$VERSION" > "$BLD/MANIFEST.MF"
find "$BLD/spi" "$BLD/ex1" "$BLD/mini" "$BLD/forge" "$BLD/modstoml" "$BLD/MANIFEST.MF" -exec touch -h -d "@$EPOCH" {} +
# mkjar: sorted entries, pinned mtimes, VERSION manifest. File lists stay
# explicit because jar -C . walks in readdir order (not reproducible).
# normjar then clamps every zip entry timestamp: the JDK 8 jar tool stamps
# META-INF entries with the wall clock (verified by diff), and Reobf does
# the same for its output. python3 is already a hard dependency (anvil).
# Scope: same commit + same toolchain == same bytes (zlib/JDK may vary
# across machines; use tools/live/Dockerfile to pin the toolchain).
normjar() {
  python3 - "$1" "$EPOCH" <<'EOF'
import sys, zipfile, datetime
path, epoch = sys.argv[1], int(sys.argv[2])
dt = datetime.datetime.utcfromtimestamp(epoch).timetuple()[:6]
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
  # Controlled tree, no spaces in class paths: word-splitting is intended.
  (cd "$stage" && "$J8/jar" cfm "$out" "$BLD/MANIFEST.MF" $files)
  normjar "$out"
}
mkjar "$BLD/jars/matou-spi.jar" "$BLD/spi"
mkjar "$BLD/jars/matou-example1.jar" "$BLD/ex1"
mkjar "$BLD/jars/matou-minimap.jar" "$BLD/mini"
rm -rf "$BLD/bridgemod" && mkdir -p "$BLD/bridgemod"
cp -r "$BLD/forge/"* "$BLD/bridgemod/"
# Stubs are compile-only: they must never ship (a fake Block on the
# runtime classpath would shadow vanilla). Refuse loudly if leaked.
# (mods.toml ships from forge/src, not the stub tree, so it survives this.)
rm -rf "$BLD/bridgemod/net" "$BLD/bridgemod/META-INF"
if [ -e "$BLD/bridgemod/net" ]; then
  echo "FAIL e3-live : stub leak into mod jar"
  exit 1
fi
# ModLauncher isolates every mods/ jar (same family as D3's 47.2.0
# finding): a slim bridge jar cannot see matou-spi.jar next to it
# (NoClassDefFoundError). The bridge ships FAT — spi + example1 classes
# embedded, same sources, same bytes provenance. The slim jars above stay
# dev/library artifacts (and the java-52 contract still checks them).
cp -r "$BLD/spi/"* "$BLD/ex1/"* "$BLD/bridgemod/"
mkdir -p "$BLD/bridgemod/META-INF"
cp "$BLD/modstoml/META-INF/mods.toml" "$BLD/bridgemod/META-INF/mods.toml"
touch -h -d "@$EPOCH" "$BLD/bridgemod/META-INF/mods.toml"
mkjar "$BLD/jars/matoubridge.jar" "$BLD/bridgemod"
echo "ok e3-live : jars built (VERSION=$VERSION)"

# 4. Reobfuscate MCP-named refs to SRG (ForgeGradle reobf equivalent:
#    runtime vanilla only declares SRG names, so un-reobfed jars die with
#    NoSuchMethodError — found live in B3, never again silently).
"$J8/javac" -nowarn -cp "$ASM:$ASM_COMMONS" -d "$BLD" tools/live/Reobf.java
"$J8/java" -cp "$BLD:$ASM:$ASM_COMMONS" Reobf "$SRG_NARROW" "$BLD/jars/matoubridge.jar" "$BLD/jars/matoubridge-reobf.jar"
normjar "$BLD/jars/matoubridge-reobf.jar"
echo "ok e3-live : bridge reobfuscated"

# Annotation visibility: eventbus discovers handlers through
# RuntimeVisibleAnnotations. A stub whose retention drifts from the real
# annotation (RUNTIME) would emit invisible usages and register nothing,
# silently — found live in D3. Refuse any invisible annotation in our
# classes and demand the two visible ones on the mod class.
# (javap -v on Java 8 prints annotation types as constant-pool refs
# `#135()`, not resolved names like newer javap — so resolve through
# the pool in python instead of grepping the marker line.)
for c in fr.iamacat.bridge.forge.MatouBridgeMod fr.iamacat.bridge.forge.PackWire fr.iamacat.bridge.forge.WorldCellSink; do
  "$J8/javap" -v -cp "$BLD/jars/matoubridge-reobf.jar" "$c" > "$BLD/annot.txt" 2>/dev/null \
    || { echo "FAIL e3-live : javap on reobf <$c>"; exit 1; }
  grep -q "RuntimeInvisibleAnnotations" "$BLD/annot.txt" \
    && { echo "FAIL e3-live : invisible annotation in <$c> (stub retention drift)"; exit 1; }
done
"$J8/javap" -v -cp "$BLD/jars/matoubridge-reobf.jar" fr.iamacat.bridge.forge.MatouBridgeMod > "$BLD/annot.txt" 2>/dev/null
python3 - "$BLD/annot.txt" <<'EOF'
import re, sys
text = open(sys.argv[1]).read()
pool = dict(re.findall(r"#(\d+) = Utf8\s+(\S+)", text))
seen = set()
for m in re.finditer(r"RuntimeVisibleAnnotations:\s*\n((?:\s+\d+: #\d+[^\n]*\n)+)", text):
    for ref in re.findall(r"#(\d+)", m.group(1)):
        if ref in pool:
            seen.add(pool[ref])
for want in ("Lnet/minecraftforge/eventbus/api/SubscribeEvent;",
             "Lnet/minecraftforge/fml/common/Mod;"):
    if want not in seen:
        print("FAIL e3-live : annotation <%s> not visible in <MatouBridgeMod>" % want)
        sys.exit(1)
print("ok e3-live : annotations visible")
EOF

# Dual-runtime contract (Java 8 vanilla + modern JVM): shipped bytes stay
# major 52 with no module-info and no multi-release entries — v52 loads on
# 8 and newer alike. Anything newer fails loudly here, on both the live
# and the release path, never silently.
python3 - "$BLD/jars" <<'EOF'
import sys, zipfile, struct
jars = ["matou-spi.jar", "matou-example1.jar", "matou-minimap.jar",
        "matoubridge-reobf.jar"]
bad = []
for j in jars:
    zf = zipfile.ZipFile("%s/%s" % (sys.argv[1], j))
    for n in zf.namelist():
        if n == "module-info.class" or n.startswith("META-INF/versions/"):
            bad.append("%s!%s (multi-release)" % (j, n))
        elif n.endswith(".class"):
            major = struct.unpack(">H", zf.read(n)[6:8])[0]
            if major != 52:
                bad.append("%s!%s (major %d, want 52)" % (j, n, major))
if bad:
    print("FAIL e3-live : java-52 contract broken:")
    print("\n".join("  " + b for b in bad))
    sys.exit(1)
print("ok e3-live : java 52 contract (4 jars, no multi-release)")
EOF

# R2 release assembly: versioned server drop, then exit before booting.
# The MCP-named bridge jar never ships (only the reobf one is copied).
# The drop's mod is the FAT bridge (spi+example1 embedded, see step 3);
# the slim jars ship alongside as dev/library artifacts.
if [ "${BUILD_ONLY:-}" = "1" ]; then
  rm -rf dist && mkdir -p dist/matou-content
  cp "$BLD/jars/matou-spi.jar" "dist/matou-spi-$VERSION.jar"
  cp "$BLD/jars/matou-example1.jar" "dist/matou-example1-$VERSION.jar"
  cp "$BLD/jars/matou-minimap.jar" "dist/matou-minimap-$VERSION.jar"
  cp "$BLD/jars/matoubridge-reobf.jar" "dist/matoubridge-$VERSION.jar"
  cp ../example1/content/owned.matou ../example1/content/additive.matou ../example1/content/structure.matou ../example1/content/vein.matou dist/matou-content/
  printf '# Copy to <server>/config/matoubridge/packs.cfg and replace <SERVER>.\n# Wire y=63 keeps plane cells on their own slice, off the structure slices (64..65).\n# The wire block is the registered custom ore (DeferredRegister queues example1:my_ore from owned.matou, the fill lands before setup binds resolve it); aliases stay vanilla stone.\n# Vein clusters land on the BASE_Y=60 band (slices 60..61) as the registered ore via the veinblock alias.\nfr.iamacat.example1.ExamplePack 63 example1:my_ore ownedFile=<SERVER>/matou-content/owned.matou scatterFile=<SERVER>/matou-content/additive.matou structureFile=<SERVER>/matou-content/structure.matou block.example1.structures:hut_wall=minecraft:stone block.example1.structures:hut_roof=minecraft:stone veinFile=<SERVER>/matou-content/vein.matou veinblock.example1.content:my_ore=example1:my_ore\n' > dist/packs.cfg.example
  (cd dist && sha256sum "matou-spi-$VERSION.jar" "matou-example1-$VERSION.jar" "matou-minimap-$VERSION.jar" "matoubridge-$VERSION.jar" matou-content/owned.matou matou-content/additive.matou matou-content/structure.matou matou-content/vein.matou packs.cfg.example > SHA256SUMS.txt)
  (cd dist && sha256sum -c SHA256SUMS.txt)
  echo "ok r2-release : dist/ assembled (VERSION=$VERSION)"
  exit 0
fi

# 5. Deploy the mod + content + packs.cfg, boot the server. Only the FAT
# bridge deploys (module isolation, see step 3) — slim jars never boot.
mkdir -p "$SERV/mods" "$SERV/config/matoubridge"
rm -f "$SERV/mods/"*.jar
cp "$BLD/jars/matoubridge-reobf.jar" "$SERV/mods/matoubridge.jar"
rm -rf "$SERV/matou-content" && cp -r ../example1/content "$SERV/matou-content"
# Wire y=63: plane cells stay on their own slice, off the structure
# slices (64..65), so the verdict stays per-shape sensitive despite the
# set collapse (a 2D and a 3D cell can share x,z, never y). The wire
# block is the registered custom ore (the constructor queues it from
# owned.matou, the deferred fill registers it before setup binds
# resolve it). Vein clusters land on their own band (BASE_Y=60, slices
# 60..61) as the registered ore through the veinblock alias.
printf 'fr.iamacat.example1.ExamplePack 63 example1:my_ore ownedFile=%s/matou-content/owned.matou scatterFile=%s/matou-content/additive.matou structureFile=%s/matou-content/structure.matou block.example1.structures:hut_wall=minecraft:stone block.example1.structures:hut_roof=minecraft:stone veinFile=%s/matou-content/vein.matou veinblock.example1.content:my_ore=example1:my_ore\n' "$SERV" "$SERV" "$SERV" "$SERV" > "$SERV/config/matoubridge/packs.cfg"
echo "eula=true" > "$SERV/eula.txt"
printf 'online-mode=false\nlevel-type=FLAT\ngamemode=1\ndifficulty=0\nmotd=E3 live proof\nmax-tick-time=-1\n' > "$SERV/server.properties"
rm -rf "$SERV/world" "$SERV/logs"
set +e
(cd "$SERV" && timeout "$BOOT_SECS" "$J8/java" -Xmx1G -jar "$BOOT_JAR" nogui > boot-e3.log 2>&1)
code=$?
set -e
[ "$code" -eq 124 ] || { echo "FAIL e3-live : server exited early (code $code, see $SERV/boot-e3.log)"; exit 1; }
echo "ok e3-live : server ran ($BOOT_SECS s)"

# 6. Fail loudly on any runtime refusal or linkage error (stdout log plus
#    the rolling server log — Forge splits output across both).
LOGS="$SERV/boot-e3.log"
[ -f "$SERV/logs/latest.log" ] && LOGS="$LOGS $SERV/logs/latest.log"
if grep -a -q "NoSuchMethodError\|NoSuchFieldError\|NoClassDefFoundError\|E_FORGE\|E_BRIDGE\|E_EXAMPLE\|E_REG\|E_LOOT\|Encountered an unexpected exception" $LOGS; then
  echo "FAIL e3-live : runtime refusal (see $SERV/boot-e3.log)"
  grep -a -m5 "NoSuchMethodError\|NoSuchFieldError\|NoClassDefFoundError\|E_FORGE\|E_BRIDGE\|E_EXAMPLE\|E_REG\|E_LOOT\|Caused by" $LOGS
  exit 1
fi
grep -a -q "matoubridge" $LOGS \
  || { echo "FAIL e3-live : mod never loaded"; exit 1; }
echo "ok e3-live : bind clean, ticks clean"
# Registration proof: the setup-time verify line carries the dynamic
# state id (post-flattening names need no numeric table — the anvil
# probe reads namespaced names, and this line proves the custom name
# resolved through the registry, never defaulted).
grep -a -q '\[MatouBridge\] registered <example1:my_ore> id [0-9][0-9]*' $LOGS \
  || { echo "FAIL e3-live : my_ore registration line absent from boot log (deferred fill never registered? see $SERV/boot-e3.log)"; exit 1; }
echo "ok e3-live : my_ore registered ($(grep -a -o '\[MatouBridge\] registered <example1:my_ore> id [0-9][0-9]*' $LOGS | tail -n 1))"

# 7. Positive proof: world blocks in chunks (0..1, -1..1) at y=60..61
#    plus y=63..65 must equal the pure decision union — plane cells
#    carry the registered custom ore, volume cells their alias stone,
#    vein clusters the registered ore, nothing foreign, nothing
#    missing. Plane cells land at the wire y=63, volume cells at their own
#    y=64..65; structure offsets reach x,z=17, and the hut anchor z=-4
#    spills into chunk row -1 (region r.0.-1.mca) — hence the 6-chunk,
#    5-slice read. (Same geometry as C3: the mod writes force chunk
#    generation around the origin regardless of world spawn.)
"$J8/javac" -nowarn -cp "$BLD/spi:$BLD/ex1" -d "$BLD" tools/live/CellUnion.java
"$J8/java" -cp "$BLD:$BLD/spi:$BLD/ex1" CellUnion \
  "$SERV/config/matoubridge/packs.cfg" 4000 "$BLD/union.txt"
: > "$BLD/world.txt"
for spec in "r.0.0.mca 0 0" "r.0.0.mca 1 0" "r.0.0.mca 0 1" \
    "r.0.0.mca 1 1" "r.0.-1.mca 0 -1" "r.0.-1.mca 1 -1"; do
  set -- $spec
  for y in 60 61 63 64 65; do
    python3 tools/live/anvil.py "$SERV/world/region/$1" "$2" "$3" "$y" \
      | awk -v cx="$2" -v cz="$3" -v y="$y" \
        '{split($1, a, ","); print (cx*16+a[1])" "y" "(cz*16+a[2])" "$2}' \
      >> "$BLD/world.txt"
  done
done
python3 - "$BLD/union.txt" "$BLD/world.txt" "$SERV/config/matoubridge/packs.cfg" <<'EOF'
import sys
# Names resolve through packs.cfg itself (wire block plus every
# block.<ref>=<name> alias value) — never hardcoded, never guessed. A
# world name outside that set fails loudly (extend the wire explicitly).
wire_y, wire_block, allowed = None, None, set()
for line in open(sys.argv[3]):
    line = line.strip()
    if line and not line.startswith("#"):
        toks = line.split()
        wire_y, wire_block = int(toks[1]), toks[2]
        allowed.add(wire_block)
        for tok in toks[3:]:
            if tok.startswith("block.") and "=" in tok:
                allowed.add(tok.split("=", 1)[1])
if wire_y is None:
    print("FAIL e3-live : no wire in packs.cfg")
    sys.exit(1)
u = {}
for line in open(sys.argv[1]):
    cell = line.split()[0]
    parts = cell.split(",")
    if len(parts) == 3 and ":" in parts[2]:
        z, bname = parts[2].split(":", 1)
        pos = (int(parts[0]), int(parts[1]), int(z))
    else:
        x, z = cell.split(",")
        pos, bname = (int(x), wire_y, int(z)), wire_block
    if bname not in allowed:
        print("FAIL e3-live : union block <%s> outside packs.cfg set (extend the wire, never guess)" % bname)
        sys.exit(1)
    u[pos] = bname
rows = [l.split() for l in open(sys.argv[2])]
w = {(int(x), int(y), int(z)): n for x, y, z, n in rows}
if not w:
    print("FAIL e3-live : world empty at y=60..61,63..65 (no tick applied?)")
    sys.exit(1)
if set(w.values()) - allowed:
    print("FAIL e3-live : foreign blocks %s" % sorted(set(w.values()) - allowed))
    sys.exit(1)
bad = {p: (w[p], u.get(p)) for p in w if u.get(p) != w[p]}
if bad:
    print("FAIL e3-live : name mismatch at %s (want pure union names)" % sorted(bad.items())[:5])
    sys.exit(1)
if u.keys() - w.keys():
    print("FAIL e3-live : pure cells missing from world (%d)" % len(u.keys() - w.keys()))
    sys.exit(1)
print("ok e3-live : world == pure union (%d cells, names %s)" % (len(w), sorted(set(w.values()))))
EOF
