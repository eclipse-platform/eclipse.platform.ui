package org.eclipse.jface.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.resource.*;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import java.util.*;

/**
 * Abstract base implementation of a dialog page.
 * All dialog pages are subclasses of this one.
 */
public abstract class DialogPage implements IDialogPage {
	/**
	 * The control for this dialog page.
	 */
	private Control control;
	/**
	 * Optional title; <code>null</code> if none.
	 *
	 * @see #setTitle
	 */
	private String title = null;
	/**
	 * Optional description; <code>null</code> if none.
	 *
	 * @see #setDescription
	 */
	private String description = null;
	/**
	 * Cached image; <code>null</code> if none.
	 *
	 * @see #setImageDescription
	 */
	private Image image = null;
	/**
	 * Optional image; <code>null</code> if none.
	 *
	 * @see #setImageDescription
	 */
	private ImageDescriptor imageDescriptor = null;
	/**
	 * The current message; <code>null</code> if none.
	 */
	private String message = null;
	
	/**
	 * The current error message; <code>null</code> if none.
	 */
	private String errorMessage = null;

	/**
	 * Font metrics to use for determining pixel sizes.
	 */
	private FontMetrics fontMetrics;

/**
 * Creates a new empty dialog page.
 */
protected DialogPage() {
}
/**
 * Creates a new dialog page with the given title.
 *
 * @param title the title of this dialog page, 
 *  or <code>null</code> if none
 */
protected DialogPage(String title) {
	this.title = title;
}
/**
 * Creates a new dialog page with the given title and image.
 *
 * @param title the title of this dialog page, 
 *  or <code>null</code> if none
 * @param image the image for this dialog page, 
 *  or <code>null</code> if none
 */
protected DialogPage(String title, ImageDescriptor image) {
	this(title);
	imageDescriptor = image;
}
/**
 * Returns the number of pixels corresponding to the
 * height of the given number of characters.
 * <p>
 * This method may only be called after <code>initializeDialogUnits</code>
 * has been called.
 * </p>
 * <p>
 * Clients may call this framework method, but should not override it.
 * </p>
 *
 * @param chars the number of characters
 * @return the number of pixels
 */
protected int convertHeightInCharsToPixels(int chars) {
	Assert.isNotNull(fontMetrics, "Must call initializeDialogUnits before calling this method"); //$NON-NLS-1$
	return Dialog.convertHeightInCharsToPixels(fontMetrics, chars);
}
/**
 * Returns the number of pixels corresponding to the
 * given number of horizontal dialog units.
 * <p>
 * This method may only be called after <code>initializeDialogUnits</code>
 * has been called.
 * </p>
 * <p>
 * Clients may call this framework method, but should not override it.
 * </p>
 *
 * @param dlus the number of horizontal dialog units
 * @return the number of pixels
 */
protected int convertHorizontalDLUsToPixels(int dlus) {
	Assert.isNotNull(fontMetrics, "Must call initializeDialogUnits before calling this method"); //$NON-NLS-1$
	return Dialog.convertHorizontalDLUsToPixels(fontMetrics, dlus);
}
/**
 * Returns the number of pixels corresponding to the
 * given number of vertical dialog units.
 * <p>
 * This method may only be called after <code>initializeDialogUnits</code>
 * has been called.
 * </p>
 * <p>
 * Clients may call this framework method, but should not override it.
 * </p>
 *
 * @param dlus the number of vertical dialog units
 * @return the number of pixels
 */
protected int convertVerticalDLUsToPixels(int dlus) {
	Assert.isNotNull(fontMetrics, "Must call initializeDialogUnits before calling this method"); //$NON-NLS-1$
	return Dialog.convertVerticalDLUsToPixels(fontMetrics, dlus);
}
/**
 * Returns the number of pixels corresponding to the
 * width of the given number of characters.
 * <p>
 * This method may only be called after <code>initializeDialogUnits</code>
 * has been called.
 * </p>
 * <p>
 * Clients may call this framework method, but should not override it.
 * </p>
 *
 * @param chars the number of characters
 * @return the number of pixels
 */
protected int convertWidthInCharsToPixels(int chars) {
	Assert.isNotNull(fontMetrics, "Must call initializeDialogUnits before calling this method"); //$NON-NLS-1$
	return Dialog.convertWidthInCharsToPixels(fontMetrics, chars);
}
/**	
 * The <code>DialogPage</code> implementation of an <code>IDialogPage</code>
 * method does nothing. Subclasses may extend.
 */
public void dispose() {
	// deallocate SWT resources
	if (image != null) {
		image.dispose();
		image = null;
	}		
}
/**
 * Returns the top level control for this dialog page.
 *
 * @return the top level control
 */
public Control getControl() {
	return control;	
}
/* (non-Javadoc)
 * Method declared on IDialogPage.
 */
public String getDescription() {
	return description;
}
/**
 * Returns the symbolic font name used by dialog pages.
 *
 * @return the symbolic font name
 */
protected String getDialogFontName() {
	return JFaceResources.DIALOG_FONT;
}
/* (non-Javadoc)
 * Method declared on IDialogPage.
 */
public String getErrorMessage() {
	return errorMessage;
}
/**
 * Returns the default font to use for this dialog page.
 *
 * @return the font
 */
protected Font getFont() {
	return JFaceResources.getFontRegistry().get(getDialogFontName());
}
/* (non-Javadoc)
 * Method declared on IDialogPage.
 */
public Image getImage() {
	if (image == null) {
		if (imageDescriptor != null) {
			image = imageDescriptor.createImage();
		}
	}
	return image;
}
/* (non-Javadoc)
 * Method declared on IDialogPage.
 */
public String getMessage() {
	return message;
}
/**
 * Returns this dialog page's shell.
 * Convenience method for <code>getControl().getShell()</code>.
 * This method may only be called after the page's control
 * has been created.
 *
 * @return the shell 
 */
public Shell getShell() {
	return getControl().getShell();
}
/* (non-Javadoc)
 * Method declared on IDialogPage.
 */
public String getTitle() {
	return title;
}
/**
 * Returns the tool tip text for the widget with the given id.
 * <p>
 * The default implementation of this framework method
 * does nothing and returns <code>null</code>. 
 * Subclasses may override.
 * </p>
 *
 * @param widgetID the id of the widget for which  
 *    hover help is requested
 * @return the tool tip text, or <code>null</code> if none
 * @depecated will be removed
 */
protected final String getToolTipText(int widgetId) {
	// return nothing by default
	return null;
}
/**
 * Initializes the computation of horizontal and vertical dialog units
 * based on the size of current font.
 * <p>
 * This method must be called before any of the dialog unit based
 * conversion methods are called.
 * </p>
 *
 * @param control a control from which to obtain the current font
 */
protected void initializeDialogUnits(Control control) {
	// Compute and store a font metric
	GC gc = new GC(control);
	gc.setFont(control.getFont());
	fontMetrics = gc.getFontMetrics();
	gc.dispose();
}
/**
 * Tests whether this page's UI content has already been created.
 *
 * @return <code>true</code> if the control has been created,
 * and <code>false</code> if not
 */
protected boolean isControlCreated() {
	return control != null;
}
/**	
 * This default implementation of an <code>IDialogPage</code>
 * method does nothing. Subclasses should override to take some
 * action in response to a help request.
 */
public void performHelp() {
}
/* (non-Javadoc)
 * Set the control for this page.
 */
protected void setControl(Control newControl) {
	control = newControl;
}
	/* (non-Javadoc)
	 * Method declared on IDialogPage.
	 */
	public void setDescription(String description) {
		this.description= description;
	}
/**
 * Sets or clears the error message for this page.
 *
 * @param newMessage the message, or <code>null</code> to clear
 *   the error message
 */
public void setErrorMessage(String newMessage) {
	errorMessage = newMessage;
}
/* (non-Javadoc)
 * Method declared on IDialogPage.
 */
public void setImageDescriptor(ImageDescriptor desc) {
	imageDescriptor = desc;
	if (image != null) {
		image.dispose();
		image = null;
	}
}
/**
 * Sets or clears the message for this page.
 *
 * @param newMessage the message, or <code>null</code> to clear
 *   the message
 */
public void setMessage(String newMessage) {
	message = newMessage;
}
/**
 * The <code>DialogPage</code> implementation of this <code>IDialogPage</code>
 * method remembers the title in an internal state variable.
 * Subclasses may extend.
 */
public void setTitle(String title) {
	this.title = title;
}
/**
 * The <code>DialogPage</code> implementation of this <code>IDialogPage</code>
 * method sets the control to the given visibility state.
 * Subclasses may extend.
 */
public void setVisible(boolean visible) {
	control.setVisible(visible);
}
}
