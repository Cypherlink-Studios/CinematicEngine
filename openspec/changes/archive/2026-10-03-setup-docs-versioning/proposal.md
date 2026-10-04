# Proposal

## Why

CinematicEngine currently lacks a structured documentation directory and metadata definition, preventing integration with the centralized DarkBladeDev documentation portal and hindering server administrators and developers from discovering its features. Modernizing the documentation under the `/plugin-docs-versioning` specification establishes a valid `docs/metadata.yml` and full multilingual guides in Spanish and English with 1:1 filename parity for seamless Astro + Starlight ingestion.

## What Changes

- Create `docs/metadata.yml` conforming to `RemotePluginMetadataSchema`, establishing version `1.0.0` as the active canonical release (commit `163f160`) with platform compatibility, dependencies, and navigation configuration.
- Implement a complete documentation suite across `docs/es/` and `docs/en/` with strict 1:1 kebab-case filename parity and Starlight frontmatter (`title`, `description`, `sidebar.order`).
- Create 8 comprehensive guides in both English and Spanish:
  - `index.md`: Overview, architecture, core concepts, and key capabilities.
  - `getting-started.md`: Installation, server requirements, ProtocolLib integration, and first cinematic walkthrough.
  - `commands-and-permissions.md`: Complete reference for `/cinematic` (`/cine`) subcommands, syntax, and permission nodes.
  - `dsl-scenes.md`: Declarative YAML scene specification, structure, validation rules, and schema definitions.
  - `camera-dynamics.md`: Display entity rig architecture, spectator packet smoothing, and LookAt targeting strategies (`fixed`, `static`, `actor`, `forward`).
  - `tracks-and-interpolations.md`: Camera, actor, and effect tracks, keyframe positioning, linear and ease-in-out interpolators, and Catmull-Rom spline paths.
  - `actors-and-effects.md`: Actor lifecycle (`PlayerActor`, `FakeEntityActor`, `NPCActor`), particle bursts, and spatial audio playback.
  - `developer-api.md`: Programmatic integration via `CinematicService`, custom track factories, interpolator registries, and runtime event hooks.
- Register `CinematicEngine` in the DarkBladeDev documentation portal registry (`src/data/plugins/cinematicengine.yaml`).

## Capabilities

### New Capabilities
- `plugin-documentation`: Specifies portal metadata compliance, localized documentation structure with 1:1 slug parity, and complete feature coverage for CinematicEngine.

### Modified Capabilities
<!-- None -->

## Impact

- Provides full user and developer documentation ready for static generation by `github-docs-loader.ts` in the DarkBladeDev portal.
- Zero breaking changes or behavioral impact on Java runtime modules or compiled binaries.
