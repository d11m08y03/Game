package game.model;

import game.utils.Constants;

public final class Player {
    private int x;
    private int y;
    private int velocityY;
    private boolean onGround;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.velocityY = 0;
        this.onGround = true;
    }

    public void update(double deltaTime) {
        if (!onGround) {
            velocityY += Constants.GRAVITY.getValue() * deltaTime;
            y += velocityY * deltaTime;

            if (y >= Constants.GROUND_Y.getValue()) {
                y = Constants.GROUND_Y.getValue();
                onGround = true;
                velocityY = 0;
            }
        }
    }

    public void jump() {
        if (onGround) {
            velocityY = -400; // Jump strength
            onGround = false;
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isOnGround() {
        return onGround;
    }
}
