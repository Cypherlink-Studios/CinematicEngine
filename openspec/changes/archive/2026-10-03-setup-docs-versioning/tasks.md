# Tasks

## 1. Metadata Configuration

- [x] 1.1 Create `docs/metadata.yml` conforming to `RemotePluginMetadataSchema` with active release `1.0.0` (commit `163f160`), Paper platform compatibility, and ProtocolLib dependency
- [x] 1.2 Register `CinematicEngine` in `DarkBladeDev-plugins` portal registry (`src/data/plugins/cinematicengine.yaml`)

## 2. English Documentation Suite (`docs/en/`)

- [x] 2.1 Create `docs/en/index.md` covering overview, key capabilities, modular architecture, and quick tour
- [x] 2.2 Create `docs/en/getting-started.md` covering requirements, installation, ProtocolLib setup, and first scene
- [x] 2.3 Create `docs/en/commands-and-permissions.md` documenting `/cinematic` & `/cine` subcommands, parameters, and permission nodes
- [x] 2.4 Create `docs/en/dsl-scenes.md` detailing the YAML DSL scene structure, duration, tracks, and metadata
- [x] 2.5 Create `docs/en/camera-dynamics.md` detailing Display entity rigs, spectator packets, and LookAt strategies (`fixed`, `static`, `actor`, `forward`)
- [x] 2.6 Create `docs/en/tracks-and-interpolations.md` detailing camera/actor/effect tracks, linear & ease-in-out interpolators, and Catmull-Rom splines
- [x] 2.7 Create `docs/en/actors-and-effects.md` detailing `PlayerActor`, fake entities, NPCs, particle effects, and spatial sounds
- [x] 2.8 Create `docs/en/developer-api.md` detailing `CinematicService`, custom tracks, custom interpolators, and runtime hooks

## 3. Spanish Documentation Suite (`docs/es/`)

- [x] 3.1 Create `docs/es/index.md` mirroring English content with 1:1 filename parity
- [x] 3.2 Create `docs/es/getting-started.md` mirroring English content with 1:1 filename parity
- [x] 3.3 Create `docs/es/commands-and-permissions.md` mirroring English content with 1:1 filename parity
- [x] 3.4 Create `docs/es/dsl-scenes.md` mirroring English content with 1:1 filename parity
- [x] 3.5 Create `docs/es/camera-dynamics.md` mirroring English content with 1:1 filename parity
- [x] 3.6 Create `docs/es/tracks-and-interpolations.md` mirroring English content with 1:1 filename parity
- [x] 3.7 Create `docs/es/actors-and-effects.md` mirroring English content with 1:1 filename parity
- [x] 3.8 Create `docs/es/developer-api.md` mirroring English content with 1:1 filename parity

## 4. Validation and Verification

- [x] 4.1 Validate 1:1 filename parity between `docs/en/` and `docs/es/` and verify Starlight frontmatter in all 16 files
- [x] 4.2 Run validation in `DarkBladeDev-plugins` portal (`pnpm test` and `pnpm check`) to verify schema and docs ingestion
- [x] 4.3 Validate OpenSpec change status and specs with `openspec validate`
