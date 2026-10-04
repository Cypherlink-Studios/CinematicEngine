# Design

## Context

CinematicEngine is a modular cinematic sequencing engine for Paper 1.20.6+ and 1.21.x servers running on Java 21. Currently, the repository has no `docs/` folder or `metadata.yml`. To enable automated ingestion by the DarkBladeDev documentation portal (`github-docs-loader.ts` in Astro + Starlight), the project must adhere to the `/plugin-docs-versioning` specification, which requires a validated `docs/metadata.yml`, 1:1 filename parity between `docs/es/` and `docs/en/`, and portal registry integration.

## Goals / Non-Goals

**Goals:**
- Provide a valid `docs/metadata.yml` conforming to `RemotePluginMetadataSchema`.
- Author complete, high-quality documentation in Spanish (`docs/es/`) and English (`docs/en/`) with strict 1:1 kebab-case filename parity.
- Cover all existing engine systems: YAML DSL, Display entity rigs, LookAt targeting, Catmull-Rom splines, ProtocolLib packet smoothing, commands/permissions, and developer API.
- Register `CinematicEngine` in `DarkBladeDev-plugins` portal registry (`src/data/plugins/cinematicengine.yaml`).

**Non-Goals:**
- Modifying Java source code or runtime mechanics.
- Setting up older historical versions with documentation (`1.0.0` is the initial version, so there are no prior releases requiring tarball caching).

## Decisions

### Decision 1: Metadata Schema & Active Version
- **Choice**: Mark `1.0.0` at index 0 of `versions` with `releaseDate: "2026-10-03"` and `reference: "163f160"`.
- **Rationale**: `163f160` is the latest commit SHA on `main`. Following `github-docs-loader.ts`, index 0 represents the active canonical documentation served at `/[lang]/docs/cinematicengine/...`.
- **Alternatives considered**: Using branch `"main"` as reference (rejected because the loader prefers immutable commit SHAs or release tags for reproducibility).

### Decision 2: 1:1 Locale Structure
- **Choice**: Organize files strictly into `docs/en/` and `docs/es/` matching identical kebab-case filenames:
  1. `index.md` (order: 1)
  2. `getting-started.md` (order: 2)
  3. `commands-and-permissions.md` (order: 3)
  4. `dsl-scenes.md` (order: 4)
  5. `camera-dynamics.md` (order: 5)
  6. `tracks-and-interpolations.md` (order: 6)
  7. `actors-and-effects.md` (order: 7)
  8. `developer-api.md` (order: 8)
- **Rationale**: Starlight i18n routing pairs pages across languages using exact relative path matching. Missing files produce sidebar build warnings.

### Decision 3: Portal Registry Registration
- **Choice**: Create `src/data/plugins/cinematicengine.yaml` in the `DarkBladeDev-plugins` portal repository with `id: cinematicengine`, `repo: Cypherlink-Studios/CinematicEngine`, `featured: true`, tags, and fallback metadata.
- **Rationale**: Enables local build and live preview in the centralized documentation portal.

## Risks / Trade-offs

- [Risk]: Starlight frontmatter missing fields causing build failure → *Mitigation*: Ensure all `.md` files contain `title`, `description`, and `sidebar.order`.
- [Risk]: Portal Zod schema validation mismatch → *Mitigation*: Validate against `RemotePluginMetadataSchema` and test portal build with `pnpm test`.
