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

import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * A console page in the console view.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @since 3.0
 */
public interface IConsolePage extends IPageBookViewPage {
	
	/**
	 * Adds a listener for changes to properties of this console page.
	 * Has no effect if an identical listener is already registered.
	 * <p>
	 * The supported property ids are as follows:
	 * <ul>
	 *   <li><code>IWorkbenchPart.PROP_TITLE</code> </li>
	 * </ul>
	 * </p>
	 *
	 * @param listener a property listener
	 */
	public void addPropertyListener(IPropertyListener listener);
	
	/**
	 * Removes the given property listener from this console page.
	 * Has no effect if an identical listener is not alread registered.
	 * 
	 * @param listener a property listener
	 */
	public void removePropertyListener(IPropertyListener listener);

}
