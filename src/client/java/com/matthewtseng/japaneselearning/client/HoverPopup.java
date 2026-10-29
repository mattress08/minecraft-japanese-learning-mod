package com.matthewtseng.japaneselearning.client;

import com.matthewtseng.japaneselearning.Reading;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.joml.Matrix3x2fc;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

/** Finds the Japanese text under the mouse on any screen and draws its English + reading next to it. */
public final class HoverPopup {
	private static boolean tracking;
	private static int mouseX, mouseY;
	private static String hovered;

	private HoverPopup() {}

	public static void beforeExtract(Screen screen, GuiGraphicsExtractor g, int mx, int my, float delta) {
		tracking = Translations.active();
		mouseX = mx;
		mouseY = my;
		hovered = null;
	}

	/** Called by TextHitboxMixin for every piece of text drawn; keeps the last one under the mouse (topmost). */
	public static void onText(Font font, FormattedCharSequence text, int x, int y, Matrix3x2fc pose) {
		if (!tracking) return;
		Vector2f min = pose.transformPosition(x, y, new Vector2f());
		Vector2f max = pose.transformPosition(x + font.width(text), y + font.lineHeight, new Vector2f());
		if (mouseX < min.x || mouseX >= max.x || mouseY < min.y || mouseY >= max.y) return;
		StringBuilder sb = new StringBuilder();
		text.accept((index, style, codePoint) -> {
			sb.appendCodePoint(codePoint);
			return true;
		});
		hovered = sb.toString();
	}

	public static void afterExtract(Screen screen, GuiGraphicsExtractor g, int mx, int my, float delta) {
		if (!tracking) return;
		tracking = false; // stop recording before drawing our own text
		String text = hovered;
		for (AbstractWidget widget : Screens.getWidgets(screen)) {
			if (widget.visible && widget.isMouseOver(mx, my)) text = widget.getMessage().getString();
		}
		if (text == null || !Reading.hasJapanese(text)) return;

		List<Component> lines = describe(text);
		if (lines.isEmpty()) return;
		draw(g, Minecraft.getInstance().font, lines, mx, my);
	}

	/** Adds English + reading under an item's name in its tooltip. */
	public static void addItemTooltip(ItemStack stack, Item.TooltipContext context, TooltipFlag flag, List<Component> tooltip) {
		if (tooltip.isEmpty() || !Translations.active()) return;
		String name = tooltip.getFirst().getString();
		if (Reading.hasJapanese(name)) tooltip.addAll(1, describe(name));
	}

	private static List<Component> describe(String ja) {
		List<Component> lines = new ArrayList<>();
		String english = Translations.english(ja);
		if (english != null) lines.add(Component.literal(english).withStyle(ChatFormatting.WHITE));
		Reading.Result reading = Reading.of(ja);
		if (reading != null) {
			lines.add(Component.literal(reading.hiragana()).withStyle(ChatFormatting.YELLOW));
			lines.add(Component.literal(reading.romaji()).withStyle(ChatFormatting.GRAY));
		}
		return lines;
	}

	private static void draw(GuiGraphicsExtractor g, Font font, List<Component> lines, int mx, int my) {
		Config config = Config.get();
		List<FormattedCharSequence> wrapped = new ArrayList<>();
		for (Component line : lines) wrapped.addAll(font.split(line, config.boxWidth()));
		int w = 0;
		for (FormattedCharSequence line : wrapped) w = Math.max(w, font.width(line));
		int h = wrapped.size() * (font.lineHeight + 1) - 1;
		float s = config.textScale();

		// above-right of the cursor so vanilla tooltips (below-right) stay readable; kept on screen
		float boxW = (w + 8) * s, boxH = (h + 8) * s;
		float x = Math.clamp(mx + 12, 0, Math.max(0, g.guiWidth() - boxW));
		float y = Math.clamp(my - 12 - boxH, 0, Math.max(0, g.guiHeight() - boxH));

		g.nextStratum();
		g.pose().pushMatrix();
		g.pose().translate(x, y);
		g.pose().scale(s, s);
		g.fill(0, 0, w + 8, h + 8, 0xF0100010);
		g.outline(0, 0, w + 8, h + 8, 0xFF5000FF);
		int lineY = 4;
		for (FormattedCharSequence line : wrapped) {
			g.text(font, line, 4, lineY, 0xFFFFFFFF, true);
			lineY += font.lineHeight + 1;
		}
		g.pose().popMatrix();
	}
}
