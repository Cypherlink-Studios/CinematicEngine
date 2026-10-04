---
title: Commands and Permissions
description: Comprehensive command reference, subcommands, arguments, and permission nodes for CinematicEngine.
sidebar:
  order: 3
---

# Commands and Permissions

CinematicEngine provides an intuitive, robust command structure managed by `CommandManager`. All subcommands feature smart contextual tab-completion and detailed feedback messages.

---

## ⌨️ Command Syntax

- **Root Command**: `/cinematic`
- **Command Alias**: `/cine`

---

## 📜 Subcommands Reference

### 1. `/cine play <scene> [player]`
Plays the specified cinematic scene. If no player is specified, the command executes for the sender.

- **Arguments**:
  - `<scene>`: The unique identifier of a loaded scene (e.g. `welcome`, `boss_intro`).
  - `[player]` *(Optional)*: Target online player to experience the cinematic.
- **Permission**: `cinematic.play` (Default: `op`)
- **Examples**:
  ```text
  /cine play welcome
  /cine play boss_intro Alex
  ```

---

### 2. `/cine stop [player]`
Immediately stops the cinematic currently playing for the target player (or sender) and restores their original state (gamemode, location, and spectator view).

- **Arguments**:
  - `[player]` *(Optional)*: Target online player.
- **Permission**: `cinematic.stop` (Default: `op`)
- **Example**:
  ```text
  /cine stop
  /cine stop Steve
  ```

---

### 3. `/cine pause [player]`
Freezes the active cinematic playback at its current timeline tick for the target player.

- **Arguments**:
  - `[player]` *(Optional)*: Target online player.
- **Permission**: `cinematic.pause` (Default: `op`)
- **Example**:
  ```text
  /cine pause
  ```

---

### 4. `/cine resume [player]`
Resumes playback of a previously paused cinematic for the target player.

- **Arguments**:
  - `[player]` *(Optional)*: Target online player.
- **Permission**: `cinematic.resume` (Default: `op`)
- **Example**:
  ```text
  /cine resume
  ```

---

### 5. `/cine list`
Displays a list of all currently registered and validated cinematic scenes along with their durations in ticks and metadata titles.

- **Permission**: `cinematic.list` (Default: `true`)
- **Example**:
  ```text
  /cine list
  ```

---

### 6. `/cine reload`
Reloads all scene YAML files from disk and refreshes internal registries without requiring a server reboot. Validates each scene against `SceneDtoValidator` and logs any parsing errors to the console.

- **Permission**: `cinematic.reload` (Default: `op`)
- **Example**:
  ```text
  /cine reload
  ```

---

### 7. `/cine help`
Prints the help menu with available subcommands, argument descriptions, and syntax guides.

- **Permission**: `cinematic.help` (Default: `true`)
- **Example**:
  ```text
  /cine help
  ```

---

## 🔒 Permissions Matrix

| Permission Node | Description | Default | Recommended Group |
| :--- | :--- | :--- | :--- |
| `cinematic.play` | Allows playing a cinematic scene for oneself or other players. | `op` | Admin, Event Coordinator |
| `cinematic.stop` | Allows halting an ongoing cinematic scene. | `op` | Admin, Moderator |
| `cinematic.pause` | Allows pausing an active cinematic scene. | `op` | Admin, Moderator |
| `cinematic.resume` | Allows resuming a paused cinematic scene. | `op` | Admin, Moderator |
| `cinematic.list` | Allows viewing the catalog of registered scenes. | `true` | Everyone |
| `cinematic.reload` | Allows reloading scenes and configuration from disk. | `op` | Admin |
| `cinematic.help` | Allows viewing command help and usage syntax. | `true` | Everyone |
| `cinematic.*` | Grants full administrative access to all commands. | `op` | Superadmin / Server Owner |

---

## 🛡️ LuckPerms Configuration Examples

### Allow Server Staff to Play Cutscenes
```bash
/lp group moderator permission set cinematic.play true
/lp group moderator permission set cinematic.stop true
/lp group moderator permission set cinematic.pause true
/lp group moderator permission set cinematic.resume true
```

### Full Administrative Access
```bash
/lp group admin permission set cinematic.* true
```
