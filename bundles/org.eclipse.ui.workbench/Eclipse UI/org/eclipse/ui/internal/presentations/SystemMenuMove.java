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

import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.presentations.IStackPresentationSite;

public class SystemMenuMove extends MenuManager {

    private IStackPresentationSite stackPresentationSite;

    private String movePart;

    private SystemMenuMovePane movePaneAction;

    private SystemMenuMoveFolder moveFolderAction;

    public SystemMenuMove(IStackPresentationSite stackPresentationSite,
            String partName) {
        super(WorkbenchMessages.getString("PartPane.move")); //$NON-NLS-1$
        this.stackPresentationSite = stackPresentationSite;
        this.movePart = partName;

        movePaneAction = new SystemMenuMovePane(stackPresentationSite);
        movePaneAction.setText(partName);
        moveFolderAction = new SystemMenuMoveFolder(stackPresentationSite);

        add(movePaneAction);
        add(moveFolderAction);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.MenuManager#update(boolean, boolean)
     */
    protected void update(boolean force, boolean recursive) {
        movePaneAction.update();
        moveFolderAction.update();

        super.update(force, recursive);
    }

}