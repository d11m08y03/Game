package game.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import game.utils.Constants;

public class ParallaxBackground {
	private static class Layer {
		private final Image image;
		private final float speedFactor;
		private float x;

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
			layer.x -= 1 * layer.speedFactor;

			int imgWidth = layer.image.getWidth(null);
			if (layer.x <= -imgWidth) {
				layer.x += imgWidth;
			}
		}
	}

	public void render(Graphics g) {
		for (Layer layer : layers) {
			int imgWidth = layer.image.getWidth(null);
			int startX = (int) layer.x;

			// Draw as many images as needed to fill the window
			for (int x = startX; x < Constants.WINDOW_WIDTH.getValue(); x += imgWidth) {
				g.drawImage(layer.image, x, 0, null);
			}
		}
	}
}
