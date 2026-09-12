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
# Shared harness steps (hub SSOT, thin version wrapper — hub
# decisions/LIVE_SHELL_COMMON.md): sibling-absent fails loud, same shim
# discipline as tools/run-client.sh.
[ -f ../hub/tools/live-common.sh ] \
  || { echo "FAIL e3-live : hub sibling absent (clone hub next to bridge-1165 — live steps source ../hub/tools/live-common.sh)"; exit 1; }
[ -f ../hub/tools/live-derive.sh ] \
  || { echo "FAIL e3-live : hub sibling absent (clone hub next to bridge-1165 — derive steps source ../hub/tools/live-derive.sh)"; exit 1; }
# shellcheck disable=SC1091
. ../hub/tools/live-common.sh
# shellcheck disable=SC1091
. ../hub/tools/live-derive.sh
live_init "e3-live"
# Era-bound adapters: the hub libs own the mechanics; these bind the
# caller-owned map/jars so every pin/jar call site below stays byte-identical.
pin_method() { live_pin_method "$SRG_NARROW" "$@"; }
pin_field() { live_pin_field "$SRG_NARROW" "$@"; }
pin_uni() { live_pin_uni "$J8" "$UNI" "$@"; }
mkjar() { live_mkjar "$1" "$2" "$BLD/MANIFEST.MF" "$J8/jar" "$EPOCH"; }
normjar() { live_normjar "$1" "$EPOCH"; }
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
# 1a. E3_DIR preflight (docker root-owned leftovers fail fast, loudly).
live_preflight_dir "E3_DIR" "$E3_DIR"
live_fetch "$E3_DIR/forge-installer.jar" "$FORGE_URL" "$INSTALLER_SHA1" "${E3_OFFLINE:-0}"
UNI="$SERV/libraries/net/minecraftforge/forge/1.16.5-36.2.42/forge-1.16.5-36.2.42-universal.jar"
if [ ! -f "$UNI" ]; then
  live_install_server "$SERV" "$E3_DIR/forge-installer.jar" "$J8"
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
FORGESPI=$(find "$SERV/libraries/net/minecraftforge/forgespi" -name "forgespi-*.jar" | head -n 1)
[ -n "$FORGESPI" ] || { echo "FAIL e3-live : forgespi missing from server libs"; exit 1; }
live_fetch "$E3_DIR/mcp_config-1.16.5-20210115.111550.zip" "$MCP_CONFIG_URL" "$MCP_CONFIG_SHA1" "${E3_OFFLINE:-0}"
live_fetch "$E3_DIR/mcp_snapshot-20210309-1.16.5.zip" "$MCP_SNAPSHOT_URL" "$MCP_SNAPSHOT_SHA1" "${E3_OFFLINE:-0}"
# Pinned vanilla client (tools/autoplay/client-pin.txt — the AUTOPLAY
# companion derive already trusts it; this script reuses the same bytes,
# never its own pin): client-only vanilla members (net/minecraft/client/*,
# com/mojang/*, e.g. the renderer tranche's Minecraft/getInstance) cannot
# javap-verify against the notch SERVER jar (no client classes in it), so
# the derive below checks those rows against these bytes instead. Same
# offline rule as every other fetch above.
CLIENT_PIN_URL="$(sed -n 's/^URL=//p' tools/autoplay/client-pin.txt)"
CLIENT_PIN_SHA1="$(sed -n 's/^SHA1=//p' tools/autoplay/client-pin.txt)"
[ -n "$CLIENT_PIN_URL" ] && [ -n "$CLIENT_PIN_SHA1" ] \
  || { echo "FAIL e3-live : malformed tools/autoplay/client-pin.txt (want URL= + SHA1=)"; exit 1; }
