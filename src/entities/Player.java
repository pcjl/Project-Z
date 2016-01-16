package entities;

import items.Consumable;
import items.Firearm;
import items.Item;
import items.Melee;
import items.Throwable;

import java.awt.Graphics;
import java.awt.Point;

import main.Game;
import map.Map;
import utilities.Assets;
import utilities.GameCamera;
import utilities.KeyHandler;
import utilities.MouseHandler;
import utilities.World;
import enums.ItemState;

/**
 * Subclass of Mob that represents a player in Project Z.
 * 
 * @author Allen Han, Alosha Reymer, Eric Chee, Patrick Liu
 * @see Mob
 * @since 1.0
 * @version 1.0
 */
public class Player extends Mob {
	public static final int MOVEMENT_SPEED = 2;
	public static final int MAX_STAMINA = 300;
	public static final int MIN_STAMINA = MAX_STAMINA / 10;
	public static final int SPRINT_COST = Player.MAX_STAMINA / 300;
	private MouseHandler mouse;
	private GameCamera camera;
	private KeyHandler key;
	private World world;
	private boolean exhausted = false;
	private int stamina;

	private int selectedItem = 0;

	public Player(boolean solid, Game game) {
		super(solid, game);
		this.movementSpeed = Player.MOVEMENT_SPEED;
		this.stamina = Player.MAX_STAMINA;
		this.mouse = game.getDisplay().getMouseHandler();
	}

	public Player(Point position, boolean solid, Game game, Map map) {
		super(32, 32, position, solid, game, map);
		this.movementSpeed = Player.MOVEMENT_SPEED;
		this.stamina = Player.MAX_STAMINA;
		addItem(new Consumable((Consumable) this.game.getItems().get(0)));
		addItem(new Consumable((Consumable) this.game.getItems().get(1)));
		addItem(new Melee((Melee) this.game.getItems().get(2)));
		addItem(new Firearm((Firearm) this.game.getItems().get(3)));
		addItem(new Throwable((Throwable) this.game.getItems().get(4)));
		this.mouse = game.getDisplay().getMouseHandler();
		this.camera = game.getCamera();
		this.key = game.getDisplay().getKeyHandler();
		this.world = game.getDisplay().getGamePanel().getWorld();
	}

	public int getStamina() {
		return this.stamina;
	}

	public void setStamina(int stamina) {
		this.stamina = stamina;
	}

	public int getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(int selectedItem) {
		this.selectedItem = selectedItem;
	}

	@Override
	public void render(Graphics g) {
		g.drawImage(this.getImages()[0],
				(int) (this.getPosition().x - camera.getxOffset()),
				(int) (this.getPosition().y - camera.getyOffset()), null);
	}

	// TODO Getters & setters VS protected?
	// Reorganize code; looks messy
	public void update() {
		if (this.stamina < Player.SPRINT_COST) {
			this.exhausted = true;
		} else if (this.stamina > Player.MIN_STAMINA) {
			this.exhausted = false;
		}

		if (mouse.isClick()) {
			useItem();
			mouse.setClick(false);
		}

		if (key.isQ()) {
			dropItem();
			key.setQ(false);
		}

		if (key.isE()) {
			pickUpItem();
			key.setE(false);
		}

		if (key.isShift()
				&& !exhausted
				&& (key.isUp() || key.isDown() || key.isLeft() || key.isRight())) {
			this.movementSpeed = Player.MOVEMENT_SPEED * 2;
			this.stamina -= Player.SPRINT_COST;
		} else {
			this.movementSpeed = Player.MOVEMENT_SPEED;
			if (this.stamina < Player.MAX_STAMINA) {
				this.stamina++;
			}
		}

		key.setLastNumber(key.getLastNumber() + mouse.getMouseWheel());
		mouse.setMouseWheel(0);

		this.selectedItem = key.getLastNumber();

		this.getPosition().setLocation(this.getPosition().getX(),
				this.getPosition().getY() + yMove());
		this.getPosition().setLocation(this.getPosition().getX() + xMove(),
				this.getPosition().getY());
		if (position.getX() < 0)
			position.setLocation(0, position.getY());
		else if (position.getX() > Assets.TILE_WIDTH * (world.getWidth() - 1))
			position.setLocation(Assets.TILE_WIDTH * (world.getWidth() - 1),
					position.getY());
		if (position.getY() < 0)
			position.setLocation(position.getX(), 0);
		else if (position.getY() + 32 > Assets.TILE_HEIGHT
				* (world.getHeight() - 1))
			position.setLocation(position.getX(),
					Assets.TILE_HEIGHT * (world.getHeight() - 1) - 32);

		this.game.getCamera().centerOnEntity(this);

		if (key.isUp() || key.isDown() || key.isRight() || key.isLeft()) {
			if (key.isShift())
				makeNoise(400, true);
			else
				makeNoise(300, true);
		}
	}

