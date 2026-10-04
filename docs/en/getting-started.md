---
title: Getting Started with CinematicEngine
description: Installation requirements, PacketEvents configuration, and creating your first cinematic scene.
sidebar:
  order: 2
---

# Getting Started with CinematicEngine

This guide will walk you through installing **CinematicEngine**, configuring necessary dependencies, and authoring your very first cinematic flythrough in minutes.

---

## 📋 System Requirements

| Requirement | Supported Version | Notes |
| :--- | :--- | :--- |
| **Server Software** | Paper, Purpur | Paper 1.20.4, 1.20.6, or 1.21.x recommended. |
| **Java Runtime** | Java 21+ | Compiled for Java 21 LTS virtual machine. |
| **PacketEvents** | v2.14.0+ | **Required** for packet-only virtual camera mounts, sub-tick motion updates, and actor staging. |
| **Folia** | Unsupported | Uses Bukkit tick scheduler tasks; regional Folia threading is unsupported. |

---

## 📦 Installation Steps

1. **Install PacketEvents**:
   - Download the latest release of **PacketEvents** (v2.14.0 or newer) from its [official repository](https://github.com/retrooper/packetevents).
   - Place `packetevents-spigot.jar` into your server's `plugins/` directory.

2. **Install CinematicEngine**:
   - Download `CinematicEngine.jar` from the official [Releases](https://github.com/Cypherlink-Studios/CinematicEngine/releases).
   - Place `CinematicEngine.jar` into your server's `plugins/` directory.

3. **Start the Server**:
   - Start or restart your server to initialize default directories and configuration files:
     ```bash
     plugins/CinematicEngine/
     ├── config.yml
     └── scenes/
     ```

4. **Verify Installation**:
   - Run the root command in your server console or in-game:
     ```text
     /cine help
     ```
   - If installed correctly, you will see the full list of available CinematicEngine commands.

---

## ⚙️ Camera Mount Modes

CinematicEngine features a **Dual Camera Mount Architecture** configurable in `plugins/CinematicEngine/config.yml`:

```yaml
camera:
  # Default camera mount mode: PACKET_VIRTUAL or SERVER_DISPLAY
  mount-mode: PACKET_VIRTUAL
```

- **`PACKET_VIRTUAL` (Default & Prioritized)**: Mounts the spectator directly to a clientbound virtual camera entity via PacketEvents (`WrapperPlayServerCamera`). No world entities are spawned, chunk state remains unmutated, and camera pans are butter-smooth.
- **`SERVER_DISPLAY`**: Spawns an `ItemDisplay` entity with client-side transform interpolation in the Minecraft world. Ideal for long-range scenes spanning distant chunks.

---

## 🎬 Authoring Your First Scene

Cinematic scenes are defined as YAML files located in the `plugins/CinematicEngine/scenes/` folder.

Create a file named `welcome.yml` inside `plugins/CinematicEngine/scenes/`:

```yaml
id: "welcome"
duration: 160 # 8 seconds (20 ticks = 1 second)
metadata:
  title: "Welcome Flythrough"
  author: "Admin"

tracks:
  - type: "camera"
    id: "flyby_camera"
    data:
      path-mode: "spline"
      look-at:
        mode: "static"
        target: [0.5, 68.0, 0.5] # Focus point (e.g. your town square or monument)
    keyframes:
      - tick: 0
        position: [25.0, 78.0, -25.0]
        fov: 70.0
        interpolation: "ease_in_out"
      - tick: 80
        position: [0.0, 82.0, -35.0]
        fov: 80.0
        interpolation: "ease_in_out"
      - tick: 160
        position: [-25.0, 75.0, -20.0]
        fov: 70.0
        interpolation: "linear"

  - type: "effect"
    id: "scene_soundtrack"
    keyframes:
      - tick: 10
        sound:
          name: "ui.toast.challenge_complete"
          volume: 1.0
          pitch: 1.0
```

---

## 🕹️ Testing and Playing Your Scene

1. **Reload Scenes**:
   To load your newly created scene without restarting the server, execute:
   ```text
   /cine reload
   ```

2. **Verify Loaded Scenes**:
   Check if the engine successfully parsed and validated the scene:
   ```text
   /cine list
   ```
   You should see `welcome` listed along with its duration (160 ticks).

3. **Play the Scene**:
   Play the scene for yourself:
   ```text
   /cine play welcome
   ```
   Or trigger it for a specific online player:
   ```text
   /cine play welcome Steve
   ```

4. **Stop or Pause Early**:
   If you need to halt the cinematic before it finishes:
   ```text
   /cine stop
   ```
   Your original location, gamemode, and flight abilities will be safely restored instantly.

---

## 💡 Troubleshooting Tips

- **No Camera Movement**: Ensure `PacketEvents` (2.14.0+) is enabled and running without errors in your server console.
- **Jittery View**: Verify that the client is running Minecraft 1.20+ with client rendering enabled.
- **YAML Syntax Errors**: If `/cine reload` reports validation errors, check that coordinates are formatted as `[x, y, z]` numeric lists and that keyframe ticks are sequential.
