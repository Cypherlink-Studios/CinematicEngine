---
title: Introducción a CinematicEngine
description: Motor modular de secuencias cinemáticas y dinámicas de cámara para servidores Minecraft Paper y Purpur.
sidebar:
  order: 1
---

# Introducción a CinematicEngine

Bienvenido a la documentación oficial de **CinematicEngine**, un motor avanzado y modular de secuencias cinemáticas y dinámicas de cámara diseñado para servidores de Minecraft **Paper** y **Purpur** (versiones 1.20+ y 1.21.x).

CinematicEngine permite a los administradores de servidores y desarrolladores coreografiar cinemáticas de calidad cinematográfica, recorridos aéreos de construcciones, introducciones épicas de jefes y experiencias narrativas inmersivas directamente en Minecraft vainilla sin requerir modificaciones en el cliente.

---

## ⚡ Visión General de la Arquitectura

CinematicEngine está construido bajo una arquitectura modular desacoplada:

```text
+-----------------------------------------------------------------------------+
|                        ARQUITECTURA DE CINEMATICENGINE                      |
+-----------------------------------------------------------------------------+
|                                                                             |
|  [CAPA DE PRESENTACIÓN]  Comandos /cinematic y /cine                        |
|                          Autocompletado contextual y controles admin        |
|                          Gestión de sesiones DemoCinematicOrchestrator      |
|                                     │                                       |
|                                     ▼                                       |
|  [DSL Y PARSEO]          Escenas declarativas en YAML                       |
|                          SnakeYamlSceneParser y SceneDtoValidator           |
|                          Mapeo en TrackRegistry e InterpolatorRegistry      |
|                                     │                                       |
|                                     ▼                                       |
|  [CÁMARA Y DINÁMICAS]    CameraTrack y CameraFrame                          |
|                          Seguimiento LookAt (Fijo, POI estático, Actor, Dir)|
|                          Interpolación lineal y Spline Catmull-Rom          |
|                                     │                                       |
|                                     ▼                                       |
|  [ADAPTADORES Y SEGURIDAD] Display Entity Rig (Movimiento fluido en cliente)|
|                          ProtocolLibBridge (Paquetes CAMERA de espectador)  |
|                          SpectatorSafetyListener (Restauración de estado)   |
|                                     │                                       |
|                                     ▼                                       |
|  [NÚCLEO Y RUNTIME]      TimelinePlayer y BukkitTickScheduler               |
|                          ActorTrack y EffectTrack (Partículas y Sonidos)    |
|                          TimelineContext y ejecución por ticks              |
|                                                                             |
+-----------------------------------------------------------------------------+
```

---

## 🚀 Capacidades Principales

### 1. Rigs de Display Entities y Suavizado de Paquetes
Los plugins tradicionales teletransportan al jugador tick a tick, produciendo tirones visuales (stuttering) y desincronizaciones de red. CinematicEngine ancla la vista del espectador a un **Display Entity Rig** invisible mediante paquetes `PacketType.Play.Server.CAMERA` de ProtocolLib. Esto aprovecha la canalización de interpolación nativa del cliente de Minecraft para lograr movimientos a 60+ FPS sin vibraciones.

### 2. Interpolación Spline Cúbica Catmull-Rom
Las trayectorias puramente lineales provocan esquinas toscas y cambios bruscos de dirección. CinematicEngine incluye interpolación **spline Catmull-Rom**, calculando tangentes continuas de velocidad en tres dimensiones para que la cámara fluya con curvas orgánicas y naturales.

### 3. Motor Dinámico de Enfoque LookAt
Separa la posición de la cámara de su punto de enfoque:
- **Ángulos Fijos (Fixed)**: Control explícito de yaw y pitch en cada fotograma clave.
- **Punto Estático (POI)**: La cámara fija su mirada en unas coordenadas de bloque mientras el rig se desplaza.
- **Seguimiento de Actor (Actor)**: Rastrea a jugadores, NPCs o mobs en movimiento en tiempo real.
- **Dirección de Avance (Forward)**: Alinea automáticamente la cámara con el vector tangente de movimiento.

### 4. DSL Declarativo en YAML
Diseña cinemáticas complejas utilizando archivos YAML limpios y legibles. Define duraciones, coordenadas de keyframes, campo de visión (FOV), transiciones, partículas y efectos de sonido espaciales con validación estricta de esquemas.

### 5. Secuenciación Multicanal en Línea de Tiempo
Sincroniza el movimiento de la cámara con sucesos del mundo en una misma línea temporal:
- **Pistas de Cámara (Camera Tracks)**: Coordenadas, rotación, FOV y modo de trayectoria.
- **Pistas de Actores (Actor Tracks)**: Desplazamiento y orientación de entidades participantes.
- **Pistas de Efectos (Effect Tracks)**: Emisión de partículas y reproducción de sonidos en ticks exactos.

### 6. Protección Integral del Espectador y Seguridad de Estado
El motor cuida la experiencia del usuario de inicio a fin:
- Guarda el modo de juego original, estado de vuelo, posición e inventario.
- Bloquea interacciones, desmontes involuntarios y comandos durante la reproducción.
- Garantiza la restauración completa del jugador al finalizar la escena, ante cancelaciones o desconexiones.

---

## 📖 Navegación por la Documentación

- **[Primeros Pasos](/es/docs/cinematicengine/getting-started/)**: Requisitos previos, instalación de ProtocolLib y tu primera cinemática.
- **[Comandos y Permisos](/es/docs/cinematicengine/commands-and-permissions/)**: Referencia completa de `/cinematic` (`/cine`) y nodos de permiso.
- **[Escenas DSL](/es/docs/cinematicengine/dsl-scenes/)**: Especificación de escenas YAML, estructura y reglas de sintaxis.
- **[Dinámica de Cámaras](/es/docs/cinematicengine/camera-dynamics/)**: Rigs de Display entities, paquetes de espectador y modos LookAt.
- **[Pistas e Interpolación](/es/docs/cinematicengine/tracks-and-interpolations/)**: Keyframes, splines, curvas de aceleración y líneas temporales.
- **[Actores y Efectos](/es/docs/cinematicengine/actors-and-effects/)**: Control de entidades, ráfagas de partículas y audio posicional.
- **[API para Desarrolladores](/es/docs/cinematicengine/developer-api/)**: Integración en Java, `CinematicService`, pistas personalizadas y eventos.
