/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.provisional.cheatsheets;

import org.eclipse.ui.cheatsheets.ICheatSheetManager;

/**
 * Interface representing a composite cheatsheet. 
 * A composite cheat sheet has a single root task, each task may have 
 * zero or more children.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */

public interface ICompositeCheatSheet {
	
	/**
	 * The ID of the task explorer that should be the
	 * default for this composite cheat sheet. Can be null.
	 * @return the task explorer identifier or <code>null</code> if
	 * the view's default should be used.
	 */
	String getTaskExplorerId();
	
	/**
	 * The root task of this composite cheat sheet
	 * @return the root task object
	 */
	ICompositeCheatSheetTask getRootTask();
	
	/**
	 * Get the manager which allows data to be shared between tasks.
	 * @return the CheatSheetManager for this composite cheat cheet. May not be null.
	 */
	ICheatSheetManager getCheatSheetManager();
}
