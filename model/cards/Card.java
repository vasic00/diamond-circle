package model.cards;

import java.io.File;

public abstract class Card {
	
	private static final String PATH_PREFIX = "resources" + File.separator + "images" + File.separator + "card_";
	public static final String invalidCardPath = "resources" + File.separator + "images" + File.separator + "no_card.png";
	public static final String invalidCardText = "INVALID";
	private final int value;
	private final String path;
	
	public Card(int n, String suffix)
	{
		value = n;
		path = PATH_PREFIX + suffix + ".png";
	}
	
	public int getValue()
	{
		return value;
	}

	public String getPath() {
		return path;
	}
}
