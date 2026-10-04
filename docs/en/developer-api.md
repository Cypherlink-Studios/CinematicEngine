---
title: Developer API Integration
description: Extending CinematicEngine, using CinematicService, registering custom tracks, interpolators, and programmatically playing cutscenes.
sidebar:
  order: 8
---

# Developer API Integration

CinematicEngine exposes a clean, modular Java API allowing third-party plugins to trigger cinematics, register custom track types, introduce mathematical interpolators, and construct scenes dynamically in code.

---

## 📦 Adding the Dependency

### Gradle (Kotlin DSL)
```kotlin
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    compileOnly("com.darkbladedev:cinematic-core:1.0.0-SNAPSHOT")
    compileOnly("com.darkbladedev:cinematic-runtime:1.0.0-SNAPSHOT")
    compileOnly("com.darkbladedev:plugin-bootstrap:1.0.0-SNAPSHOT")
}
```

### Paper Plugin Descriptor (`paper-plugin.yml` or `plugin.yml`)
```yaml
softdepend:
  - CinematicEngine
  - ProtocolLib
```

---

## 🎮 The `CinematicService` Interface

The primary entry point for managing cutscenes is `CinematicService`:

```java
import com.darkbladedev.cinematic.bootstrap.CinematicService;
import com.darkbladedev.cinematic.bootstrap.CinematicActionResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class QuestCutsceneManager {

    private final CinematicService cinematicService;

    public QuestCutsceneManager() {
        // Retrieve registered Bukkit service provider
        this.cinematicService = Bukkit.getServicesManager().load(CinematicService.class);
    }

    public void triggerBossCutscene(Player player) {
        CinematicActionResult result = cinematicService.play(player, "boss_intro");

        switch (result) {
            case SUCCESS -> player.sendMessage("Cinematic started!");
            case ALREADY_PLAYING -> player.sendMessage("You are already in a cinematic!");
            case SCENE_NOT_FOUND -> player.sendMessage("Scene 'boss_intro' not found!");
            default -> player.sendMessage("Could not start cinematic.");
        }
    }

    public void abortCutscene(Player player) {
        cinematicService.stop(player);
    }
}
```

### `CinematicActionResult` Status Codes

| Result | Meaning |
| :--- | :--- |
| `SUCCESS` | The requested action executed successfully. |
| `ALREADY_PLAYING` | Cannot start playback; player already has an active scene. |
| `NOT_PLAYING` | Cannot stop/pause/resume; player is not currently in a cinematic. |
| `SCENE_NOT_FOUND` | The specified scene identifier does not exist in registry. |
| `INVALID_STATE` | Player state or environment does not permit cinematic playback. |

---

## 🛠️ Registering Custom Track Types

You can register custom track factories using `TrackRegistry` to support gameplay-specific timeline events such as dialogue subtitles, camera filters, or world time manipulation.

```java
import com.darkbladedev.cinematic.dsl.registry.TrackRegistry;
import com.darkbladedev.cinematic.dsl.registry.TrackFactory;
import com.darkbladedev.cinematic.dsl.validator.TrackValidator;
import com.darkbladedev.cinematic.dsl.dto.TrackDTO;
import com.darkbladedev.cinematic.core.model.Track;

public class SubtitleTrackFactory implements TrackFactory {

    @Override
    public Track create(TrackDTO dto) {
        // Parse track data and return custom Track instance
        return new SubtitleTrack(dto.id(), dto.keyframes());
    }
}

// Registration during plugin onLoad() or onEnable():
TrackRegistry trackRegistry = DefaultDslComponents.trackRegistry(interpolatorRegistry);
trackRegistry.register("subtitle", new SubtitleTrackFactory(), new SubtitleTrackValidator());
```

---

## 📐 Registering Custom Interpolators

Register custom animation curves (e.g. bounce, cubic bezier, elastic) via `InterpolatorRegistry`:

```java
import com.darkbladedev.cinematic.dsl.mapper.InterpolatorRegistry;
import com.darkbladedev.cinematic.core.interpolation.Interpolator;

InterpolatorRegistry registry = InterpolatorRegistry.defaultRegistry();

// Register a bounce-out curve
registry.register("bounce_out", new Interpolator() {
    @Override
    public double interpolate(double t) {
        double n1 = 7.5625;
        double d1 = 2.75;
        if (t < 1 / d1) {
            return n1 * t * t;
        } else if (t < 2 / d1) {
            return n1 * (t -= 1.5 / d1) * t + 0.75;
        } else if (t < 2.5 / d1) {
            return n1 * (t -= 2.25 / d1) * t + 0.9375;
        } else {
            return n1 * (t -= 2.625 / d1) * t + 0.984375;
        }
    }
});
```
This new curve can immediately be referenced in any YAML scene file:
```yaml
keyframes:
  - tick: 40
    interpolation: "bounce_out"
```

---

## 🧩 Programmatic Scene Construction

Scenes can also be constructed entirely in Java without touching YAML files:

```java
import com.darkbladedev.cinematic.core.model.Scene;
import com.darkbladedev.cinematic.camera.CameraTrack;
import com.darkbladedev.cinematic.camera.CameraFrame;
import com.darkbladedev.cinematic.camera.CameraPathMode;
import com.darkbladedev.cinematic.camera.targeting.FixedAnglesStrategy;
import com.darkbladedev.cinematic.core.interpolation.EaseInOutInterpolator;
import org.joml.Vector3d;

import java.util.List;
import java.util.Map;

List<CameraFrame> frames = List.of(
    new CameraFrame(0, new Vector3d(0, 70, 0), 0f, 0f, 70f, EaseInOutInterpolator.INSTANCE),
    new CameraFrame(100, new Vector3d(50, 80, 50), 90f, 15f, 85f, EaseInOutInterpolator.INSTANCE)
);

CameraTrack cameraTrack = new CameraTrack(
    "main_cam",
    frames,
    CameraPathMode.SPLINE,
    FixedAnglesStrategy.INSTANCE
);

Scene dynamicScene = new Scene(
    "dynamic_intro",
    100,
    List.of(cameraTrack),
    Map.of("author", "PluginAPI")
);
```
