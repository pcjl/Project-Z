package map;

import java.awt.Point;
import java.util.ArrayList;

import utilities.PathFinder;
import entities.MapObject;
import entities.Zombie;
import items.Consumable;
import items.Firearm;
import items.Item;
import items.Melee;
import items.Throwable;
import main.Game;

/**
 * Map of a game of Project Z.
 * 
 * @author Patrick Liu, Eric Chee, Allen Han, Alosha Reymer
 * @see MapObject
 * @since 1.0
 * @version 1.0
 */
public class Map {
	public enum Direction {
		UP, DOWN, RIGHT, LEFT
	};

	// Map parameters
	private int height;
	private int width;

	// Generation
	final int MAX_AREA = 3025;
	final int MAX_ROOM_AREA = 100;
	final int MIN_SIDE_LENGTH = 55;
	final int MIN_BUILD_LENGTH = 15;
	final int BUILD_LENGTH_RANGE = 5;
	final int MAX_BUILD_PER_SIDE = 5;
	final double HEIGHT_WIDTH_RATIO = 0.5;
	final int MAIN_ROAD_SIZE = 17;
	final int ROAD_WIDTH = 11;

	// Values determining the maximum items/zombies per area
	final int MAX_ZOMBIE_PER_ROOM = 2;
	final int MAX_ZOMBIE_PER_FOREST = 10;
	final int MAX_ITEM_PER_ROOM = 1;
	final int MAX_ITEM_PER_FOREST = 5;

	// Map storage
	private short[][] tileMap;
	private short[][] upperTileMap;
	private Chunk[][] chunkMap;
	private Game game;
	private PathFinder pathFinder;
	private int safeHouseDistance;

	// Important points on the map
	private Point playerStart;
	private Point safehouseStart;
	private Point safehouseEnd;
	private Point flagLocation;

	private ArrayList<Item> items;
	private ArrayList<Item> itemSpawns;
	private ArrayList<Point> plazaStarts;
	private ArrayList<Point> plazaEnds;

	/**
	 * Constructs a new Map object.
	 * 
	 * @param height
	 *            height of map to make.
	 * @param width
	 *            width of map to make.
	 * @param game
	 *            the instance of the game.
	 */
	public Map(int height, int width, Game game) {
		// Initializes variables
		this.width = width;
		this.height = height;
		this.game = game;
		this.items = this.game.getItems();
		this.tileMap = new short[width][height];
		this.upperTileMap = new short[width][height];
		this.chunkMap = new Chunk[this.width / 16][this.height / 16];

		safeHouseDistance = (height + width) / 8;
		plazaStarts = new ArrayList<Point>();
		plazaEnds = new ArrayList<Point>();

		// Initializes the array of chunks
		for (int i = 0; i < this.width / 16; i++)
			for (int j = 0; j < this.height / 16; j++)
				chunkMap[i][j] = new Chunk();

		// Sets up the item rarities
		itemSpawns = new ArrayList<Item>();
		for (int item = 0; item < this.items.size(); item++) {
			Item currentItem = this.items.get(item);
			int chance = (int) Math.pow(2, currentItem.getRarity());
			for (int rarity = 0; rarity < chance; rarity++) {
				itemSpawns.add(currentItem);
			}
		}

		// Generate main road
		int mainRoadX = (int) ((width / 4) + Math.random() * (width / 2));
		generateVerticalRoad(mainRoadX, height - 1, MAIN_ROAD_SIZE);

		// Generates all other roads
		generateSideRoads(new Point(0, 0), new Point(mainRoadX - (MAIN_ROAD_SIZE + 1) / 2, height - 1));
		generateSideRoads(new Point(mainRoadX + (MAIN_ROAD_SIZE + 1) / 2, 0), new Point(height - 1, width - 1));

		// Generates locations for the starting safehouse and the target
		// safehouse
		int startHouse = (int) Math.floor((Math.random() * (plazaStarts.size() - 1)));
		int endHouse;
		int count = 0;

		do {
			endHouse = (int) Math.ceil(Math.random() * (plazaStarts.size() - 1));
			count++;
			if (count >= 10) {
				startHouse = (int) Math.floor((Math.random() * (plazaStarts.size() - 1)));
				count = 0;
			}
		} while (Math.abs(plazaStarts.get(startHouse).getX() - plazaStarts.get(endHouse).getX()) < safeHouseDistance
				&& Math.abs(plazaStarts.get(startHouse).getY() - plazaStarts.get(endHouse).getY()) < safeHouseDistance);

		// Generates all of the plazas
		for (int i = 0; i < plazaStarts.size(); i++) {
			if (i == startHouse)
				generateSafehousePlaza(plazaStarts.get(i), plazaEnds.get(i), true);
			else if (i == endHouse) {
				generateSafehousePlaza(plazaStarts.get(i), plazaEnds.get(i), false);
				safehouseStart = plazaStarts.get(i);
				safehouseEnd = plazaEnds.get(i);
			} else
				generatePlaza(plazaStarts.get(i), plazaEnds.get(i));
		}

		pathFinder = new PathFinder(this);
	}

