/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.cheatsheet.util;

import org.eclipse.jface.action.Action;

/*
 * An action that increments its own static variable. This is used to test
 * whether or not cheat sheet actions are being run properly (by checking
 * the variable after).
 */
public class IncrementVariableAction extends Action {

	public static int variable = 0;
	
	/*
	 * Increments the variable by one.
	 */
	public void run() {
		variable++;
	}
}
