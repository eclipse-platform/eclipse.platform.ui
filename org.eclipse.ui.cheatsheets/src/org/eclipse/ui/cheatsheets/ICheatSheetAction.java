/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.cheatsheets;

/**
 * Cheat sheet-aware action.
 * <p>
 * This interface should be implemented by actions 
 * (subclasses of {@link org.eclipse.jface.action.Action}) that provide extra
 * support for use in cheat sheets. These actions get passed additional
 * parameters and the invoking cheat sheet manager.
 * </p>
 * <p>
 * It is strongly recommended that actions intended to be invoked from cheat
 * sheets should report success/fail outcome if running the action might fail
 * (perhaps because the user cancels the action from its dialog). 
 * See {@link org.eclipse.jface.action.Action#notifyResult(boolean)} for
 * details.
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