	/**
	 * Generates buildings within the plaza.
	 * 
	 * @param start
	 *            starting point of the plaza.
	 * @param end
	 *            ending point of the plaza.
	 */
	public void generatePlaza(Point start, Point end) {

		// Initializes arrays for the side lengths, starts and ends of the
		// corner buildings
		int[] cornerWidths = new int[4];
		int[] cornerHeights = new int[4];
		Point[] buildingStarts = new Point[4];
		Point[] buildingEnds = new Point[4];

		// Fills the plaza with grass
		for (int i = (int) start.getX(); i <= end.getX(); i++) {
			for (int j = (int) start.getY(); j <= end.getY(); j++) {
				setTile(i, j, 108, Direction.UP, false);
			}
		}

		// Top Left Corner
		cornerWidths[0] = (int) (Math.random() * BUILD_LENGTH_RANGE) + MIN_BUILD_LENGTH;
		cornerHeights[0] = (int) (Math.random() * BUILD_LENGTH_RANGE) + MIN_BUILD_LENGTH;
		buildingStarts[0] = new Point((int) start.getX(), (int) start.getY());
		buildingEnds[0] = new Point((int) start.getX() + cornerWidths[0], (int) start.getY() + cornerHeights[0]);
		generateBuilding(buildingStarts[0], buildingEnds[0], Direction.UP);

		// Top Right Corner
		cornerWidths[1] = (int) (Math.random() * BUILD_LENGTH_RANGE) + MIN_BUILD_LENGTH;
		cornerHeights[1] = (int) (Math.random() * BUILD_LENGTH_RANGE) + MIN_BUILD_LENGTH;
		buildingStarts[1] = new Point((int) (end.getX() - cornerWidths[1]), (int) start.getY());
		buildingEnds[1] = new Point((int) end.getX(), (int) start.getY() + cornerHeights[1]);
		generateBuilding(buildingStarts[1], buildingEnds[1], Direction.RIGHT);

		// Bottom Left Corner
		cornerWidths[2] = (int) (Math.random() * BUILD_LENGTH_RANGE) + MIN_BUILD_LENGTH;
		cornerHeights[2] = (int) (Math.random() * BUILD_LENGTH_RANGE) + MIN_BUILD_LENGTH;
		buildingStarts[2] = new Point((int) start.getX(), (int) end.getY() - cornerHeights[2]);
		buildingEnds[2] = new Point((int) start.getX() + cornerWidths[2], (int) end.getY());
		generateBuilding(buildingStarts[2], buildingEnds[2], Direction.LEFT);

		// Bottom Right Corner
		cornerWidths[3] = (int) (Math.random() * BUILD_LENGTH_RANGE) + MIN_BUILD_LENGTH;
		cornerHeights[3] = (int) (Math.random() * BUILD_LENGTH_RANGE) + MIN_BUILD_LENGTH;
		buildingStarts[3] = new Point((int) end.getX() - cornerWidths[3], (int) end.getY() - cornerHeights[3]);
		buildingEnds[3] = new Point((int) end.getX(), (int) end.getY());
		generateBuilding(buildingStarts[3], buildingEnds[3], Direction.DOWN);

		// Generates the vertical buildings
		generateVerticalBuildings(new Point((int) buildingStarts[0].getX(), (int) buildingEnds[0].getY()),
				buildingStarts[2], 1, MAX_BUILD_PER_SIDE);
		generateVerticalBuildings(buildingEnds[1],
				new Point((int) buildingEnds[3].getX(), (int) buildingStarts[3].getY()), -1, MAX_BUILD_PER_SIDE);

		// Generates the horizontal buildings
		if (buildingEnds[0].getY() <= buildingEnds[1].getY())
			generateHorizontalBuildings(new Point((int) buildingEnds[0].getX(), (int) buildingStarts[0].getY()),
					buildingStarts[1], 1, MAX_BUILD_PER_SIDE, cornerHeights[0] - MIN_BUILD_LENGTH);
		else
			generateHorizontalBuildings(new Point((int) buildingEnds[0].getX(), (int) buildingStarts[0].getY()),
					buildingStarts[1], 1, MAX_BUILD_PER_SIDE, cornerHeights[1] - MIN_BUILD_LENGTH);

		if (buildingStarts[2].getY() >= buildingStarts[3].getY())
			generateHorizontalBuildings(buildingEnds[2],
					new Point((int) buildingStarts[3].getX(), (int) buildingEnds[3].getY()), -1, MAX_BUILD_PER_SIDE,
					cornerHeights[2] - MIN_BUILD_LENGTH);
		else
			generateHorizontalBuildings(buildingEnds[2],
					new Point((int) buildingStarts[3].getX(), (int) buildingEnds[3].getY()), -1, MAX_BUILD_PER_SIDE,
					cornerHeights[3] - MIN_BUILD_LENGTH);

		// Generates the trees for the plaza
		generateTrees(buildingStarts[0], buildingEnds[3]);
	}

	/**
	 * Generates buildings for a column.
	 * 
	 * @param start
	 *            the start of the column.
	 * @param end
	 *            the end of the column.
	 * @param dir
	 *            the direction of the column (1 = West, -1 = East).
	 * @param numToGenerate
	 *            the number of buildings to generate.
	 */
	public void generateVerticalBuildings(Point start, Point end, int dir, int numToGenerate) {
		// Creates variables for the building to be generated
		int sideLength = (int) (end.getY() - start.getY());
		int sideBuildingLength = 0;
		Point sideBuildingStart;
		Point sideBuildingEnd;

		// Generates the last building in a column making sure that it fills up
		// all remaining space
		if (numToGenerate == 1) {
			sideBuildingLength = sideLength;

			// Generates building using different points depending on whether
			// the building is on the left or right of the plaza
			if (dir == 1) {
				sideBuildingStart = new Point((int) end.getX(), (int) start.getY() + 1);
				sideBuildingEnd = new Point(
						(int) end.getX() + dir * ((int) (Math.random() * BUILD_LENGTH_RANGE) + MIN_BUILD_LENGTH),
						(int) start.getY() + sideBuildingLength - 1);
			} else {
				sideBuildingStart = new Point(
						(int) end.getX() + dir * ((int) (Math.random() * BUILD_LENGTH_RANGE) + MIN_BUILD_LENGTH),
						(int) start.getY() + 1);
				sideBuildingEnd = new Point((int) end.getX(), (int) start.getY() + sideBuildingLength - 1);
			}

			generateBuilding(sideBuildingStart, sideBuildingEnd, Direction.LEFT);
		} else if (sideLength < MIN_BUILD_LENGTH * numToGenerate) {
			// Reruns the method if the specified number of buildings cannot
			// possibly fit into the column
			generateVerticalBuildings(start, end, dir, numToGenerate - 1);
		} else {
			// Generates a length for the building
			do {
				sideBuildingLength = (int) (Math.random() * BUILD_LENGTH_RANGE) + MIN_BUILD_LENGTH;
			} while (sideLength - sideBuildingLength * (numToGenerate - 1) < MIN_BUILD_LENGTH);

			// Generates building using different points depending on whether
			// the building is on the left or right of the plaza
			if (dir == 1) {
				sideBuildingStart = new Point((int) end.getX(), (int) start.getY() + 1);
				sideBuildingEnd = new Point(
						(int) end.getX() + dir * ((int) (Math.random() * BUILD_LENGTH_RANGE) + MIN_BUILD_LENGTH),
						(int) start.getY() + sideBuildingLength);
			} else {
				sideBuildingStart = new Point(
						(int) end.getX() + dir * ((int) (Math.random() * BUILD_LENGTH_RANGE) + MIN_BUILD_LENGTH),
						(int) start.getY() + 1);
				sideBuildingEnd = new Point((int) end.getX(), (int) start.getY() + sideBuildingLength);
			}

			// Creates the building itself
			generateBuilding(sideBuildingStart, sideBuildingEnd, Direction.RIGHT);

			generateVerticalBuildings(new Point((int) sideBuildingStart.getX(), (int) sideBuildingEnd.getY()), end, dir,
					numToGenerate - 1);
		}
	}

