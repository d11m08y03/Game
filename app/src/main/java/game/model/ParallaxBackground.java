package game.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import game.utils.Constants;

public class ParallaxBackground {
	private static class Layer {
		private final Image image;
		private final float speedFactor;
		private int x;

		public Layer(Image image, float speedFactor) {
			this.image = image;
			this.speedFactor = speedFactor;
			this.x = 0;
		}
	}

	private final List<Layer> layers = new ArrayList<>();

	public void addLayer(Image image, float speedFactor) {
		int originalWidth = image.getWidth(null);
		int originalHeight = image.getHeight(null);

		float aspectRatio = (float) originalWidth / originalHeight;
		int scaledWidth = (int) (Constants.WINDOW_HEIGHT.getValue() * aspectRatio);
		int scaledHeight = Constants.WINDOW_HEIGHT.getValue();

		Image scaledImage = image.getScaledInstance(
				scaledWidth,
				scaledHeight,
				Image.SCALE_SMOOTH);

		layers.add(new Layer(scaledImage, speedFactor));
	}

	public void update() {
		for (Layer layer : layers) {
			layer.x -= (1 * layer.speedFactor);
			if (layer.x <= -layer.image.getWidth(null)) {
				layer.x = 0;
			}
		}
	}

	public void render(Graphics g) {
		for (Layer layer : layers) {
			// Draw two copies of each layer for looping
			g.drawImage(layer.image, layer.x, 0, null);
			g.drawImage(layer.image, layer.x + layer.image.getWidth(null), 0, null);
		}
	}
}
