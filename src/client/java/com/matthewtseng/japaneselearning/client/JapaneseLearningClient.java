package com.matthewtseng.japaneselearning.client;

import com.matthewtseng.japaneselearning.JapaneseLearning;
import com.matthewtseng.japaneselearning.Reading;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;

public class JapaneseLearningClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Config.load();
		Reading.load();
		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(JapaneseLearning.id("translations"), new Translations());
		ItemTooltipCallback.EVENT.register(HoverPopup::addItemTooltip);

		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			ScreenEvents.beforeExtract(screen).register(HoverPopup::beforeExtract);
			ScreenEvents.afterExtract(screen).register(HoverPopup::afterExtract);
			// top-left corner so it never overlaps vanilla (or other mods') pause buttons
			if (screen instanceof PauseScreen pause && pause.showsPauseMenu()) {
				Screens.getWidgets(screen).add(Button.builder(Component.translatable("japaneselearning.settings.button"),
						b -> client.gui.setScreen(new SettingsScreen(screen))).bounds(4, 4, 98, 20).build());
			}
		});
	}
}
