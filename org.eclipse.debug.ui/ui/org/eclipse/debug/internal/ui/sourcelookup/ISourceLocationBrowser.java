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
package org.eclipse.debug.internal.ui.sourcelookup;

import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;
import org.eclipse.swt.widgets.Shell;


/**
 * User interface used to create a new source container. Contributed via
 * a source container presentation extension.
 * <p>
 * THIS INTERFACE IS EXPERIMENTAL AND SUBJECT TO CHANGE
 * </p>
 * 
 * TODO: issue - should we use a wizard instead of a dialog for this? 
 *
 *@since 3.0
 */
public interface ISourceLocationBrowser {
	/**
	* Displays a browse dialog and then uses the input to create a source location.
	* If user input is not required, it can just return a new source location without
	* displaying a browse dialog.
	* @param shell the shell to use to display the dialog
	* @return the new source location or locations (if multiple items selected by user)
	*/
	public ISourceContainer[] createSourceContainers(Shell shell);

}
