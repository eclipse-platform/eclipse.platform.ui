/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.views;


import org.eclipse.jface.action.Action;
import org.eclipse.ui.internal.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.CheatSheetPlugin;

/**
 * Action used to enable / disable method filter properties
 */
public class CheatSheetExpandRestoreAction extends Action {
	private CheatSheetViewer viewer;
	private boolean collapsed = false;

	public CheatSheetExpandRestoreAction(String title, boolean initValue, CheatSheetViewer viewer) {
		super(title);
		this.viewer = viewer;
		
		setChecked(initValue);
	}
	
	/*
	 * @see Action#actionPerformed
	 */
	public void run() {
		viewer.toggleExpandRestore();	
	}
	
	public boolean isCollapsed() {
		return collapsed;
	}

	public void setCollapsed(boolean value) {
		super.setChecked(value);
		collapsed = value;
		if(value) {
			setToolTipText(CheatSheetPlugin.getResourceString(ICheatSheetResource.RESTORE_ALL_TOOLTIP));
		} else {
			setToolTipText(CheatSheetPlugin.getResourceString(ICheatSheetResource.COLLAPSE_ALL_BUT_CURRENT_TOOLTIP));
		}
	}
}