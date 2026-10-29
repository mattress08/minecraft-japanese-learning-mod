package com.matthewtseng.japaneselearning;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/** Pronunciation of Japanese text as hiragana and romaji. */
public final class Reading {
	public record Result(String hiragana, String romaji) {}

	private static final Map<String, Result> CACHE = new ConcurrentHashMap<>();
	private static CompletableFuture<Tokenizer> tokenizer = new CompletableFuture<>();

	private Reading() {}

	/** Loads the dictionary in the background (takes about a second). */
	public static void load() {
		tokenizer = CompletableFuture.supplyAsync(Tokenizer::new);
	}

	/** Reading of the text, or null while the dictionary is still loading. */
	public static Result of(String text) {
		Tokenizer t = tokenizer.getNow(null);
		if (t == null) return null;
		return CACHE.computeIfAbsent(text, s -> {
			StringBuilder kana = new StringBuilder();
			List<String> words = new ArrayList<>();
			for (Token token : t.tokenize(s)) {
				String reading = token.getReading();
				String word = toHiragana(reading == null || reading.equals("*") ? token.getSurface() : reading);
				kana.append(word);
				if (!word.isBlank()) words.add(toRomaji(word));
			}
			return new Result(kana.toString(), String.join(" ", words));
		});
	}

	public static boolean hasJapanese(String s) {
		return s.codePoints().anyMatch(c -> (c >= 0x3040 && c <= 0x30FF) || (c >= 0x4E00 && c <= 0x9FFF));
	}

	static String toHiragana(String s) {
		StringBuilder out = new StringBuilder(s.length());
		for (char c : s.toCharArray()) out.append(c >= 'ァ' && c <= 'ヶ' ? (char) (c - 0x60) : c);
		return out.toString();
	}

	private static final Map<String, String> KANA = new HashMap<>();
	static {
		String table = "あa いi うu えe おo かka きki くku けke こko さsa しshi すsu せse そso たta ちchi つtsu てte とto "
				+ "なna にni ぬnu ねne のno はha ひhi ふfu へhe ほho まma みmi むmu めme もmo やya ゆyu よyo "
				+ "らra りri るru れre ろro わwa をwo んn がga ぎgi ぐgu げge ごgo ざza じji ずzu ぜze ぞzo "
				+ "だda ぢji づzu でde どdo ばba びbi ぶbu べbe ぼbo ぱpa ぴpi ぷpu ぺpe ぽpo ゔvu "
				+ "ぁa ぃi ぅu ぇe ぉo ゃya ゅyu ょyo ゎwa "
				+ "きゃkya きゅkyu きょkyo しゃsha しゅshu しょsho ちゃcha ちゅchu ちょcho にゃnya にゅnyu にょnyo "
				+ "ひゃhya ひゅhyu ひょhyo みゃmya みゅmyu みょmyo りゃrya りゅryu りょryo ぎゃgya ぎゅgyu ぎょgyo "
				+ "じゃja じゅju じょjo びゃbya びゅbyu びょbyo ぴゃpya ぴゅpyu ぴょpyo しぇshe ちぇche じぇje "
				+ "ふぁfa ふぃfi ふぇfe ふぉfo てぃti でぃdi うぃwi うぇwe うぉwo ゔぁva ゔぃvi ゔぇve ゔぉvo";
		for (String pair : table.split(" ")) {
			int split = pair.charAt(1) < 0x80 ? 1 : 2;
			KANA.put(pair.substring(0, split), pair.substring(split));
		}
	}

	/** Hepburn-style romaji; characters that aren't kana pass through unchanged. */
	static String toRomaji(String text) {
		String s = toHiragana(text);
		StringBuilder out = new StringBuilder();
		boolean doubleNext = false; // after small っ, the next consonant is doubled
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == 'っ') {
				doubleNext = true;
				continue;
			}
			String romaji;
			String pair = i + 1 < s.length() ? KANA.get(s.substring(i, i + 2)) : null;
			if (pair != null) {
				romaji = pair;
				i++;
			} else if (c == 'ー') {
				char last = out.isEmpty() ? ' ' : out.charAt(out.length() - 1);
				romaji = "aiueo".indexOf(last) >= 0 ? String.valueOf(last) : "";
			} else {
				romaji = KANA.getOrDefault(String.valueOf(c), String.valueOf(c));
			}
			if (doubleNext && !romaji.isEmpty() && "bcdfghjkmprstvwz".indexOf(romaji.charAt(0)) >= 0) {
				out.append(romaji.startsWith("ch") ? 't' : romaji.charAt(0));
			}
			doubleNext = false;
			out.append(romaji);
		}
		return out.toString();
	}
}
