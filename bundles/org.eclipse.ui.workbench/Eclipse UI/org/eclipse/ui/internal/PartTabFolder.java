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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.presentations.SystemMenuFastView;
import org.eclipse.ui.internal.presentations.SystemMenuSize;
import org.eclipse.ui.internal.presentations.UpdatingActionContributionItem;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.presentations.IPresentablePart;

/**
 * Manages a set of ViewPanes that are docked into the workbench window. The container for a PartTabFolder
 * is always a PartSashContainer (or null), and its children are always either PartPlaceholders or ViewPanes.
 * 
 * TODO: eliminate PartTabFolder and EditorWorkbook. PartStack should be general enough to handle editors 
 * and views without any specialization for editors and views. The differences should be in the 
 * presentation and in the PartPanes themselves.
 * 
 * TODO: eliminate PartPlaceholder. Placeholders should not be children of the PartTabFolder.
 *  
 */
public class PartTabFolder extends PartStack {

    private boolean allowStateChanges;
    private WorkbenchPage page;
    
    private SystemMenuSize sizeItem = new SystemMenuSize(null);
    
	public void addSystemActions(IMenuManager menuManager) {
		appendToGroupIfPossible(menuManager, "misc", new UpdatingActionContributionItem(fastViewAction)); //$NON-NLS-1$
		sizeItem = new SystemMenuSize((PartPane)getVisiblePart());
		appendToGroupIfPossible(menuManager, "size", sizeItem); //$NON-NLS-1$
	}
    
    public boolean isCloseable(IPresentablePart part) {
    	if (part == null) {
    		return canMoveFolder();
    	}
    	
        ViewPane pane = (ViewPane)getPaneFor(part);

        if (pane == null) {
            // Shouldn't happen -- this should only be called for ViewPanes
            // that are already in the tab folder
            return false; 
        }
        
        return !isFixedView(pane);
    }
    
    private SystemMenuFastView fastViewAction;

    public PartTabFolder(WorkbenchPage page) {
    	this(page, true);
    }
    
    public PartTabFolder(WorkbenchPage page, boolean allowsStateChanges) {

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
            // Shouldn't happen -- can't have a PartTabFolder without a
            // perspective
            return false; 
        }        	
    	
        return !perspective.isFixedLayout();    	
    }

    public void createControl(Composite parent) {
    	if (!isDisposed()) {
    		return;
    	}
    	
        AbstractPresentationFactory factory = ((WorkbenchWindow) page
                .getWorkbenchWindow()).getWindowConfigurer()
                .getPresentationFactory();
        super.createControl(parent, factory.createPresentation(parent,
                getPresentationSite(), AbstractPresentationFactory.ROLE_DOCKED_VIEW,
                page.getPerspective().getId(), getID()));
    }

    public void setSelection(LayoutPart part) {
    	super.setSelection(part);

    	ViewPane pane = null;
    	
    	if (part instanceof ViewPane) {
    		pane = (ViewPane)part;
    	}
    	
        fastViewAction.setPane(pane);        
        sizeItem.setPane(pane);
    }
	
	
	public boolean isFixedView(ViewPane pane) {
        Perspective perspective = page.getActivePerspective();

        if (perspective == null) {
            // Shouldn't happen -- can't have a PartTabFolder without a
            // perspective
            return true; 
        }

        return perspective.isFixedView(pane.getViewReference());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.PartStack#isMoveable(org.eclipse.ui.presentations.IPresentablePart)
	 */
	protected boolean isMoveable(IPresentablePart part) {
		return isCloseable(part);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.PartStack#supportsState(int)
	 */
	protected boolean supportsState(int newState) {
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