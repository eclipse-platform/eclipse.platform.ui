/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.ui.IWorkbenchWindow;

/**
 * Action for showing a view.
 */
public class ShowViewMenuAction extends ShowPartPaneMenuAction {

    /**
     * Constructor for ShowViewMenuAction.
     * @param window
     */
    public ShowViewMenuAction(IWorkbenchWindow window) {
        super(window);
        // @issue missing action id
        window.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.SHOW_VIEW_MENU_ACTION);
        setActionDefinitionId("org.eclipse.ui.window.showViewMenu"); //$NON-NLS-1$
    }

    /**
     * Initialize the menu text and tooltip.
     */
    protected void initText() {
        setText(WorkbenchMessages.ShowViewMenuAction_text);
        setToolTipText(WorkbenchMessages.ShowViewMenuAction_toolTip);
    }

    /**
     * Show the pane title menu.
     */
    protected void showMenu(PartPane pane) {
        pane.showPaneMenu();
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
            setEnabled((pane instanceof ViewPane)
                    && ((ViewPane) pane).hasViewMenu());
        }
    }
    
}
