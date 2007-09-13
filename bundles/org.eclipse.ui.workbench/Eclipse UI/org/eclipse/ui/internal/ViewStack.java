/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.internal.presentations.PresentablePart;
import org.eclipse.ui.internal.presentations.PresentationFactoryUtil;
import org.eclipse.ui.internal.presentations.SystemMenuDetach;
import org.eclipse.ui.internal.presentations.SystemMenuFastView;
import org.eclipse.ui.internal.presentations.SystemMenuSize;
import org.eclipse.ui.internal.presentations.UpdatingActionContributionItem;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;
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

        // We need to search if one of the presentations is not moveable
        // if that's the case the whole folder should not be moveable
        IStackPresentationSite presenationSite;

        if( (presenationSite = getPresentationSite()) != null ) {
        	IPresentablePart[] parts = presenationSite.getPartList();
        	for( int i = 0; i < parts.length; i++ ) {
        		if( !presenationSite.isPartMoveable(parts[i]) ) {
        			return false;
        		}
        	}
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

	/**
	 * Sets the minimized state for this stack. The part may call this method to
	 * minimize or restore itself. The minimized state only affects the view
	 * when unzoomed.
	 *
	 * This implementation is specific to the 3.3 presentation's
	 * min/max story; otherwise it just forwards the call.
	 */
	public void setMinimized(boolean minimized) {
		// 'Smart' minimize; move the stack to the trim
		Perspective persp = getPage().getActivePerspective();
		if (Perspective.useNewMinMax(persp)) {
			FastViewManager fvm = persp.getFastViewManager();
			if (minimized) {
				fvm.moveToTrim(this, false);
			} else {
				// First, if we're maximized then revert
				if (persp.getPresentation().getMaximizedStack() != null) {
					PartStack maxStack = persp.getPresentation().getMaximizedStack();
					if (maxStack != null) {
						maxStack.setState(IStackPresentationSite.STATE_RESTORED);
					}
				}

				fvm.restoreToPresentation(getID());
			}
		}

		super.setMinimized(minimized);
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
}
