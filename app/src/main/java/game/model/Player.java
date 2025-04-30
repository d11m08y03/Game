package game.model;

import game.utils.Constants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public final class Player {
    private int x;
    private int y;
    private double velocityY;
    private boolean isAscending;
    private boolean isDescending;
    private int originalY;

    // Animation-related
    private Image spriteSheet;
    private int frameWidth;
    private int frameHeight;
    private int currentFrame;
    private int totalFrames;
    private double frameTimer = 0;

    private static final double DESCENT_SPEED = 200;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.velocityY = 0;
        this.isAscending = false;
        this.isDescending = false;
        this.originalY = startY;
        loadSpriteSheet();
    }

    private void loadSpriteSheet() {
        try {
            spriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResource("/pigeon_fiy-Sheet.png")));
            frameWidth = 32;
            frameHeight = 32;
            totalFrames = 7;
        } catch (IOException e) {
            System.err.println("Failed to load player sprite sheet: " + e.getMessage());
        }
    }

    public void update(double deltaTime) {
        if (isAscending) {
            velocityY += Constants.GRAVITY.getValue() * deltaTime;
            y += (int) (velocityY * deltaTime);

            // Check upper window boundary
            if (y < 64) {
                y = 64;
                velocityY = 0;
            }

            // Check lower boundary (ground)
            if (y >= Constants.AIRWAY_Y.getValue()) {
                y = Constants.AIRWAY_Y.getValue();
                isAscending = false;
                velocityY = 0;
            }
        }
        else if (isDescending) {
            // Gradual controlled descent
            y += (int) (DESCENT_SPEED * deltaTime);

            if (y >= Constants.AIRWAY_Y.getValue()) {
                y = Constants.AIRWAY_Y.getValue();
                isDescending = false;
            }
        }

        // Boundary checks
        if (x < 0) {
            x = 0;
        }
        if (x > Constants.WINDOW_WIDTH.getValue() - (frameWidth * 2)) {
            x = Constants.WINDOW_WIDTH.getValue() - (frameWidth * 2);
        }

        // Animation update
        frameTimer += deltaTime;
        if (frameTimer >= 0.1) {
            frameTimer = 0;
            currentFrame = (currentFrame + 1) % totalFrames;
        }
    }

    public void applyUpwardForce() {
        if (!isAscending && !isDescending) {
            velocityY = -200;
            isAscending = true;
        }
    }

    public void resetPosition() {
        if (y < Constants.AIRWAY_Y.getValue()) {
            isDescending = true;
            isAscending = false;
            velocityY = 0;
        } else {
            y = Constants.AIRWAY_Y.getValue();
            isAscending = false;
            isDescending = false;
            velocityY = 0;
        }
    }

    public void render(Graphics g) {
        if (spriteSheet == null) return;

        int sx = currentFrame * frameWidth;
        int sy = 0;
        int scale = 2;
        int scaledWidth = frameWidth * scale;
        int scaledHeight = frameHeight * scale;

        g.drawImage(
                spriteSheet,
                x, y, x + scaledWidth, y + scaledHeight,
                sx, sy, sx + frameWidth, sy + frameHeight,
                null
        );
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public boolean isOnGround() {
        return !isAscending && !isDescending && y == Constants.AIRWAY_Y.getValue();
    }
}