/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.console;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * A logical console. A console is commonly used to display messages. For example,
 * a console may display the output streams of a system process. A console can be
 * displayed by one or more views.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @since 3.0
 */
public interface IConsole {
		
	/**
	 * Returns the name of this console.
	 * 
	 * @return the name of this console
	 */
	public String getName();
	
	/**
	 * Returns an image descriptor for this console, or <code>null</code>
	 * if none.
	 * 
	 * @return an image descriptor for this console, or <code>null</code>
	 *  if none
	 */
	public ImageDescriptor getImageDescriptor();
		
	/**
	 * Creates and returns a new page for this console. The page is displayed
	 * for this console in the console given view.
	 * 
	 * @param view the view in which the page is to be created
	 * @return a page book view page representation of this console
	 */
	public IPageBookViewPage createPage(IConsoleView view);
	
	/**
	 * Adds a listener for changes to properties of this console.
	 * Has no effect if an identical listener is already registered.
	 * <p>
	 * The changes supported by the console view are as follows:
	 * <ul>
	 *   <li><code>IBasicPropertyConstants.P_TEXT</code> - indicates the name
	 *      of a console has changed</li>
	 * 	 <li><code>IBasicPropertyConstants.P_IMAGE</code> - indicates the image
	 *      of a console has changed</li>
	 * </ul>
	 * </p>
	 * <p>
	 * Clients may define additional properties as required.
	 * </p>
	 *
	 * @param listener a property change listener
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener);
	
	/**
	 * Removes the given property listener from this console page.
	 * Has no effect if an identical listener is not alread registered.
	 * 
	 * @param listener a property listener
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener);	

}
