---
title: Actores y Efectos Ambientales
description: Puesta en escena de actores, tracks multicapa de movimiento y acción, poses, animaciones, partículas y audio espacial.
sidebar:
  order: 7
---

# Actores y Efectos Ambientales

CinematicEngine incorpora un sistema integral de **puesta en escena (staging)** para actores y **efectos de entorno** (partículas y sonidos sincronizados) que operan en perfecta armonía con los sistemas de cámara.

---

## 🎭 Puesta en Escena de Actores (`actors`)

A partir de CinematicEngine 1.0, las escenas pueden declarar un elenco de personajes en la cabecera `actors:` del archivo YAML. La orquestación corre a cargo de `SceneActorSession`, que instancia entidades simuladas basadas exclusivamente en paquetes clientes (mediante PacketEvents 2.14.0) para evitar ensuciar los chunks del servidor con entidades huérfanas.

```yaml
id: "emboscada_nocturna"
duration: 120

actors:
  - id: "heroe"
    type: "self_clone"
    initial_position: [120.0, 64.0, 50.0]
    initial_yaw: 0.0
    initial_equipment:
      main_hand: "DIAMOND_SWORD"
      helmet: "IRON_HELMET"

  - id: "asesino"
    type: "virtual"
    skin: "ShadowRogue"
    initial_position: [140.0, 68.0, 65.0]
    initial_yaw: 180.0
    initial_equipment:
      main_hand: "BOW"
```

### Tipos de Actores Soportados

| Tipo (`type`) | Implementación | Descripción |
| :--- | :--- | :--- |
| `virtual` | `VirtualPlayerActor` | Entidad virtual tipo jugador generada por paquetes. Permite skins de Mojang resueltos y cacheados localmente (`SkinCacheService`). |
| `self_clone` | `VirtualPlayerActor` | Clona dinámicamente la apariencia, skin, armadura y objetos en mano del espectador principal que visualiza la cinemática. |
| `persistent` | `NPCActor` / `PlayerActor` | Se vincula a una entidad física existente en el mundo (por UUID o nombre) sin destruirla al finalizar. |

---

## 🧩 Modelo de Capacidades Segregadas (`Actor`)

Para evitar arquitecturas monolíticas rígidas, los actores exponen capacidades opcionales:

- **`Movable` (`asMovable()`)**: Controla teletransporte continuo, velocidad, orientación (`yaw`/`pitch`) y posición tridimensional.
- **`Animatable` (`asAnimatable()`)**: Controla poses del cuerpo (`ActorPose`), animaciones de combate (`ActorAction`) y estados de uso de objetos (`ItemUsageState`).
- **`Equippable` (`asEquippable()`)**: Controla el cambio de armas y armaduras en tiempo real (`EquipmentSlot`).

Si una pista intenta modificar una capacidad que el actor no posee (por ejemplo, equipar una espada a una entidad no equipable), la pista omite la acción de forma segura sin abortar la cinemática.

---

## 🛤️ Pistas Multicapa de Actores

Para permitir que un personaje camine fluidamente mientras agacha la cabeza, blinda su escudo o cambia de arma en momentos clave, CinematicEngine divide el control del actor en dos pistas independientes:

### 1. Pista de Movimiento Continuo (`actor_motion`)

Evalúa trayectorias espaciales continuas interpolando coordenadas entre keyframes mediante interpolación lineal o splines Catmull-Rom.

```yaml
tracks:
  - type: "actor_motion"
    id: "heroe_carrera"
    data:
      actor_id: "heroe"
      path_mode: "spline"      # "spline" o "linear"
      heading: "tangent"        # "tangent" calcula el yaw automáticamente según la dirección de avance
    keyframes:
      - tick: 0
        position: [120.0, 64.0, 50.0]
      - tick: 60
        position: [130.0, 64.0, 58.0]
      - tick: 120
        position: [140.0, 64.0, 65.0]
```

#### Propiedades de `actor_motion`:
- `actor_id` (`string`, obligatorio): Identificador del actor declarado en `actors:`.
- `path_mode` (`string`): Modo de interpolación (`spline` para curvas Catmull-Rom suaves, `linear` para trayectorias rectas).
- `heading` (`string` o `follow_path: true`): Si se establece en `tangent`, la rotación del cuerpo se alinea automáticamente con la derivada tangencial del vector de velocidad.

---

### 2. Pista de Acciones y Expresión Dramática (`actor_action`)

