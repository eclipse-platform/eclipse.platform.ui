/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.browser;
import org.eclipse.help.ui.internal.browser.*;
/**
 * Implementators of org.eclipse.help.ui.browsers
 * extension points must provide implementation of this
 * interface.
 * @since 2.0
 */
public interface IBrowserFactory {
	/**
	 * Checks whether the factory can work on the user system.
	 * @return false if the factory cannot work on this system;
	 * for example the required native browser required
	 * by browser adapters that it creates is not installed.
	 */
	public boolean isAvailable();
	/**
	 * Obtains a new instance of a web browser.
	 * @return instance of IBrowser
	 */
	public IBrowser createBrowser();
}