MCCLIENT="$E3_DIR/vanilla-client.jar"
if [ ! -f "$MCCLIENT" ] || ! echo "$CLIENT_PIN_SHA1  $MCCLIENT" | sha1sum -c - >/dev/null 2>&1; then
  if [ "${E3_OFFLINE:-}" = "1" ]; then
    echo "FAIL e3-live : offline and vanilla client absent ($MCCLIENT)"
    exit 1
  fi
  echo "note e3-live : fetching pinned vanilla client (network once, $CLIENT_PIN_SHA1)"
  rm -f "$MCCLIENT"
  curl -sL -o "$MCCLIENT" "$CLIENT_PIN_URL" \
    || { echo "FAIL e3-live : vanilla client download failed"; exit 1; }
  echo "$CLIENT_PIN_SHA1  $MCCLIENT" | sha1sum -c - >/dev/null 2>&1 \
    || { echo "FAIL e3-live : vanilla client sha1 drift (want $CLIENT_PIN_SHA1, never silent upgrade)"; exit 1; }
fi
echo "ok e3-live : vanilla client pinned ($CLIENT_PIN_SHA1)"
echo "ok e3-live : server provisioned (pins verified)"

# 2. Derive the narrow MCP->SRG map from pinned bytes (no srg-mcp.srg on
#    1.16.5): joined.tsrg gives obf<->SRG per class, the notch jars
#    disambiguate overloads and static-ness via javap (exactly-one assert
#    per member, loud otherwise), and the MCP snapshot locks SRG<->MCP
#    names. Server classes verify against the notch server jar;
#    client-only owners (net/minecraft/client/*, com/mojang/*, plus
#    Matrix4f whose write() ships client-only — measured: zero
#    FloatBuffer members on the server bytes) verify against the pinned
#    vanilla client jar above (1122 split, never defaulted). The map covers every vanilla member our forge/ bytecode
#    references (verified by constant-pool scan at E3 time:
#    getDefaultState, setBlockState, getDimensionKey, OVERWORLD, plus the
#    registration tranche: getStateId, Properties.create,
#    hardnessAndResistance, Material.ROCK, plus the loot tranche:
#    Entity/world/getPosX/getPosY/getPosZ, World/isRemote,
#    ServerWorld/addEntity, Items/DIAMOND, Vector3i/getX/getY/getZ,
#    AbstractBlockState/getBlock, plus the spawn tranche:
#    Entity/getEntityId/removed/setPositionAndRotation,
#    World/getEntitiesWithinAABB,
#    LivingEntity/getAttribute/getMaxHealth/setHealth,
#    ModifiableAttributeInstance/setBaseValue, Attributes/MAX_HEALTH,
#    EntityType/PIG, plus the custom entity tranche (hub
#    decisions/SPAWN.md): EntityType$Builder/create/size/trackingRange/
#    build. EntityClassification/CREATURE needs no row (enum constants
#    ship MCP-named in joined.tsrg — runtime name identical, passthrough
#    by construction like Forge classes); Forge refs (ENTITIES,
#    RenderingRegistry, DistExecutor) pass the server Reobf untouched
#    (unmapped refs pass through — same split as the 1122 custom entity
#    tranche, client link measured at live time).
#    Plus the renderer tranche (hub decisions/MATOU_MODEL.md +
#    GL_INSTANCING_ADAPTER.md): Entity/prevPosX/prevPosY/prevPosZ/
#    rotationYaw/rotationPitch, Minecraft/getInstance/world/
#    getRenderViewEntity, ClientWorld/getAllEntities,
#    MatrixStack/getLast, MatrixStack$Entry/getMatrix, Matrix4f/write —
#    client-only rows verified against the pinned vanilla client jar
#    above, every row (and only it) pinned below.
#    The snapshot lock is load-bearing, not documentary: World carries
#    three same-type static RegistryKey fields (OVERWORLD, THE_NETHER,
#    THE_END), so descriptor + static-ness alone cannot pick OVERWORLD —
#    the field resolves snapshot-first (SRG name), then tsrg + javap
#    confirm (obf owner, static, RegistryKey type).
# Mechanics live in hub/tools/live-derive.sh (era 1.16), rows in
# tools/live/want.tsv — same 59 lines, byte-identical output.
SRG_NARROW="$E3_DIR/srg-narrow.srg"
live_derive_mcp_snapshot "$E3_DIR/mcp_config-1.16.5-20210115.111550.zip" "$E3_DIR/mcp_snapshot-20210309-1.16.5.zip" "$MCSERV" "$J8/javap" "$SRG_NARROW" "$MCCLIENT" "tools/live/want.tsv"
# 2b. Pin every derived line: a derivation the SRG does not confirm is a loud
#     failure, never a silent default.
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
pin_method "net/minecraft/entity/Entity/getEntityId" "()I"
pin_field "net/minecraft/entity/Entity/removed"
pin_method "net/minecraft/entity/Entity/setPositionAndRotation" "(DDDFF)V"
pin_method "net/minecraft/world/World/getEntitiesWithinAABB" "(Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/function/Predicate;)Ljava/util/List;"
pin_method "net/minecraft/entity/LivingEntity/getAttribute" "(Lnet/minecraft/entity/ai/attributes/Attribute;)Lnet/minecraft/entity/ai/attributes/ModifiableAttributeInstance;"
pin_method "net/minecraft/entity/LivingEntity/getMaxHealth" "()F"
pin_method "net/minecraft/entity/LivingEntity/setHealth" "(F)V"
pin_method "net/minecraft/entity/ai/attributes/ModifiableAttributeInstance/setBaseValue" "(D)V"
pin_field "net/minecraft/entity/ai/attributes/Attributes/MAX_HEALTH"
pin_field "net/minecraft/entity/EntityType/PIG"
pin_method "net/minecraft/entity/EntityType\$Builder/create" "(Lnet/minecraft/entity/EntityType\$IFactory;Lnet/minecraft/entity/EntityClassification;)Lnet/minecraft/entity/EntityType\$Builder;"
pin_method "net/minecraft/entity/EntityType\$Builder/size" "(FF)Lnet/minecraft/entity/EntityType\$Builder;"
pin_method "net/minecraft/entity/EntityType\$Builder/trackingRange" "(I)Lnet/minecraft/entity/EntityType\$Builder;"
pin_method "net/minecraft/entity/EntityType\$Builder/build" "(Ljava/lang/String;)Lnet/minecraft/entity/EntityType;"
pin_method "net/minecraft/entity/ai/attributes/AttributeModifierMap\$MutableAttribute/create" "()Lnet/minecraft/entity/ai/attributes/AttributeModifierMap;"
pin_method "net/minecraft/item/Item\$Properties/maxStackSize" "(I)Lnet/minecraft/item/Item\$Properties;"
pin_method "net/minecraft/item/Item/getIdFromItem" "(Lnet/minecraft/item/Item;)I"
# Renderer tranche (hub decisions/MATOU_MODEL.md + GL_INSTANCING_ADAPTER.md):
# every net/minecraft/* + com/mojang/* member the client-only
# InstancedMeshRenderer touches. Anchors are snapshot+tsrg+javap-derived
# above (same measure discipline — never recalled): getInstance is the
# static func_71410_x, world is the ClientWorld-typed field_71441_e,
# getRenderViewEntity is func_175606_aa (a method here, Entity-typed),
# iteration rides ClientWorld.getAllEntities (func_217416_b), the
# projection rides the event Matrix4f.write, the camera view is rebuilt
# bridge-side from the interpolated eye plus the already-pinned
# rotationYaw/rotationPitch (the event stack top is a leftover rotation,
# never the camera — measured live 2026-09-12 with an offline replay —
# so it feeds nothing; its getLast/getMatrix rows below stay pinned but
# unreferenced until the next row rebalance, the hub derive asserts 59
# lines), interpolation rides the prevPos + rotation fields.
pin_field "net/minecraft/entity/Entity/prevPosX"
pin_field "net/minecraft/entity/Entity/prevPosY"
pin_field "net/minecraft/entity/Entity/prevPosZ"
pin_field "net/minecraft/entity/Entity/rotationYaw"
pin_field "net/minecraft/entity/Entity/rotationPitch"
pin_method "net/minecraft/client/Minecraft/getInstance" "()Lnet/minecraft/client/Minecraft;"
pin_field "net/minecraft/client/Minecraft/world"
pin_method "net/minecraft/client/Minecraft/getRenderViewEntity" "()Lnet/minecraft/entity/Entity;"
pin_method "net/minecraft/client/world/ClientWorld/getAllEntities" "()Ljava/lang/Iterable;"
pin_method "com/mojang/blaze3d/matrix/MatrixStack/getLast" "()Lcom/mojang/blaze3d/matrix/MatrixStack\$Entry;"
pin_method "com/mojang/blaze3d/matrix/MatrixStack\$Entry/getMatrix" "()Lnet/minecraft/util/math/vector/Matrix4f;"
pin_method "net/minecraft/util/math/vector/Matrix4f/write" "(Ljava/nio/FloatBuffer;)V"
# Combat tranche (hub decisions/VIRTUAL_HITBOXES.md, server weakspot
# hook): Entity/getLookVec + getEyeHeight (the attacker eye/look
# surface, owner Entity — 1.16.5 keeps no posX fields, so the origin
# rides the already-pinned getPosX/Y/Z), DamageSource/getTrueSource
# (the true attacker behind the hurt source) and Vector3d/x/y/z (the
# look components on the 1.16 vector package — the 1.12 Vec3d owner
# does not port). The narrow map grows 48 -> 54 lines.
#    The second-beast tranche (hub decisions/VIRTUAL_HITBOXES.md,
#    per-mob NBT identity) adds 5 rows: PigEntity/writeAdditional +
#    readAdditional (the persist helpers, owner PigEntity — the public
#    writeWithoutTypeId lives one level up on Entity and its super call
#    would emit an unmappable intermediate owner, so the beast overrides
#    the Pig-declared helpers func_213281_b/func_70037_a instead, public
#    on the notch bytes) and CompoundNBT/contains + getString +
#    putString (the string-tag surface, 1.16 names — the 1.12
#    hasKey/setString names do not port) — the narrow map grows
#    54 -> 59 lines.
pin_method "net/minecraft/entity/Entity/getLookVec" "()Lnet/minecraft/util/math/vector/Vector3d;"
pin_method "net/minecraft/entity/Entity/getEyeHeight" "()F"
pin_method "net/minecraft/util/DamageSource/getTrueSource" "()Lnet/minecraft/entity/Entity;"
pin_field "net/minecraft/util/math/vector/Vector3d/x"
pin_field "net/minecraft/util/math/vector/Vector3d/y"
pin_field "net/minecraft/util/math/vector/Vector3d/z"
pin_method "net/minecraft/entity/passive/PigEntity/writeAdditional" "(Lnet/minecraft/nbt/CompoundNBT;)V"
pin_method "net/minecraft/entity/passive/PigEntity/readAdditional" "(Lnet/minecraft/nbt/CompoundNBT;)V"
pin_method "net/minecraft/nbt/CompoundNBT/contains" "(Ljava/lang/String;)Z"
pin_method "net/minecraft/nbt/CompoundNBT/getString" "(Ljava/lang/String;)Ljava/lang/String;"
pin_method "net/minecraft/nbt/CompoundNBT/putString" "(Ljava/lang/String;Ljava/lang/String;)V"
[ "$(grep -c . "$SRG_NARROW")" = "59" ] \
  || { echo "FAIL e3-live : narrow map drift (want 59 lines)"; exit 1; }
