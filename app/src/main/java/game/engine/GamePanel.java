package game.engine;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import game.model.*;
import game.ai.AIController;
import game.utils.Constants;

public final class GamePanel extends JPanel {
    private static final int TARGET_FPS = 60;
    private static final long OPTIMAL_TIME = 1_000_000_000 / TARGET_FPS; // Nanoseconds per frame

    private final Player player;
    private final List<Obstacle> obstacles;
    private final AIController ai;
    private volatile boolean gameOver = false;
    private ParallaxBackground background;

    public GamePanel() {
        this.player = new Player(50, Constants.GROUND_Y.getValue());
        this.obstacles = new CopyOnWriteArrayList<>();
        this.ai = new AIController(player);

        try {
            background = new ParallaxBackground();
            background.addLayer(ImageIO.read(getClass().getResource("/parallax/sky.png")), 0.1f);
            background.addLayer(ImageIO.read(getClass().getResource("/parallax/far-clouds.png")), 0.2f);
            background.addLayer(ImageIO.read(getClass().getResource("/parallax/near-clouds.png")), 0.3f);
            background.addLayer(ImageIO.read(getClass().getResource("/parallax/far-mountains.png")), 0.4f);
            background.addLayer(ImageIO.read(getClass().getResource("/parallax/mountains.png")), 0.5f);
            background.addLayer(ImageIO.read(getClass().getResource("/parallax/trees.png")), 1.0f);
        } catch (Exception e) {
            System.err.println("Failed to load parallax backgrounds: " + e.getMessage());
        }

        initializeObstacles();
        setPreferredSize(new Dimension(
            Constants.WINDOW_WIDTH.getValue(), 
            Constants.WINDOW_HEIGHT.getValue()
        ));

        startGameLoop();
    }

    private void initializeObstacles() {
        List.of(800, 1200, 1600, 2000, 2400)
            .forEach(x -> obstacles.add(new Obstacle(x)));
    }

    private void startGameLoop() {
        new Thread(() -> {
            long lastTime = System.nanoTime();
            long timer = System.currentTimeMillis();
            int frames = 0;

            while (!gameOver) {
                long now = System.nanoTime();
                long elapsedTime = now - lastTime; // Time elapsed since the last frame
                lastTime = now;

                double deltaTime = elapsedTime / 1_000_000_000.0; // Convert to seconds

                // Update game logic with deltaTime
                updateGame(deltaTime);

                // Render the game
                repaint();

                frames++;

                // Sleep to maintain target frame rate
                long sleepTime = (OPTIMAL_TIME - (System.nanoTime() - now)) / 1_000_000; // Convert to milliseconds
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                // Print FPS for debugging
                if (System.currentTimeMillis() - timer >= 1000) {
                    System.out.println("FPS: " + frames);
                    frames = 0;
                    timer += 1000;
                }
            }
        }).start();
    }

    private void updateGame(double deltaTime) {
        if (gameOver) return;

        player.update(deltaTime);
        background.update();

        for (Obstacle obstacle : obstacles) {
            obstacle.update(deltaTime);
        }

        gameOver = checkCollisions();

        if (!gameOver) {
            ai.update(obstacles);
        }
    }

    private boolean checkCollisions() {
        return obstacles.stream()
                      .anyMatch(this::isCollidingWithPlayer);
    }

    private boolean isCollidingWithPlayer(Obstacle obstacle) {
        return player.getX() < obstacle.getX() + Constants.OBSTACLE_WIDTH.getValue() &&
               player.getX() + Constants.PLAYER_WIDTH.getValue() > obstacle.getX() &&
               player.getY() < Constants.GROUND_Y.getValue() + Constants.OBSTACLE_HEIGHT.getValue() &&
               player.getY() + Constants.PLAYER_HEIGHT.getValue() > Constants.GROUND_Y.getValue();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        background.render(g);
        renderGame(g);
    }

    private void renderGame(Graphics g) {
        if (gameOver) {
            renderGameOver(g);
        } else {
            renderPlayer(g);
            renderObstacles(g);
        }
    }

    private void renderGameOver(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        String text = "GAME OVER";
        int x = (getWidth() - g.getFontMetrics().stringWidth(text)) / 2;
        g.drawString(text, x, getHeight() / 2);
    }

    private void renderPlayer(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(
            player.getX(), 
            player.getY(), 
            Constants.PLAYER_WIDTH.getValue(), 
            Constants.PLAYER_HEIGHT.getValue()
        );
    }

    private void renderObstacles(Graphics g) {
        g.setColor(Color.RED);
        obstacles.forEach(obstacle -> 
            g.fillRect(
                obstacle.getX(), 
                Constants.GROUND_Y.getValue(), 
                Constants.OBSTACLE_WIDTH.getValue(), 
                Constants.OBSTACLE_HEIGHT.getValue()
            )
        );
    }
}
