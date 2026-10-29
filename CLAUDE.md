# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Fabric mod for Minecraft **26.3** (mod id `japaneselearning`) that helps English speakers learn Japanese. When the game language is `ja_jp`, hovering any Japanese text on a screen shows a popup with the English translation, hiragana and romaji. Item tooltips get the same info as extra lines. A pause-menu button (top-left) opens a settings screen (popup text scale, box width).

## Commands

- `./gradlew build` — compile, run tests, produce `build/libs/japaneselearning-<version>.jar`
- `./gradlew test` — unit tests only; single test: `./gradlew test --tests "com.matthewtseng.japaneselearning.ReadingTest.romaji"`
- `./gradlew runClient` — launch the game with the mod (dev world/config live in `run/`). Set language to 日本語 to activate the mod.
- `./gradlew runServer` — dedicated server (needs `eula=true` in `run/eula.txt`)

There is no linter.

## Toolchain gotchas

- Minecraft 26.x and Loom require **Java 25**. `gradle.properties` pins `org.gradle.java.home` to the local JDK 25 path, so the build works even from shells whose `JAVA_HOME` is still Java 21. Update that path if the JDK moves.
- 26.x ships unobfuscated with Mojang names, so there are no mappings. Several names differ from older (1.21.x) tutorials: `GuiGraphics` → `GuiGraphicsExtractor` (`drawString` → `text`), `render` → `extractRenderState`, screen render events are `ScreenEvents.beforeExtract/afterExtract`, and screens are opened with `minecraft.gui.setScreen(...)` (not `minecraft.setScreen`).
- When unsure of a 26.3 signature, inspect the deobfuscated jars with `javap` rather than guessing. They are in `~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-{clientonly,common}-deobf/26.3/`, and Fabric API jars are under `~/.gradle/caches/modules-2/files-2.1/net.fabricmc.fabric-api/`.
- Colors passed to `text`/`fill` are ARGB and need the alpha byte (`0xFFFFFFFF`, not `0xFFFFFF`).

## Architecture

Split source sets (`splitEnvironmentSourceSets`): `src/main` is common code, `src/client` is client-only (anything touching screens/rendering), and `src/test` holds JUnit 5 tests that can only see `main`. That is why `Reading` (pure Java, no Minecraft classes) lives in `main`: so it is unit-testable.

How a hover becomes a popup:
1. **`Translations`** (client, a `ResourceManagerReloadListener` registered via Fabric `ResourceLoader`). On every resource reload it reads every namespace's `lang/ja_jp.json` and builds a reverse map from Japanese text to translation key. It also loads an English `ClientLanguage` for `en_us`. Translation is exact-match only, so formatted strings (with `%s` filled in) get a reading but no English.
2. **`Reading`** (main). Kuromoji tokenizes the text to get its katakana reading, which is converted to hiragana. A built-in kana table then produces romaji, space-separated per token. The dictionary loads asynchronously (`Reading.load()` at client init), and `Reading.of` returns null until it is ready. Results are cached per string.
3. **`TextHitboxMixin`** injects into `GuiGraphicsExtractor.text(Font, FormattedCharSequence, int, int, int, boolean)`. Every other `text`/`centeredText`/`textWithWordWrap` overload funnels into this one. It hands each draw call to `HoverPopup.onText`, which transforms the bounds through the current pose matrix and keeps the last string under the mouse.
4. **`HoverPopup`** is wired per screen in `ScreenEvents.AFTER_INIT`:
   - `beforeExtract` resets state and enables tracking. Tracking happens only while a screen is open and the language is `ja_jp`.
   - `afterExtract` prefers a hovered widget's `getMessage()` over the recorded text.
   - It then draws the box on a new stratum (`g.nextStratum()`), scaled by `Config`.
   - Tracking is switched off before drawing, so the popup's own text isn't recorded.
5. **`Config`** is a record serialized with Gson to `config/japaneselearning.json`. Its compact constructor clamps values, which also covers missing or hand-edited fields. **`SettingsScreen`** edits it and saves on close.

The mod's own UI strings live in `src/main/resources/assets/japaneselearning/lang/{en_us,ja_jp}.json`. Add both languages for any new text so it can be hovered too.

Kuromoji (`kuromoji-ipadic` + `kuromoji-core`) is bundled into the jar with Loom `include`, which is not transitive, so both artifacts are listed. This makes the jar about 13 MB.

## Not built yet

Planned but not implemented: spoken pronunciation audio (and the volume setting that goes with it), and word-by-word translation within a label.
