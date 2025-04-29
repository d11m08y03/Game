package game.utils;

public enum Constants {
	WINDOW_WIDTH(16*70),
	WINDOW_HEIGHT(9*70),
	GROUND_Y(300),
	GRAVITY(1),
	PLAYER_WIDTH(30),
	PLAYER_HEIGHT(30),
	OBSTACLE_WIDTH(20),
	OBSTACLE_HEIGHT(30);

	private final int value;

	Constants(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
