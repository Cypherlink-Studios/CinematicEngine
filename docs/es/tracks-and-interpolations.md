---
title: Pistas, Fotogramas Clave e Interpolación
description: Guía completa sobre pistas de línea temporal, posicionamiento de keyframes, interpolación lineal y splines Catmull-Rom.
sidebar:
  order: 6
---

# Pistas, Fotogramas Clave e Interpolación

En el corazón de CinematicEngine opera un motor de evaluación temporal determinista. Cada escena consta de una o más **pistas (tracks)** ejecutadas concurrentemente, cuyas propiedades se calculan a través de **fotogramas clave (keyframes)** discretos.

---

## 🛤️ Tipos de Pistas Soportadas

| Tipo de Pista | Factoría | Salida Primaria | Casos de Uso Típicos |
| :--- | :--- | :--- | :--- |
| `camera` | `CameraTrackFactory` | `CameraFrame` (pos, yaw, pitch, fov) | Control de la perspectiva y trayectoria del espectador. |
| `actor` | `ActorTrackFactory` | `ActorFrame` (pos, yaw, pitch, acción) | Coreografía de jugadores, actores virtuales y NPCs. |
| `effect` | `EffectTrackFactory` | `EffectFrame` (partículas, sonidos) | Acentos audiovisuales, explosiones y música de ambiente. |

---

## ⏱️ Mecánica de Fotogramas Clave (Keyframes)

Un fotograma clave establece el estado de una pista en un tick exacto. Entre un fotograma y el siguiente, el motor interpola los estados intermedios mediante funciones matemáticas:

```yaml
keyframes:
  - tick: 0
    position: [0.0, 64.0, 0.0]
    interpolation: "ease_in_out"
  - tick: 60
    position: [20.0, 75.0, 10.0]
    interpolation: "linear"
```

- Al evaluar el tick `30`, el sistema calcula el progreso normalizado $t = \frac{30 - 0}{60 - 0} = 0.5$ y aplica la función de interpolación configurada.

---

## 📈 Algoritmos de Interpolación

### 1. Interpolación Lineal (`LinearInterpolator`)
Realiza una transición constante a velocidad uniforme:
$$f(t) = t$$

```yaml
interpolation: "linear"
```
*Idóneo para*: Desplazamientos mecánicos a velocidad fija o barridos constantes.

### 2. Interpolación Suave Inicio/Fin (`EaseInOutInterpolator`)
Acelera suavemente al salir y decelera gradualmente al llegar utilizando una curva polinómica smoothstep:
$$f(t) = t^2 (3 - 2t)$$

```yaml
interpolation: "ease_in_out"
```
*Idóneo para*: Movimientos de cámara orgánicos, frenadas cinematográficas e inicios suaves de toma.

---

## 🌀 Curvas Spline Cúbicas Catmull-Rom

La interpolación puramente lineal traza rectas entre puntos contiguos, generando quiebros angulares y choques visuales en los cambios de dirección.

CinematicEngine resuelve esto con **splines cúbicos Catmull-Rom** (`CatmullRomSpline`):

```yaml
data:
  path-mode: "spline"
```

```text
Trayectoria Lineal (Quiebros Bruscos):
[P0] ───────────────> [P1] ╲
                           ╲
                            ╲
                             ▼
                            [P2] ───────────────> [P3]

Spline Catmull-Rom (Curva Suave Continua):
[P0] ╭────────────────[P1]
      ╲                  ╲
       ╲                  ╰───────[P2]────────────╮
        ╰──────────────────────────────────────────╯ [P3]
```

### Formulación Matemática
Para cualquier segmento delimitado entre los puntos $P_1$ y $P_2$, el spline evalúa el punto previo $P_0$ y el siguiente $P_3$ para derivar vectores tangentes continuos:

$$P(t) = \frac{1}{2} \left[ 2P_1 + (P_2 - P_0)t + (2P_0 - 5P_1 + 4P_2 - P_3)t^2 + (-P_0 + 3P_1 - 3P_2 + P_3)t^3 \right]$$

### Ventajas Destacadas:
- **Continuidad Tangente $C^1$**: Ausencia total de quiebros o saltos angulares; el paso por los puntos de control mantiene velocidad continua.
- **Paso Directo por Puntos de Control**: A diferencia de las curvas Bézier, el camino atraviesa obligatoriamente las coordenadas definidas en cada keyframe.
- **Manejo Dinámico de Extremos**: El motor refleja automáticamente los puntos en los bordes iniciales y finales para garantizar entradas y salidas fluidas.
