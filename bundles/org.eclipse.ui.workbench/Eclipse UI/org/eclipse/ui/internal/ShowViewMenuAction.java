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
package org.eclipse.ui.internal;

import org.eclipse.ui.help.WorkbenchHelp;

public class ShowViewMenuAction extends ShowPartPaneMenuAction {

/**
 * Constructor for ShowViewMenuAction.
 * @param window
 */
public ShowViewMenuAction(WorkbenchWindow window) {
	super(window);
	WorkbenchHelp.setHelp(this, IHelpContextIds.SHOW_VIEW_MENU_ACTION);
}

/**
 * Initialize the menu text and tooltip.
 */
protected void initText() {
	setText(WorkbenchMessages.getString("ShowViewMenuAction.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("ShowViewMenuAction.toolTip")); //$NON-NLS-1$
}
/**
 * Show the pane title menu.
 */
protected void showMenu(PartPane pane) {
	pane.showViewMenu();
}

/**
 * Updates the enabled state.
 */
protected void updateState() {
	super.updateState();

	//All of the conditions in the super class passed
	//now check for the menu.
	if (isEnabled()) {
		PartPane pane = (((PartSite) getActivePart().getSite()).getPane());
		setEnabled(
			(pane instanceof ViewPane)
			&& ((ViewPane) pane).hasViewMenu());
	}
}
}
