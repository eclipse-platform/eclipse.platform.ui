package org.eclipse.ui.internal;

/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials! are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation Cagatay
 * Kavukcuoglu <cagatayk@acm.org>- Fix for bug 10025 - Resizing views should
 * not use height ratios
 ******************************************************************************/

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.internal.presentations.PresentationFactoryUtil;
import org.eclipse.ui.internal.presentations.SystemMenuFastView;
import org.eclipse.ui.internal.presentations.SystemMenuSize;
import org.eclipse.ui.internal.presentations.UpdatingActionContributionItem;
import org.eclipse.ui.presentations.IPresentablePart;

/**
 * Manages a set of ViewPanes that are docked into the workbench window. The container for a ViewStack
 * is always a PartSashContainer (or null), and its children are always either PartPlaceholders or ViewPanes.
 * This contains the real behavior and state for stacks of views, although the widgets for the tabs are contributed
 * using a StackPresentation.
 * 
 * TODO: eliminate ViewStack and EditorStack. PartStack should be general enough to handle editors 
 * and views without any specialization for editors and views. The differences should be in the 
 * presentation and in the PartPanes themselves.
 * 
 * TODO: eliminate PartPlaceholder. Placeholders should not be children of the ViewStack.
 *  
 */
public class ViewStack extends PartStack {

    private boolean allowStateChanges;
    private WorkbenchPage page;
    
    private SystemMenuSize sizeItem = new SystemMenuSize(null);
    private SystemMenuFastView fastViewAction;
    
	public void addSystemActions(IMenuManager menuManager) {
		appendToGroupIfPossible(menuManager, "misc", new UpdatingActionContributionItem(fastViewAction)); //$NON-NLS-1$
		sizeItem = new SystemMenuSize((PartPane)getVisiblePart());
		appendToGroupIfPossible(menuManager, "size", sizeItem); //$NON-NLS-1$
	}
    
    public ViewStack(WorkbenchPage page) {
    	this(page, true);
    }
    
    public ViewStack(WorkbenchPage page, boolean allowsStateChanges) {
    	this(page, allowsStateChanges, PresentationFactoryUtil.ROLE_VIEW);
    }
    
    public ViewStack(WorkbenchPage page, boolean allowsStateChanges, int appearance) {
    	super(appearance);
    	
    	this.page = page;
        setID(this.toString());
        // Each folder has a unique ID so relative positioning is unambiguous.

        this.allowStateChanges = allowsStateChanges;
        fastViewAction = new SystemMenuFastView(getPresentationSite());
    }
    
    protected WorkbenchPage getPage() {
    	return page;
    }

    protected boolean canMoveFolder() {
        Perspective perspective = page.getActivePerspective();

        if (perspective == null) {
            // Shouldn't happen -- can't have a ViewStack without a
            // perspective
            return false; 
        }        	
    	
        return !perspective.isFixedLayout();    	
    }

    protected void updateActions() {
    	ViewPane pane = null;
    	PartPane part = getVisiblePart();
    	
    	if (part instanceof ViewPane) {
    		pane = (ViewPane)part;
    	}
    	
        fastViewAction.setPane(pane);        
        sizeItem.setPane(pane);
    }

    public boolean isCloseable(IPresentablePart part) {
        ViewPane pane = (ViewPane)getPaneFor(part);
        Perspective perspective = page.getActivePerspective();
        if (perspective == null) {
            // Shouldn't happen -- can't have a ViewStack without a
            // perspective
            return true; 
        }
        return perspective.isCloseable(pane.getViewReference());
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.PartStack#isMoveable(org.eclipse.ui.presentations.IPresentablePart)
	 */
	protected boolean isMoveable(IPresentablePart part) {
        ViewPane pane = (ViewPane)getPaneFor(part);
        Perspective perspective = page.getActivePerspective();
        if (perspective == null) {
            // Shouldn't happen -- can't have a ViewStack without a
            // perspective
            return true; 
        }
        return perspective.isMoveable(pane.getViewReference());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.PartStack#supportsState(int)
	 */
	protected boolean supportsState(int newState) {
	    if (page.isFixedLayout())
	        return false;
		return allowStateChanges;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.PartStack#derefPart(org.eclipse.ui.internal.LayoutPart)
	 */
	protected void derefPart(LayoutPart toDeref) {
		page.getActivePerspective().getPresentation()
        	.derefPart(toDeref);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.PartStack#allowsDrop(org.eclipse.ui.internal.PartPane)
	 */
	protected boolean allowsDrop(PartPane part) {
		return part instanceof ViewPane;
	}

}
