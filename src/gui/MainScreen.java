package gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import main.Game;
import enums.GameState.State;

public class MainScreen extends Screen {
	private static final long serialVersionUID = 1L;
	private boolean hoverPlay;
	private boolean hoverHelp;
	private boolean hoverExit;
	private Rectangle play;
	private Rectangle help;
	private Rectangle exit;
	// Used for pulsating hand.
	private float colour = 30;
	private boolean decrease;

	public MainScreen(Game game) {
		super(game);
	}

	public void render(Graphics g) {
		Graphics2D g2D = (Graphics2D) g;

		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		// Make the background black
		g2D.setColor(Color.BLACK);
		g2D.fillRect(0, 0, game.getDisplay().getFrame().getWidth(), game
				.getDisplay().getFrame().getHeight());
		g.drawRect(0, 0, game.getDisplay().getFrame().getWidth(), game
				.getDisplay().getFrame().getHeight());
		// Draws the hand
		// Pulsates the hand
		g.setColor(new Color((int) colour, 0, 0));
		if (!decrease || colour == 30) {
			decrease = false;
			colour += 0.5;
		} else {
			colour -= 0.5;
		}
		if (colour == 160) {
			decrease = true;
		}
		g2D.setFont(game.getZombieFontL());
		g2D.drawString("}", 375, 260);
		// Draw title
		g.drawImage(game.getMainMenu(), 0, 0, null);
		// Draw play button
		g2D.setFont(game.getUiFont());
		FontMetrics fm = g2D.getFontMetrics();

		button(g2D, hoverPlay, play, "PLAY", 512 - fm.stringWidth("PLAY") / 2,
				367, 460, 390);
		button(g2D, hoverHelp, help, "HELP", 512 - fm.stringWidth("HELP") / 2,
				487, 460, 510);
		button(g2D, hoverExit, exit, "QUIT", 512 - fm.stringWidth("QUIT") / 2,
				607, 460, 630);
		// Displays the current level
		g2D.setColor(Color.WHITE);
		g2D.setFont(game.getUiFontS());
		g2D.drawString("LEVEL: " + game.getLevel(), 5, 25);
		// Credits
		g2D.setFont(game.getUiFontXS());
		g2D.drawString(
				"Ver. 1.0 CREATED BY ALLEN HAN, ALOSHA REYMER, ERIC CHEE, & PATRICK LIU",
				680, 760);
	}

	public void update() {
		if (play.contains(game.getDisplay().getMouseHandler()
				.getMouseLocation())) {
			hoverPlay = true;
			if (game.getDisplay().getMouseHandler().isClick()) {
				game.getState().setState(State.INGAME, false);
				game.getDisplay().getMouseHandler().setClick(false);
			}
		} else {
			hoverPlay = false;
		}
		if (help.contains(game.getDisplay().getMouseHandler()
				.getMouseLocation())) {
			hoverHelp = true;
			if (game.getDisplay().getMouseHandler().isClick()) {
				game.getState().setState(State.HELP, false);
				game.getDisplay().getMouseHandler().setClick(false);
			}
		} else {
			hoverHelp = false;
		}
		if (exit.contains(game.getDisplay().getMouseHandler()
				.getMouseLocation())) {
			hoverExit = true;
			if (game.getDisplay().getMouseHandler().isClick()) {
				System.exit(0);
			}
		} else {
			hoverExit = false;
		}
		game.getDisplay().getMouseHandler().setClick(false);
	}

	public void setup(Game game) {
		this.game = game;
		play = new Rectangle(412, 300, 200, 100);
		help = new Rectangle(412, 420, 200, 100);
		exit = new Rectangle(412, 540, 200, 100);
	}
}