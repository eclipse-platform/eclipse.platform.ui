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
package org.eclipse.team.internal.ui.synchronize.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;

/**
 * Experimental toolbar that supports toolitem action contributions that support
 * showing a text label.
 */
public class TextToolbarManager extends ToolBarManager {
	
	public TextToolbarManager(int style) {
		super(style);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#add(org.eclipse.jface.action.IAction)
	 */
	public void add(IAction action) {		
		super.add(new ToolItemActionContributionItem(action));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#add(org.eclipse.jface.action.IAction)
	 */
	public void add(IAction action, int preferedSize) {		
		super.add(new ToolItemActionContributionItem(action, preferedSize));
	}
}
