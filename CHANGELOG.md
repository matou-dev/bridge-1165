# Changelog — bridge-1165

- E1 scaffold: `forge/` for MC 1.16.5 (Forge 36.2.42, modern sink), seam
  from `matou-spi` (see `SPI_PIN`), gate `tools/check.sh` (etages 1+2
  green without MC; live E3 todo).
- E1 wiring: 1.16.5 event-bus port (`EVENT_BUS` constructor register,
  `LogicalSide`, `OVERWORLD` dimension key, `BlockState`,
  `ForgeRegistries` block resolve), 1.12 stubs replaced, etages 1+2
  green.
- E2 content wiring: `ForgeContentCheck` pure E2E (bridge-1122
  pattern), packs from `config/matoubridge/packs.cfg`.
- E3 live wiring: `tools/run-live.sh` ported (36.2.42 pins, narrow
  MCP→SRG derive with snapshot name lock, stub pins incl. eventbus,
  FAT bridge + `mods.toml`, palette anvil probe, world==union verdict).
