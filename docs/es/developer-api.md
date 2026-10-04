---
title: API para Desarrolladores
description: Extensión de CinematicEngine, uso de CinematicService, registro de pistas personalizadas, interpoladores y reproducción por código.
sidebar:
  order: 8
---

# API para Desarrolladores

CinematicEngine ofrece una API modular y tipada en Java que permite a otros plugins reproducir cinemáticas por código, incorporar fábricas de pistas a medida, registrar curvas matemáticas de interpolación y generar secuencias dinámicas en memoria.

---

## 📦 Instalación de la Dependencia

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

### Descriptor de Plugin (`paper-plugin.yml` o `plugin.yml`)
```yaml
softdepend:
  - CinematicEngine
  - ProtocolLib
```

---

## 🎮 La Interfaz `CinematicService`

El punto principal de interacción con el motor es `CinematicService`:

```java
import com.darkbladedev.cinematic.bootstrap.CinematicService;
import com.darkbladedev.cinematic.bootstrap.CinematicActionResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class QuestCutsceneManager {

    private final CinematicService cinematicService;

    public QuestCutsceneManager() {
        // Obtener el proveedor de servicio registrado en Bukkit
        this.cinematicService = Bukkit.getServicesManager().load(CinematicService.class);
    }

    public void triggerBossCutscene(Player player) {
        CinematicActionResult result = cinematicService.play(player, "boss_intro");

        switch (result) {
            case SUCCESS -> player.sendMessage("¡Cinemática iniciada!");
            case ALREADY_PLAYING -> player.sendMessage("¡Ya estás presenciando una cinemática!");
            case SCENE_NOT_FOUND -> player.sendMessage("¡No se encontró la escena 'boss_intro'!");
            default -> player.sendMessage("No se pudo iniciar la cinemática.");
        }
    }

    public void abortCutscene(Player player) {
        cinematicService.stop(player);
    }
}
```

### Códigos de Estado `CinematicActionResult`

| Resultado | Significado |
| :--- | :--- |
| `SUCCESS` | La acción solicitada se ejecutó exitosamente. |
| `ALREADY_PLAYING` | No se puede iniciar; el jugador ya tiene una cinemática en curso. |
| `NOT_PLAYING` | No se puede detener/pausar/reanudar; el jugador no está en cinemática. |
| `SCENE_NOT_FOUND` | La escena solicitada no existe en el registro. |
| `INVALID_STATE` | El estado del jugador o del entorno no permite la reproducción. |

---

## 🛠️ Registrar Tipos de Pistas Personalizadas

Es posible registrar factorías de pistas mediante `TrackRegistry` para soportar dinámicas propias del servidor, como subtítulos de diálogo, efectos en pantalla o cambios en la hora del mundo.

```java
import com.darkbladedev.cinematic.dsl.registry.TrackRegistry;
import com.darkbladedev.cinematic.dsl.registry.TrackFactory;
import com.darkbladedev.cinematic.dsl.validator.TrackValidator;
import com.darkbladedev.cinematic.dsl.dto.TrackDTO;
import com.darkbladedev.cinematic.core.model.Track;

public class SubtitleTrackFactory implements TrackFactory {

    @Override
    public Track create(TrackDTO dto) {
        // Interpretar los datos del DTO y retornar la instancia de Track
        return new SubtitleTrack(dto.id(), dto.keyframes());
    }
}

// Registro durante onLoad() u onEnable():
TrackRegistry trackRegistry = DefaultDslComponents.trackRegistry(interpolatorRegistry);
trackRegistry.register("subtitle", new SubtitleTrackFactory(), new SubtitleTrackValidator());
```

---

## 📐 Registrar Interpoladores Personalizados

Agrega curvas de aceleración propias (ej. rebotes elásticos o curvas Bézier complejas) mediante `InterpolatorRegistry`:

```java
import com.darkbladedev.cinematic.dsl.mapper.InterpolatorRegistry;
import com.darkbladedev.cinematic.core.interpolation.Interpolator;

InterpolatorRegistry registry = InterpolatorRegistry.defaultRegistry();

// Registrar una curva de rebote descendente (bounce-out)
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
Esta nueva curva queda inmediatamente disponible para cualquier escena YAML:
```yaml
keyframes:
  - tick: 40
    interpolation: "bounce_out"
```

---

## 🧩 Construcción Dinámica de Escenas en Memoria

Las escenas también pueden ensamblarse completamente en Java sin emplear archivos YAML:

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
