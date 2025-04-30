package game.engine;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import game.model.*;
import game.ai.AIController;
import game.utils.Constants;

public final class GamePanel extends JPanel {
    private static final int TARGET_FPS = 60;
    private final Player player;
    private final List<Obstacle> obstacles;
    private final AIController ai;
    private volatile boolean gameOver = false;
    private volatile boolean gameStarted = false;
    private volatile boolean gameWon = false;
    private ParallaxBackground background;
    private JButton startButton;
    private JButton restartButton;

    public GamePanel() {
        this.player = new Player(50, Constants.AIRWAY_Y.getValue());
        this.obstacles = new CopyOnWriteArrayList<>();
        this.ai = new AIController(player);

        try {
            // Load background layers
            background = new ParallaxBackground();
            background.addLayer(ImageIO.read(Objects.requireNonNull(getClass().getResource("/parallax/sky.png"))), 0.05f);
            background.addLayer(ImageIO.read(Objects.requireNonNull(getClass().getResource("/parallax/far-clouds.png"))), 0.1f);
            background.addLayer(ImageIO.read(Objects.requireNonNull(getClass().getResource("/parallax/near-clouds.png"))), 0.2f);
            background.addLayer(ImageIO.read(Objects.requireNonNull(getClass().getResource("/parallax/far-mountains.png"))), 0.4f);
            background.addLayer(ImageIO.read(Objects.requireNonNull(getClass().getResource("/parallax/mountains.png"))), 0.6f);
            background.addLayer(ImageIO.read(Objects.requireNonNull(getClass().getResource("/parallax/trees.png"))), 1.0f);
        } catch (Exception e) {
            System.err.println("Failed to load resources: " + e.getMessage());
        }

        initializeObstacles();
        setPreferredSize(new Dimension(
                Constants.WINDOW_WIDTH.getValue(),
                Constants.WINDOW_HEIGHT.getValue()
        ));
        setLayout(new BorderLayout());

        createStartScreen();
    }