echo "ok e3-live : stubs pinned to derived SRG"

# 2c. Pin every stubbed Forge member against the provisioned jars. Forge
#     classes are never obfuscated, so names are final — presence is the
#     pin. (Vanilla-typed Forge members reference notch classes in the
#     universal, which is why forge/ compiles against stubs, not it.)
#     FML (LogicalSide, Mod) ships inside the 36.2.42 universal; eventbus
#     (Event, IEventBus, SubscribeEvent) ships its own 4.0.0 jar.
pin_uni 'net.minecraftforge.common.MinecraftForge' 'EVENT_BUS'
pin_uni 'net.minecraftforge.event.TickEvent' 'side'
pin_uni 'net.minecraftforge.event.TickEvent' 'phase'
pin_uni 'net.minecraftforge.event.TickEvent$Phase' 'END'
pin_uni 'net.minecraftforge.event.TickEvent$WorldTickEvent' 'world'
pin_uni 'net.minecraftforge.fml.LogicalSide' 'SERVER'
pin_uni 'net.minecraftforge.fml.common.Mod' 'value('
pin_uni 'net.minecraftforge.registries.ForgeRegistries' 'BLOCKS'
pin_uni 'net.minecraftforge.registries.ForgeRegistries' 'ITEMS'
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
pin_uni 'net.minecraftforge.event.entity.living.LivingHurtEvent' 'LivingHurtEvent('
pin_uni 'net.minecraftforge.event.entity.living.LivingHurtEvent' 'getSource('
pin_uni 'net.minecraftforge.event.entity.living.LivingHurtEvent' 'getAmount('
pin_uni 'net.minecraftforge.event.entity.living.LivingHurtEvent' 'setAmount('
pin_uni 'net.minecraftforge.event.entity.EntityJoinWorldEvent' 'EntityJoinWorldEvent('
pin_uni 'net.minecraftforge.event.entity.EntityJoinWorldEvent' 'getWorld('
pin_uni 'net.minecraftforge.event.entity.EntityAttributeCreationEvent' 'put('
pin_uni 'net.minecraftforge.event.entity.EntityEvent' 'getEntity('
# Custom entity tranche (hub decisions/SPAWN.md): the generic beast
# queues through DeferredRegister on ForgeRegistries.ENTITIES (same
# create/register calls the block tranche already pins), the setup-time
# tripwire reads it back, the client-only renderer rides the
# IRenderFactory path (the single (EntityRendererManager) pig ctor,
# measured from the pinned notch client jar), and DistExecutor keeps the
# mapping off dedicated servers. Forge names are runtime-final:
# presence is the pin. Dist/OnlyIn ship the forgespi jar, not the
# universal (measured — same split as eventbus below, no sha1 pin,
# presence only).
pin_uni 'net.minecraftforge.registries.ForgeRegistries' 'ENTITIES'
pin_uni 'net.minecraftforge.fml.DistExecutor' 'runWhenOn('
pin_uni 'net.minecraftforge.fml.client.registry.RenderingRegistry' 'registerEntityRenderingHandler('
pin_uni 'net.minecraftforge.fml.client.registry.IRenderFactory' 'createRenderFor('
# Renderer tranche (hub decisions/GL_INSTANCING_ADAPTER.md): the client
# frame event the instanced overlay subscribes to (Forge-added, never
# obfuscated — presence is the pin, same as every row above; 1.16.5
# carries the MatrixStack whose top the renderer uploads, measured via
# javap on this same universal).
pin_uni 'net.minecraftforge.client.event.RenderWorldLastEvent' 'getPartialTicks('
pin_uni 'net.minecraftforge.client.event.RenderWorldLastEvent' 'getMatrixStack('
pin_uni 'net.minecraftforge.client.event.RenderWorldLastEvent' 'getProjectionMatrix('
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
pin_eb 'net.minecraftforge.eventbus.api.Event' 'setCanceled('
pin_eb 'net.minecraftforge.eventbus.api.IEventBus' 'register('
pin_eb 'net.minecraftforge.eventbus.api.IEventBus' 'addListener('
pin_eb 'net.minecraftforge.eventbus.api.IEventBus' 'post('
pin_eb 'net.minecraftforge.eventbus.api.SubscribeEvent' 'SubscribeEvent'
pin_spi() {
  "$J8/javap" -p -cp "$FORGESPI" "$1" 2>/dev/null | grep -q "$2" \
    || { echo "FAIL e3-live : forgespi pin unmet <$1 :: $2>"; exit 1; }
}
pin_spi 'net.minecraftforge.api.distmarker.Dist' 'CLIENT'
pin_spi 'net.minecraftforge.api.distmarker.OnlyIn' 'value('
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
mkjar "$BLD/jars/matou-spi.jar" "$BLD/spi"
mkjar "$BLD/jars/matou-example1.jar" "$BLD/ex1"
mkjar "$BLD/jars/matou-minimap.jar" "$BLD/mini"
rm -rf "$BLD/bridgemod" && mkdir -p "$BLD/bridgemod"
cp -r "$BLD/forge/"* "$BLD/bridgemod/"
# Stubs are compile-only: they must never ship (a fake Block on the
# runtime classpath would shadow vanilla). Refuse loudly if leaked.
# (mods.toml ships from forge/src, not the stub tree, so it survives this.)
# The renderer tranche adds org/lwjgl/*C and com/mojang/* stubs beside the
# net/* ones (same strip as the 1122 visual tranche — a fake GL11C or
# MatrixStack on the runtime classpath would shadow the real classes).
rm -rf "$BLD/bridgemod/net" "$BLD/bridgemod/org" "$BLD/bridgemod/com" "$BLD/bridgemod/META-INF"
if [ -e "$BLD/bridgemod/net" ] || [ -e "$BLD/bridgemod/org" ] || [ -e "$BLD/bridgemod/com" ]; then
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

