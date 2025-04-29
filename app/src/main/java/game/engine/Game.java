package game.engine;

import javax.swing.*;

public final class Game {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(Game::createAndShowGUI);
	}

	private static void createAndShowGUI() {
		JFrame frame = new JFrame("AI Dino Game");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new GamePanel());
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
