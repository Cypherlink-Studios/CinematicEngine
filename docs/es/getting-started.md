---
title: Primeros Pasos con CinematicEngine
description: Requisitos del sistema, configuración de ProtocolLib y creación de tu primera escena cinemática.
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
| **ProtocolLib** | v5.3.0+ | **Requerido** para los paquetes de cámara de espectador. |
| **Folia** | No soportado | Utiliza tareas síncronas de Bukkit; regional multithreading no compatible. |

---

## 📦 Proceso de Instalación

1. **Instalar ProtocolLib**:
   - Descarga la versión más reciente de **ProtocolLib** (v5.3.0 o superior) desde su repositorio oficial.
   - Coloca el archivo `ProtocolLib.jar` en la carpeta `plugins/` de tu servidor.

2. **Instalar CinematicEngine**:
   - Descarga `CinematicEngine.jar` desde las [Versiones Oficiales](https://github.com/Cypherlink-Studios/CinematicEngine/releases).
   - Coloca `CinematicEngine.jar` en el directorio `plugins/`.

3. **Iniciar el Servidor**:
   - Inicia o reinicia el servidor para generar los directorios y archivos por defecto:
     ```bash
     plugins/CinematicEngine/
     ├── config.yml
     └── scenes/
     ```

4. **Verificar la Instalación**:
   - Ejecuta el comando raíz en la consola o dentro del juego:
     ```text
     /cine help
     ```
   - Si la instalación fue exitosa, verás el listado de subcomandos disponibles.

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

- **La cámara no se mueve**: Asegúrate de que `ProtocolLib` esté cargado correctamente sin errores en consola.
- **Movimiento con tirones**: Confirma que el cliente ejecute Minecraft 1.20+. Los Display entity rigs interpolan las transformaciones de forma nativa a la tasa de refresco del monitor.
- **Errores de sintaxis YAML**: Si `/cine reload` arroja advertencias de validación, revisa que las posiciones numéricas tengan el formato `[x, y, z]` y que los ticks sean cronológicamente ordenados.
