---
title: Primeros Pasos con CinematicEngine
description: Requisitos del sistema, configuración de PacketEvents y creación de tu primera escena cinemática.
sidebar:
  order: 2
---

# Primeros Pasos con CinematicEngine

Esta guía te acompañará paso a paso en la instalación de **CinematicEngine**, la configuración de sus dependencias esenciales y la creación de tu primera escena cinemática en cuestión de minutos.

---

## 📋 Requisitos del Sistema

| Requisito | Versión Soportada | Notas |
| :--- | :--- | :--- |
| **Software de Servidor** | Paper, Purpur | Paper 1.20.4, 1.20.6 o 1.21.x recomendados. |
| **Java Runtime** | Java 21+ | Compilado para la máquina virtual Java 21 LTS. |
| **PacketEvents** | v2.14.0+ | **Requerido** para la cámara virtual packet-only, actualizaciones sub-tick y actores virtuales. |
| **Folia** | No soportado | Utiliza tareas síncronas de Bukkit; regional multithreading no compatible. |

---

## 📦 Proceso de Instalación

1. **Instalar PacketEvents**:
   - Descarga la versión más reciente de **PacketEvents** (v2.14.0 o superior) desde su [repositorio oficial](https://github.com/retrooper/packetevents).
   - Coloca el archivo `packetevents-spigot.jar` en la carpeta `plugins/` de tu servidor.

2. **Instalar CinematicEngine**:
   - Descarga `CinematicEngine.jar` desde las [Versiones Oficiales](https://github.com/Cypherlink-Studios/CinematicEngine/releases).
   - Coloca `CinematicEngine.jar` en el directorio `plugins/`.

3. **Iniciar el Servidor**:
   - Inicia o reinicia el servidor para generar los directorios y archivos por defecto:
     ```bash
     plugins/CinematicEngine/
     ├── config.yml
     ├── locales/
     │   ├── messages_es.yml
     │   └── messages_en.yml
     └── scenes/
     ```

4. **Verificar la Instalación**:
   - Ejecuta el comando raíz en la consola o dentro del juego:
     ```text
     /cine help
     ```
   - Si la instalación fue exitosa, verás el listado de subcomandos disponibles.

---

## 🌐 Configuración de Idioma e Internacionalización (i18n)

CinematicEngine integra un sistema de localización completo con soporte para **Adventure MiniMessage** y archivos de traducción YAML externos:

```yaml
locale:
  # Idioma por defecto del servidor (es, en)
  default: "es"
  # Si está activado, detecta el idioma del cliente de cada jugador
  per-player: true
```

- **Paquetes externos (`locales/`)**: Los mensajes del plugin se extraen automáticamente en `locales/messages_es.yml` y `locales/messages_en.yml`. Puedes modificarlos o añadir nuevos idiomas (`messages_<codigo>.yml`).
- **Detección por jugador (`per-player: true`)**: Los jugadores reciben los mensajes en el idioma configurado en su cliente de Minecraft (ej. `es_es`, `en_us`), recurriendo al idioma por defecto del servidor ante claves no traducidas.
- **Formato MiniMessage**: Soporta gradientes, colores hex, decoraciones y etiquetas interactivas de Adventure (como `<gold>`, `<bold>`, `<hover:...>`).
- **Recarga en caliente**: Cualquier cambio en los archivos de idioma se aplica al instante con `/cine reload`.

---

## ⚙️ Modos de Montaje de Cámara

CinematicEngine incorpora una **Arquitectura de Montaje Dual** configurable en `plugins/CinematicEngine/config.yml`:

```yaml
camera:
  # Modo de cámara por defecto: PACKET_VIRTUAL o SERVER_DISPLAY
  mount-mode: PACKET_VIRTUAL
```

- **`PACKET_VIRTUAL` (Por Defecto y Prioritario)**: Monta la cámara del espectador directamente a una entidad virtual enviada por paquetes mediante PacketEvents (`WrapperPlayServerCamera`). No genera entidades en el mundo del servidor, no muta los chunks y garantiza transiciones cinemáticas ultrasuaves.
- **`SERVER_DISPLAY`**: Instancia un rig de `ItemDisplay` con interpolación de transformaciones en el mundo. Indicado para cinemáticas que cruzan grandes distancias a través de chunks no cargados.

---

## 🎬 Creando Tu Primera Escena

Las escenas cinemáticas se configuran como archivos YAML dentro de la carpeta `plugins/CinematicEngine/scenes/`.

Crea un archivo llamado `welcome.yml` dentro de `plugins/CinematicEngine/scenes/`:

```yaml
id: "welcome"
duration: 160 # 8 segundos (20 ticks = 1 segundo)
metadata:
  title: "Recorrido de Bienvenida"
  author: "Admin"

tracks:
  - type: "camera"
    id: "flyby_camera"
    data:
      path-mode: "spline"
      look-at:
        mode: "static"
        target: [0.5, 68.0, 0.5] # Punto de enfoque (ej. plaza o monumento central)
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

## 🕹️ Probar y Reproducir Tu Escena

1. **Recargar Escenas**:
   Para cargar nuevas escenas sin reiniciar el servidor:
   ```text
   /cine reload
   ```

2. **Listar Escenas Cargadas**:
   Verifica que la escena se haya validado correctamente:
   ```text
   /cine list
   ```
   Aparecerá `welcome` con su duración (160 ticks).

3. **Reproducir la Escena**:
   Pruébala en ti mismo:
   ```text
   /cine play welcome
   ```
   O reprodúcela para otro jugador conectado:
   ```text
   /cine play welcome Steve
   ```

4. **Detener o Pausar**:
   Si necesitas interrumpir la cinemática en cualquier momento:
   ```text
   /cine stop
   ```
   Tu posición, modo de juego e inventario original se restaurarán de inmediato.

---

## 💡 Consejos y Resolución de Problemas

- **La cámara no se mueve**: Asegúrate de que `PacketEvents` (2.14.0+) esté cargado correctamente sin errores en consola.
- **Movimiento con tirones**: Confirma que el cliente ejecute Minecraft 1.20+.
- **Errores de sintaxis YAML**: Si `/cine reload` arroja advertencias de validación, revisa que las posiciones numéricas tengan el formato `[x, y, z]` y que los ticks sean cronológicamente ordenados.
