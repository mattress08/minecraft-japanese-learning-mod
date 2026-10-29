package com.matthewtseng.japaneselearning.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.matthewtseng.japaneselearning.JapaneseLearning;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public record Config(float textScale, int boxWidth) {
	public static final float MIN_SCALE = 0.5f, MAX_SCALE = 2f;
	public static final int MIN_WIDTH = 100, MAX_WIDTH = 400;

	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve(JapaneseLearning.MOD_ID + ".json");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static Config current = new Config(1f, 200);

	public Config {
		// clamps hand-edited or missing values (Gson leaves missing fields at 0)
		textScale = Mth.clamp(textScale, MIN_SCALE, MAX_SCALE);
		boxWidth = Mth.clamp(boxWidth, MIN_WIDTH, MAX_WIDTH);
	}

	public static Config get() {
		return current;
	}

	public static void load() {
		try (Reader reader = Files.newBufferedReader(FILE)) {
			Config loaded = GSON.fromJson(reader, Config.class);
			if (loaded != null) current = loaded;
		} catch (NoSuchFileException e) {
			// first launch: keep defaults
		} catch (IOException | JsonParseException e) {
			JapaneseLearning.LOGGER.warn("Couldn't read {}, using defaults", FILE, e);
		}
	}

	public static void save(Config config) {
		current = config;
		try {
			Files.writeString(FILE, GSON.toJson(config));
		} catch (IOException e) {
			JapaneseLearning.LOGGER.warn("Couldn't save {}", FILE, e);
		}
	}
}
