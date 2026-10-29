package com.matthewtseng.japaneselearning.client.mixin;

import com.matthewtseng.japaneselearning.client.HoverPopup;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// every other text()/centeredText()/textWithWordWrap() overload ends up in this one
@Mixin(GuiGraphicsExtractor.class)
public class TextHitboxMixin {
	@Inject(method = "text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V", at = @At("HEAD"))
	private void japaneselearning$recordText(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow, CallbackInfo ci) {
		HoverPopup.onText(font, text, x, y, ((GuiGraphicsExtractor) (Object) this).pose());
	}
}