# 3b. Narrow-map coverage: every net/minecraft/* + com/mojang/* member
#     the built MCP jar references must resolve in the derived map. Reobf
#     passes unmapped names through silently, so an uncovered ref dies
#     linking live (the 1122 visual tranche found the first
#     RenderWorldLastEvent crashing on unmapped getMinecraft — the map
#     covered server refs only, and the step-2 comment claiming full
#     coverage had no check behind it). The walk mirrors Reobf.walk
#     exactly (in-jar superclass chain, fields by name): <init>/<clinit>
#     never rename, Forge/LWJGL owners pass through by design, so neither
#     is asserted. SRG-spelled refs (func_*/field_*) pass through to
#     identical runtime names by construction (MCP names never match that
#     shape), so only MCP-spelled refs are asserted. ALLOW is Forge-added
#     runtime-final (MCP name at runtime).
python3 - "$BLD/jars/matoubridge.jar" "$SRG_NARROW" <<'EOF'
import re, struct, sys, zipfile
jar, mapf = sys.argv[1:3]
methods, fields = set(), set()
for raw in open(mapf):
    t = raw.split()
    if not t:
        continue
    if t[0] == "MD:":
        own, name = t[3].rsplit("/", 1)
        methods.add((own, name, t[4]))
    elif t[0] == "FD:":
        own, name = t[2].rsplit("/", 1)
        fields.add((own, name))
