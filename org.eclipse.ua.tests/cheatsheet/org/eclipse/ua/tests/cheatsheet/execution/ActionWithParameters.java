/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.cheatsheet.execution;

import junit.framework.Assert;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;

/**
 * Action which saves the parameters it was passed. Used for testing
 * parameter passing to action.
 */
public class ActionWithParameters extends Action implements ICheatSheetAction {

	/**
	 * Should never be called
	 */
	public void run() {
		Assert.fail("Should not call this version of run");
	}
	
	public void run(String[] params, ICheatSheetManager manager) {
		ActionEnvironment.setParams(params);
        ActionEnvironment.actionCompleted();
	}

}
