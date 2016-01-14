package entities;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import utilities.Assets;
import main.Game;
import map.Map;

/**
 * Subclass of Mob that represents a player in Project Z.
 * 
 * @author Allen Han, Alosha Reymer, Eric Chee, Patrick Liu
 * @see Mob
 * @since 1.0
 * @version 1.0
 */
public class Player extends Mob {
	public static final int MOVEMENT_SPEED = 3;
	public static final int MAX_STAMINA = 300;
	public static final int SPRINT_COST = 3;
	// *************THIS SHOULD BE PUT SOMEWHERE MORE APPROPRIATE***************
	// WHAT IS BOUNDS IN ENTITY? G
	// - ALLEN
	private Rectangle hitbox;
	private int stamina;

	private int selectedItem = 0;

	public Player(boolean solid, Game game) {
		super(solid, game);
		this.movementSpeed = Player.MOVEMENT_SPEED;
		this.stamina = Player.MAX_STAMINA;
	}

	public Player(Point position, boolean solid, Game game, Map map) {
		super(32, 32, position, solid, game, map);
		this.movementSpeed = Player.MOVEMENT_SPEED;
		this.stamina = Player.MAX_STAMINA;
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
				(int) (this.getPosition().x - this.game.getCamera()
						.getxOffset()), (int) (this.getPosition().y - this.game
						.getCamera().getyOffset()), null);
	}

	// TODO Getters & setters VS protected?
	// Reorganize code; looks messy
	public void update() {
		this.selectedItem = this.game.getDisplay().getKeyHandler()
				.getLastNumber();

		if (this.game.getDisplay().getKeyHandler().isShift()
				&& this.stamina > Player.SPRINT_COST) {
			this.movementSpeed = Player.MOVEMENT_SPEED * 2;
			this.stamina -= Player.SPRINT_COST;
		} else {
			this.movementSpeed = Player.MOVEMENT_SPEED;
			if (this.stamina < Player.MAX_STAMINA) {
				this.stamina++;
			}
		}

		if (this.game.getDisplay().getKeyHandler().isUp()) {
			this.getPosition().setLocation(this.getPosition().getX(),
					this.getPosition().getY() - this.movementSpeed);
		}
		if (this.game.getDisplay().getKeyHandler().isDown()) {
			this.getPosition().setLocation(this.getPosition().getX(),
					this.getPosition().getY() + this.movementSpeed);
		}
		if (this.game.getDisplay().getKeyHandler().isLeft()) {
			this.getPosition().setLocation(
					this.getPosition().getX() - this.movementSpeed,
					this.getPosition().getY());
		}
		if (this.game.getDisplay().getKeyHandler().isRight()) {
			this.getPosition().setLocation(
					this.getPosition().getX() + this.movementSpeed,
					this.getPosition().getY());
		}
		if (position.getX() < 0)
			position.setLocation(0, position.getY());
		else if (position.getX() > Assets.TILE_WIDTH
				* (game.getDisplay().getGamePanel().getWorld().getWidth() - 1))
			position.setLocation(
					Assets.TILE_WIDTH
							* (game.getDisplay().getGamePanel().getWorld()
									.getWidth() - 1), position.getY());
		if (position.getY() < 0)
			position.setLocation(position.getX(), 0);
		else if (position.getY() + 32 > Assets.TILE_HEIGHT
				* (game.getDisplay().getGamePanel().getWorld().getHeight() - 1))
			position.setLocation(position.getX(),
					Assets.TILE_HEIGHT
							* (game.getDisplay().getGamePanel().getWorld()
									.getHeight() - 1) - 32);

		this.game.getCamera().centerOnEntity(this);

		if (this.game.getDisplay().getKeyHandler().isUp()
				|| this.game.getDisplay().getKeyHandler().isDown()
				|| this.game.getDisplay().getKeyHandler().isRight()
				|| this.game.getDisplay().getKeyHandler().isLeft()) {
			if (this.game.getDisplay().getKeyHandler().isShift())
				makeNoise(400);
			else
				makeNoise(200);
		}
		collision();
	}

	private void collision() {
		hitbox = new Rectangle(this.getPosition().x, this.getPosition().y,
				Assets.TILE_WIDTH, Assets.TILE_HEIGHT);

		for (int i = 0; i < game.getDisplay().getGamePanel().getWorld()
				.getSolid().length; i++) {
			for (int j = 0; j < game.getDisplay().getGamePanel().getWorld()
					.getSolid()[0].length; j++) {
				if (game.getDisplay().getGamePanel().getWorld().getSolid()[i][j] != null) {
					if (hitbox.intersects(game.getDisplay().getGamePanel()
							.getWorld().getSolid()[i][j])) {
						System.out.println("collided "
								+ hitbox.getX()
								+ " "
								+ hitbox.getY()
								+ " HIT "
								+ game.getDisplay().getGamePanel().getWorld()
										.getSolid()[i][j].getX()
								+ " "
								+ game.getDisplay().getGamePanel().getWorld()
										.getSolid()[i][j].getY());
					}
				}
			}
		}
	}
}
