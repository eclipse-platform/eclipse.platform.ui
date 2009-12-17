/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.help;

import java.net.URL;

import org.eclipse.help.IContext;

/**
 * Abstract base class for the help system UI.
 * <p>
 * The Eclipse platform provides an extension point (<code>"org.eclipse.ui.helpSupport"</code>)
 * for plugging in a help system UI. The help system UI is an optional
 * component; applications may provide a UI for presenting help to the user by
 * implementing a subclass and including the name of their class in the
 * <code>&lt;config&gt;</code> element in an extension to the
 * <code>"org.eclipse.ui.helpSupport"</code> extension point.
 * </p>
 * <p>
 * Note that the standard implementation of the help system UI is provided by
 * the <code>"org.eclipse.help.ui"</code> plug-in. Since the platform can only
 * make use of a single help system UI implementation, make sure that the
 * platform is not configured with more than one plug-in trying to extend this
 * extension point.
 * </p>
 * 
 * @since 3.0
 */
public abstract class AbstractHelpUI {

	/**
	 * Displays the entire help bookshelf.
	 */
	public abstract void displayHelp();

	/**
	 * Displays the help search facility. For backward compatibility, the
	 * default implementation does nothing.
	 * 
	 * @since 3.1
	 */
	public void displaySearch() {
		// do nothing
	}

	/**
	 * Displays the dynamic help for the active context. For backward
	 * compatibility, the default implementation does nothing.
	 * 
	 * @since 3.1
	 */
	public void displayDynamicHelp() {
		// do nothing
	}

	/**
	 * Starts the help search using the help search facility. For backward
	 * compatibility, the default implementation does nothing.
	 * 
	 * @param expression
	 *            the search expression
	 * @since 3.1
	 */
	public void search(String expression) {
		// do nothing
	}

	/**
	 * Resolves the help resource href according to the help system
	 * implementation.
	 * 
	 * @param href
	 *            the help resource
	 * @param documentOnly
	 *            if <code>true</code>, the resulting URL must point at the
	 *            document referenced by href. Otherwise, it can be a URL that
	 *            contains additional elements like navigation that the help
	 *            system adds to the document.
	 * @return the fully resolved URL of the help resource or <code>null</code>
	 *         if not supported. Help systems that use application servers
	 *         typically return URLs with http: protocol. Simple help system
	 *         implementations can return URLs with file: protocol.
	 * 
	 * @since 3.1
	 */
	public URL resolve(String href, boolean documentOnly) {
		return null;
	}

	/**
	 * Displays context-sensitive help for the given context.
	 * <p>
	 * (x,y) coordinates specify the location where the context sensitive help
	 * UI will be presented. These coordinates are screen-relative (ie: (0,0) is
	 * the top left-most screen corner). The platform is responsible for calling
	 * this method and supplying the appropriate location.
	 * </p>
	 * 
	 * @param context
	 *            the context to display
	 * @param x
	 *            horizontal position
	 * @param y
	 *            verifical position
	 */
	public abstract void displayContext(IContext context, int x, int y);

	/**
	 * Displays help content for the help resource with the given URL.
	 * <p>
	 * This method is called by the platform to launch the help system UI,
	 * displaying the documentation identified by the <code>href</code>
	 * parameter.
	 * </p>
	 * <p>
	 * The help system makes no guarantee that all the help resources can be
	 * displayed or how they are displayed.
	 * </p>
	 * 
	 * @param href
	 *            the URL of the help resource.
	 *            <p>
	 *            Valid href are as described in
	 *            {@link  org.eclipse.help.IHelpResource#getHref() IHelpResource.getHref()}
	 *            </p>
	 */
	public abstract void displayHelpResource(String href);

	/**
	 * Returns whether the context-sensitive help window is currently being
	 * displayed.
	 * 
	 * @return <code>true</code> if the context-sensitive help window is
	 *         currently being displayed, <code>false</code> if not
	 */
	public abstract boolean isContextHelpDisplayed();
}
