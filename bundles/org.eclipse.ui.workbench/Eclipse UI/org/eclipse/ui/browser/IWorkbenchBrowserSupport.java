/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.browser;

import org.eclipse.ui.PartInitException;

/**
 * Web browser support. This class allows you to open URLs using internal or
 * external Web browsers. Implementers may provide varying levels of support.
 * The most rudimentary support that must be provided is to open URLs in an
 * external web browser window. Everything else is a hint that browser support
 * implementation may choose to honor but is not required (although a good
 * implementation should aspire to support all the styles if possible on the
 * given platform).
 * <p>
 * The support has a two-phase approach to opening URLs. A browser instance is
 * created first, then <code>openURL</code> is called on it. This provides for
 * browser instance reuse for as long as needed. The step of creating the
 * browser instance encourages reuse itself by not creating new instances of
 * browsers if one with the same id is already open. It also makes it possible
 * to reuse browser instances restored after workbench is restarted.
 * <p>
 * The simplest way to open a URL is:
 * 
 * <pre>
 * IWorkbenchSupport.createBrowser(&quot;myId&quot;).openURL(url);
 * </pre>
 * 
 * <p>
 * The call above will show the provided URL by reusing the browser instance
 * with the matching id, or creating a new one if one does not exist already.
 * <p>
 * When more advanced control over the behavior of a browser instance is
 * required, it is recommended to create the instance first, then reuse it as
 * needed.
 * <p>
 * This interface is not intended to be implemented by clients.
 * 
 * @see IWebBrowser
 * @since 3.1
 * @noimplement This interface is not intended to be implemented by clients.
 */

public interface IWorkbenchBrowserSupport {
	/**
	 * Style parameter (value 1&lt;&lt;1) indicating that the address combo and
	 * 'Go' button will created for the browser. This style is ignored if the
	 * support is forced to open the browser as external.
	 */
	int LOCATION_BAR = 1 << 1;

	/**
	 * Style parameter (value 1&lt;&lt;2) indicating that the navigation bar for
	 * navigating web pages will be created for the web browser. This style is
	 * ignored if the support is forced to open the browser as external.
	 */
	int NAVIGATION_BAR = 1 << 2;

	/**
	 * Style constant (value 1&lt;&lt;3) indicating that status will be tracked
	 * and shown for the browser (page loading progress, text messages etc.).
	 */
	int STATUS = 1 << 3;

	/**
	 * Style constant (value 1&lt;&lt;4) indicating that the internal web
	 * browser will reopen after restarting the workbench (if used). In
	 * addition, the URLs will appear in the MRU list.
	 */
	int PERSISTENT = 1 << 4;

	/**
	 * Style constant (value 1&lt;&lt;5) indicating that the internal web
	 * browser will be hosted in a workbench editor area. This is just a hint -
	 * implementers of the browser support may not honor it.
	 */
	int AS_EDITOR = 1 << 5;

	/**
	 * Style constant (value 1&lt;&lt;6) indicating that the internal web
	 * browser will be hosted in a workbench view. This is just a hint -
	 * implementers of the browser support may not honor it.
	 */
	int AS_VIEW = 1 << 6;

	/**
	 * Style constant (value 1&lt;&lt;7) indicating that the external web
	 * browser must be used even if the implementation supports internal
	 * browsers and the user didn't set the preference to external browsers.
	 */
	int AS_EXTERNAL = 1 << 7;

	/**
	 * Creates the new web browser instance. If the user has chosen to use the
	 * internal Web browser, the given style bits (see class header for values)
	 * will be used to open the browser.
	 * <p>
	 * The method will reuse an existing browser instance if the same
	 * <code>browserId</code> value is passed to it. A persisted browser
	 * instance restored upon startup can be accessed this way. If
	 * <code>null</code> is passed as a browserId, a unique id will be
	 * generated each time method is called.
	 * <p>
	 * If the user has chosen not to use the internal browser or it is not
	 * available on the current platform, an external browser will be used and
	 * all style parameters will be ignored.
	 * </p>
	 * 
	 * @param style
	 *            the style display constants. Style constants should be
	 *            bitwise-ORed together.
	 * @param browserId
	 *            if an instance of a browser with the same id is already
	 *            opened, it will be returned instead of creating a new one.
	 *            Passing <code>null</code> will create a new instance with a
	 *            generated id every time.
	 * @param name
	 *            a name used for the presentation of the internal browser
	 * @param tooltip
	 *            a tooltip used for the presentation of the internal browser
	 * @return the browser instance that can be used to open the URL. Clients
	 *         intending to reuse the instance for all the URLs should cache the
	 *         instance and call IWebBrowser#openURL() on it. Clients are
	 *         responsible for closing the browser instance when not needed.
	 * @exception PartInitException
	 *                if the operation failed for some reason
	 */
	IWebBrowser createBrowser(int style, String browserId, String name,
			String tooltip) throws PartInitException;

	/**
	 * Creates the new web browser instance. This is a simplified method that
	 * creates the instance using default values for style, name and tooltip
	 * parameters. The method can be used to quickly open the URL by calling
	 * <code>createBrowser(id).openURL(url)</code>.
	 * <p>
	 * 
	 * @param browserId
	 *            if an instance of a browser with the same id is already
	 *            opened, it will be returned instead of creating a new one.
	 *            Passing <code>null</code> will create a new instance with a
	 *            generated id every time.
	 * @return the browser instance that can be used to open the URL. Clients
	 *         intending to reuse the instance for all the URLs should cache the
	 *         instance and call IWebBrowser#openURL() on it. Clients are
	 *         responsible for closing the browser instance when not needed.
	 * @exception PartInitException
	 *                if the operation failed for some reason
	 */
	IWebBrowser createBrowser(String browserId) throws PartInitException;

	/**
	 * Returns a shared instance of the external web browser. Clients can use it
	 * to share one external browser. The external browser that will be used is
	 * subject to browser support implementation. A suggested implementation is
	 * to use the operating system's default browser. Implementations that offer
	 * users a choice of the web browser should honour the users choice of
	 * external browser, with the initial selection being the system default
	 * browser.
	 * 
	 * @return the shared instance of the external browser
	 * @exception PartInitException
	 *                if the operation failed for some reason
	 */
	IWebBrowser getExternalBrowser() throws PartInitException;

	/**
	 * Tests whether web browser as an SWT widget can be created in this
	 * workbench instance. If this method returns <code>false</code>,
	 * <code>createBrowser</code> would ignore browser styles
	 * <code>AS_EDITOR</code> and <code>AS_VIEW</code> and always create an
	 * external browser.
	 * 
	 * @return <code>true</code> if internal web browser can be created on
	 *         this platform, <code>false</code> otherwise.
	 */
	boolean isInternalWebBrowserAvailable();
}