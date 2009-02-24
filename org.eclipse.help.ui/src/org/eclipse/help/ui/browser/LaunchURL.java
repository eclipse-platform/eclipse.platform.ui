/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.browser;

import java.util.Hashtable;

import org.eclipse.core.runtime.*;
import org.eclipse.help.browser.*;
import org.eclipse.help.internal.browser.*;
import org.eclipse.help.ui.internal.*;
import org.eclipse.help.ui.internal.util.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;

/**
 * Action that launches a URL in a browser.
 * <p>
 * This class is intended to be specified as a value of a class attribute of an
 * action element in plugin.xml for extensions of org.eclipse.ui.actionSets
 * extension point.  The URL to launch must be specified in the markup in one
 * of two ways.
 * </p>
 * </p>
 * The action element can have an attribute named url, in addition to markup
 * required by org.eclipse.ui.actionSets extension point specification. The
 * value of the url attribute should specify a URL to be opened in a browser.
 * </p>
 * </p>
 * Alternatively, since 3.1, instead of a class attribute on the action element,
 * the extension can specify a nested class element with a class attribute
 * and URL specified in a parameter sub-element.  For example:
 * <pre>         &lt;class class="org.eclipse.help.ui.browser.LaunchURL"&gt;
 *              &lt;parameter name="url" value="http://eclipse.org/" /&gt;
 *          &lt;/class&gt;</pre>
 * </p>
 */
public class LaunchURL implements IWorkbenchWindowActionDelegate,
		IExecutableExtension {
	private String url;

	/**
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}

	/**
	 * @see IExecutableExtension#setInitializationData(IConfigurationElement,
	 *      String, Object)
	 */
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		if (data != null && data instanceof Hashtable) {
			url = (String) ((Hashtable) data).get("url"); //$NON-NLS-1$
		}
		if (url == null || url.length() == 0)
			url = config.getAttribute("url"); //$NON-NLS-1$
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (url == null || "".equals(url)) { //$NON-NLS-1$
			return;
		}
		IBrowser browser = BrowserManager.getInstance().createBrowser(true);
		try {
			browser.displayURL(url);
		} catch (Exception e) {
			HelpUIPlugin.logError("Exception occurred when opening URL: " + url //$NON-NLS-1$
					+ ".", e); //$NON-NLS-1$
			ErrorUtil.displayErrorDialog(Messages.LaunchURL_exception); 
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
