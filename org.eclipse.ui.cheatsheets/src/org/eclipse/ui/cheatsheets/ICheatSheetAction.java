/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.cheatsheets;

/**
 * Cheat sheet-aware action.
 * <p>
 * This interface should be implemented by actions that provide extra support
 * for use in cheat sheets. These actions can be given parameters and the
 * invoking cheat sheet manager.
 * </p>
 *
 * @since 3.0
 */
public interface ICheatSheetAction {

	/**
	 * Runs this Cheat sheet-aware action.
	 * 
	 * @param params an array of strings
	 * @param manager the cheat sheet manager
	 */
	public void run(String [] params, ICheatSheetManager manager);

}
