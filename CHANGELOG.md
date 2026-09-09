# Changelog — matou-dev/bridge-1165

Notable changes to this repo. The bridge jar is the only loadable Forge
mod and it never ships alone: releases are versioned source + server drops
(`dist/`, reproducible). Store listings stay DRAFT (see hub `NAMES.md`).
Full notes per tag: https://github.com/matou-dev/bridge-1165/releases.

## [Unreleased]

## [1.2.0] - 2026-09-09

Versioned server drop: https://github.com/matou-dev/bridge-1165/releases/tag/v1.2.0

- First drop (no v1.0.0/v1.1.0 tags on this repo; aligns with the
  unified v1.2.0 round over `matou-spi` `3e819a9`).
- E1 scaffold + wiring: `forge/` for MC 1.16.5 (Forge 36.2.42, modern
  sink), 1.16.5 event-bus port (`EVENT_BUS` constructor register,
  `LogicalSide`, `OVERWORLD` dimension key, `BlockState`,
  `ForgeRegistries` block resolve), 1.12 stubs replaced, etages 1+2
  green.
- E2 content wiring: `ForgeContentCheck` pure E2E (bridge-1122
  pattern), packs from `config/matoubridge/packs.cfg`.
- E3 live proof: `tools/run-live.sh` on Forge 36.2.42 (MCP model with
  snapshot name lock, FAT bridge + `mods.toml` with `@VERSION@` stamp,
  `Level.Sections`/`Palette`/`BlockStates` anvil reads), verdict
  world == pure union (1274 cells, stone only — same count as the
  1614/2860/47.2.0 proofs).
- Dev-client helpers as thin wrappers over hub `tools/run-client.sh`
  (1165-proven: generalized staging builds byte-identical jars).
