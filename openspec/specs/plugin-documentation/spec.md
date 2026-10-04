# plugin-documentation Specification

## Purpose

Provides structured, versioned, and localized documentation for CinematicEngine conforming to the DarkBladeDev ecosystem portal specification and Starlight Astro loader rules.

## Requirements

### Requirement: Portal Metadata Configuration
The repository SHALL maintain a `docs/metadata.yml` file conforming to `RemotePluginMetadataSchema` defining plugin identity, platform compatibility, runtime requirements, Folia status, dependencies, documentation locales, and a `versions` collection where index 0 represents the active release `1.0.0` (commit `163f160`).

#### Scenario: Valid metadata ingestion
- **WHEN** the DarkBladeDev portal content loader parses `docs/metadata.yml`
- **THEN** it successfully validates the Zod schema, identifies `1.0.0` as the active canonical release, and extracts version details without errors.

### Requirement: Multilingual Documentation Parity
The documentation SHALL be organized into `docs/es/` and `docs/en/` subdirectories with identical kebab-case filenames across both locales, ensuring strict 1:1 slug parity.

#### Scenario: 1:1 Slug parity across locales
- **WHEN** the content loader or documentation validator inspects localized files
- **THEN** every markdown document in `docs/es/` has a matching document in `docs/en/` with the exact same filename.

### Requirement: Starlight Frontmatter Specification
Every documentation page SHALL define YAML frontmatter specifying `title`, `description`, and `sidebar.order`.

#### Scenario: Frontmatter parsing
- **WHEN** Astro and Starlight compile the documentation pages
- **THEN** all pages are indexed with the appropriate page title, navigation hierarchy, and metadata description without build errors.

### Requirement: Modern Engine Feature Coverage
The documentation suite SHALL provide comprehensive guides covering all features implemented up to version 1.0.0 across eight specific files: `index.md`, `getting-started.md`, `commands-and-permissions.md`, `dsl-scenes.md`, `camera-dynamics.md`, `tracks-and-interpolations.md`, `actors-and-effects.md`, and `developer-api.md`.

#### Scenario: Complete feature coverage
- **WHEN** a server owner or developer reads the documentation suite
- **THEN** they find accurate syntax references for YAML DSL scene creation, Display entity rigs, LookAt targeting modes, Catmull-Rom splines, ProtocolLib spectator safety, `/cine` commands, and the programmatic Java API.
