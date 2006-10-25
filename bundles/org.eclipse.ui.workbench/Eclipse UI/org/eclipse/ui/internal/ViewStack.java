/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Cagatay Kavukcuoglu <cagatayk@acm.org>
 *     - Fix for bug 10025 - Resizing views should not use height ratios
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.internal.layout.ITrimManager;
import org.eclipse.ui.internal.presentations.PresentablePart;
import org.eclipse.ui.internal.presentations.PresentationFactoryUtil;
import org.eclipse.ui.internal.presentations.SystemMenuDetach;
import org.eclipse.ui.internal.presentations.SystemMenuFastView;
import org.eclipse.ui.internal.presentations.SystemMenuSize;
import org.eclipse.ui.internal.presentations.UpdatingActionContributionItem;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.StackPresentation;

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

    private SystemMenuDetach detachViewAction;
    
    public void addSystemActions(IMenuManager menuManager) {
        appendToGroupIfPossible(menuManager,
                "misc", new UpdatingActionContributionItem(fastViewAction)); //$NON-NLS-1$
        appendToGroupIfPossible(menuManager,
        		"misc", new UpdatingActionContributionItem(detachViewAction)); //$NON-NLS-1$
        sizeItem = new SystemMenuSize(getSelection());
        appendToGroupIfPossible(menuManager, "size", sizeItem); //$NON-NLS-1$
    }

    public ViewStack(WorkbenchPage page) {
        this(page, true);
    }

    public ViewStack(WorkbenchPage page, boolean allowsStateChanges) {
        this(page, allowsStateChanges, PresentationFactoryUtil.ROLE_VIEW, null);
    }

    public ViewStack(WorkbenchPage page, boolean allowsStateChanges,
            int appearance, AbstractPresentationFactory factory) {
        super(appearance, factory);

        this.page = page;
        setID(this.toString());
        // Each folder has a unique ID so relative positioning is unambiguous.

        this.allowStateChanges = allowsStateChanges;
        fastViewAction = new SystemMenuFastView(getPresentationSite());
        detachViewAction = new SystemMenuDetach(getPresentationSite());
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

    protected void updateActions(PresentablePart current) {
        ViewPane pane = null;
        
        if (current != null && current.getPane() instanceof ViewPane) {
            pane = (ViewPane) current.getPane();
        }

        fastViewAction.setPane(current);
        detachViewAction.setPane(pane);
        sizeItem.setPane(pane);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartStack#isMoveable(org.eclipse.ui.presentations.IPresentablePart)
     */
    protected boolean isMoveable(IPresentablePart part) {
        ViewPane pane = (ViewPane) getPaneFor(part);
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
        if (page.isFixedLayout()) {
			return false;
		}
        return allowStateChanges;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartStack#derefPart(org.eclipse.ui.internal.LayoutPart)
     */
    protected void derefPart(LayoutPart toDeref) {
        page.getActivePerspective().getPresentation().derefPart(toDeref);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartStack#allowsDrop(org.eclipse.ui.internal.PartPane)
     */
    protected boolean allowsDrop(PartPane part) {
        return part instanceof ViewPane;
    }

    /**
     * Get the presentation for testing purposes.  This is for testing
     * purposes <b>ONLY</b>.
     * 
     * @return the presentation in use for this view stack
     * @since 3.2
     */
    public StackPresentation getTestPresentation() {
    	return getPresentation();
    }

    
    // Trim Stack Support
    
    public void setTrimState(int newTrimState) {
    	if (newTrimState == getTrimState())
    		return;

    	// Remember the new state
    	int oldTrimState = getTrimState();
    	
    	// set the new one
    	super.setTrimState(newTrimState);

		WorkbenchWindow wbw = (WorkbenchWindow) getWorkbenchWindow();
		if (wbw == null)
			return;
		
		// Access workbench context
        Perspective persp = page.getActivePerspective();
    	ITrimManager tbm = wbw.getTrimManager();
    	ViewStackTrimPart viewStackTrim = (ViewStackTrimPart) tbm.getTrim(getID());
    	
    	// Are we moving the View Stack -to- the trim?
    	if (oldTrimState == LayoutPart.TRIMSTATE_NORMAL) {
        	// Remove the real stack from the presentation
        	ContainerPlaceholder ph = null;
	        ph = new ContainerPlaceholder(getID());
	        ph.setRealContainer(this);
	        getContainer().replace(this, ph);
	        page.refreshActiveView();

        	// Is it already in the trim?
        	if (viewStackTrim == null) {
        		// If it's not already in the trim...create it
        		int side = SWT.BOTTOM;
        		if (persp != null)
        			side = persp.calcStackSide(getBounds());
        		
        		viewStackTrim = new ViewStackTrimPart(wbw, ph);
    	    	viewStackTrim.dock(side);
        		tbm.addTrim(side, viewStackTrim);
        	}
	        
        	// Refresh the trim's state and show it
        	viewStackTrim.setPlaceholder(ph);
        	viewStackTrim.refresh();
        	
        	// Make the views 'fast'
        	if (persp != null) {
        		List refs = viewStackTrim.getViewRefs();
        		for (Iterator refIter = refs.iterator(); refIter
						.hasNext();) {
					IViewReference ref = (IViewReference) refIter.next();
					persp.addFastViewHack(ref);
				}
        	}
    		tbm.setTrimVisible(viewStackTrim, true);
    	}
    	
    	// Are we restoring the View Stack -from- the trim?
    	if (newTrimState == LayoutPart.TRIMSTATE_NORMAL) {
    		if (viewStackTrim == null)
    			return;
        	
        	// Make the views un-'fast'
        	if (persp != null) {
        		List refs = viewStackTrim.getViewRefs();
        		for (Iterator refIter = refs.iterator(); refIter
						.hasNext();) {
					IViewReference ref = (IViewReference) refIter.next();
					persp.removeFastViewHack(ref);
				}
        	}
    		
        	// hide the trim widget
        	tbm.setTrimVisible(viewStackTrim, false);
        	
        	// Restore the real container
        	ContainerPlaceholder ph = viewStackTrim.getPlaceholder();
        	ILayoutContainer container = ph.getContainer();
        	LayoutPart ps = ph.getRealContainer();
    		ph.setRealContainer(null);
            container.replace(ph, ps);
    	}
    }
}