	private int xMove() {
		int xMove = 0;
		if (key.isLeft()) {
			xMove = -this.movementSpeed;
		}
		if (key.isRight()) {
			xMove = this.movementSpeed;
		}
		int playerX = (int) (this.getPosition().x - camera.getxOffset());
		int playerY = (int) (this.getPosition().y - camera.getyOffset());
		if (xMove > 0) {// Moving right
			for (int j = 0; j < world.getSolid().length; j++) {
				for (int i = 0; i < world.getSolid()[0].length; i++) {
					if (world.getSolid()[j][i] != null) {
						if (playerX + xMove + Assets.TILE_WIDTH >= world
								.getSolid()[j][i].getX()
								&& playerX + xMove + Assets.TILE_WIDTH <= world
										.getSolid()[j][i].getX() + 32
								&& ((playerY >= world.getSolid()[j][i].getY() && playerY <= world
										.getSolid()[j][i].getY() + 32) || (playerY
										+ Assets.TILE_HEIGHT >= world
											.getSolid()[j][i].getY() && playerY
										+ Assets.TILE_HEIGHT <= world
										.getSolid()[j][i].getY() + 32))) {
							this.getPosition().setLocation(
									world.getSolid()[j][i].getX() - 40,
									this.getPosition().getY());
							xMove = 0;
						}
					}
				}
			}
		} else if (xMove < 0) {// Moving Left
			for (int j = 0; j < game.getDisplay().getGamePanel().getWorld()
					.getSolid().length; j++) {
				for (int i = 0; i < game.getDisplay().getGamePanel().getWorld()
						.getSolid()[0].length; i++) {
					if (game.getDisplay().getGamePanel().getWorld().getSolid()[j][i] != null) {
						if (playerX + xMove >= game.getDisplay().getGamePanel()
								.getWorld().getSolid()[j][i].getX()
								&& playerX + xMove <= game.getDisplay()
										.getGamePanel().getWorld().getSolid()[j][i]
										.getX() + 32
								&& ((playerY >= game.getDisplay()
										.getGamePanel().getWorld().getSolid()[j][i]
										.getY() && playerY <= game.getDisplay()
										.getGamePanel().getWorld().getSolid()[j][i]
										.getY() + 32) || (playerY
										+ Assets.TILE_HEIGHT >= game
										.getDisplay().getGamePanel().getWorld()
										.getSolid()[j][i].getY() && playerY
										+ Assets.TILE_HEIGHT <= game
										.getDisplay().getGamePanel().getWorld()
										.getSolid()[j][i].getY() + 32))) {
							this.getPosition().setLocation(
									game.getDisplay().getGamePanel().getWorld()
											.getSolid()[j][i].getX() + 40,
									this.getPosition().getY());
							xMove = 0;
						}
					}
				}
			}
		}
		return xMove;
	}

