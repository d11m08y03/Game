package game.model;

import java.util.random.RandomGenerator;

public final class Obstacle {
    private int x;
    private final int speed;

    public Obstacle(int startX) {
        this.x = startX;
        this.speed = RandomGenerator.getDefault().nextInt(100, 400); // Pixels per second
    }

    public void update(double deltaTime) {
        x -= speed * deltaTime;
    }

    public int getX() {
        return x;
    }

    public int getSpeed() {
        return speed;
    }
}
