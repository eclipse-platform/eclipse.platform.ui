/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup;

import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.internal.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.swt.widgets.Shell;


/**
 * User interface used to create a new source container. Contributed via
 * a source container presentation extension.
 * <p>
 * THIS INTERFACE IS EXPERIMENTAL AND SUBJECT TO CHANGE
 * </p>
 * 
 *@see ISourceContainerType
 *@since 3.0
 */
public interface ISourceContainerBrowser {
	/**
	 * Creates and returns new source containers for a source container type
	 * the user has selected to add to the given launch configuration.
	 * 
	 * @param shell the shell to use to display any dialogs
	 * @param director TODO
	 * @return the new source container or containers
	 */
	public ISourceContainer[] createSourceContainers(Shell shell, ISourceLookupDirector director);
	
}