ALLOW = {
    ("net/minecraft/world/WorldProvider", "getDimension"),
    ("net/minecraft/block/Block", "setRegistryName"),
    ("net/minecraft/item/Item", "setRegistryName"),
    # EntityClassification/CREATURE: enum constants ship MCP-named
    # (joined.tsrg carries no row — runtime name identical, passthrough
    # by construction like Forge classes; every server run executes the
    # Builder.create call reading it, so a wrong name would already die
    # linking before the verdict).
    ("net/minecraft/entity/EntityClassification", "CREATURE"),
}
SRG_SPELLED = re.compile(r"^(func_|field_)\d+_")
def u(pool, i):
    return pool[i][1].decode("utf-8")
def parse(data):
    assert data[:4] == b"\xca\xfe\xba\xbe", "E_MAP_COVER:not a class"
    n = struct.unpack(">H", data[8:10])[0]
    pool = [None] * n
    i, p = 1, 10
    while i < n:
        tag = data[p]
        p += 1
        if tag == 1:
            ln = struct.unpack(">H", data[p:p + 2])[0]
            pool[i] = (tag, data[p + 2:p + 2 + ln])
            p += 2 + ln
        elif tag in (7, 8, 16, 19, 20):
            pool[i] = (tag, struct.unpack(">H", data[p:p + 2])[0])
            p += 2
        elif tag in (9, 10, 11, 12, 17, 18):
            pool[i] = (tag, struct.unpack(">H", data[p:p + 2])[0],
                       struct.unpack(">H", data[p + 2:p + 4])[0])
            p += 4
        elif tag == 15:
            p += 3
        elif tag in (3, 4):
            p += 4
        elif tag in (5, 6):
            p += 8
            i += 1
        else:
            raise AssertionError("E_MAP_COVER:bad tag %d" % tag)
        i += 1
    this_idx = struct.unpack(">H", data[p + 2:p + 4])[0]
    super_idx = struct.unpack(">H", data[p + 4:p + 6])[0]
    this_name = u(pool, pool[this_idx][1])
    super_name = u(pool, pool[super_idx][1]) if super_idx else None
    refs = []
    for e in pool[1:]:
        if e is None or e[0] not in (9, 10, 11):
            continue
        owner = u(pool, pool[e[1]][1])
        _, ni, di = pool[e[2]]
        refs.append((owner, u(pool, ni), u(pool, di), e[0] == 9))
    return this_name, super_name, refs
