#!/bin/sh
# Client save verifier (NOT a gate): replays the E3 verdict
# (world == pure union, stone only) on a Prism singleplayer save produced
# by the tools/run-client.sh instance. Quit the game first (flush the save).
#
# Union logic is never duplicated: CellUnion stays compiled in the
# run-client build dir and anvil.py is the same file the server verdict
# uses (tools/live/anvil.py). Verdict geometry (chunks 0..1, rows -1..1,
# slices y=63..65) is the E3 contract, see run-live.sh step 7.
#
# Env: CLIENT_DIR (default ${TMPDIR:-/tmp}/matou-e3-client),
#      PRISM_DIR (default ~/.local/share/PrismLauncher).
# Usage: tools/verify-client-save.sh [world-name]  (default: matou)
set -eu
cd "$(dirname "$0")/.."
CLIENT_DIR="${CLIENT_DIR:-${TMPDIR:-/tmp}/matou-e3-client}"
PRISM_DIR="${PRISM_DIR:-$HOME/.local/share/PrismLauncher}"
WORLD="${1:-matou}"
INST="matou-1165-dev"
GDIR="$PRISM_DIR/instances/$INST/minecraft"
PACKS="$GDIR/config/matoubridge/packs.cfg"
SAVE="$GDIR/saves/$WORLD"
BLD="$CLIENT_DIR/build"
[ -f "$PACKS" ] || { echo "FAIL verify-client : packs.cfg absent ($PACKS, run tools/run-client.sh first)"; exit 1; }
[ -d "$SAVE/region" ] || { echo "FAIL verify-client : save absent ($SAVE/region, create world <$WORLD> in-game first)"; exit 1; }
[ -f "$BLD/CellUnion.class" ] || [ -f "$BLD/CellUnion.jar" ] || ls "$BLD"/CellUnion*.class >/dev/null 2>&1 \
  || { echo "FAIL verify-client : CellUnion not compiled ($BLD, run tools/run-client.sh first)"; exit 1; }
command -v python3 >/dev/null || { echo "FAIL verify-client : python3 required (anvil)"; exit 1; }
command -v java >/dev/null || { echo "FAIL verify-client : java required (CellUnion)"; exit 1; }
java -cp "$BLD:$BLD/spi:$BLD/ex1" CellUnion \
  "$PACKS" 4000 "$CLIENT_DIR/union.txt"
: > "$CLIENT_DIR/world.txt"
for spec in "r.0.0.mca 0 0" "r.0.0.mca 1 0" "r.0.0.mca 0 1" \
    "r.0.0.mca 1 1" "r.0.-1.mca 0 -1" "r.0.-1.mca 1 -1"; do
  set -- $spec
  [ -f "$SAVE/region/$1" ] || continue
  for y in 63 64 65; do
    python3 tools/live/anvil.py "$SAVE/region/$1" "$2" "$3" "$y" \
      | awk -v cx="$2" -v cz="$3" -v y="$y" \
        '{split($1, a, ","); print (cx*16+a[1])" "y" "(cz*16+a[2])" "$2}' \
      >> "$CLIENT_DIR/world.txt"
  done
done
python3 - "$CLIENT_DIR/union.txt" "$CLIENT_DIR/world.txt" "$PACKS" <<'EOF'
import sys
wire_y = None
for line in open(sys.argv[3]):
    line = line.strip()
    if line and not line.startswith("#"):
        wire_y = int(line.split()[1])
if wire_y is None:
    print("FAIL verify-client : no wire in packs.cfg")
    sys.exit(1)
u = set()
for line in open(sys.argv[1]):
    cell = line.split()[0]
    if ":" in cell:
        x, rest = cell.split(",", 1)
        y, z = rest.split(",", 1)[0], rest.split(",", 1)[1].split(":")[0]
        u.add((int(x), int(y), int(z)))
    else:
        x, z = cell.split(",")
        u.add((int(x), wire_y, int(z)))
rows = [l.split() for l in open(sys.argv[2])]
w = {(int(x), int(y), int(z)): i for x, y, z, i in rows}
if not w:
    print("FAIL verify-client : world empty at y=63..65 (no tick applied? chunks ungenerated?)")
    sys.exit(1)
if set(w.values()) != {"minecraft:stone"}:
    print("FAIL verify-client : foreign blocks %s" % sorted(set(w.values())))
    sys.exit(1)
if set(w) - u:
    print("FAIL verify-client : world cells outside pure union %s" % sorted(set(w) - u)[:5])
    sys.exit(1)
if u - set(w):
    print("FAIL verify-client : pure cells missing from world (%d of %d)" % (len(u - set(w)), len(u)))
    sys.exit(1)
print("ok verify-client : world == pure union (%d cells, stone only)" % len(w))
EOF
