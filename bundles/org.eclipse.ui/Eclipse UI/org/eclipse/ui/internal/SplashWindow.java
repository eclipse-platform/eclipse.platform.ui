package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * A splash window for an application.  
 */
public class SplashWindow implements PaintListener {
	private Shell shell;
	private String title;
	private Image image;
/**
 * Constructs a new splash window.
 */
public SplashWindow(String title, Image image) {
	super();
	this.title = title;
	this.image = image;
}
/**
 * Closes the window.  
 */
public void close() {
	shell.dispose();
	if (image != null)
		image.dispose();
}
/**
 * Opens the window.  The caller is responsable for running an event loop.
 */
public void open() {
	// Create shell
	shell = new Shell(SWT.NO_TRIM);
	shell.addPaintListener(this);

	// Get image size.
	int width = 400, height = 300;
	if (image != null) {
		Rectangle bounds = image.getBounds();
		width = bounds.width;
		height = bounds.height;
	}

	// Set shell position.
	Rectangle displayBounds = Display.getDefault().getClientArea();
	int x = (displayBounds.width - width) / 2;
	int y = (displayBounds.height - height) / 2;
	shell.setBounds(x, y, width, height);

	// Create canvas for painting.
	Canvas canvas = new Canvas(shell, SWT.NONE);
	canvas.setBounds(shell.getClientArea());
	canvas.addPaintListener(this);

	// Open shell.
	shell.open();
}
/**
 * Paints the splash image.
 */
public void paintControl(PaintEvent event) {
	if (image != null) {
		event.gc.drawImage(image, 0, 0);
	} else {
		event.gc.drawText(title + ": Splash screen image is not found", 0, 0);//$NON-NLS-1$
	}
}
}