z = zipfile.ZipFile(jar)
supers, allrefs = {}, []
for info in z.infolist():
    if not info.filename.endswith(".class"):
        continue
    this_name, super_name, refs = parse(z.read(info.filename))
    supers[this_name] = super_name
    allrefs.extend(refs)
missing = []
for owner, name, desc, is_field in allrefs:
    if not (owner.startswith("net/minecraft/") or owner.startswith("com/mojang/")):
        continue
    if name in ("<init>", "<clinit>") or SRG_SPELLED.match(name):
        continue
    o, hit = owner, False
    while o is not None:
        if is_field:
            if (o, name) in fields:
                hit = True
                break
        elif (o, name, desc) in methods:
            hit = True
            break
        o = supers.get(o)
    if not hit and (owner, name) not in ALLOW:
        missing.append("%s %s %s %s" % ("FD" if is_field else "MD", owner, name, desc))
assert not missing, "E_MAP_COVER:unmapped vanilla refs:\n%s" % "\n".join(sorted(set(missing)))
print("ok e3-live : narrow map covers forge refs")
EOF
echo "ok e3-live : narrow map covers forge refs"

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
  cp tools/live/my_beast.geo.json dist/my_beast.geo.json
  cp tools/live/my_beast.png dist/my_beast.png
  printf '# Copy to <server>/config/matoubridge/packs.cfg and replace <SERVER>.\n# Wire y=63 keeps plane cells on their own slice, off the structure slices (64..65).\n# The wire block is the registered custom ore (DeferredRegister queues example1:my_ore from owned.matou, the fill lands before setup binds resolve it); aliases stay vanilla stone.\n# Vein clusters land on the BASE_Y=60 band (slices 60..61) as the registered ore via the veinblock alias.\nfr.iamacat.example1.ExamplePack 63 example1:my_ore ownedFile=<SERVER>/matou-content/owned.matou scatterFile=<SERVER>/matou-content/additive.matou structureFile=<SERVER>/matou-content/structure.matou block.example1.structures:hut_wall=minecraft:stone block.example1.structures:hut_roof=minecraft:stone veinFile=<SERVER>/matou-content/vein.matou veinblock.example1.content:my_ore=example1:my_ore\n' > dist/packs.cfg.example
  (cd dist && sha256sum "matou-spi-$VERSION.jar" "matou-example1-$VERSION.jar" "matou-minimap-$VERSION.jar" "matoubridge-$VERSION.jar" matou-content/owned.matou matou-content/additive.matou matou-content/structure.matou matou-content/vein.matou packs.cfg.example my_beast.geo.json my_beast.png > SHA256SUMS.txt)
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
# Beast shape: the shipped Blockbench geometry the renderer bakes and the
# hitboxes derive from (hub decisions/MATOU_MODEL.md). Deployed beside
# packs.cfg, operator-replaceable like it. ROTATED_GEO overlays the
# rotated-content proof asset as my_beast.geo.json (rotation live-proof
# tranche, ported from 1122 — proof-only, never shipped in dist/).
GEO_SRC="tools/live/my_beast.geo.json"
[ -n "${ROTATED_GEO:-}" ] && GEO_SRC="$ROTATED_GEO"
cp "$GEO_SRC" "$SERV/config/matoubridge/my_beast.geo.json"
# Beast texture: the shipped 64x64 skin the V2 renderer samples (hub
# decisions/MATOU_MODEL.md). Deployed beside the geometry,
# operator-replaceable like it.
cp tools/live/my_beast.png "$SERV/config/matoubridge/my_beast.png"
echo "eula=true" > "$SERV/eula.txt"
printf 'online-mode=false\nlevel-type=FLAT\ngamemode=1\ndifficulty=0\nmotd=E3 live proof\nmax-tick-time=-1\n' > "$SERV/server.properties"
rm -rf "$SERV/world" "$SERV/logs"
live_boot "$SERV" "$BOOT_SECS" "boot-e3.log" "$J8/java" -Xmx1G -jar "$BOOT_JAR" nogui

