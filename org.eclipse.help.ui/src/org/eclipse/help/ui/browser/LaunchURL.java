/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.browser;

import org.eclipse.core.runtime.*;
import org.eclipse.help.ui.internal.browser.BrowserManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.*;

/**
 * Action that launches a URL in a browser.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * <p>
 * This class is intended to be specified as a value
 * of a class attribute of an action element in plugin.xml
 * for extensions of org.eclipse.ui.actionSets extension point.
 * </p>
 * </p>
 * The action element must have an attribute
 * named url, in addition to markup required by
 * org.eclipse.ui.actionSets extension point specification.
 * The value of the url attribute should specify
 * a URL to be opened in a browser.
 * </p>
 */
public class LaunchURL
	implements IWorkbenchWindowActionDelegate, IExecutableExtension {
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
	 * @see IExecutableExtension#setInitializationData(IConfigurationElement, String, Object)
	 */
	public void setInitializationData(
		IConfigurationElement config,
		String propertyName,
		Object data)
		throws CoreException {
		url = config.getAttribute("url");
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (url == null || "".equals(url)) {
			return;
		}
		if (SWT.getPlatform().equals("win32")) {
			Program.launch(url);
		} else {
			IBrowser browser = BrowserManager.getInstance().createBrowser();
			browser.displayURL(url);
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

}