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
package org.eclipse.ui.internal.presentations;

import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.presentations.IStackPresentationSite;

public class EditorPresentationSystemContribution extends
        PartPaneSystemContribution {

    public EditorPresentationSystemContribution(IStackPresentationSite site) {
        super(site);
    }

    public void fill(Menu menu, int index) {
        addStateContribution(menu, WorkbenchMessages
                .getString("PartPane.restore"),
                IStackPresentationSite.STATE_RESTORED); //$NON-NLS-1$
        addMoveMenuItem(menu, getMovePaneName(), WorkbenchMessages
                .getString("ViewPane.moveFolder")); //$NON-NLS-1$
        addSizeMenuItem(menu);
        addStateContribution(menu, WorkbenchMessages
                .getString("PartPane.maximize"),
                IStackPresentationSite.STATE_MAXIMIZED); //$NON-NLS-1$
        addCloseMenuItem(menu);
    }
}