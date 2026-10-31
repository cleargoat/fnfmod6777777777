# WaosFNF — Minecraft 1.20.1 / Forge

Friday Night Funkin' dentro de Minecraft.

## Requisitos

- Minecraft 1.20.1
- Minecraft Forge 47.3.0 o compatible de la rama 47.x
- Java 17
- **Cloth Config API 11.1.136 o superior (Forge)**

Cloth Config es una dependencia obligatoria del mod y también debe estar instalada en la carpeta `mods` del cliente.

## Funciones

- Pulsa **U** dentro del mundo para abrir el menú FNF.
- Jugar canciones y configurar opciones.
- Teclas configurables para las cuatro direcciones.
- Upscroll / Downscroll.
- Flechas originales o pixeladas.
- Carpeta de canciones creada automáticamente en `fnf/songs/`, con un tutorial de ejemplo.

## Comandos (OP nivel 2)

```text
/fnfclear start <cancion> <jugadores>
/fnfclear stop <jugadores>
```

La canción corresponde al nombre de la carpeta dentro de `fnf/songs/`.

## Compilar localmente

Necesitas JDK 17 y Gradle 8.8:

```bash
gradle --no-daemon build
```

El JAR se genera en `build/libs/`.

## Compilar en GitHub Actions

1. Sube el contenido de esta carpeta a la raíz de un repositorio de GitHub.
2. Entra en **Actions**.
3. El workflow **Build WaosFNF (Forge 1.20.1)** se ejecuta al hacer `push` o `pull_request`.
4. También puedes iniciarlo manualmente con **Run workflow**.
5. Descarga el artefacto `waosfnf-forge-1.20.1` cuando termine.

## Instalar

Coloca el JAR de WaosFNF y el JAR de **Cloth Config API para Forge 1.20.1** en la carpeta `mods`.
