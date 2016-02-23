/*
 * File: ImageShop.java
 * --------------------
 * This program implements a much simplified version of Photoshop that allows
 * the user to manipulate images.
 */

import acm.graphics.*;
import acm.program.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ImageShop extends GraphicsProgram {

	public void init() {
		add(new FlipVerticalButton(), WEST);
		add(new FlipHorizontalButton(), WEST);
		add(new RotateLeftButton(), WEST);
		addActionListeners();
		ui = new ImageShopUI(this);
	}

	/**
	 * Gets the image from the ImageShopUI.
	 *
	 * @return The current image or null if there isn't one
	 */

	public GImage getImage() {
		return ui.getImage();
	}

	/**
	 * Sets the current image.
	 *
	 * @param image The new GImage
	 */

	public void setImage(GImage image) {
		ui.setImage(image);
	}

	/* Implement the ActionListener interface */

	public void actionPerformed(ActionEvent e) {
		ImageShopButton button = (ImageShopButton) e.getSource();
		button.execute(this);
	}

	/* Constants */

	public static final int APPLICATION_WIDTH = 950;
	public static final int APPLICATION_HEIGHT = 600;

	/* Private instance variables */

	private ImageShopUI ui;

}

/**
 * This class represents the abstract superclass of all ImageShop buttons
 * as they appear on the left side of the window.  All subclasses must
 * invoke the constructor for this class by calling
 *
 *     super(name)
 *
 * where name is a string indicating the name of the button.  The abstract
 * class ImageShopButton specifies a method
 *
 *    public abstract void execute(ImageShop app)
 *
 * This definition forces each of the concrete classes to supply a
 * definition of execute that performs the appropriate operations.
 * The execute method takes the ImageShop application as an argument,
 * which makes it possible for the button to manipulate the image
 * and determine the crop box.
 */

abstract class ImageShopButton extends JButton {

	/**
	 * Constructs a new ImageShopButton.  Subclasses must invoke this
	 * constructor with the appropriate button name.
	 *
	 * @param name The name that appears on the button
	 */

	public ImageShopButton(String name) {
		super(name);
	}

	/**
	 * Executes the operation for the specific button.  This method must
	 * be defined individually for each subclass.
	 *
	 * @param app The ImageShop application
	 */

	public abstract void execute(ImageShop app);

}

/**
 * This class implements the "Flip Vertical" button, which flips the
 * image vertically.  All other buttons will have a similar structure.
 */

class FlipVerticalButton extends ImageShopButton {

	public FlipVerticalButton() {
		super("Flip Vertical");
	}

	/*
	 * Creates a new image which consists of the bits in the original image
	 * flipped vertically around the center line.  This code comes from
	 * page 434 of The Art and Science of Java.
	 */

	public void execute(ImageShop app) {
		GImage image = app.getImage();
		if (image == null) return;
		int[][] array = image.getPixelArray();
		int height = array.length;
		for (int p1 = 0; p1 < height / 2; p1++) {
			int p2 = height - p1 - 1;
			int[] temp = array[p1];
			array[p1] = array[p2];
			array[p2] = temp;
		}
		app.setImage(new GImage(array));
	}
}

class FlipHorizontalButton extends ImageShopButton {

	public FlipHorizontalButton() {
		super("Flip Horizontal");
	}

	/*
	 * Creates a new image which consists of the bits in the original image
	 * flipped horizontally around the center line.  
	 */

	public void execute(ImageShop app) {
		GImage image = app.getImage();
		if (image == null) return;
		int[][] array = image.getPixelArray();
		int width = array[0].length;
		for (int p1 = 0; p1 < width / 2; p1++) {
			int p2 = width - p1;
			int[] temp = array [p1];
			array[p1] = array[p2];
			array[p2] = temp;
		}
		app.setImage(new GImage(array));
	}

}

class RotateLeftButton extends ImageShopButton {

	public RotateLeftButton() {
		super("Rotate Left");
	}

	/*
	 * Creates a new image which consists of the bits in the original image
	 * rotated left 90 degrees.  
	 */

	public void execute(ImageShop app) {
		GImage image = app.getImage();
		if (image == null) return;
		int[][] array = image.getPixelArray();
		int height = array.length;
		int width = array[0].length;
		int[][] newArray = new int[width][height];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; i++) {
				newArray[width - j - 1][i] = array[i][j];
			}
		}
		app.setImage(new GImage(newArray));
	}

}

