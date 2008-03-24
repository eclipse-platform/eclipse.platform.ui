/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations.util;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.presentations.SystemMenuCloseAll;
import org.eclipse.ui.internal.presentations.SystemMenuCloseOthers;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * Implements the standard system menu used by the default presentation.
 * Not intended to be subclassed by clients
 * 
 * @since 3.1
 */
public class StandardEditorSystemMenu extends StandardViewSystemMenu {

    private SystemMenuCloseOthers closeOthers;
    private SystemMenuCloseAll closeAll;
    private IAction openAgain;
    
    /**
     * @param site
     */
    public StandardEditorSystemMenu(IStackPresentationSite site) {
        super(site);
        
        closeOthers = new SystemMenuCloseOthers(site);
        closeAll = new SystemMenuCloseAll(site);
		openAgain = ActionFactory.NEW_EDITOR.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
        menuManager.add(closeOthers);
        menuManager.add(closeAll);
		menuManager.add(new Separator());
		menuManager.add(openAgain);
    }

    String getMoveMenuText() {
    	return WorkbenchMessages.EditorPane_moveEditor;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.ISystemMenu#show(org.eclipse.swt.widgets.Control, org.eclipse.swt.graphics.Point, org.eclipse.ui.presentations.IPresentablePart)
     */
    public void show(Control parent, Point displayCoordinates,
            IPresentablePart currentSelection) {
        closeOthers.setTarget(currentSelection);
        closeAll.update();
        super.show(parent, displayCoordinates, currentSelection);
    }
}