	/**
	 * Generates buildings for a row.
	 * 
	 * @param start
	 *            the start of the row.
	 * @param end
	 *            the end of the row.
	 * @param dir
	 *            the direction (1 = North, -1 = South).
	 * @param numToGenerate
	 *            the number of buildings to generate.
	 * @param maxRange
	 *            the maximum height of the buildings.
	 */
	public void generateHorizontalBuildings(Point start, Point end, int dir, int numToGenerate, int maxRange) {
		// Creates variables for the building to be generated
		int sideLength = (int) (end.getX() - start.getX());
		int sideBuildingLength = 0;
		Point sideBuildingStart;
		Point sideBuildingEnd;

		// Generates the last building in a row making sure that it fills up all
		// remaining space
		if (numToGenerate == 1) {
			sideBuildingLength = sideLength;
			// Generates building using different points depending on whether
			// the building is on the Northern or Southern side of the plaza
			if (dir == 1) {
				sideBuildingStart = new Point((int) start.getX() + 1, (int) start.getY());
				sideBuildingEnd = new Point((int) start.getX() + sideBuildingLength - 1,
						(int) start.getY() + dir * ((int) (Math.random() * maxRange) + MIN_BUILD_LENGTH));
			} else {
				sideBuildingStart = new Point((int) start.getX() + 1,
						(int) start.getY() + dir * ((int) (Math.random() * maxRange) + MIN_BUILD_LENGTH));
				sideBuildingEnd = new Point((int) start.getX() + sideBuildingLength - 1, (int) start.getY());
			}

			generateBuilding(sideBuildingStart, sideBuildingEnd, Direction.UP);
		} else if (sideLength < MIN_BUILD_LENGTH * numToGenerate) {
			// Reruns the method if the specified number of buildings cannot
			// possibly fit into the row
			generateHorizontalBuildings(start, end, dir, numToGenerate - 1, maxRange);
		} else {
			// Generates a length for the building
			do {
				sideBuildingLength = (int) (Math.random() * BUILD_LENGTH_RANGE) + MIN_BUILD_LENGTH;
			} while (sideLength - sideBuildingLength * (numToGenerate - 1) < MIN_BUILD_LENGTH);

			// Generates building using different points depending on whether
			// the building is on the Northern or Southern side of the plaza
			if (dir == 1) {
				sideBuildingStart = new Point((int) start.getX() + 1, (int) start.getY());
				sideBuildingEnd = new Point((int) start.getX() + sideBuildingLength,
						(int) start.getY() + dir * ((int) (Math.random() * maxRange) + MIN_BUILD_LENGTH));
			} else {
				sideBuildingStart = new Point((int) start.getX() + 1,
						(int) start.getY() + dir * ((int) (Math.random() * maxRange) + MIN_BUILD_LENGTH));
				sideBuildingEnd = new Point((int) start.getX() + sideBuildingLength, (int) start.getY());
			}

			// Creates the building itself
			generateBuilding(sideBuildingStart, sideBuildingEnd, Direction.DOWN);

			if (dir == 1)
				generateHorizontalBuildings(new Point((int) sideBuildingEnd.getX(), (int) sideBuildingStart.getY()),
						end, dir, numToGenerate - 1, maxRange);
			else
				generateHorizontalBuildings(sideBuildingEnd, end, dir, numToGenerate - 1, maxRange);
		}
	}

	/**
	 * Generates the plaza for the safehouse.
	 * 
	 * @param start
	 *            the start of the safehouse.
	 * @param end
	 *            the end of the safehouse.
	 */
	public void generateSafehousePlaza(Point start, Point end, boolean isStart) {
		// Gets the width and height of the plaza
		int boxWidth = (int) (Math.abs(end.getX() - (start.getX())));
		int boxHeight = (int) (Math.abs(end.getY() - (start.getY())));

		// Fills the plaza with grass
		for (int i = (int) start.getX(); i <= end.getX(); i++) {
			for (int j = (int) start.getY(); j <= end.getY(); j++) {
				setTile(i, j, 108, Direction.UP, false);
			}
		}

		// Generates the safehouse
		generateSafeBuilding(new Point((int) start.getX() + boxWidth / 3, (int) start.getY() + boxHeight / 3),
				new Point((int) end.getX() - boxWidth / 3, (int) end.getY() - boxHeight / 3), Direction.UP, isStart);

		int playerX;
		int playerY;

		// Generates a spawn point for the player
		do {
			int xRange = boxWidth - 2 * (boxWidth / 3);
			int yRange = boxHeight - 2 * (boxHeight / 3);

			int startX = (int) (start.getX() + boxWidth / 3);
			int startY = (int) (start.getY() + boxHeight / 3);

			playerX = (int) (Math.random() * xRange + startX);
			playerY = (int) (Math.random() * yRange + startY);
		} while ((tileMap[playerX][playerY] & 0xFFF) != 201);

		// Sets the spawn point of the player
		if (!isStart)
			playerStart = new Point(playerX, playerY);

		// Generates the Trees inside the plaza
		generateTrees(new Point((int) start.getX() + 1, (int) start.getY() + 1),
				new Point((int) end.getX() - 1, (int) end.getY() - 1));

	}

