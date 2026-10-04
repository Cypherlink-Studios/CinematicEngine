---
title: Comandos y Permisos
description: Referencia completa de comandos, subcomandos, parámetros y nodos de permisos en CinematicEngine.
sidebar:
  order: 3
---

# Comandos y Permisos

CinematicEngine dispone de un sistema de comandos robusto e intuitivo gestionado por `CommandManager`. Todos los subcomandos cuentan con autocompletado contextual por pestañas (Tab-Completion) y mensajes de retroalimentación claros.

---

## ⌨️ Sintaxis de Comandos

- **Comando Raíz**: `/cinematic`
- **Alias del Comando**: `/cine`

---

## 📜 Referencia de Subcomandos

### 1. `/cine play <scene> [player]`
Inicia la reproducción de una escena cinemática. Si no se especifica ningún jugador, se ejecuta sobre el usuario que emitió el comando.

- **Parámetros**:
  - `<scene>`: Identificador único de la escena cargada (ej. `welcome`, `boss_intro`).
  - `[player]` *(Opcional)*: Jugador objetivo conectado que experimentará la cinemática.
- **Permiso**: `cinematic.play` (Por defecto: `op`)
- **Ejemplos**:
  ```text
  /cine play welcome
  /cine play boss_intro Alex
  ```

---

### 2. `/cine stop [player]`
Detiene de inmediato la cinemática en curso para el jugador indicado (o el remitente) y restaura su estado original (modo de juego, coordenadas y punto de vista).

- **Parámetros**:
  - `[player]` *(Opcional)*: Jugador objetivo conectado.
- **Permiso**: `cinematic.stop` (Por defecto: `op`)
- **Ejemplo**:
  ```text
  /cine stop
  /cine stop Steve
  ```

---

### 3. `/cine pause [player]`
Pausa la reproducción en el tick actual de la línea de tiempo para el jugador objetivo.

- **Parámetros**:
  - `[player]` *(Opcional)*: Jugador objetivo conectado.
- **Permiso**: `cinematic.pause` (Por defecto: `op`)
- **Ejemplo**:
  ```text
  /cine pause
  ```

---

### 4. `/cine resume [player]`
Reanuda la reproducción de una cinemática previamente pausada para el jugador objetivo.

- **Parámetros**:
  - `[player]` *(Opcional)*: Jugador objetivo conectado.
- **Permiso**: `cinematic.resume` (Por defecto: `op`)
- **Ejemplo**:
  ```text
  /cine resume
  ```

---

### 5. `/cine list`
Muestra el catálogo de todas las escenas cinemáticas registradas y validadas, junto con su duración en ticks y títulos de metadatos.

- **Permiso**: `cinematic.list` (Por defecto: `true`)
- **Ejemplo**:
  ```text
  /cine list
  ```

---

### 6. `/cine reload`
Recarga todos los archivos YAML de escenas desde el disco y actualiza los registros internos sin necesidad de reiniciar el servidor. Valida cada escena con `SceneDtoValidator` e informa de cualquier error en la consola.

- **Permiso**: `cinematic.reload` (Por defecto: `op`)
- **Ejemplo**:
  ```text
  /cine reload
  ```

---

### 7. `/cine help`
Muestra el menú de ayuda interactivo con la lista de subcomandos, descripciones de argumentos y sintaxis de uso.

- **Permiso**: `cinematic.help` (Por defecto: `true`)
- **Ejemplo**:
  ```text
  /cine help
  ```

---

## 🔒 Matriz de Permisos

| Nodo de Permiso | Descripción | Por Defecto | Grupo Sugerido |
| :--- | :--- | :--- | :--- |
| `cinematic.play` | Permite reproducir cinemáticas para uno mismo u otros jugadores. | `op` | Administrador, Staff de Eventos |
| `cinematic.stop` | Permite detener cinemáticas en reproducción. | `op` | Administrador, Moderador |
| `cinematic.pause` | Permite pausar una escena activa. | `op` | Administrador, Moderador |
| `cinematic.resume` | Permite reanudar una escena pausada. | `op` | Administrador, Moderador |
| `cinematic.list` | Permite consultar el catálogo de escenas disponibles. | `true` | Todos los jugadores |
| `cinematic.reload` | Permite recargar archivos de configuración y escenas desde el disco. | `op` | Administrador |
| `cinematic.help` | Permite ver la ayuda de comandos. | `true` | Todos los jugadores |
| `cinematic.*` | Concede acceso administrativo total a todos los comandos. | `op` | Superadmin / Dueño del servidor |

---

## 🛡️ Ejemplos de Configuración con LuckPerms

### Asignar Control a Moderadores
```bash
/lp group moderator permission set cinematic.play true
/lp group moderator permission set cinematic.stop true
/lp group moderator permission set cinematic.pause true
/lp group moderator permission set cinematic.resume true
```

### Acceso Administrativo Completo
```bash
/lp group admin permission set cinematic.* true
```
