/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.navigator;

import org.eclipse.ui.Saveable;

/**
 * @since 3.2
 * 
 * @deprecated use the methods on INavigatorSaveablesService directly.
 */
public interface ISaveablesSourceHelper {

	/**
	 * @return the saveables
	 */
	public Saveable[] getSaveables();

	/**
	 * @return the active saveables based on the current selection
	 */
	public Saveable[] getActiveSaveables();

	/**
	 * 
	 */
	public void dispose();
}
