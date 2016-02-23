/*
 * File: ImageShopUI.java
 * ----------------------
 * This file implements parts of the user interface for the ImageShop
 * application.  The primary functions of this file are to manage the
 * menus and read images from files.  You should not need to change this
 * file and do not need to understand the details of its implementation.
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import javax.swing.*;

/**
 * This class implements the user interface for the <code>ImageShop</code>
 * application.
 */

public class ImageShopUI {

/**
 * Creates a new instance of a <code>ImageShopUI</code> object.
 *
 * @param app The ImageShop application
 */

	public ImageShopUI(ImageShop app) {
		this.app = app;
		patchQuitHandler();
		actionListener = new ImageShopActionListener();
		updateMenuBar();
		Container centerPanel = app.getRegionPanel(Program.CENTER);
		canvas = app.getGCanvas();
		try {
			centerPanel.remove(canvas);
		} catch (Exception ex) {
			/* Ignore the exception */
		}
		titleBar = new JLabel("Untitled", JLabel.CENTER);
		Dimension size = titleBar.getPreferredSize();
		size.height = TITLE_BAR_HEIGHT;
		titleBar.setPreferredSize(size);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(canvas, BorderLayout.CENTER);
		panel.add(titleBar, BorderLayout.NORTH);
		centerPanel.add(panel);
		centerPanel.requestFocus();
		currentImage = null;
		dirty = false;
		closeAction();
	}

/**
 * Gets the current image.  If no image has been loaded, this method returns
 * <code>null</code>.
 *
 * @return The current <code>GImage</code>
 */

	public GImage getImage() {
		return currentImage;
	}

/**
 * Sets the current image and centers it in the window.
 *
 * @param image The new <code>GImage</code>
 */

	public void setImage(GImage image) {
		if (currentImage != null) canvas.remove(currentImage);
		double x = (canvas.getWidth() - image.getWidth()) / 2.0;
		double y = (canvas.getHeight() - image.getHeight()) / 2.0;
		currentImage = image;
		canvas.add(image, x, y);
		image.sendToBack();
		setDirtyBit(true);
	}

/**
 * Implements the "Open" menu item.
 */

	private void openAction() {
		if (dirty && !confirmSave()) return;
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(getImageDirectory());
		int result = chooser.showOpenDialog(app);
		if (result == JFileChooser.APPROVE_OPTION) {
			currentFile = chooser.getSelectedFile();
			titleBar.setText(currentFile.getName());
			loadImage(new GImage(currentFile.getAbsolutePath()));
			setDirtyBit(false);
		}
	}

/**
 * Implements the "Overlay" menu item.
 */

	private void overlayAction() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(getImageDirectory());
		int result = chooser.showOpenDialog(app);
		if (result == JFileChooser.APPROVE_OPTION) {
			if (currentImage == null) {
				currentFile = chooser.getSelectedFile();
				titleBar.setText(currentFile.getName());
				loadImage(new GImage(currentFile.getAbsolutePath()));
				setDirtyBit(false);
			} else {
				Image oldImage = currentImage.getImage();
				int width = oldImage.getWidth(canvas);
				int height = oldImage.getHeight(canvas);
				Image newImage = canvas.createImage(width, height);
				Graphics g = newImage.getGraphics();
				g.drawImage(oldImage, 0, 0, canvas);
				File file = chooser.getSelectedFile();
				Image overlay = new GImage(file.getAbsolutePath()).getImage();
				int x0 = (width - overlay.getWidth(canvas)) / 2;
				int y0 = (height - overlay.getHeight(canvas)) / 2;
				g.drawImage(overlay, x0, y0, canvas);
				currentImage.setImage(newImage);
				setDirtyBit(true);
			}
		}
	}

/**
 * Implements the "Close" menu item.
 */

	private void closeAction() {
		if (dirty && !confirmSave()) return;
		setDirtyBit(false);
		canvas.removeAll();
		currentImage = null;
		currentFile = null;
		titleBar.setText("Untitled");
	}

