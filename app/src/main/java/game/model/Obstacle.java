package game.model;

import game.utils.Constants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.Objects;
import java.util.random.RandomGenerator;

public final class Obstacle {
    private int x;
    private final int speed;

    // Sprite and animation
    private static Image sprite;
    private static final int SPRITE_WIDTH = 32;
    private static final int SPRITE_HEIGHT = 32;
    private static final int SCALE = 2;

    private static final int TOTAL_FRAMES = 4;
    private int currentFrame = 0;
    private double frameTimer = 0;
    private static final double FRAME_TIME = 0.1;

    static {
        try {
            sprite = ImageIO.read(Objects.requireNonNull(Obstacle.class.getResource("/pigeon_fiy-Sheet.png")));
        } catch (IOException e) {
            System.err.println("Failed to load obstacle sprite: " + e.getMessage());
        }
    }

    public Obstacle(int startX) {
        this.x = startX;
        this.speed = RandomGenerator.getDefault().nextInt(100, 400);
    }

    public void update(double deltaTime) {
        x -= (int) (speed * deltaTime);

        // Update animation frame
        frameTimer += deltaTime;
        if (frameTimer >= FRAME_TIME) {
            frameTimer = 0;
            currentFrame = (currentFrame + 1) % TOTAL_FRAMES;
        }
    }

    public void render(Graphics g) {
        if (sprite != null) {
            int sx = currentFrame * SPRITE_WIDTH;
            int sy = 0; // top row

            int drawX = x;
            int drawY = Constants.AIRWAY_Y.getValue() + Constants.OBSTACLE_HEIGHT.getValue() - (SPRITE_HEIGHT * SCALE) + 32;

            Graphics2D g2d = (Graphics2D) g;

            AffineTransform originalTransform = g2d.getTransform();

            g2d.scale(-1, 1); // Flip horizontally

            // Translate the image so that it appears correctly after the flip
            // The x-coordinate of the image needs to be adjusted for the flip, because
            // scaling with -1 will flip the image about its own center.
            g2d.translate(-drawX - SPRITE_WIDTH * SCALE, drawY);

            g2d.drawImage(
                    sprite,
                    0, 0, SPRITE_WIDTH * SCALE, SPRITE_HEIGHT * SCALE, // Draw at the correct location after flipping
                    sx, sy, sx + SPRITE_WIDTH, sy + SPRITE_HEIGHT,
                    null
            );

            // Restore the original transformation
            g2d.setTransform(originalTransform);
        }
    }

    public int getX() {
        return x;
    }

    public int getWidth() {
        return SPRITE_WIDTH * SCALE;
    }
}
