/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.browser;
import org.eclipse.help.ui.internal.browser.*;
/**
 * Represents a web browser that can be used
 * by clients to display documents for the given URLs.
 * @since 2.0
 */
public interface IBrowser {
	/**
	 * Closes the browser.
	 */
	public void close();
	/**
	 * Queries the browser if close
	 * method is supported.
	 * @return true if the method is fully implemented
	 */
	public boolean isCloseSupported();
	/**
	 * Displays document with the given URL,
	 * and makes the browser visible.
	 * This method starts the browser if necessary.
	 * @param url the URL to display in the browser
	 */
	public void displayURL(String url);
	/**
	 * Queries the browser if setLocation
	 * method is supported.
	 * @return true if the method is fully implemented
	 */
	public boolean isSetLocationSupported();
	/**
	 * Queries the browser if setSize
	 * method is supported.
	 * @return true if the method is fully implemented
	 */
	public boolean isSetSizeSupported();
	/**
	 * Causes browser to be moved to the specified
	 * location.  If the actual browser is not visible,
	 * the next time it becomes visible, it will be shown
	 * at the give location
	 * @param x horizontal coordinates of the left-top
	 *  external corner
	 * @param y vertical coordinates of the left-top
	 *  external corner
	 */
	public void setLocation(int x, int y);
	/**
	 * Causes browser to be resized to the specified
	 * size.  If the actual browser is not visible,
	 * the next time it becomes visible, it will be shown
	 * with the give size.
	 * @param width width in pixels
	 * @param height height in pixels
	 *  external corner
	 */
	public void setSize(int width, int height);
}

