/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.part;

/**
 * This interface can be provided by a 'Show In' source that needs to add
 * additional entries to the 'Show In' menu.
 * <p>
 * The part can either directly implement this interface, or provide it via
 * <code>IAdaptable.getAdapter(IShowInTargetList)</code>.
 * </p>
 * 
 * @see IShowInSource
 * @see IShowInTarget
 * 
 * @since 2.1
 */
public interface IShowInTargetList {

	/**
	 * Returns the identifiers for the target part to show. The target views
	 * must be Show In targets.
	 * 
	 * @return the identifiers for the target views to show
	 * 
	 * @see IShowInTarget
	 */
    public String[] getShowInTargetIds();
}