	private int yMove() {
		int yMove = 0;
		if (this.game.getDisplay().getKeyHandler().isUp()) {
			yMove = -this.movementSpeed;
		}
		if (this.game.getDisplay().getKeyHandler().isDown()) {
			yMove = this.movementSpeed;
		}
		int playerX = (int) (this.getPosition().x - this.game.getCamera()
				.getxOffset());
		int playerY = (int) (this.getPosition().y - this.game.getCamera()
				.getyOffset());
		if (yMove < 0) {// Moving up
			for (int j = 0; j < game.getDisplay().getGamePanel().getWorld()
					.getSolid().length; j++) {
				for (int i = 0; i < game.getDisplay().getGamePanel().getWorld()
						.getSolid()[0].length; i++) {
					if (game.getDisplay().getGamePanel().getWorld().getSolid()[j][i] != null) {
						if (playerY + yMove >= game.getDisplay().getGamePanel()
								.getWorld().getSolid()[j][i].getY()
								&& playerY + yMove <= game.getDisplay()
										.getGamePanel().getWorld().getSolid()[j][i]
										.getY() + Assets.TILE_HEIGHT
								&& ((playerX >= game.getDisplay()
										.getGamePanel().getWorld().getSolid()[j][i]
										.getX() && playerX <= game.getDisplay()
										.getGamePanel().getWorld().getSolid()[j][i]
										.getX() + Assets.TILE_WIDTH) || (playerX
										+ Assets.TILE_WIDTH >= game
										.getDisplay().getGamePanel().getWorld()
										.getSolid()[j][i].getX() && playerX
										+ Assets.TILE_WIDTH <= game
										.getDisplay().getGamePanel().getWorld()
										.getSolid()[j][i].getX()
										+ Assets.TILE_WIDTH))) {
							this.getPosition().setLocation(
									this.getPosition().getX(),
									game.getDisplay().getGamePanel().getWorld()
											.getSolid()[j][i].getY() + 40);
							yMove = 0;
						}
					}
				}
			}
		} else if (yMove > 0) {// Moving down
			for (int j = 0; j < game.getDisplay().getGamePanel().getWorld()
					.getSolid().length; j++) {
				for (int i = 0; i < game.getDisplay().getGamePanel().getWorld()
						.getSolid()[0].length; i++) {
					if (game.getDisplay().getGamePanel().getWorld().getSolid()[j][i] != null) {
						if (playerY + yMove + Assets.TILE_HEIGHT >= game
								.getDisplay().getGamePanel().getWorld()
								.getSolid()[j][i].getY()
								&& playerY + yMove + Assets.TILE_HEIGHT <= game
										.getDisplay().getGamePanel().getWorld()
										.getSolid()[j][i].getY()
										+ Assets.TILE_HEIGHT
								&& ((playerX >= game.getDisplay()
										.getGamePanel().getWorld().getSolid()[j][i]
										.getX() && playerX <= game.getDisplay()
										.getGamePanel().getWorld().getSolid()[j][i]
										.getX() + Assets.TILE_WIDTH) || (playerX
										+ Assets.TILE_WIDTH >= game
										.getDisplay().getGamePanel().getWorld()
										.getSolid()[j][i].getX() && playerX
										+ Assets.TILE_WIDTH <= game
										.getDisplay().getGamePanel().getWorld()
										.getSolid()[j][i].getX()
										+ Assets.TILE_WIDTH))) {
							this.getPosition().setLocation(
									this.getPosition().getX(),
									game.getDisplay().getGamePanel().getWorld()
											.getSolid()[j][i].getY() - 40);
							yMove = 0;
						}
					}
				}
			}
		}
		return yMove;
	}

	public void useItem() {
		Item item = getItem(this.selectedItem);

		if (item == null) {
			return;
		}
		if (item instanceof Consumable) {
			Consumable newItem = (Consumable) item;
			switch (newItem.getEffect()) {
			case HEAL:
				if (this.health < 100) {
					this.health = Math.min(100,
							this.health + newItem.getEffectValue());
					newItem.removeDurability();
					if (newItem.getDurability() <= 0) {
						this.removeItem(this.selectedItem);
					}
				}
				break;
			case AMMO:
				for (int itemNo = 0; itemNo < Inventory.NO_OF_ITEMS; itemNo++) {
					Item currentItem = getItem(itemNo);
					if (currentItem instanceof Firearm) {
						Firearm firearm = (Firearm) currentItem;

						if (firearm.getAmmoID() == newItem.getItemID()
								&& !firearm.isFull()) {
							firearm.setCurrentAmmo(firearm.getMaxAmmo());
							newItem.removeDurability();
							if (newItem.getDurability() <= 0) {
								this.removeItem(this.selectedItem);
							}
						}
					}
				}
				break;
			}
		} else if (item instanceof Melee) {

		} else if (item instanceof Firearm) {
			Firearm newItem = (Firearm) item;
			double angle = Math.atan2((position.y + 16 - game.getCamera()
					.getyOffset())
					- game.getDisplay().getMouseHandler().getMouseLocation().y,
					(position.x + 16 - game.getCamera().getxOffset())
							- game.getDisplay().getMouseHandler()
									.getMouseLocation().x)
					- Math.PI / 2;

			if (!newItem.isEmpty()) {
				newItem.removeAmmo();
			}
		} else if (item instanceof Throwable) {

		}
	}

	public void dropItem() {
		Item item = getItem(this.selectedItem);
		if (item != null) {
			item.setPosition(new Point(this.position.x, this.position.y));
			item.setState(ItemState.DROPPED);
			removeItem(this.selectedItem);
			this.chunkMap[this.position.x / 512][this.position.y / 512]
					.add(item);
		}
	}

	public void pickUpItem() {
		Item hoverItem = this.game.getDisplay().getGamePanel().getWorld()
				.getHoverItem();

		if (hoverItem != null
				&& !isFull()
				&& Math.sqrt(Math.pow(
						(this.position.x - hoverItem.getPosition().x), 2)
						+ Math.pow(
								(this.position.y - hoverItem.getPosition().y),
								2)) <= 1 * 32) {
			hoverItem.setState(ItemState.INVENTORY);
			this.chunkMap[hoverItem.getPosition().x / 512][hoverItem
					.getPosition().y / 512].remove(hoverItem);
			addItem(hoverItem);
		}
	}
}
