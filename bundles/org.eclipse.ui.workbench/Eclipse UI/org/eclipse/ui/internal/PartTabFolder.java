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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.presentations.SystemMenuFastView;
import org.eclipse.ui.internal.presentations.SystemMenuSize;
import org.eclipse.ui.internal.presentations.UpdatingActionContributionItem;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * Manages a set of ViewPanes that are docked into the workbench window. The container for a PartTabFolder
 * is always a PartSashContainer (or null), and its children are always either PartPlaceholders or ViewPanes.
 * This contains the real behavior and state for stacks of views, although the widgets for the tabs are contributed
 * using a StackPresentation.
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
    private SystemMenuFastView fastViewAction;
    private boolean standalone = false;
    private boolean showTitle = true;
    
	public void addSystemActions(IMenuManager menuManager) {
		appendToGroupIfPossible(menuManager, "misc", new UpdatingActionContributionItem(fastViewAction)); //$NON-NLS-1$
		sizeItem = new SystemMenuSize((PartPane)getVisiblePart());
		appendToGroupIfPossible(menuManager, "size", sizeItem); //$NON-NLS-1$
	}
    
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
        StackPresentation presentation;
        if (isStandalone()) {
            presentation = factory.createStandaloneViewPresentation(parent, getPresentationSite(), getShowTitle());
        }
        else {
            presentation = factory.createViewPresentation(parent, getPresentationSite());
        }
        super.createControl(parent, presentation);
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
            // Shouldn't happen -- can't have a PartTabFolder without a
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
            // Shouldn't happen -- can't have a PartTabFolder without a
            // perspective
            return true; 
        }
        return perspective.isMoveable(pane.getViewReference());
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

    /**
     * Sets whether this represents a standalone folder.
     * A standalone folder contains a single part that cannot be docked with others.
     * It can optionally hide the part's title.
     * 
     * @param standalone whether this is a standalone folder
     * @param showTitle whether to show the title (applies only if standalone is <code>true</code>)
     */
    public void setStandalone(boolean standalone, boolean showTitle) {
        this.standalone = standalone;
        this.showTitle = standalone ? showTitle : true;
    }
    
    /**
     * Returns <code>true</code> iff this is a standalone folder.
     */
    private boolean isStandalone() {
        return standalone;
    }

    /**
     * Returns <code>true</code> iff the title should be shown in the presentation.
     * Applies only to standalone views.
     */
    private boolean getShowTitle() {
        return showTitle;
    }
    
    public IStatus saveState(IMemento memento) {
        IStatus status = super.saveState(memento);
        if (isStandalone()) {
            memento.putString(IWorkbenchConstants.TAG_STANDALONE, IWorkbenchConstants.TRUE);
            memento.putString(IWorkbenchConstants.TAG_SHOW_TITLE, Boolean.toString(getShowTitle()));
        }
        return status;
    }

    public IStatus restoreState(IMemento memento) {
        IStatus status = super.restoreState(memento);
        // restore the standalone and showViewState, defaulting to (false, true) if absent
        if (IWorkbenchConstants.TRUE.equals(memento.getString(IWorkbenchConstants.TAG_STANDALONE))) {
            boolean showTitle = !IWorkbenchConstants.FALSE.equals(memento.getString(IWorkbenchConstants.TAG_SHOW_TITLE));
            setStandalone(true, showTitle);
        }
        return status;
    }
}
