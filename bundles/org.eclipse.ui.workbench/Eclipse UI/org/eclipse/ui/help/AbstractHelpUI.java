/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.help;

import org.eclipse.help.IContext;

/**
 * Abstract base class for the help system UI.
 * <p>
 * The Eclipse platform provides an extension point 
 * (<code>"org.eclipse.ui.helpSupport"</code>) for plugging in a help system UI.
 * The help system UI is an optional component; applications may provide a UI
 * for presenting help to the user by implementing a subclass and including the
 * name of their class in the <code>&lt;config&gt;</code> element in an
 * extension to the <code>"org.eclipse.ui.helpSupport"</code> extension point.
 * </p>
 * <p>
 * Note that the standard implementation of the help system UI is provided by
 * the <code>"org.eclipse.help.ui"</code> plug-in. Since the platform can only
 * make use of a single help system UI implementation, make sure that the
 * platform is not configured with more than one plug-in trying to extend
 * this extension point.
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
     * Displays context-sensitive help for the given context.
     * <p>
     * (x,y) coordinates specify the location where the context sensitive 
     * help UI will be presented. These coordinates are screen-relative 
     * (ie: (0,0) is the top left-most screen corner).
     * The platform is responsible for calling this method and supplying the 
     * appropriate location.
     * </p>
     * 
     * @param context the context to display
     * @param x horizontal position
     * @param y verifical position
     */
    public abstract void displayContext(IContext context, int x, int y);

    /**
     * Displays help content for the help resource with the given URL.
     * <p>
     * This method is called by the platform to launch the help system UI, displaying
     * the documentation identified by the <code>href</code> parameter.
     * </p> 
     * <p>
     * The help system makes no guarantee that all the help resources can be displayed or how they are displayed.
     * </p>
     * @param href the URL of the help resource.
     * <p>Valid href are as described in 
     * 	{@link  org.eclipse.help.IHelpResource#getHref() IHelpResource.getHref()}
     * </p>
     */
    public abstract void displayHelpResource(String href);

    /**
     * Returns whether the context-sensitive help window is currently being
     * displayed.
     * 
     * @return <code>true</code> if the context-sensitive help
     * window is currently being displayed, <code>false</code> if not
     */
    public abstract boolean isContextHelpDisplayed();
}