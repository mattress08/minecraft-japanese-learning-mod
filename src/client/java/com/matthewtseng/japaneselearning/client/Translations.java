package com.matthewtseng.japaneselearning.client;

import com.matthewtseng.japaneselearning.JapaneseLearning;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Maps Japanese UI text back to its translation key, then to the English text. Rebuilt on every resource reload. */
public final class Translations implements ResourceManagerReloadListener {
	private static Map<String, String> jaToKey = Map.of();
	private static Language english;

	public static boolean active() {
		return "ja_jp".equals(Minecraft.getInstance().getLanguageManager().getSelected());
	}

	/** English for an exact Japanese UI string, or null if it isn't a known translation. */
	public static String english(String ja) {
		String key = jaToKey.get(ja);
		return key == null || english == null ? null : english.getOrDefault(key, null);
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		Map<String, String> reverse = new HashMap<>();
		for (String namespace : manager.getNamespaces()) {
			for (Resource resource : manager.getResourceStack(Identifier.fromNamespaceAndPath(namespace, "lang/ja_jp.json"))) {
				try (InputStream in = resource.open()) {
					Language.loadFromJson(in, (key, value) -> reverse.put(value, key));
				} catch (IOException | RuntimeException e) {
					JapaneseLearning.LOGGER.warn("Skipping broken {}:lang/ja_jp.json in pack {}", namespace, resource.sourcePackId(), e);
				}
			}
		}
		jaToKey = reverse;
		english = ClientLanguage.loadFrom(manager, List.of("en_us"), false);
	}
}
