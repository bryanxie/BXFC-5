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
import java.util.Arrays;

import javax.swing.*;

public class ImageShop extends GraphicsProgram {

	public void init() {
		add(new FlipVerticalButton(), WEST);
		add(new FlipHorizontalButton(), WEST);
		add(new RotateLeftButton(), WEST);
		add(new RotateRightButton(), WEST);
		add(new GrayscaleButton(), WEST);
		add(new GreenScreenButton(), WEST);
		add(new EqualizeButton(), WEST);
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
		button.execute(this); //passing yourself for the ride. i have info on the image, so i need to give it to you
	}

	/* Constants */

	public static final int APPLICATION_WIDTH = 950;
	public static final int APPLICATION_HEIGHT = 600;

	/* Private instance variables */

	private ImageShopUI ui;

	/* Methods */

	public static int computeLuminosity(int r, int g, int b) {
		return GMath.round(0.299 * r + 0.587 * g + 0.114 * b);
	}
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
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < width / 2; j++) {
				int temp = array[i][width - j - 1];
				array[i][width - j - 1] = array[i][j];
				array[i][j] = temp;
			}
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
			for (int j = 0; j < width; j++) {
				newArray[width - j - 1][i] = array[i][j];
			}
		}
		app.setImage(new GImage(newArray));
	}
}

class RotateRightButton extends ImageShopButton {

	public RotateRightButton() {
		super("Rotate Right");
	}

	/*
	 * Creates a new image which consists of the bits in the original image
	 * rotated right 90 degrees.  
	 */

	public void execute(ImageShop app) {
		GImage image = app.getImage();
		if (image == null) return;
		int[][] array = image.getPixelArray();
		int height = array.length;
		int width = array[0].length;
		int[][] newArray = new int[width][height];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				newArray[j][height - i - 1] = array[i][j];
			}
		}
		app.setImage(new GImage(newArray));
	}
}

class GrayscaleButton extends ImageShopButton {

	public GrayscaleButton() {
		super("Grayscale");
	}

	/*
	 * Creates a grayscale image of the original image
	 */

	public void execute(ImageShop app) {
		GImage image = app.getImage();
		if (image == null) return;
		int[][] array = image.getPixelArray();
		int height = array.length;
		int width = array[0].length;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int pixel = array[i][j];
				int red = (pixel >> 16) & 0xFF;
				int green = (pixel >> 8) & 0xFF;
				int blue = pixel & 0xFF;
				int xx = ImageShop.computeLuminosity(red, green, blue);
				pixel = (0xFF << 24) | (xx << 16) | (xx << 8) | xx;
				array[i][j] = pixel;
			}
		}
		app.setImage(new GImage(array));
	}
}

class GreenScreenButton extends ImageShopButton {

	public GreenScreenButton() {
		super("Green Screen");
	}

	/*
	 * Erases green pixels that are twice as large as the maximum of red and blue components
	 */

	public void execute(ImageShop app) {
		GImage image = app.getImage();
		if (image == null) return;
		int[][] array = image.getPixelArray();
		int height = array.length;
		int width = array[0].length;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int pixel = array[i][j];
				int red = GImage.getRed(pixel);
				int green = GImage.getGreen(pixel);
				int blue = GImage.getBlue(pixel);
				int alpha = 0;
				int maxNum = Math.max(red, blue);
				if (green >= maxNum * 2) {
					pixel = GImage.createRGBPixel(red, green, blue, alpha);
					array[i][j] = pixel;
				}
			}
		}
		app.setImage(new GImage(array));
	}
}

class EqualizeButton extends ImageShopButton {

	public EqualizeButton() {
		super("Equalize");
	}

	public void execute(ImageShop app) {
		GImage image = app.getImage();
		if (image == null) return;
		int[][] array = image.getPixelArray();
		int height = array.length;
		int width = array[0].length;
		int totalPixel = height * width;
		int[] luminosityHistogram = new int[256]; 
		int[] luminosityCulHistogram = new int[256];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int pixel = array[i][j];
				int red = GImage.getRed(pixel);
				int green = GImage.getGreen(pixel);
				int blue = GImage.getBlue(pixel);
				int luminosity = ImageShop.computeLuminosity(red, green, blue);
				luminosityHistogram[luminosity]++;
			}
		}
		System.out.print(luminosity);
		for (int i = 1; i < luminosityHistogram.length; i++) {
			luminosityHistogram[i] += luminosityHistogram[i - 1];
		}
		luminosityCulHistogram = luminosityHistogram;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int pixel = array[i][j];
				int red = GImage.getRed(pixel);
				int green = GImage.getGreen(pixel);
				int blue = GImage.getBlue(pixel);
				int luminosity = ImageShop.computeLuminosity(red, green, blue);
				pixel = (255 * luminosityCulHistogram[luminosity] / totalPixel);
				array[i][j] = pixel;
			}
		}
		app.setImage(new GImage(array));
	}
}
