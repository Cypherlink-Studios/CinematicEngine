---
title: Dinámica de Cámaras y Rigs
description: Guía técnica sobre rigs con Display entities, suavizado de paquetes e interpolación dinámica LookAt.
sidebar:
  order: 5
---

# Dinámica de Cámaras y Rigs

El sistema de cámara de CinematicEngine fue concebido para superar una de las mayores limitaciones en Minecraft: **eliminar los tirones de imagen, vibraciones y desincronizaciones de red en movimientos automatizados**.

---

## 🎥 El Rig de Cámara con Display Entities

### El Problema del Teletransporte Tradicional
Los plugins clásicos de cinemáticas teletransportan directamente a la entidad `Player` del espectador en cada tick (`player.teleport()`). En Minecraft:
- El servidor procesa 20 ticks por segundo (1 tick = 50 ms).
- El monitor del jugador renderiza a 60, 144 o 240 FPS.
- La teletransportación directa obliga al cliente a saltar bruscamente entre posiciones cada 50 ms, causando micro-stuttering y vibración constante del ángulo de visión.

### La Solución: Display Entity Rig Invisible
A partir de Minecraft 1.19.4 y 1.20, las **Display Entities** poseen interpolación de transformaciones nativa calculada en la GPU del cliente. CinematicEngine implementa esta técnica mediante `CameraRigManager` y `CameraRigSession`:

```text
+-------------------------------------------------------------------------+
|                    CICLO DE EJECUCIÓN DEL RIG DE CÁMARA                 |
+-------------------------------------------------------------------------+

  1. INICIALIZACIÓN DEL RIG
     └── Spawnea una Display Entity invisible en el origen de la escena.
     └── Configura la duración de interpolación de transformaciones en el cliente.

  2. ANCLAJE DE CÁMARA DE ESPECTADOR
     └── ProtocolLibBridge construye un paquete PacketType.Play.Server.CAMERA.
     └── Vincula la vista de la cámara del cliente al ID de la Display Entity.
     └── El jugador permanece protegido mientras su visión sigue al rig.

  3. MOVIMIENTO FLUIDO A TASA DE REFRESCO DE MONITOR
     └── El servidor actualiza los vectores de traslación y rotación por tick.
     └── La GPU del cliente interpola las posiciones entre ticks a los FPS del monitor.

  4. RESTAURACIÓN Y DESMONTAJE
     └── Redirige el paquete CAMERA de vuelta a la entidad del propio jugador.
     └── Elimina de forma segura la Display Entity temporal.
     └── Restaura modo de juego, posición e inventario original del espectador.
+-------------------------------------------------------------------------+
```

---

## 🎯 Estrategias de Seguimiento LookAt

CinematicEngine desacopla la **posición de la cámara** (por dónde viaja el rig) de la **orientación de la cámara** (hacia dónde apunta la mirada). Esto permite maniobras envolventes como orbitar alrededor de una estatua manteniendo el enfoque constante.

### 1. Ángulos Fijos (`FixedAnglesStrategy`)
La cámara apunta en los valores exactos de `yaw` y `pitch` fijados en cada fotograma clave:
```yaml
look-at:
  mode: "fixed"
```

### 2. Punto Estático / Punto de Interés (`StaticTargetStrategy`)
La cámara calcula en cada tick el yaw y el pitch necesarios para enfocar una coordenada 3D fija del mundo:
```yaml
look-at:
  mode: "static" # o "point", "poi"
  target: [128.5, 64.0, -250.0]
```
*Recomendado para*: Vuelos en círculo alrededor de construcciones, acercamientos a cofres o panorámicas.

### 3. Seguimiento Dinámico de Actores (`ActorTargetStrategy`)
La cámara persigue en tiempo real a una entidad en movimiento (jugador, NPC o mob) utilizando `ActorPositionLookup`:
```yaml
look-at:
  mode: "actor"
  target-actor: "hero_npc" # Identificador del actor definido en la pista
```
*Recomendado para*: Secuencias de combate, persecuciones o personajes en montura.

### 4. Vector Tangente de Velocidad (`VelocityForwardStrategy`)
La cámara gira de forma automática orientándose hacia su dirección de avance instantánea:
```yaml
look-at:
  mode: "forward" # o "velocity"
```
*Recomendado para*: Vuelos estilo montaña rusa, planos subjetivos de vuelo o vista de dron.

---

## 🛡️ Seguridad del Espectador y Protección de Estado

Durante la cinemática, el espectador entra en modo espectador anclado al rig. La clase `SpectatorSafetyListener` garantiza la integridad del jugador:

- **Prevención de Desmonte**: Intercepta paquetes de agacharse/desmontar para evitar que el jugador se libere del rig.
- **Bloqueo de Interacciones**: Impide romper bloques, golpear entidades y ejecutar comandos no autorizados.
- **Serialización de Estado**: Almacena modo de juego, permiso de vuelo, coordenadas, rotación e inventario antes del inicio.
- **Limpieza de Emergencia**: Ante desconexión o reinicio, restaura al usuario al reingresar y elimina entidades huérfanas.
