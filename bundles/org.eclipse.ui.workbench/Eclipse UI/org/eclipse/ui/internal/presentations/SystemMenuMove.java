/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

public class SystemMenuMove extends MenuManager {

    private IStackPresentationSite stackPresentationSite;

    private SystemMenuMovePane movePaneAction;

    private SystemMenuMoveFolder moveFolderAction;
    private boolean assumeActivePart = false;

    public SystemMenuMove(IStackPresentationSite stackPresentationSite,
            String partName) {
        this(stackPresentationSite, partName, true);
    }
    
    public SystemMenuMove(IStackPresentationSite stackPresentationSite,
            String partName, boolean assumeActivePart) {
        super(WorkbenchMessages.PartPane_move);
        this.stackPresentationSite = stackPresentationSite;
        this.assumeActivePart = assumeActivePart;

        movePaneAction = new SystemMenuMovePane(stackPresentationSite);
        movePaneAction.setText(partName);
        moveFolderAction = new SystemMenuMoveFolder(stackPresentationSite);

        add(movePaneAction);
        add(moveFolderAction);
    }

    public void setTarget(IPresentablePart part) {
        movePaneAction.setTarget(part);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.MenuManager#update(boolean, boolean)
     */
    protected void update(boolean force, boolean recursive) {
        if (assumeActivePart) {
            setTarget(stackPresentationSite.getSelectedPart());
        }
        
        moveFolderAction.update();

        super.update(force, recursive);
    }

}
