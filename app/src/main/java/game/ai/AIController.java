package game.ai;

import game.model.Player;
import game.model.Obstacle;
import java.util.List;

public final class AIController {
	private static final int JUMP_DISTANCE_THRESHOLD = 150;
	private static final int SAFE_LANDING_MARGIN = 100;
	private static final int EARLY_JUMP_MARGIN = 30;
	// Clear area needed for landing
	private static final int SAFE_LANDING_AREA = 200;
	private final Player player;
	private boolean isJumping = false;

	public AIController(Player player) {
		this.player = player;
	}

	public void update(List<Obstacle> obstacles) {
		if (obstacles.isEmpty()) {
			// If no obstacles, ensure player comes down if jumping
			if (isJumping && isSafeToLand(obstacles)) {
				player.resetPosition();
				isJumping = false;
			}
			return;
		}

		Obstacle closestObstacle = findClosestObstacle(obstacles);

		if (shouldJump(closestObstacle)) {
			triggerJump();
			isJumping = true;
		}
		// Check if we should come down after jumping
		else if (isJumping && isSafeToLand(obstacles)) {
			player.resetPosition();
			isJumping = false;
		}
	}

	private boolean isSafeToLand(List<Obstacle> obstacles) {
		// Check if there are no obstacles in the landing area
		return obstacles.stream()
				.noneMatch(obs -> obs.getX() - player.getX() < SAFE_LANDING_AREA &&
						obs.getX() - player.getX() > 0);
	}

	private Obstacle findClosestObstacle(List<Obstacle> obstacles) {
		Obstacle closest = null;
		float minDistance = Float.MAX_VALUE;

		for (Obstacle obstacle : obstacles) {
			float distance = obstacle.getX() - player.getX();
			if (distance > SAFE_LANDING_MARGIN && distance < minDistance) {
				minDistance = distance;
				closest = obstacle;
			}
		}
		return closest;
	}

	private boolean shouldJump(Obstacle obstacle) {
		if (obstacle == null) return false;

		float distance = obstacle.getX() - player.getX();
		float obstacleWidth = obstacle.getWidth();

		return distance < (JUMP_DISTANCE_THRESHOLD + EARLY_JUMP_MARGIN) &&
				distance > (SAFE_LANDING_MARGIN - EARLY_JUMP_MARGIN) &&
				player.isOnGround();
	}

	private void triggerJump() {
		player.applyUpwardForce();
	}

	public void resetPlayer() {
		player.resetPosition();
		isJumping = false;
	}
}