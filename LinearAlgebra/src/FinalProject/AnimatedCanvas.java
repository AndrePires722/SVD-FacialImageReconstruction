package FinalProject;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public abstract class AnimatedCanvas extends Canvas{
	public AnimatedCanvas() {
		super(300,300);
	}
	/**
	 *	Update the data before its drawn, if necessary
	 */
	public abstract void update();
	/**
	 * Draw the data, scaled to the size of its container
	 */
	public abstract void drawData();

}
