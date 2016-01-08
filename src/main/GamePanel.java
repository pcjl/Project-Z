package main;

import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JPanel;

import utilities.World;
import entities.Player;

public class GamePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private World world;
	private Player player;
	private boolean setUp;

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (setUp) {
			world.render(g);
			player.render(g);
		}
	}

	public void update() {
		player.update();
	}

	public void setup(Game game) {
		world = new World(game, 1600, 1600);
		player = new Player(game, new Point(0, 0), true);
		player.setImages(game.getPlayer()[0]);
		setUp = true;
	}

	public World getWorld() {
		return world;
	}

	public Player getPlayer() {
		return player;
	}
}