	/**
	 * Generates the building and its tiles.
	 * 
	 * @param start
	 *            the start of the building.
	 * @param end
	 *            the end of the building.
	 * @param direction
	 *            the direction the building is facing.
	 */
	public void generateBuilding(Point start, Point end, Direction direction) {
		// Fills the given area with the required building tiles
		for (int i = (int) start.getX(); i <= end.getX(); i++) {
			for (int j = (int) start.getY(); j <= end.getY(); j++) {
				if (i == start.getX() || i == end.getX() || j == start.getY() || j == end.getY()) {
					setTile(i, j, 200, Direction.UP, false);
					// Spawns zombies on the building alleys
					if (Math.random() > 0.99) {
						chunkMap[i / 16][j / 16].addZombie(
								new Zombie(new Point(i * 32, j * 32), Zombie.zombieHealth, game.getZombieImages()[0],
										null, this.game, this, (int) Math.floor((Math.random() * 5))));
					}
				} else if (i == start.getX() + 1 && j == start.getY() + 1) {
					setTile(i, j, 203, Direction.DOWN, true);
				} else if (i == start.getX() + 1 && j == end.getY() - 1) {
					setTile(i, j, 203, Direction.RIGHT, true);
				} else if (i == end.getX() - 1 && j == start.getY() + 1) {
					setTile(i, j, 203, Direction.LEFT, true);
				} else if (i == end.getX() - 1 && j == end.getY() - 1) {
					setTile(i, j, 203, Direction.UP, true);
				} else if (i == start.getX() + 1) {
					setTile(i, j, 202, Direction.DOWN, true);
				} else if (j == start.getY() + 1) {
					setTile(i, j, 202, Direction.LEFT, true);
				} else if (i == end.getX() - 1) {
					setTile(i, j, 202, Direction.UP, true);
				} else if (j == end.getY() - 1) {
					setTile(i, j, 202, Direction.RIGHT, true);
				} else
					setTile(i, j, 201, Direction.UP, false);
			}
		}

		// Generates the rooms for the building
		generateRooms(new Point((int) start.getX() + 2, (int) start.getY() + 2),
				new Point((int) end.getX() - 2, (int) end.getY() - 2), false);

		// Creates the doors for the building
		if (direction == Direction.UP || direction == Direction.DOWN) {
			setTile((int) start.getX() + 4, (int) start.getY() + 1, 207, Direction.UP, false);
			setTile((int) start.getX() + 5, (int) start.getY() + 1, 207, Direction.UP, false);

			setTile((int) end.getX() - 4, (int) end.getY() - 1, 207, Direction.UP, false);
			setTile((int) end.getX() - 5, (int) end.getY() - 1, 207, Direction.UP, false);
		} else if (direction == Direction.RIGHT || direction == Direction.LEFT) {
			setTile((int) end.getX() - 1, (int) start.getY() + 4, 207, Direction.UP, false);
			setTile((int) end.getX() - 1, (int) start.getY() + 5, 207, Direction.UP, false);

			setTile((int) start.getX() + 1, (int) end.getY() - 4, 207, Direction.UP, false);
			setTile((int) start.getX() + 1, (int) end.getY() - 5, 207, Direction.UP, false);
		}

	}

	/**
	 * Generates the building and its tiles.
	 * 
	 * @param start
	 *            the start of the safehouse.
	 * @param end
	 *            the end of the safehouse.
	 */
	public void generateSafeBuilding(Point start, Point end, Direction direction, boolean isStart) {
		// Fills the given area with the required safehouse building tiles
		for (int i = (int) start.getX(); i <= end.getX(); i++) {
			for (int j = (int) start.getY(); j <= end.getY(); j++) {
				if (i == start.getX() || i == end.getX() || j == start.getY() || j == end.getY()) {
					setTile(i, j, 200, Direction.UP, false);
				} else if (i == start.getX() + 1 && j == start.getY() + 1) {
					setTile(i, j, 209, Direction.DOWN, true);
				} else if (i == start.getX() + 1 && j == end.getY() - 1) {
					setTile(i, j, 209, Direction.RIGHT, true);
				} else if (i == end.getX() - 1 && j == start.getY() + 1) {
					setTile(i, j, 209, Direction.LEFT, true);
				} else if (i == end.getX() - 1 && j == end.getY() - 1) {
					setTile(i, j, 209, Direction.UP, true);
				} else if (i == start.getX() + 1) {
					setTile(i, j, 208, Direction.DOWN, true);
				} else if (j == start.getY() + 1) {
					setTile(i, j, 208, Direction.LEFT, true);
				} else if (i == end.getX() - 1) {
					setTile(i, j, 208, Direction.UP, true);
				} else if (j == end.getY() - 1) {
					setTile(i, j, 208, Direction.RIGHT, true);
				} else
					setTile(i, j, 201, Direction.UP, false);
			}
		}

		// Generates rooms for the safehouse
		generateRooms(new Point((int) start.getX() + 2, (int) start.getY() + 2),
				new Point((int) end.getX() - 2, (int) end.getY() - 2), true);

		// Creates the doors of the safehouse
		if (direction == Direction.UP || direction == Direction.DOWN) {
			setTile((int) start.getX() + 4, (int) start.getY() + 1, 207, Direction.UP, false);
			setTile((int) start.getX() + 5, (int) start.getY() + 1, 207, Direction.UP, false);

			setTile((int) end.getX() - 4, (int) end.getY() - 1, 207, Direction.UP, false);
			setTile((int) end.getX() - 5, (int) end.getY() - 1, 207, Direction.UP, false);
		} else if (direction == Direction.RIGHT || direction == Direction.LEFT) {
			setTile((int) end.getX() - 1, (int) start.getY() + 4, 207, Direction.UP, false);
			setTile((int) end.getX() - 1, (int) start.getY() + 5, 207, Direction.UP, false);

			setTile((int) start.getX() + 1, (int) end.getY() - 4, 207, Direction.UP, false);
			setTile((int) start.getX() + 1, (int) end.getY() - 5, 207, Direction.UP, false);
		}
		// Spawns the victory flag in any corner of the building
		if (isStart) {
			int corner = (int) Math.floor((Math.random() * 4));

			if (corner == 0) {
				setTile((int) start.getX() + 2, (int) start.getY() + 2, 211, Direction.DOWN, false);
				flagLocation = new Point((int) start.getX() + 2, (int) start.getY() + 2);
			} else if (corner == 1) {
				setTile((int) start.getX() + 2, (int) end.getY() - 2, 211, Direction.DOWN, false);
				flagLocation = new Point((int) start.getX() + 2, (int) end.getY() - 2);
			} else if (corner == 2) {
				setTile((int) end.getX() - 2, (int) start.getY() + 2, 211, Direction.DOWN, false);
				flagLocation = new Point((int) end.getX() - 2, (int) start.getY() + 2);
			} else if (corner == 3) {
				setTile((int) end.getX() - 2, (int) end.getY() - 2, 211, Direction.DOWN, false);
				flagLocation = new Point((int) end.getX() - 2, (int) end.getY() - 2);
			}
		}

	}

