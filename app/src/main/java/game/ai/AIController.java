package game.ai;

import game.model.Player;
import game.model.Obstacle;
import java.util.List;

public final class AIController {
	private static final int JUMP_DISTANCE_THRESHOLD = 100;
	private final Player player;

	public AIController(Player player) {
		this.player = player;
	}

	public void update(List<Obstacle> obstacles) {
		obstacles.stream()
				.filter(this::shouldJumpOver)
				.findFirst()
				.ifPresent(obstacle -> player.jump());
	}

	private boolean shouldJumpOver(Obstacle obstacle) {
		return obstacle.getX() - player.getX() < JUMP_DISTANCE_THRESHOLD
				&& player.isOnGround();
	}
}
