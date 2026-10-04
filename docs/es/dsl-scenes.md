---
title: Especificación de Escenas DSL
description: Referencia completa del lenguaje de dominio (DSL) en YAML para la creación de cinemáticas en CinematicEngine.
sidebar:
  order: 4
---

# Especificación de Escenas DSL

CinematicEngine utiliza un **lenguaje de dominio específico (DSL)** declarativo basado en YAML para estructurar cinemáticas. El formato es legible para humanos, modular y rigurosamente comprobado durante la carga por `SceneDtoValidator` y `SnakeYamlSceneParser`.

---

## 🏗️ Estructura Raíz de una Escena

Cada archivo representa una secuencia temporal independiente y debe definir las siguientes claves principales:

```yaml
id: "castle_siege_intro"
duration: 300 # Duración total de la secuencia en ticks (20 ticks = 1 segundo)

metadata:
  title: "Introducción Dramática del Asedio"
  author: "BuildTeam"
  description: "Vuelo panorámico sobre las murallas del castillo hasta el salón del trono."

tracks:
  - type: "camera"
    # ... configuración de la pista de cámara ...
  - type: "actor"
    # ... configuración de la pista de actores ...
  - type: "effect"
    # ... configuración de la pista de efectos ...
```

### Propiedades de Nivel Superior

| Campo | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | `string` | **Sí** | Identificador único de la escena (usado en `/cine play <id>`). |
| `duration` | `integer` | **Sí** | Longitud de la secuencia en ticks de juego (`> 0`, ej. `200` = 10s). |
| `metadata` | `map` | No | Metadatos informativos arbitrarios (ej. `title`, `author`, `description`). |
| `tracks` | `list` | **Sí** | Colección de pistas concurrentes en la línea temporal. |

---

## 🛤️ Estructura de las Pistas (Tracks)

Una pista representa un canal independiente en la línea temporal que controla un subsistema (cámara, actores o efectos de entorno).

```yaml
tracks:
  - type: "camera"
    id: "main_camera"
    data:
      path-mode: "spline"
      look-at:
        mode: "static"
        target: [120.5, 75.0, -45.0]
    keyframes:
      - tick: 0
        position: [150.0, 90.0, -10.0]
        fov: 70.0
        interpolation: "ease_in_out"
      - tick: 150
        position: [130.0, 80.0, -30.0]
        fov: 85.0
        interpolation: "ease_in_out"
      - tick: 300
        position: [115.0, 75.0, -42.0]
        fov: 70.0
        interpolation: "linear"
```

### Propiedades Comunes de Pista

- `type` (`string`): Tipo de fábrica de pista registrada (`camera`, `actor`, `effect`).
- `id` (`string`, opcional): Identificador de la pista (por defecto `<type>_track`).
- `data` (`map`, opcional): Parámetros de configuración específicos del tipo de pista.
- `keyframes` (`list`): Lista ordenada de fotogramas clave con las propiedades en el tiempo.

---

## 🎯 Especificación de Fotogramas Clave (Keyframes)

Los fotogramas clave establecen los valores en ticks concretos:

```yaml
keyframes:
  - tick: 0
    position: [100.0, 64.0, 100.0]
    yaw: -90.0
    pitch: 15.0
    fov: 70.0
    interpolation: "ease_in_out"
```

### Campos de Keyframe

| Campo | Tipo | Pistas | Descripción |
| :--- | :--- | :--- | :--- |
| `tick` | `integer` | Todas | Momento temporal en ticks (`0 <= tick <= duration`). |
| `position` | `[x, y, z]` | `camera`, `actor` | Coordenadas en el mundo como lista numérica de 3 valores. |
| `yaw` | `float` | `camera`, `actor` | Ángulo de rotación horizontal en grados (-180 a 180). |
| `pitch` | `float` | `camera`, `actor` | Ángulo de inclinación vertical en grados (-90 a 90). |
| `fov` | `float` | `camera` | Campo de visión en grados (por defecto `70.0`). |
| `interpolation` | `string` | `camera`, `actor` | Función de transición desde el fotograma anterior (`linear`, `ease_in_out`). |
| `particle` | `map` | `effect` | Definición de ráfaga de partículas activada en este tick. |
| `sound` | `map` | `effect` | Definición de efecto de sonido reproducido en este tick. |

---

## 🛡️ Reglas de Validación (`SceneDtoValidator`)

Al ejecutar `/cine reload` o al cargar escenas, el motor aplica estrictas comprobaciones de integridad:

1. **Consistencia Cronológica**:
   - `tick` debe ser `>= 0` y `<= duration`.
   - Los fotogramas se ordenan automáticamente por tick ascendente durante el mapeo.
2. **Validación de Coordenadas**:
   - Las listas de `position` y `target` deben contener exactamente 3 valores numéricos: `[x, y, z]`.
3. **Mínimo de Pistas**:
   - Toda escena debe contener al menos una pista funcional.
4. **Requisitos de Pista de Cámara**:
   - Una pista de cámara requiere al menos 2 fotogramas clave para definir una trayectoria.
5. **Modos de Trayectoria Válidos**:
   - `path-mode` admite `linear` o `spline` (valores no reconocidos recurren de forma segura a `linear`).
6. **Validación de Enfoque LookAt**:
   - `look-at.mode` admite `fixed`, `static`, `actor` o `forward`.
   - `mode: static` exige un vector `target: [x, y, z]`.
   - `mode: actor` exige `target-actor` (o `actorId`).
