package uk.co.streefland.rhys.finalyearproject.gui.chatbubble;

import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handles the generation of Emojis
 */
public class EmojiOne {
	private static final boolean USE_ASCII = true;
	private static final HashMap<String, EmojiEntry> emojiList = new HashMap<>();
	private static final HashMap<String, String> asciiToShortname = new HashMap<>();
	private static final HashMap<String, String> shortnameToUnicode = new HashMap<>();
	private static final HashMap<String, String> unicodeToShortname = new HashMap<>();
	private static final HashMap<String, String> unicodeToHex = new HashMap<>();

	private static Pattern ASCII_PATTERN;
	private static Pattern UNICODE_PATTERN;
	private static Pattern SHORTNAME_PATTERN;

	private static final EmojiOne INSTANCE = new EmojiOne();

	public static EmojiOne getInstance() {
		return INSTANCE;
	}

	private EmojiOne() {
		URL url = EmojiOne.class.getResource("/emoji.json");
		Json j = Json.read(url);
		Map<String, Json> map = j.asJsonMap();
		map.forEach((key, value) -> {
			Map<String, Json> valueMap = value.asJsonMap();
			String name = valueMap.get("name").asString();
			String shortname = valueMap.get("shortname").asString();
			List<String> unicodes = valueMap.get("unicode_alternates").asList().stream().map(o -> (String) o).collect(Collectors.toList());
			unicodes.add(valueMap.get("unicode").asString());
			List<String> aliases = valueMap.get("aliases").asList().stream().map(o -> (String) o).collect(Collectors.toList());
			List<String> aliases_ascii = valueMap.get("aliases_ascii").asList().stream().map(o -> (String) o).collect(Collectors.toList());
			List<String> keywords = valueMap.get("keywords").asList().stream().map(o -> (String) o).distinct().collect(Collectors.toList());
			String category = valueMap.get("category").asString();
			int emojiOrder = valueMap.get("emoji_order").asInteger();

			EmojiEntry entry = new EmojiEntry();
			entry.setName(name);
			entry.setShortname(shortname);
			entry.setUnicodes(unicodes);
			entry.setAliases(aliases);
			entry.setAliasesAscii(aliases_ascii);
			entry.setKeywords(keywords);
			entry.setCategory(category);
			entry.setEmojiOrder(emojiOrder);
			EmojiOne.emojiList.put(shortname, entry);
		});


		EmojiOne.emojiList.forEach((shortname, entry) -> {
			entry.getUnicodes().forEach(unicode -> {
				if(unicode == null || unicode.isEmpty()) return;
				unicodeToHex.put(convert(unicode), unicode);
				shortnameToUnicode.put(shortname, convert(unicode));
				unicodeToShortname.put(convert(unicode), shortname);
			});
			entry.getAliasesAscii().forEach(ascii -> asciiToShortname.put(ascii, shortname));

		});


		ASCII_PATTERN = Pattern.compile(String.join("|", asciiToShortname.keySet().stream().map(Pattern::quote).collect(Collectors.toList())));
		SHORTNAME_PATTERN = Pattern.compile(String.join("|", emojiList.keySet().stream().collect(Collectors.toList())));
		UNICODE_PATTERN = Pattern.compile(String.join("|", unicodeToHex.keySet().stream().map(Pattern::quote).collect(Collectors.toList())));

	}

	public Queue<Object> toEmojiAndText(String str) {
		Queue<Object> queue = new LinkedList<>();
		String unicodeStr = shortnameToUnicode(str);
		Matcher matcher = UNICODE_PATTERN.matcher(unicodeStr);
		int lastEnd = 0;
		while (matcher.find()) {
			String lastText = unicodeStr.substring(lastEnd, matcher.start());
			if (!lastText.isEmpty())
				queue.add(lastText);
			String m = matcher.group();
			String hexStr = emojiList.get(unicodeToShortname.get(m)).getLastUnicode();
			if (hexStr == null || hexStr.isEmpty()) {
				queue.add(m);
			} else {
				queue.add(new Emoji(unicodeToShortname.get(m), m, hexStr));
			}
			lastEnd = matcher.end();
		}
		String lastText = unicodeStr.substring(lastEnd);
		if (!lastText.isEmpty())
			queue.add(lastText);
		return queue;
	}

	private String shortnameToUnicode(String str) {
		String output = replaceWithFunction(str, SHORTNAME_PATTERN, (shortname) -> {
			if (shortname == null || shortname.isEmpty() || (!emojiList.containsKey(shortname))) {
				return shortname;
			}
			if (emojiList.get(shortname).getUnicodes().isEmpty()) {
				return shortname;
			}

			String unicode = emojiList.get(shortname).getLastUnicode().toUpperCase();
			return convert(unicode);
		});

		if (USE_ASCII) {
			output = replaceWithFunction(output, ASCII_PATTERN, (ascii) -> {
				String shortname = asciiToShortname.get(ascii);
				String unicode = emojiList.get(shortname).getLastUnicode().toUpperCase();
				return convert(unicode);
			});
		}

		return output;
	}

	private String replaceWithFunction(String input, Pattern pattern, Function<String, String> func) {
		StringBuilder builder = new StringBuilder();
		Matcher matcher = pattern.matcher(input);
		int lastEnd = 0;
		while (matcher.find()) {
			String lastText = input.substring(lastEnd, matcher.start());
			builder.append(lastText);
			builder.append(func.apply(matcher.group()));
			lastEnd = matcher.end();
		}
		builder.append(input.substring(lastEnd));
		return builder.toString();
	}

	private String convert(String unicodeStr) {
		if (unicodeStr.isEmpty()) return unicodeStr;
		String[] parts = unicodeStr.split("-");
		StringBuilder buff = new StringBuilder();
		for (String s : parts) {
			int part = Integer.parseInt(s, 16);
			if (part >= 0x10000 && part <= 0x10FFFF) {
				int hi = (int) (Math.floor((part - 0x10000) / 0x400) + 0xD800);
				int lo = ((part - 0x10000) % 0x400) + 0xDC00;
				buff.append(new String(Character.toChars(hi))).append(new String(Character.toChars(lo)));
			} else {
				buff.append(new String(Character.toChars(part)));
			}
		}
		return buff.toString();
	}

	class EmojiEntry {
		private String name;
		private String shortname;
		private List<String> unicodes;
		private List<String> aliases;
		private List<String> aliasesAscii;
		private List<String> keywords;
		private String category;
		private int emojiOrder;

		public EmojiEntry() {}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getShortname() {
			return shortname;
		}

		public void setShortname(String shortname) {
			this.shortname = shortname;
		}

		public List<String> getUnicodes() {
			return unicodes;
		}

		public void setUnicodes(List<String> unicodes) {
			this.unicodes = unicodes;
		}

		public String getLastUnicode() {
			if (unicodes.isEmpty()) return null;
			return unicodes.get(unicodes.size() - 1);
		}

		public void setAliases(List<String> aliases) {
			this.aliases = aliases;
		}

		public List<String> getAliasesAscii() {
			return aliasesAscii;
		}

		public void setAliasesAscii(List<String> aliasesAscii) {
			this.aliasesAscii = aliasesAscii;
		}

		public void setKeywords(List<String> keywords) {
			this.keywords = keywords;
		}

		public void setCategory(String category) {
			this.category = category;
		}

		public void setEmojiOrder(int emojiOrder) {
			this.emojiOrder = emojiOrder;
		}
	}

}
