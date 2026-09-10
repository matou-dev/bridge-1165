# Changelog — matou-dev/bridge-1165

Notable changes to this repo. The bridge jar is the only loadable Forge
mod and it never ships alone: releases are versioned source + server drops
(`dist/`, reproducible). Store listings stay DRAFT (see hub `NAMES.md`).
Full notes per tag: https://github.com/matou-dev/bridge-1165/releases.

## [Unreleased]

- Custom entity E0 (hub `decisions/SPAWN.md`): `MatouEntity` shell
  replaced by the generic beast (`extends PigEntity`, pig shape/AI/sounds
  reused), `Example1Mod` queues it on a `DeferredRegister` over
  `ForgeRegistries.ENTITIES` (short mob name from the single-mob spawn
  table, pig-classification/hitbox/tracking measured on the pinned
  36.2.42 bytes — the 1.7.10/1.12 `EntityRegistry` call does not exist
  here) with a setup-time `ENTITIES.getValue` tripwire plus a
  `registered-entity` log line + client-only vanilla `PigRenderer`
  mapping through `IRenderFactory` (single `(EntityRendererManager)`
  ctor, measured from the pinned notch client jar) behind
  `DistExecutor` + `@OnlyIn`; census/veto/reconcile/kill-hook/landing
  and the companion legs narrowed to the beast; companion loads AFTER
  `matoubridge` in autoplay-mods.toml (lead-measured, unproven on
  36.2.42 until live). Live proof TODO.
- Custom entity live fixes (hub `decisions/SPAWN.md`, both caught loud
  on the first two direct-client runs, never silent): the setup
  tripwire looks up the registry id (`example1:my_beast` from this
  `DeferredRegister`'s own modid, never the SPI mob ref
  `example1.content:my_beast` — the 1.7.10 tripwire is class-keyed and
  never reads the string) ; the beast's attribute map registers on the
  mod-bus `EntityAttributeCreationEvent` (vanilla pig map reused
  wholesale — the vanilla `LivingEntity` ctor NPEs on the first landing
  without one ; pig builder `func_234215_eI_` ships with no MCP name in
  snapshot 20210309 so it stays SRG-direct, passthrough, while the
  finishing `create` rides the narrow map 33->34).

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
