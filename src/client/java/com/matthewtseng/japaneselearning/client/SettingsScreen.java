package com.matthewtseng.japaneselearning.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.DoubleFunction;

public class SettingsScreen extends Screen {
	private final Screen parent;
	private float textScale = Config.get().textScale();
	private int boxWidth = Config.get().boxWidth();

	public SettingsScreen(Screen parent) {
		super(Component.translatable("japaneselearning.settings.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int x = width / 2 - 100, y = height / 4;
		addRenderableWidget(new Slider(x, y, (textScale - Config.MIN_SCALE) / (Config.MAX_SCALE - Config.MIN_SCALE), v -> {
			textScale = Math.round((float) (Config.MIN_SCALE + v * (Config.MAX_SCALE - Config.MIN_SCALE)) * 10) / 10f;
			return Component.translatable("japaneselearning.settings.text_scale", textScale);
		}));
		addRenderableWidget(new Slider(x, y + 24, (double) (boxWidth - Config.MIN_WIDTH) / (Config.MAX_WIDTH - Config.MIN_WIDTH), v -> {
			boxWidth = (int) Math.round(Config.MIN_WIDTH + v * (Config.MAX_WIDTH - Config.MIN_WIDTH));
			return Component.translatable("japaneselearning.settings.box_width", boxWidth);
		}));
		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose()).bounds(x, y + 56, 200, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		super.extractRenderState(g, mouseX, mouseY, delta);
		g.centeredText(font, title, width / 2, 20, 0xFFFFFFFF);
	}

	@Override
	public void onClose() {
		Config.save(new Config(textScale, boxWidth));
		minecraft.gui.setScreen(parent);
	}

	/** Slider whose label function also applies the 0..1 value to the setting. */
	private static final class Slider extends AbstractSliderButton {
		private final DoubleFunction<Component> apply;

		Slider(int x, int y, double value, DoubleFunction<Component> apply) {
			super(x, y, 200, 20, apply.apply(value), value);
			this.apply = apply;
		}

		@Override
		protected void updateMessage() {
			setMessage(apply.apply(value));
		}

		@Override
		protected void applyValue() {
		}
	}
}