	/**
	 * Generates rooms in each building.
	 * 
	 * @param start
	 *            the start of the room.
	 * @param end
	 *            the end of the room.
	 * @param safe
	 *            if the room is safe or not.
	 */
	public void generateRooms(Point start, Point end, boolean safe) {
		// Gets the width and height of the area
		int boxWidth = (int) (Math.abs(end.getX() - (start.getX())));
		int boxHeight = (int) (Math.abs(end.getY() - (start.getY())));

		// Generates a wall and then checks to see if a the two rooms created
		// can be split into more rooms
		if (boxWidth * boxHeight > MAX_ROOM_AREA) {

			if (boxWidth <= boxHeight) {
				Point midPoint = new Point((int) start.getX(), (int) start.getY() + (boxHeight / 2));
				generateWall(midPoint, new Point((int) end.getX(), (int) start.getY() + (boxHeight / 2)), safe);
				generateRooms(start, new Point((int) end.getX(), (int) start.getY() + (boxHeight / 2)), safe);
				generateRooms(midPoint, end, safe);
			} else if (boxWidth > boxHeight) {
				Point midPoint = new Point((int) start.getX() + boxWidth / 2, (int) start.getY());
				generateWall(midPoint, new Point((int) start.getX() + boxWidth / 2, (int) end.getY()), safe);
				generateRooms(start, new Point((int) start.getX() + boxWidth / 2, (int) end.getY()), safe);
				generateRooms(midPoint, end, safe);
			}
		}
		// If the room is not safe, spawns zombies inside of the room
		if (!safe) {
			for (int z = 0; z < MAX_ZOMBIE_PER_ROOM; z++) {
				int randomX = (int) (Math.random() * boxWidth + start.getX());
				int randomY = (int) (Math.random() * boxHeight + start.getY());

				if (Math.random() > 0 && (tileMap[randomX][randomY] & 0xFFF) == 201) {
					chunkMap[randomX / 16][randomY / 16]
							.addZombie(new Zombie(new Point(randomX * 32, randomY * 32), 100, game.getZombieImages()[0],
									null, this.game, this, (int) Math.floor((Math.random() * 5))));
				}
			}
		}
		// Spawns items inside of the room
		for (int item = 0; item < MAX_ITEM_PER_ROOM; item++) {
			// Clone the item
			Item itemSpawned = itemSpawns.get((int) (Math.random() * itemSpawns.size()));
			if (itemSpawned instanceof Consumable) {
				itemSpawned = new Consumable((Consumable) itemSpawned);
			} else if (itemSpawned instanceof Melee) {
				itemSpawned = new Melee((Melee) itemSpawned);
			} else if (itemSpawned instanceof Firearm) {
				itemSpawned = new Firearm((Firearm) itemSpawned);
			} else if (itemSpawned instanceof Throwable) {
				itemSpawned = new Throwable((Throwable) itemSpawned);
			}

			// Gets random coordinates for the item
			int randomX = (int) (Math.random() * boxWidth + start.getX());
			int randomY = (int) (Math.random() * boxHeight + start.getY());

			// If the tile is valid then spawns and item on the tile
			if ((tileMap[randomX][randomY] & (1 << 14)) == 0) {
				if (Math.random() > 0.25) {
					itemSpawned.setPosition(new Point(randomX * 32, randomY * 32));
					chunkMap[randomX / 16][randomY / 16].add(itemSpawned);
				}
			}
		}
	}

	/**
	 * Generates trees in the empty plaza areas.
	 * 
	 * @param start
	 *            the start of the forest.
	 * @param end
	 *            the end of the forest.
	 */
	public void generateTrees(Point start, Point end) {
		// The dimensions of the forest
		int boxWidth = (int) (Math.abs(end.getX() - (start.getX())));
		int boxHeight = (int) (Math.abs(end.getY() - (start.getY())));

		// Goes through the forest area with a small chance of placing a tree in
		// every valid tile
		for (int i = (int) start.getX(); i <= end.getX(); i++) {
			for (int j = (int) start.getY(); j <= end.getY(); j++) {
				if ((tileMap[i][j] & 0xFFF) == 108) {
					if (Math.random() > 0.90 && (upperTileMap[i - 1][j] & 0xFFF) < 109
							&& (upperTileMap[i - 1][j - 1] & 0xFFF) < 109 && (upperTileMap[i - 1][j + 1] & 0xFFF) < 109
							&& (upperTileMap[i][j - 1] & 0xFFF) < 109 && (upperTileMap[i][j + 1] & 0xFFF) < 109
							&& (upperTileMap[i + 1][j - 1] & 0xFFF) < 109 && (upperTileMap[i + 1][j] & 0xFFF) < 109
							&& (upperTileMap[i + 1][j + 1] & 0xFFF) < 109) {
						setTile(i, j, 109, Direction.UP, true);
						setUpperTile(i, j, 110, Direction.UP);
						setUpperTile(i - 1, j - 1, 111, Direction.RIGHT);
						setUpperTile(i - 1, j, 110, Direction.UP);
						setUpperTile(i - 1, j + 1, 111, Direction.UP);
						setUpperTile(i, j - 1, 110, Direction.UP);
						setUpperTile(i, j + 1, 110, Direction.UP);
						setUpperTile(i + 1, j - 1, 111, Direction.DOWN);
						setUpperTile(i + 1, j, 110, Direction.UP);
						setUpperTile(i + 1, j + 1, 111, Direction.LEFT);
					}
				}
			}
		}

		// Spawns zombies inside of the forest
		for (int z = 0; z < MAX_ZOMBIE_PER_FOREST; z++) {
			int randomX = (int) (Math.random() * boxWidth + start.getX());
			int randomY = (int) (Math.random() * boxHeight + start.getY());

			if (Math.random() > 0.10 && (tileMap[randomX][randomY] & 0xFFF) == 108
					|| ((tileMap[randomX][randomY] & 0xFFF) >= 110 && (tileMap[randomX][randomY] & 0xFFF) < 200)) {
				chunkMap[randomX / 16][randomY / 16].addZombie(new Zombie(new Point(randomX * 32, randomY * 32), 100,
						game.getZombieImages()[0], null, this.game, this, (int) Math.floor((Math.random() * 5))));
			}
		}

		// Spawns items inside of the forest
		for (int item = 0; item < MAX_ITEM_PER_FOREST; item++) {
			// Clone the item
			Item itemSpawned = itemSpawns.get((int) (Math.random() * itemSpawns.size()));
			if (itemSpawned instanceof Consumable) {
				itemSpawned = new Consumable((Consumable) itemSpawned);
			} else if (itemSpawned instanceof Melee) {
				itemSpawned = new Melee((Melee) itemSpawned);
			} else if (itemSpawned instanceof Firearm) {
				itemSpawned = new Firearm((Firearm) itemSpawned);
			} else if (itemSpawned instanceof Throwable) {
				itemSpawned = new Throwable((Throwable) itemSpawned);
			}

			int randomX = (int) (Math.random() * boxWidth + start.getX());
			int randomY = (int) (Math.random() * boxHeight + start.getY());

			// Checks to see if the chosen tile is valid and if it is, places an
			// item there
			if ((tileMap[randomX][randomY] & (1 << 14)) == 0 && (tileMap[randomX][randomY] & 0xFFF) != 201
					&& (tileMap[randomX][randomY] & 0xFFF) != 207) {
				itemSpawned.setPosition(new Point(randomX * 32, randomY * 32));
				chunkMap[randomX / 16][randomY / 16].add(itemSpawned);
			}
		}
	}