    private void createStartScreen() {
        JPanel mainPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                background.render(g);
                renderObstacles(g);

                // Draw title
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 72));
                String title = "Kot Geter";
                int titleWidth = g.getFontMetrics().stringWidth(title);
                g.drawString(title, (getWidth() - titleWidth) / 2, 150);

                // Draw subtitle
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.PLAIN, 24));
                String subtitle = "Dodge the obstacles and survive!";
                int subtitleWidth = g.getFontMetrics().stringWidth(subtitle);
                g.drawString(subtitle, (getWidth() - subtitleWidth) / 2, 200);
            }
        };
        mainPanel.setOpaque(false);

        // Create start button
        startButton = new JButton("START GAME");
        startButton.setFont(new Font("Arial", Font.BOLD, 28));
        startButton.setBackground(new Color(34, 139, 34)); // Forest green
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 100, 0), 2),
                BorderFactory.createEmptyBorder(15, 40, 15, 40)
        ));

        // Add hover effect
        startButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                startButton.setBackground(new Color(0, 180, 0));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                startButton.setBackground(new Color(34, 139, 34));
            }
        });

        // Center button
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.insets = new Insets(250, 0, 0, 0);
        mainPanel.add(startButton, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Start game when button clicked
        startButton.addActionListener(e -> startGame());
    }

    private void startGame() {
        gameStarted = true;
        removeAll();
        revalidate();
        repaint();
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

            double delta = 0;
            double nsPerUpdate = 1_000_000_000.0 / TARGET_FPS;

            while (!gameOver && !gameWon) {
                long now = System.nanoTime();
                delta += (now - lastTime) / nsPerUpdate;
                lastTime = now;

                while (delta >= 1) {
                    updateGame(1.0 / TARGET_FPS);
                    repaint();
                    frames++;
                    delta--;
                }

                if (System.currentTimeMillis() - timer >= 1000) {
                    System.out.println("FPS: " + frames);
                    frames = 0;
                    timer += 1000;
                }
            }
        }).start();
    }

    private void updateGame(double deltaTime) {
        if (gameOver || gameWon) return;

        player.update(deltaTime);
        background.update();

        for (Obstacle obstacle : obstacles) {
            obstacle.update(deltaTime);
        }

        gameOver = checkCollisions();

        if (!gameOver) {
            ai.update(obstacles);
            checkWinCondition();
        }
    }

    private boolean checkCollisions() {
        return obstacles.stream()
                .anyMatch(this::isCollidingWithPlayer);
    }

    private boolean isCollidingWithPlayer(Obstacle obstacle) {
        return player.getX() < obstacle.getX() + Constants.OBSTACLE_WIDTH.getValue() &&
                player.getX() + Constants.PLAYER_WIDTH.getValue() > obstacle.getX() &&
                player.getY() < Constants.AIRWAY_Y.getValue() + Constants.OBSTACLE_HEIGHT.getValue() &&
                player.getY() + Constants.PLAYER_HEIGHT.getValue() > Constants.AIRWAY_Y.getValue();
    }

    private void checkWinCondition() {
        boolean allPassed = obstacles.stream()
                .allMatch(obstacle -> obstacle.getX() + Constants.OBSTACLE_WIDTH.getValue() < player.getX());

        if (allPassed) {
            gameWon = true;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!gameStarted) {
            setBackground(Color.BLACK);
        } else {
            background.render(g);
            renderGame(g);
        }
    }

    private void renderGame(Graphics g) {
        if (gameOver) {
            renderGameOver(g);
        } else if (gameWon) {
            renderGameWon(g);
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

    private void renderGameWon(Graphics g) {
        String text = "YOU WIN!";
        Font font = new Font("Arial", Font.BOLD, 40);
        g.setFont(font);

        FontMetrics metrics = g.getFontMetrics(font);
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getHeight();

        int x = (getWidth() - textWidth) / 2;
        int y = getHeight() / 2;

        int padding = 30;
        int arcWidth = 30;
        int arcHeight = 30;

        Graphics2D g2d = (Graphics2D) g.create();

        // Draw semi-transparent rounded box
        g2d.setColor(new Color(0, 0, 0, 170));
        g2d.fillRoundRect(x - padding, y - textHeight, textWidth + 2 * padding, textHeight + padding, arcWidth, arcHeight);

        // Draw the win text
        g2d.setColor(new Color(0, 255, 0));
        g2d.drawString(text, x, y);

        g2d.dispose();

        // Create "Play Again" button if not already visible
        if (restartButton == null) {
            restartButton = new JButton("PLAY AGAIN");
            restartButton.setFont(new Font("Arial", Font.BOLD, 24));
            restartButton.setBackground(new Color(34, 139, 34));
            restartButton.setForeground(Color.WHITE);
            restartButton.setFocusPainted(false);
            restartButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 100, 0), 2),
                    BorderFactory.createEmptyBorder(10, 30, 10, 30)
            ));

            // Hover effect
            restartButton.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    restartButton.setBackground(new Color(0, 180, 0));
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    restartButton.setBackground(new Color(34, 139, 34));
                }
            });

            // Action: reset game state
            restartButton.addActionListener(e -> restartGame());

            // Place it on the panel
            this.setLayout(null);
            int buttonWidth = 250;
            int buttonHeight = 50;
            restartButton.setBounds((getWidth() - buttonWidth) / 2, y + 40, buttonWidth, buttonHeight);
            this.add(restartButton);
            this.repaint();
        }
    }

    private void restartGame() {
        // Clean up
        if (restartButton != null) {
            this.remove(restartButton);
            restartButton = null;
        }

        gameOver = false;
        gameWon = false;

        obstacles.clear();
        initializeObstacles();

        player.resetPosition();

        revalidate();
        repaint();

        startGameLoop();
    }

    private void renderPlayer(Graphics g) {
        player.render(g);
    }

    private void renderObstacles(Graphics g) {
        obstacles.forEach(obstacle -> obstacle.render(g));
    }
}
