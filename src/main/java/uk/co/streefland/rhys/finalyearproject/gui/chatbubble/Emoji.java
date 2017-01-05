package uk.co.streefland.rhys.finalyearproject.gui.chatbubble;

class Emoji {
	private String shortname;
	private String unicode;
	private String hex;
	private int emojiOrder;

	public Emoji(String shortname, String unicode, String hex) {
		this.shortname = shortname;
		this.unicode = unicode;
		this.hex = hex;
	}

	/**
	 * This is the filename (without extension) of the image
	 * @return Hex representation of the unicode
	 */
	public String getHex() {
		return hex;
	}

	@Override
	public String toString() {
		return "Emoji: [shortname: " +  shortname + ", unicode: " + unicode + ", hex: " + hex + "]";
	}
}