	/**
	 * Sets the tiles for the walls inside the room.
	 * 
	 * @param start
	 *            the start of the wall.
	 * @param end
	 *            the end of the wall.
	 * @param safe
	 *            if the area is safe or not.
	 */
	public void generateWall(Point start, Point end, boolean safe) {
		// Dimensions of the door
		int boxWidth = (int) (Math.abs(end.getX() - (start.getX())));
		int boxHeight = (int) (Math.abs(end.getY() - (start.getY())));
		int doorLocation;

		// Generates a vertical wall while taking into account surrounding tiles
		if (boxHeight > boxWidth) {
			doorLocation = (int) start.getY() + boxHeight / 3;

			if ((tileMap[(int) start.getX()][(int) start.getY()] & 0xFFF) != 205
					&& (tileMap[(int) start.getX()][(int) start.getY()] & 0xFFF) != 206) {
				if (!safe)
					setTile((int) start.getX(), (int) start.getY() - 1, 204, Direction.LEFT, true);
				else
					setTile((int) start.getX(), (int) start.getY() - 1, 210, Direction.LEFT, true);
				setTile((int) start.getX(), (int) start.getY(), 205, Direction.UP, true);
			} else
				setTile((int) start.getX(), (int) start.getY(), 206, Direction.LEFT, true);

			if ((tileMap[(int) start.getX()][(int) end.getY()] & 0xFFF) != 205
					&& (tileMap[(int) start.getX()][(int) end.getY()] & 0xFFF) != 206) {
				if (!safe)
					setTile((int) start.getX(), (int) end.getY() + 1, 204, Direction.RIGHT, true);
				else
					setTile((int) start.getX(), (int) end.getY() + 1, 210, Direction.RIGHT, true);
				setTile((int) start.getX(), (int) end.getY(), 205, Direction.UP, true);
			} else
				setTile((int) start.getX(), (int) end.getY(), 206, Direction.LEFT, true);

			for (int i = (int) start.getY() + 1; i < end.getY(); i++) {
				if (i != doorLocation && i != doorLocation - 1)
					setTile((int) start.getX(), i, 205, Direction.UP, true);
				else
					setTile((int) start.getX(), i, 207, Direction.UP, false);
			}
			// Generates a horizontal wall while taking into account surrounding
			// tiles
		} else {
			doorLocation = (int) start.getX() + boxWidth / 3;

			if ((tileMap[(int) start.getX()][(int) start.getY()] & 0xFFF) != 205
					&& (tileMap[(int) start.getX()][(int) start.getY()] & 0xFFF) != 206) {
				setTile((int) start.getX(), (int) start.getY(), 205, Direction.RIGHT, true);
				if (!safe)
					setTile((int) start.getX() - 1, (int) start.getY(), 204, Direction.DOWN, true);
				else
					setTile((int) start.getX() - 1, (int) start.getY(), 210, Direction.DOWN, true);
			} else {
				setTile((int) start.getX(), (int) start.getY(), 206, Direction.DOWN, true);
			}

			if ((tileMap[(int) end.getX()][(int) start.getY()] & 0xFFF) != 205
					&& (tileMap[(int) end.getX()][(int) start.getY()] & 0xFFF) != 206) {
				setTile((int) end.getX(), (int) start.getY(), 205, Direction.RIGHT, true);
				if (!safe)
					setTile((int) end.getX() + 1, (int) start.getY(), 204, Direction.UP, true);
				else
					setTile((int) end.getX() + 1, (int) start.getY(), 210, Direction.UP, true);
			} else {
				setTile((int) end.getX(), (int) start.getY(), 206, Direction.UP, true);
			}

			for (int i = (int) start.getX() + 1; i < end.getX(); i++) {
				if (i != doorLocation && i != doorLocation - 1)
					setTile(i, (int) start.getY(), 205, Direction.RIGHT, true);
			}
		}
	}

