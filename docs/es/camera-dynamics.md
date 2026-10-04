---
title: Dinámica de Cámaras y Rigs
description: Guía técnica sobre arquitectura dual de cámara, cámara virtual packet-only con PacketEvents, Display entity rigs y seguimiento dinámico LookAt.
sidebar:
  order: 5
---

# Dinámica de Cámaras y Rigs

El sistema de cámara de CinematicEngine fue concebido para superar una de las mayores limitaciones en Minecraft: **eliminar los tirones de imagen, vibraciones y desincronizaciones de red en movimientos automatizados**.

---

## 🎥 Arquitectura Dual de Montaje de Cámara

CinematicEngine implementa una **Arquitectura Polimórfica de Montaje Dual** orquestada por `CameraRigManager`:

```text
+--------------------------------------------------------------------------+
|                  ESTRATEGIA DUAL DE RIGS DE CÁMARA                       |
+--------------------------------------------------------------------------+

                           [CameraRigManager]
                                   |
             +---------------------+---------------------+
             | modo = PACKET_VIRTUAL                     | modo = SERVER_DISPLAY
             v                                           v
  [PacketCameraRigSession]                     [DisplayCameraRigSession]
  - PacketEvents 2.14.0                        - ItemDisplay en chunk del mundo
  - Spawn de entidad virtual por paquetes      - player.setSpectatorTarget()
  - WrapperPlayServerCamera(virtualId)         - rig.teleport(loc)
  - WrapperPlayServerEntityTeleport            - Interpolación nativa del cliente
  - Reset: WrapperPlayServerCamera(self)       - Restauración: rig.remove()
```

### 1. `PACKET_VIRTUAL` (Prioritario y por Defecto)
- **Cero Entidades en el Servidor**: Genera una entidad virtual en el canal de red del espectador mediante `WrapperPlayServerSpawnEntity` (`EntityType.ITEM_DISPLAY`).
- **Anclaje de Cámara de Espectador**: Despacha `WrapperPlayServerCamera(virtualEntityId)` vía PacketEvents 2.14.0. El cliente de Minecraft ancla su perspectiva sin que exista ninguna entidad en el mundo del servidor.
- **Movimiento Sub-Tick**: Cada actualización de fotograma emite `WrapperPlayServerEntityTeleport`, logrando transiciones ultrasuaves a la tasa de refresco del monitor sin sobrecargar el servidor.
- **Desmonte Limpio**: Al finalizar o cancelarse la escena, envía `WrapperPlayServerCamera(player.getEntityId())` para restaurar la vista al propio avatar del jugador y destruye la entidad virtual con `WrapperPlayServerDestroyEntities`.

### 2. `SERVER_DISPLAY` (Reserva y Streaming de Larga Distancia)
- **Rig Físico en el Mundo**: Spawnea un `ItemDisplay` invisible de Paper en el mundo con `teleportDuration = 1` para interpolación nativa en GPU.
- **Streaming entre Chunks**: Dado que la entidad reside en el servidor, Paper gestiona de forma nativa la carga y transmisión de chunks a larga distancia.
- **Fallback Automático**: Si PacketEvents no está disponible en el entorno, `CameraRigManager` conmuta de forma transparente a `SERVER_DISPLAY`.

---

## ⚙️ Configuración

Define el modo de montaje predeterminado en `plugins/CinematicEngine/config.yml`:

```yaml
camera:
  # Modo de cámara por defecto: PACKET_VIRTUAL (recomendado) o SERVER_DISPLAY
  mount-mode: PACKET_VIRTUAL
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

Durante la cinemática, el espectador entra en modo espectador anclado al rig. La clase `SpectatorSafetyListener` garantiza la integridad del jugador en ambos modos:

- **Prevención de Desmonte**: Intercepta eventos de agacharse y dejar de espectar (`PlayerToggleSneakEvent`, `PlayerStopSpectatingEntityEvent`). En modo `PACKET_VIRTUAL`, reenvía `WrapperPlayServerCamera` para mantener el anclaje seguro del cliente.
- **Serialización de Estado**: Almacena modo de juego, permiso de vuelo, coordenadas, rotación e inventario antes del inicio.
- **Limpieza de Emergencia**: Ante desconexión o reinicio, restaura al usuario de inmediato y destruye entidades o paquetes sin dejar rastros huérfanos.