Dispara eventos discretos de pose, animación, uso de objetos y cambio de equipamiento en ticks específicos sin interferir en la trayectoria cinemática continua del actor.

```yaml
tracks:
  - type: "actor_action"
    id: "heroe_acciones"
    data:
      actor_id: "heroe"
    keyframes:
      - tick: 0
        pose: "STANDING"
      - tick: 45
        pose: "CROUCHING"
        item_usage: "BLOCKING"
      - tick: 70
        action: "HURT"
      - tick: 85
        action: "SWING_MAIN_HAND"
        equipment:
          main_hand: "NETHERITE_SWORD"
```

#### Estados y Animaciones Disponibles:

| Categoría | Propiedad | Valores Disponibles |
| :--- | :--- | :--- |
| **Poses** | `pose:` | `STANDING`, `CROUCHING`, `SWIMMING`, `SLEEPING`, `FALL_FLYING`, `SPIN_ATTACK` |
| **Animaciones** | `action:` | `SWING_MAIN_HAND`, `SWING_OFF_HAND`, `HURT`, `CRITICAL_HIT`, `MAGIC_CRITICAL_HIT` |
| **Uso de Ítems**| `item_usage:` | `NONE`, `BLOCKING`, `BOW_PULL`, `CROSSBOW_CHARGE`, `EATING`, `DRINKING`, `SPEAR_CHARGE` |
| **Equipamiento**| `equipment:` | Mapa con claves: `main_hand`, `off_hand`, `helmet`, `chestplate`, `leggings`, `boots` |

---

## ✨ Pista de Efectos Ambientales (`effect`)

La pista `effect` dispara eventos discretos de audio y partículas sincronizados a ticks específicos de la línea temporal:

### 1. Efectos de Partículas
```yaml
tracks:
  - type: "effect"
    id: "chispas_magicas"
    keyframes:
      - tick: 45
        particle:
          type: "ENCHANTMENT_TABLE"
          count: 50
          offset: [1.0, 1.5, 1.0]
          speed: 0.2
      - tick: 70
        particle:
          type: "EXPLOSION_LARGE"
          count: 1
          offset: [0.0, 0.0, 0.0]
```

### 2. Sonidos Posicionales
```yaml
tracks:
  - type: "effect"
    id: "audio_ambiente"
    keyframes:
      - tick: 0
        sound:
          name: "music.credits"
          volume: 0.8
          pitch: 1.0
      - tick: 70
        sound:
          name: "entity.player.attack.crit"
          volume: 1.0
          pitch: 1.1
```

---

## 🎬 Ejemplo Completo de Escena Integrada

El siguiente archivo YAML ilustra la sincronización total: un actor clon del espectador recorre una curva spline con orientación tangente, la cámara lo sigue dinámicamente (`look-at: mode: actor`), y el personaje cambia de postura y empuña su espada en el momento exacto:

```yaml
id: "duelo_cinematico"
duration: 80

actors:
  - id: "guerrero"
    type: "self_clone"
    initial_position: [0.0, 64.0, 0.0]
    initial_yaw: 0.0
    initial_equipment:
      main_hand: "IRON_SWORD"

tracks:
  # 1. Cámara siguiendo dinámicamente al actor guerrero
  - type: "camera"
    id: "camara_orbita"
    data:
      path_mode: "spline"
      look_at:
        mode: "actor"
        target_actor: "guerrero"
    keyframes:
      - tick: 0
        position: [-10.0, 68.0, 0.0]
      - tick: 40
        position: [-8.0, 67.0, 15.0]
      - tick: 80
        position: [-5.0, 66.0, 30.0]

  # 2. Movimiento suave en spline con cabeceo tangencial automático
  - type: "actor_motion"
    id: "guerrero_camino"
    data:
      actor_id: "guerrero"
      path_mode: "spline"
      heading: "tangent"
    keyframes:
      - tick: 0
        position: [0.0, 64.0, 0.0]
      - tick: 40
        position: [5.0, 64.0, 15.0]
      - tick: 80
        position: [12.0, 64.0, 30.0]

  # 3. Acciones expresivas discretas
  - type: "actor_action"
    id: "guerrero_poses"
    data:
      actor_id: "guerrero"
    keyframes:
      - tick: 0
        pose: "STANDING"
      - tick: 35
        pose: "CROUCHING"
        item_usage: "BLOCKING"
      - tick: 60
        action: "SWING_MAIN_HAND"
        equipment:
          main_hand: "DIAMOND_SWORD"
```