	/**
	 * Generates side road off of main road.
	 * 
	 * @param start
	 *            the start of the side road area.
	 * @param end
	 *            the end of the side road area.
	 */
	public void generateSideRoads(Point start, Point end) {
		// Dimensions of the side road
		int boxWidth = (int) (Math.abs(end.getX() - (start.getX() - 1)));
		int boxHeight = (int) (Math.abs(end.getY() - (start.getY() - 1)));
		int roadX;
		int roadY;

		if (boxWidth * boxHeight > MAX_AREA) {

			if ((boxWidth > (2 * MIN_SIDE_LENGTH + ROAD_WIDTH))
					&& (((boxWidth * HEIGHT_WIDTH_RATIO < boxHeight) && (boxHeight * HEIGHT_WIDTH_RATIO < boxWidth)
							&& Math.random() > .5) || (boxHeight * HEIGHT_WIDTH_RATIO < boxWidth))) {
				roadX = (int) ((Math.min(start.getX(), end.getX()) + MIN_SIDE_LENGTH)
						+ ((Math.random() * (boxWidth - (2 * MIN_SIDE_LENGTH)))));
				roadY = (int) Math.max(start.getY(), end.getY());
				generateVerticalRoad(roadX, roadY, ROAD_WIDTH);
				// Recursive split new generated squares

				Point start1 = new Point((int) start.getX(), (int) start.getY());
				Point end1 = new Point(roadX - (ROAD_WIDTH + 1) / 2, roadY);
				generateSideRoads(start1, end1);

				Point start2 = new Point(roadX + (ROAD_WIDTH + 1) / 2, (int) start.getY());
				Point end2 = new Point((int) end.getX(), (int) end.getY());
				generateSideRoads(start2, end2);

			} else if (boxHeight > (2 * MIN_SIDE_LENGTH + ROAD_WIDTH)) {
				roadY = (int) ((Math.min(start.getY(), end.getY()) + MIN_SIDE_LENGTH)
						+ (Math.random() * (boxHeight - (2 * MIN_SIDE_LENGTH))));
				roadX = (int) Math.max(start.getX(), end.getX());

				generateHorizontalRoad(roadX, roadY, ROAD_WIDTH);
				// Recursive split new generated squares

				Point start1 = new Point((int) start.getX(), (int) start.getY());
				Point end1 = new Point(roadX, roadY - (ROAD_WIDTH + 1) / 2);
				generateSideRoads(start1, end1);

				Point start2 = new Point((int) start.getX(), (int) roadY + (ROAD_WIDTH + 1) / 2);
				Point end2 = new Point((int) end.getX(), (int) end.getY());
				generateSideRoads(start2, end2);
			} else {
				// Records corners of plaza
				plazaStarts.add(start);
				plazaEnds.add(end);

			}
		} else {
			// Records corners of plaza
			plazaStarts.add(start);
			plazaEnds.add(end);
		}
	}

	/**
	 * Generates a vertical road.
	 * 
	 * @param x
	 *            starting x.
	 * @param y
	 *            starting y.
	 * @param size
	 *            road size in blocks.
	 */
	public void generateVerticalRoad(int x, int y, int size) {
		int tempx = x - ((size - 1) / 2);
		int tempy = y;
		// Starting intersection
		// end of map
		if (y == (height - 1)) {
			for (int i = 1; i <= size; i++) {
				if (i == 1)
					setTile(tempx, tempy, 120, Direction.RIGHT, false);
				else if (i == size)
					setTile(tempx, tempy, 120, Direction.RIGHT, false);
				else
					setTile(tempx, tempy, 120, Direction.RIGHT, false);
				tempx++;
			}
			tempy--;
		}
		// Road intersection
		else {
			tempy++;
			for (int i = 1; i <= size; i++) {
				if (!(i == 1 || i == size)) {

				if (i == size - 1) {
					setTile(tempx, tempy, 107, Direction.LEFT, false);
					setTile(tempx, (tempy + 1), 103, Direction.LEFT, false);
				} else if (i == 2) {
					setTile(tempx, tempy, 107, Direction.RIGHT, false);
					setTile(tempx, (tempy + 1), 103, Direction.DOWN, false);
				} else {
					setTile(tempx, tempy, 106, Direction.RIGHT, false);
					setTile(tempx, (tempy + 1), 101, Direction.RIGHT, false);
				}
				}
				tempx++;
			}
			tempy = y;
		}

		// Actual road generation
		tempx = x - ((size - 1) / 2);

		while (tempy > 0 && tileMap[tempx][tempy] == 0) {
			// Places correct tile type and direction
			for (int i = 1; i <= size; i++) {
				if (i == 1 || i == size)
					setTile(tempx, tempy, 100, Direction.UP, false);
				else if (i == 2)
					setTile(tempx, tempy, 102, Direction.RIGHT, false);
				else if (i == size - 1)
					setTile(tempx, tempy, 102, Direction.LEFT, false);
				else if (i == (size + 1) / 2)
					setTile(tempx, tempy, 104, Direction.RIGHT, false);
				else if (size == MAIN_ROAD_SIZE
						&& (i == (((size + 1) / 2) + 1) / 2 || i == (size + 1) / 2 + (((size + 1) / 2) + 1) / 2 - 1))
					setTile(tempx, tempy, 105, Direction.RIGHT, false);
				else
					setTile(tempx, tempy, 101, Direction.UP, false);

				// Spawns zombies on roads
				if (Math.random() > 0.99) {
					chunkMap[tempx / 16][tempy / 16].addZombie(new Zombie(new Point(tempx * 32, tempy * 32), 100,
							game.getZombieImages()[0], null, this.game, this, (int) Math.floor((Math.random() * 5))));
				}
				tempx++;
			}

			tempy--;
			tempx -= size;
		}
		// End of map
		if (tempy == 0) {
			// Places correct tile type and direction
			for (int i = 1; i <= size; i++) {
				if (i == 1)
					setTile(tempx, tempy, 120, Direction.LEFT, false);
				else if (i == size)
					setTile(tempx, tempy, 120, Direction.LEFT, false);
				else
					setTile(tempx, tempy, 120, Direction.LEFT, false);
				tempx++;
			}
		}
		// Road intersection
		else {
			// Places correct tile type and direction
			for (int i = 1; i <= size; i++) {
				if (i == size || i == 1) {

				} else if (i == size - 1) {
					setTile(tempx, tempy, 107, Direction.LEFT, false);
					setTile(tempx, (tempy - 1), 103, Direction.UP, false);
				} else if (i == 2) {
					setTile(tempx, tempy, 107, Direction.RIGHT, false);
					setTile(tempx, (tempy - 1), 103, Direction.RIGHT, false);

				} else {
					setTile(tempx, tempy, 106, Direction.RIGHT, false);
					setTile(tempx, (tempy - 1), 101, Direction.UP, false);
				}
				tempx++;
			}
		}
	}