# 6. Fail loudly on any runtime refusal or linkage error (stdout log plus
#    the rolling server log — Forge splits output across both). E_HIT rides
#    it too: the combat hook refuses corrupt attacker state loudly out of
#    SPI (hub decisions/VIRTUAL_HITBOXES.md) — a NaN eye that passed would
#    mean a defaulted multiplier somewhere.
LOGS="$SERV/boot-e3.log"
[ -f "$SERV/logs/latest.log" ] && LOGS="$LOGS $SERV/logs/latest.log"
live_verdict "NoSuchMethodError\|NoSuchFieldError\|NoClassDefFoundError\|E_FORGE\|E_BRIDGE\|E_EXAMPLE\|E_REG\|E_LOOT\|E_SPAWN\|E_MODEL\|E_HIT\|Encountered an unexpected exception" "NoSuchMethodError\|NoSuchFieldError\|NoClassDefFoundError\|E_FORGE\|E_BRIDGE\|E_EXAMPLE\|E_REG\|E_LOOT\|E_SPAWN\|E_MODEL\|E_HIT\|Caused by" $LOGS
# Registration proof: the setup-time verify line carries the dynamic
# state id (post-flattening names need no numeric table — the anvil
# probe reads namespaced names, and this line proves the custom name
# resolved through the registry, never defaulted).
grep -a -q '\[MatouBridge\] registered <example1:my_ore> id [0-9][0-9]*' $LOGS \
  || { echo "FAIL e3-live : my_ore registration line absent from boot log (deferred fill never registered? see $SERV/boot-e3.log)"; exit 1; }
echo "ok e3-live : my_ore registered ($(grep -a -o '\[MatouBridge\] registered <example1:my_ore> id [0-9][0-9]*' $LOGS | tail -n 1))"
grep -a -q '\[MatouBridge\] registered-item <example1:my_gem> id [0-9][0-9]*' $LOGS \
  || { echo "FAIL e3-live : my_gem registration line absent from boot log (deferred fill never registered? see $SERV/boot-e3.log)"; exit 1; }
echo "ok e3-live : my_gem registered ($(grep -a -o '\[MatouBridge\] registered-item <example1:my_gem> id [0-9][0-9]*' $LOGS | tail -n 1))"

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
live_anvil_loop "$SERV" "$BLD"
live_compare_names "$BLD/union.txt" "$BLD/world.txt" "$SERV/config/matoubridge/packs.cfg"