/**
 * Implements the "Restore" menu item, which restores the original version
 * of the current image.
 */

	private void restoreAction() {
		if (currentFile == null) return;
		loadImage(new GImage(currentFile.getAbsolutePath()));
		setDirtyBit(false);
	}

/**
 * Implements the "Save" menu item.
 */

	private void saveAction() {
		if (currentImage == null) return;
		if (currentFile == null) {
			saveAsAction();
		} else {
			saveCurrentFile();
		}
	}

/**
 * Implements the "Save As" menu item, which asks the user for a file name.
 */

	private void saveAsAction() {
		if (currentImage == null) return;
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(getImageDirectory());
		if (currentFile != null) chooser.setSelectedFile(currentFile);
		int result = chooser.showSaveDialog(app);
		if (result != JFileChooser.APPROVE_OPTION) return;
		currentFile = chooser.getSelectedFile();
		saveCurrentFile();
	}

/**
 * Saves the image under the currently stored name.
 */

	private void saveCurrentFile() {
		titleBar.setText(currentFile.getName());
		currentImage.saveImage(currentFile);
		setDirtyBit(false);
	}

/**
 * Loads the specified image after removing any existing content.
 */

	private void loadImage(GImage image) {
		canvas.removeAll();
		setImage(image);
	}

/**
 * Rewrites the menu bar to include the ImageShop File menu.
 */

	private void updateMenuBar() {
		ProgramMenuBar mbar = app.getMenuBar();
		mbar.removeAll();
		mbar.add(createFileMenu());
	}

/**
 * Creates the new File menu.
 *
 * @return The new File menu
 */

	private JMenu createFileMenu() {
		JMenu menu = new JMenu("File");
		JMenuItem item = createMenuItem("Open@O");
		item.setText("Open File...");
		menu.add(item);
		item = createMenuItem("Overlay@^O");
		item.setText("Overlay...");
		menu.add(item);
		menu.add(createMenuItem("Close@W"));
		menu.add(createMenuItem("Restore@R"));
		saveItem = createMenuItem("Save@S");
		menu.add(saveItem);
		item = createMenuItem("SaveAs");
		item.setText("Save As...");
		menu.add(item);
		return menu;
	}

/**
 * Creates a new menu item.  The string combines the menu item name and any
 * accelerator keys, separated by an at sign.
 *
 * @param str The menu item name and any accelerator keys after an at sign
 * @return The new menu item
 */

	private JMenuItem createMenuItem(String str) {
		int at = str.indexOf("@");
		String label = (at == -1) ? str : str.substring(0, at);
		String binding = (at == -1) ? null : str.substring(at + 1);
		boolean shifted = binding != null && binding.startsWith("^");
		if (shifted) binding = binding.substring(1);
		JMenuItem item = new JMenuItem(label);
		item.setActionCommand(label);
		item.addActionListener(actionListener);
		if (binding != null) {
			setAccelerator(item, binding.charAt(0), shifted);
		}
		return item;
	}

/**
 * Sets the accelerator for a menu item.
 *
 * @param item The menu item
 * @param key The accelerator key
 * @param shifted A boolean value indicating whether the shift key is required
 */

	private void setAccelerator(JMenuItem item, int key, boolean shifted) {
		int mask = (Platform.isMac()) ? KeyEvent.META_MASK : KeyEvent.CTRL_MASK;
		if (shifted) mask |= KeyEvent.SHIFT_MASK;
		KeyStroke stroke = KeyStroke.getKeyStroke((char) key, mask);
		if (Platform.isMac()) {
			item.setAccelerator(stroke);
		} else {
			item.setMnemonic(key);
		}
	}

