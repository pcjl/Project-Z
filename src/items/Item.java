package items;

import java.applet.AudioClip;
import java.awt.Point;
import java.awt.image.BufferedImage;

import enums.ItemState;

/**
 * Abstract Item class for all items in Project Z.
 * 
 * @author Patrick Liu, Eric Chee, Allen Han, Alosha Reymer
 * @since 1.0
 * @version 1.0
 */
public abstract class Item {
	protected int itemID;
	protected String name;

	/**
	 * Integer that decides how rare it is to find this item in a map.<br>
	 * This value ranges from 1-5, where 1 is the most rare and 5 is the most
	 * common.<br>
	 * Colour of the item's name will change depending on rarity as well.<br>
	 * Grey (common) = 5<br>
	 * Blue (common?) = 4<br>
	 * Yellow (rare) = 3<br>
	 * Orange (?) = 2<br>
	 * Green (?) = 1<br>
	 * *Colours are subject to change.
	 */
	protected int rarity;

	/**
	 * The value of the effect of an item.<br>
	 * If the item is a weapon (melee, firearm, some throwables), then the
	 * effect value is the damage of the weapon.
	 */
	protected int effectValue;

	protected Point location;
	protected boolean held = false;
	protected ItemState state;

	protected BufferedImage[] images;
	protected AudioClip[] clips;

	// TODO Add effectValue?
	public Item(int itemID, String name, int rarity, int effectValue,
			ItemState state, BufferedImage[] images, AudioClip[] clips) {
		this.itemID = itemID;
		this.name = name;
		this.rarity = rarity;

		this.state = state;

		this.images = images;
		this.clips = clips;
	}

	public int getItemID() {
		return this.itemID;
	}

	public void setItemID(int itemID) {
		this.itemID = itemID;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getRarity() {
		return this.rarity;
	}

	public void setRarity(int rarity) {
		this.rarity = rarity;
	}

	public int getEffectValue() {
		return this.effectValue;
	}

	public void setEffectValue(int effectValue) {
		this.effectValue = effectValue;
	}

	public Point getLocation() {
		return this.location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public boolean isHeld() {
		return this.held;
	}

	public void setHeld(boolean held) {
		this.held = held;
	}

	public ItemState getState() {
		return this.state;
	}

	public void setState(ItemState state) {
		this.state = state;
	}

	public BufferedImage[] getImages() {
		return images;
	}

	public void setImages(BufferedImage[] images) {
		this.images = images;
	}

	public AudioClip[] getClips() {
		return clips;
	}

	public void setClips(AudioClip[] clips) {
		this.clips = clips;
	}
}