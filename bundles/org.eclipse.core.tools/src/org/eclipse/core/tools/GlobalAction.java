/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IActionBars;

/**
 * An abstract base class for global actions. Global actions
 * are able to register themselves in a view's action bars. 
 */
public abstract class GlobalAction extends Action {
	/**
	 * Constructs an action.
	 * 
	 * @param text the action description
	 */
	GlobalAction(String text) {
		super(text);
		setToolTipText(text);
	}

	/**
	 * Registers this action as a global action handler.
	 * 
	 * @param actionBars the action bars where this action will be registered.
	 * @see IActionBars#updateActionBars()
	 */
	public abstract void registerAsGlobalAction(IActionBars actionBars);
}