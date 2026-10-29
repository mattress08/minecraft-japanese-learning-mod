package com.matthewtseng.japaneselearning;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReadingTest {
	@Test
	void romaji() {
		assertEquals("sha", Reading.toRomaji("しゃ"));
		assertEquals("gakkou", Reading.toRomaji("がっこう"));
		assertEquals("keeki", Reading.toRomaji("ケーキ"));
		assertEquals("matcha", Reading.toRomaji("まっちゃ"));
		assertEquals("daiyamondo", Reading.toRomaji("ダイヤモンド"));
		assertEquals("A1", Reading.toRomaji("A1"));
	}

	@Test
	void tokenizedReading() {
		Reading.load();
		Reading.Result r;
		while ((r = Reading.of("ダイヤモンドの剣")) == null) Thread.onSpinWait();
		assertEquals("だいやもんどのけん", r.hiragana());
		assertEquals("daiyamondo no ken", r.romaji());
	}
}
