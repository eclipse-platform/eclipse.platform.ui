/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal;
import org.eclipse.help.*;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.ui.internal.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.*;

/**
 * This class is an implementation of the Help UI. In is registered into the
 * helpSupport extension point, and is responsible for handling requests to
 * display help. The methods on this class interact with the actual UI component
 * handling the display.
 * <p>
 * Most methods delegate most work to HelpDisplay class; only the UI specific
 * ones implemented in place.
 * </p>
 */
public class DefaultHelpUI extends AbstractHelpUI {
	private ContextHelpDialog f1Dialog = null;

	/**
	 * Constructor.
	 */
	public DefaultHelpUI() {
		super();
	}

	/**
	 * Displays help.
	 */
	public void displayHelp() {
		BaseHelpSystem.getHelpDisplay().displayHelp(useExternalBrowser(null));
	}
	/**
	 * Displays a help resource specified as a url.
	 * <ul>
	 * <li>a URL in a format that can be returned by
	 * {@link  org.eclipse.help.IHelpResource#getHref() IHelpResource.getHref()}
	 * <li>a URL query in the format format
	 * <em>key=value&amp;key=value ...</em> The valid keys are: "tab", "toc",
	 * "topic", "contextId". For example,
	 * <em>toc="/myplugin/mytoc.xml"&amp;topic="/myplugin/references/myclass.html"</em>
	 * is valid.
	 * </ul>
	 */
	public void displayHelpResource(String href) {
		BaseHelpSystem.getHelpDisplay().displayHelpResource(href,
				useExternalBrowser(href));
	}
	/**
	 * Displays context-sensitive help for specified context
	 * 
	 * @param context
	 *            the context to display
	 * @param x
	 *            int positioning information
	 * @param y
	 *            int positioning information
	 */
	public void displayContext(IContext context, int x, int y) {
		if (f1Dialog != null)
			f1Dialog.close();
		if (context == null)
			return;
		f1Dialog = new ContextHelpDialog(context, x, y);
		f1Dialog.open();
		// if any errors or parsing errors have occurred, display them in a
		// pop-up
		ErrorUtil.displayStatus();
	}

	/**
	 * Returns <code>true</code> if the context-sensitive help window is
	 * currently being displayed, <code>false</code> if not.
	 */
	public boolean isContextHelpDisplayed() {
		if (f1Dialog == null) {
			return false;
		}
		return f1Dialog.isShowing();
	}
	private boolean useExternalBrowser(String url) {
		// Use external when modal window is displayed
		Display display = Display.getCurrent();
		if (display != null) {
			Shell activeShell = display.getActiveShell();
			if (activeShell != null) {
				if ((activeShell.getStyle() & (SWT.APPLICATION_MODAL
						| SWT.PRIMARY_MODAL | SWT.SYSTEM_MODAL)) > 0) {
					return true;
				}
			}
		}
		// Use external when no help frames are to be displayed, otherwise no
		// navigation buttons.
		if (url != null) {
			if (url.indexOf("?noframes=true") > 0
					|| url.indexOf("&noframes=true") > 0) {
				return true;
			}
		}
		return false;
	}
}