/**
 * Patches the Quit handler on a Macintosh to ensure that the user is prompted
 * for unsaved files.
 */

	private void patchQuitHandler() {
		Window window = JTFTools.getEnclosingFrame(app);
		for (WindowListener listener : window.getWindowListeners()) {
			window.removeWindowListener(listener);
		}
		window.addWindowListener(new ImageShopWindowListener());
		if (!Platform.isMac()) return;
		try {
			Class<?> applicationClass =
					Class.forName("com.apple.eawt.Application");
			Class<?> quitStrategyClass =
					Class.forName("com.apple.eawt.QuitStrategy");
			Class<?>[] types = { quitStrategyClass };
			Field quitStrategyField =
					quitStrategyClass.getField("CLOSE_ALL_WINDOWS");
			Object[] args = { quitStrategyField.get(null) };
			Method setQuitStrategy =
					applicationClass.getMethod("setQuitStrategy", types);
			Method getApplication =
					applicationClass.getMethod("getApplication", new Class<?>[0]);
			Object application = getApplication.invoke(null, new Object[0]);
			setQuitStrategy.invoke(application, args);
		} catch (Exception ex) {
			/* Ignore any errors */
		}
	}

/**
 * Pops up a dialog on exit that warns the user about unsaved files.
 */

	private void confirmExit() {
		if (dirty && currentFile != null) {
			Object[] options = { "Save", "Don't Save" };
			String msg = currentFile.getName() + " has not been saved.";
			int status =
					JOptionPane.showOptionDialog(app, msg, "Confirm Save",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE,
							null, options, options[0]);
			if (status == JOptionPane.YES_OPTION) saveAction();
		}
	}

/**
 * Pops up a dialog letting the user save a file that has been modified.
 */

	private boolean confirmSave() {
		Object[] options = { "Save", "Don't Save", "Cancel" };
		String msg = "This image has not been saved.";
		if (currentFile != null) {
			msg = currentFile.getName() + " has not been saved.";
		}
		int status =
				JOptionPane.showOptionDialog(app, msg, "Confirm Save",
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE,
						null, options, options[0]);
		if (status == JOptionPane.YES_OPTION) {
			saveAction();
			return true;
		} else if (status == JOptionPane.NO_OPTION) {
			return true;
		}
		return false;
	}

/**
 * Returns the image directory, which is either the current directory of a
 * subdirectory named images.
 *
 * @return A File object specifying the image directory
 */

	private File getImageDirectory() {
		File dir = new File(System.getProperty("user.dir") + "/images");
		if (!dir.isDirectory()) {
			dir = new File(System.getProperty("user.dir"));
		}
		return dir;
	}

/**
 * Sets a dirty bit to indicate whether the image has been modified.  This
 * code also updates the Save menu item so that it enabled only when the
 * image has been modified.
 *
 * @param flag A boolean value indicating whether the image is modified
 */

	private void setDirtyBit(boolean flag) {
		dirty = flag;
		saveItem.setEnabled(flag);
	}

/* This inner class implements the WindowListener for ImageShopUI */

	private class ImageShopWindowListener extends WindowAdapter {

		public void windowClosed(WindowEvent e) {
			confirmExit();
			System.exit(0);
		}

		public void windowClosing(WindowEvent e) {
			confirmExit();
			System.exit(0);
		}
	}

/* This inner class implements the ActionListener for ImageShopUI */

	private class ImageShopActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd.equals("Open")) {
				openAction();
			} else if (cmd.equals("Overlay")) {
				overlayAction();
			} else if (cmd.equals("Close")) {
				closeAction();
			} else if (cmd.equals("Save")) {
				saveAction();
			} else if (cmd.equals("Restore")) {
				restoreAction();
			} else if (cmd.equals("SaveAs")) {
				saveAsAction();
			}
		}
	}

/* Private constants */

	private static final int TITLE_BAR_HEIGHT = 24;

/* Private instance variables */

	private ActionListener actionListener;
	private File currentFile;
	private GCanvas canvas;
	private GImage currentImage;
	private ImageShop app;
	private JLabel titleBar;
	private JMenuItem saveItem;
	private boolean dirty;

}