	/**
	 * Generates a horizontal road.
	 * 
	 * @param x
	 *            starting x.
	 * @param y
	 *            starting y.
	 * @param size
	 *            road size in blocks.
	 */
	public void generateHorizontalRoad(int x, int y, int size) {
		int tempx = x;
		int tempy = y - ((size - 1) / 2);
		// End of map
		if (x == (width - 1)) {
			// Places correct tile type and direction
			for (int i = 1; i <= size; i++) {
				if (i == 1)
					setTile(tempx, tempy, 120, Direction.UP, false);
				else if (i == size)
					setTile(tempx, tempy, 120, Direction.UP, false);
				else
					setTile(tempx, tempy, 120, Direction.UP, false);
				tempy++;
			}
			tempx--;
		}
		// Road intersection
		else {
			// Places correct tile type and direction
			tempx++;
			for (int i = 1; i <= size; i++) {
				if (!(i == 1 || i == size)) {

				if (i == size - 1) {
					setTile(tempx, tempy, 107, Direction.UP, false);
					setTile((tempx + 1), tempy, 103, Direction.RIGHT, false);
				} else if (i == 2) {
					setTile(tempx, tempy, 107, Direction.DOWN, false);
					setTile((tempx + 1), tempy, 103, Direction.DOWN, false);
				} else {
					setTile(tempx, tempy, 106, Direction.UP, false);
					setTile((tempx + 1), tempy, 101, Direction.UP, false);
				}
				}
				tempy++;
			}
			tempx = x;
		}

		// Road generation
		tempy -= size;
		// Until end of map or hit another road
		while (tempx > 0 && tileMap[tempx][tempy] == 0) {
			// Places correct tile type and direction
			for (int i = 1; i <= size; i++) {
				if (i == 1 || i == size)
					setTile(tempx, tempy, 100, Direction.UP, false);
				else if (i == 2)
					setTile(tempx, tempy, 102, Direction.DOWN, false);
				else if (i == size - 1)
					setTile(tempx, tempy, 102, Direction.UP, false);
				else if (i == (size + 1) / 2)
					setTile(tempx, tempy, 104, Direction.UP, false);
				else
					setTile(tempx, tempy, 101, Direction.UP, false);
				tempy++;
			}
			if (Math.random() > 0.99) {
				chunkMap[tempx / 16][tempy / 16].addZombie(new Zombie(new Point(tempx * 32, tempy * 32), 100,
						game.getZombieImages()[0], null, this.game, this, (int) Math.floor((Math.random() * 5))));
			}
			tempx--;
			tempy -= size;

		}

		// End of map
		if (tempx == 0) {
			// Places correct tile type and direction
			for (int i = 1; i <= size; i++) {
				if (i == 1)
					setTile(tempx, tempy, 120, Direction.DOWN, false);
				else if (i == size)
					setTile(tempx, tempy, 120, Direction.DOWN, false);
				else
					setTile(tempx, tempy, 120, Direction.DOWN, false);
				tempy++;
			}
		}
		// Intersection
		else {
			// Places correct tile type and direction
			for (int i = 1; i <= size; i++) {
				if (i == 1 || i == size) {
				} else if (i == size - 1) {
					setTile(tempx, tempy, 107, Direction.UP, false);
					setTile((tempx - 1), tempy, 103, Direction.UP, false);
				} else if (i == 2) {
					setTile(tempx, tempy, 107, Direction.DOWN, false);
					setTile((tempx - 1), tempy, 103, Direction.LEFT, false);
				} else {
					setTile(tempx, tempy, 106, Direction.UP, false);
					setTile((tempx - 1), tempy, 101, Direction.UP, false);
				}
				tempy++;
			}
		}
	}

	/**
	 * Sets tile to tile id and sets direction.
	 * 
	 * @param x
	 *            x position.
	 * @param y
	 *            y position.
	 * @param id
	 *            tile ID.
	 * @param direction
	 *            tile direction.
	 */
	public void setTile(int x, int y, int id, Direction direction, boolean solid) {
		// set id
		tileMap[x][y] = (short) id;
		// Set bit 12/ 13 to indicate direction
		if (direction == Direction.RIGHT) {
			tileMap[x][y] = (short) (tileMap[x][y] | (1 << 13));
		} else if (direction == Direction.LEFT) {
			tileMap[x][y] = (short) (tileMap[x][y] | (1 << 12));
		} else if (direction == Direction.UP) {
			tileMap[x][y] = (short) (tileMap[x][y] | (1 << 12));
			tileMap[x][y] = (short) (tileMap[x][y] | (1 << 13));
		}
		if (solid) {
			tileMap[x][y] = (short) (tileMap[x][y] | (1 << 14));

		}

	}

	/**
	 * Sets tile to tile id and sets direction.
	 * 
	 * @param x
	 *            x position.
	 * @param y
	 *            y position.
	 * @param id
	 *            tile ID.
	 * @param direction
	 *            tile direction.
	 */
	public void setUpperTile(int x, int y, int id, Direction direction) {
		// set id
		upperTileMap[x][y] = (short) id;
		// Set bit 12/ 13 to indicate direction
		if (direction == Direction.RIGHT) {
			upperTileMap[x][y] = (short) (upperTileMap[x][y] | (1 << 13));
		} else if (direction == Direction.LEFT) {
			upperTileMap[x][y] = (short) (upperTileMap[x][y] | (1 << 12));
		} else if (direction == Direction.UP) {
			upperTileMap[x][y] = (short) (upperTileMap[x][y] | (1 << 12));
			upperTileMap[x][y] = (short) (upperTileMap[x][y] | (1 << 13));
		}

	}

	public short[][] getMap() {
		return this.tileMap;
	}

	public int getHeight() {
		return this.height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return this.width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public Point getSafehouseStart() {
		return this.safehouseStart;
	}

	public Point getSafehouseEnd() {
		return this.safehouseEnd;
	}

	public Point getFlagLocation() {
		return this.flagLocation;
	}

	public Chunk[][] getChunkMap() {
		return this.chunkMap;
	}

	public Point getPlayerCoordinate() {
		return this.playerStart;
	}

	public void setChunkMap(Chunk[][] chunkMap) {
		this.chunkMap = chunkMap;
	}

	public PathFinder getPathFinder() {
		return this.pathFinder;
	}

	public void setPathFinder(PathFinder pathFinder) {
		this.pathFinder = pathFinder;
	}

	public short[][] getUpperTileMap() {
		return this.upperTileMap;
	}

	public void setUpperTileMap(short[][] upperTileMap) {
		this.upperTileMap = upperTileMap;
	}
}