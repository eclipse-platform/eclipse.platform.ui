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
package org.eclipse.ui.internal;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.presentations.SystemMenuFloat;
import org.eclipse.ui.internal.presentations.UpdatingActionContributionItem;
import org.eclipse.ui.presentations.IPresentablePart;

/**
 * @since 3.1
 */
public class DetachedViewStack extends ViewStack {
    
    private SystemMenuFloat floatViewAction;
    private boolean isFloating = false;
    
    /**
     * @param page
     * @param allowsStateChanges
     * @param appearance
     */
    public DetachedViewStack(WorkbenchPage page, boolean allowsStateChanges,
            int appearance) {
        super(page, allowsStateChanges, appearance);
        
        floatViewAction = new SystemMenuFloat(this);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartStack#addSystemActions(org.eclipse.jface.action.IMenuManager)
     */
    public void addSystemActions(IMenuManager menuManager) {
	    appendToGroupIfPossible(menuManager,
	      		"misc", new UpdatingActionContributionItem(floatViewAction)); //$NON-NLS-1$
        super.addSystemActions(menuManager);
    }
    
    public boolean isFloating() {
        return isFloating;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartStack#dragStart(org.eclipse.ui.presentations.IPresentablePart, org.eclipse.swt.graphics.Point, boolean)
     */
    public void dragStart(IPresentablePart beingDragged, Point initialLocation,
            boolean keyboard) {
        
        if (isFloating) {
            // If we're floating, drag the window itself
            DragUtil.performDrag(getWindow(), Geometry
                    .toDisplay(getParent(), getPresentation().getControl()
                            .getBounds()), initialLocation, !keyboard);
            return;
        }
        
        // Otherwise, drag the part
        super.dragStart(beingDragged, initialLocation, keyboard);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartStack#updateActions()
     */
    protected void updateActions() {
        super.updateActions();
        
        ViewPane pane = null;
        PartPane part = getVisiblePart();

        if (part instanceof ViewPane) {
            pane = (ViewPane) part;
        }
        
        floatViewAction.setPane(pane);
    }

    /**
     * @param b
     * @since 3.1
     */
    public void setFloatingState(boolean b) {
        isFloating = b;
    }
}
