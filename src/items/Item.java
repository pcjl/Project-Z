package items;

import java.applet.AudioClip;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import main.Game;
import entities.Player;
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
	protected boolean inHand;

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

	protected Point position;
	protected boolean held = false;
	protected ItemState state;
	protected boolean hover = true;

	protected BufferedImage[] images;
	protected AudioClip[] clips;

	protected Game game;
	
	
	public abstract void use(Player player);


	// TODO Add effectValue?
	public Item(int itemID, String name, int rarity, int effectValue, ItemState state, BufferedImage[] images,
			AudioClip[] clips, Game game) {
		this.itemID = itemID;
		this.name = name;
		this.rarity = rarity;
		this.effectValue = effectValue;

		this.state = state;

		this.images = images;
		this.clips = clips;

		this.game = game;
		this.inHand=false;
	}

	public Item(Item item) {
		this.itemID = item.itemID;
		this.name = item.name;
		this.rarity = item.rarity;
		this.effectValue = item.effectValue;

		this.state = item.state;

		this.images = item.images;
		this.clips = item.clips;

		this.game = item.game;
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

	public Color getColour() {
		switch (this.rarity) {
		case 5:
			return new Color(173, 173, 173);
		case 4:
			return new Color(0, 54, 255);
		case 3:
			return new Color(246, 195, 72);
		case 2:
			return new Color(246, 146, 72);
		case 1:
			return new Color(0, 225, 0);
		}

		return null;
	}

	public int getEffectValue() {
		return this.effectValue;
	}

	public void setEffectValue(int effectValue) {
		this.effectValue = effectValue;
	}

	public Point getPosition() {
		return this.position;
	}

	public void setPosition(Point position) {
		this.position = position;
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

	public boolean isHover() {
		return hover;
	}

	public void setHover(boolean hover) {
		this.hover = hover;
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

	public Game getGame() {
		return this.game;
	}
	

	public boolean isInHand() {
		return inHand;
	}


	public void setInHand(boolean inHand) {
		this.inHand = inHand;
	}


	public void render(Graphics g) {
		if (this.state == ItemState.DROPPED) {
			if (!hover) {
				g.drawImage(this.getImages()[0], (int) (this.getPosition().x - this.game.getCamera().getxOffset()),
						(int) (this.getPosition().y - this.game.getCamera().getyOffset()), null);
			} else {
				g.drawImage(this.getImages()[1], (int) (this.getPosition().x - this.game.getCamera().getxOffset()),
						(int) (this.getPosition().y - this.game.getCamera().getyOffset()), null);
			}
		} else if (this.state == ItemState.IN_HAND) {
			g.drawImage(this.getImages()[2], 0, 0, null);
	
		}
	}
}