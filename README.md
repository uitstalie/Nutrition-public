# Nutrition Mod

Minecraft NeoForge 1.21.1 mod — nutrition system with configurable nutrients and effects.

## Features

- **15 nutrition groups**: fruits, vegetables, grains, proteins, fishs, eggs, milks, mushrooms, nuts, sugars, honeys, wines, coffee, salt, trace_elements
- **13 effect rules**: OR/AND condition matching with potion effects and attribute modifiers
- **Arc-shaped progress bars**: 260° rendering with 64-segment tessellation
- **Auto BFS propagation**: tag-based group membership spread along recipe chains
- **Configurable decay**: per-group decay value/frequency/pressure with logarithmic formula
- **F3+H tooltips**: nutrition group tags displayed on items

## Build

```bash
./gradlew build          # Compile
./gradlew runClient      # Run client
./gradlew runData        # Generate data packs
```

Requires Java 21.

## Tech Stack

- Minecraft 1.21.1, NeoForge 21.1.226
- Java 21, Gradle (NeoGradle moddev 2.0.123)
- Parchment mappings 2024.11.17

## License

All rights reserved.
