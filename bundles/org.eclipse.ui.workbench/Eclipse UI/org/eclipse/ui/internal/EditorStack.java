/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Cagatay Kavukcuoglu <cagatayk@acm.org> - Fix for bug 10025 - Resizing views
 *     should not use height ratios
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.presentations.PresentablePart;
import org.eclipse.ui.internal.presentations.PresentationFactoryUtil;
import org.eclipse.ui.internal.presentations.SystemMenuPinEditor;
import org.eclipse.ui.internal.presentations.SystemMenuSize;
import org.eclipse.ui.internal.presentations.UpdatingActionContributionItem;
import org.eclipse.ui.internal.presentations.util.TabbedStackPresentation;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * Represents a tab folder of editors. This layout part container only accepts
 * EditorPane parts.
 * 
 * TODO: Make PartStack non-abstract and delete this class. The differences between
 * editors and views should be handled by the presentation or the editors/views themselves.
 */
public class EditorStack extends PartStack {

    private EditorSashContainer editorArea;

    private WorkbenchPage page;

    private SystemMenuSize sizeItem = new SystemMenuSize(null);

    private SystemMenuPinEditor pinEditorItem = new SystemMenuPinEditor(null);

    public EditorStack(EditorSashContainer editorArea, WorkbenchPage page) {
        super(PresentationFactoryUtil.ROLE_EDITOR);
        this.editorArea = editorArea;
        setID(this.toString());
        // Each folder has a unique ID so relative positioning is unambiguous.
        // save off a ref to the page
        //@issue is it okay to do this??
        //I think so since a ViewStack is
        //not used on more than one page.
        this.page = page;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartStack#getPage()
     */
    protected WorkbenchPage getPage() {
        return page;
    }

    public void addSystemActions(IMenuManager menuManager) {
        pinEditorItem = new SystemMenuPinEditor((EditorPane) getSelection());
        appendToGroupIfPossible(menuManager,
                "misc", new UpdatingActionContributionItem(pinEditorItem)); //$NON-NLS-1$
        sizeItem = new SystemMenuSize(getSelection());
        appendToGroupIfPossible(menuManager, "size", sizeItem); //$NON-NLS-1$
    }

    public boolean isMoveable(IPresentablePart part) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IStackPresentationSite#supportsState(int)
     */
    public boolean supportsState(int state) {
        if (page.isFixedLayout()) {
			return false;
		}

        return true;
    }

    /**
     * Factory method for editor workbooks.
     */
    public static EditorStack newEditorWorkbook(EditorSashContainer editorArea,
            WorkbenchPage page) {
        return new EditorStack(editorArea, page);
    }

    protected void add(LayoutPart newChild, Object cookie) {
        super.add(newChild, cookie);

        ((EditorPane) newChild).setWorkbook(this);
    }

    /**
     * See IVisualContainer#add
     */
    public void add(LayoutPart child) {
        super.add(child);

        if (child instanceof EditorPane) {
            ((EditorPane) child).setWorkbook(this);
        }
    }

    protected void updateActions(PresentablePart current) {
        EditorPane pane = null;
        if (current != null && current.getPane() instanceof EditorPane) {
            pane = (EditorPane) current.getPane();
        }

        sizeItem.setPane(pane);
        pinEditorItem.setPane(pane);
    }

    public Control[] getTabList() {
        return getTabList(getSelection());
    }

    public void removeAll() {
        LayoutPart[] children = getChildren();

        for (int i = 0; i < children.length; i++) {
			remove(children[i]);
		}
    }

    public boolean isActiveWorkbook() {
        EditorSashContainer area = getEditorArea();

        if (area != null) {
			return area.isActiveWorkbook(this);
		} else {
			return false;
		}
    }

    public void becomeActiveWorkbook(boolean hasFocus) {
        EditorSashContainer area = getEditorArea();

        if (area != null) {
			area.setActiveWorkbook(this, hasFocus);
		}
    }

    public EditorPane[] getEditors() {
        LayoutPart[] children = getChildren();

        EditorPane[] panes = new EditorPane[children.length];
        for (int idx = 0; idx < children.length; idx++) {
            panes[idx] = (EditorPane) children[idx];
        }

        return panes;
    }

    public EditorSashContainer getEditorArea() {
        return editorArea;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartStack#canMoveFolder()
     */
    protected boolean canMoveFolder() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartStack#derefPart(org.eclipse.ui.internal.LayoutPart)
     */
    protected void derefPart(LayoutPart toDeref) {
        EditorAreaHelper.derefPart(toDeref);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartStack#allowsDrop(org.eclipse.ui.internal.PartPane)
     */
    protected boolean allowsDrop(PartPane part) {
        return part instanceof EditorPane;
    }

    public void setFocus() {
        super.setFocus();
        becomeActiveWorkbook(true);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartStack#close(org.eclipse.ui.presentations.IPresentablePart[])
     */
    protected void close(IPresentablePart[] parts) {

        if (parts.length == 1) {
            close(parts[0]);
            return;
        }

        IEditorReference[] toClose = new IEditorReference[parts.length];
        for (int idx = 0; idx < parts.length; idx++) {
            EditorPane part = (EditorPane) getPaneFor(parts[idx]);
            toClose[idx] = part.getEditorReference();
        }

        WorkbenchPage page = getPage();

        if (page != null) {
            page.closeEditors(toClose, true);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#testInvariants()
     */
    public void testInvariants() {
        super.testInvariants();

        int active = getActive();

        if (active == StackPresentation.AS_ACTIVE_FOCUS) {
            Assert.isTrue(isActiveWorkbook());
        } else if (active == StackPresentation.AS_ACTIVE_NOFOCUS) {
            Assert.isTrue(isActiveWorkbook());
        } else if (active == StackPresentation.AS_INACTIVE) {
            Assert.isTrue(!isActiveWorkbook());
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartStack#restoreState(org.eclipse.ui.IMemento)
     */
    public IStatus restoreState(IMemento memento) {
        Integer expanded = memento.getInteger(IWorkbenchConstants.TAG_EXPANDED);
        setState((expanded == null || expanded.intValue() != IStackPresentationSite.STATE_MINIMIZED) ? IStackPresentationSite.STATE_RESTORED
                : IStackPresentationSite.STATE_MINIMIZED);

        Integer appearance = memento
                .getInteger(IWorkbenchConstants.TAG_APPEARANCE);
        if (appearance != null) {
            this.appearance = appearance.intValue();
        }

        // Determine if the presentation has saved any info here
        savedPresentationState = null;
        IMemento[] presentationMementos = memento
                .getChildren(IWorkbenchConstants.TAG_PRESENTATION);

        for (int idx = 0; idx < presentationMementos.length; idx++) {
            IMemento child = presentationMementos[idx];

            String id = child.getString(IWorkbenchConstants.TAG_ID);

            if (Util.equals(id, getFactory().getId())) {
                savedPresentationState = child;
                break;
            }
        }

        return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.PartStack#saveState(org.eclipse.ui.IMemento)
     */
    public IStatus saveState(IMemento memento) {
        memento
                .putInteger(
                        IWorkbenchConstants.TAG_EXPANDED,
                        (getPresentationSite().getState() == IStackPresentationSite.STATE_MINIMIZED) ? IStackPresentationSite.STATE_MINIMIZED
                                : IStackPresentationSite.STATE_RESTORED);

        memento.putInteger(IWorkbenchConstants.TAG_APPEARANCE, appearance);

        savePresentationState();

        if (savedPresentationState != null) {
            IMemento presentationState = memento
                    .createChild(IWorkbenchConstants.TAG_PRESENTATION);
            presentationState.putMemento(savedPresentationState);
        }

        return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ui.internal.PartStack#setMinimized(boolean)
	 */
	public void setMinimized(boolean minimized) {
    	// 'Smart' minimize; move the editor area to the trim
    	Perspective persp = getPage().getActivePerspective();
    	if (Perspective.useNewMinMax(persp)) {
    		if (minimized) {
    			persp.setEditorAreaState(IStackPresentationSite.STATE_MINIMIZED);
    		}
    		else {
				// First, if we're maximized then revert
				if (persp.getPresentation().getMaximizedStack() != null) {
					PartStack maxStack = persp.getPresentation().getMaximizedStack();
					if (maxStack instanceof ViewStack) {
						maxStack.setState(IStackPresentationSite.STATE_RESTORED);
					}
					else if (maxStack instanceof EditorStack) {
						// We handle editor max through the perspective since it's
						// shared between pages...
						persp.setEditorAreaState(IStackPresentationSite.STATE_RESTORED);
					}
				}
				
    			int curState = persp.getEditorAreaState();
    			if (curState == IStackPresentationSite.STATE_MINIMIZED)
    				curState = IStackPresentationSite.STATE_RESTORED;
    			
    			persp.setEditorAreaState(curState);
    		}
    		
    		refreshPresentationState();
    		//return;
    	}
    	
		super.setMinimized(minimized);
	}

	/**
	 * Changes the editor stack's state to the given one -without-
	 * side-effects. This is used when switching perspectives because
	 * the Editor Area is perspective based but is shared between all
	 * perspectives...
	 * 
	 * @param newState The new state to set the editor stack to
	 */
	public void setStateLocal(int newState) {
		if (newState == getState())
			return;
		
		//isMinimized = getState() == IStackPresentationSite.STATE_MINIMIZED;
		super.setMinimized(newState == IStackPresentationSite.STATE_MINIMIZED);
		presentationSite.setPresentationState(newState);
	}
	
	/**
	 * Cause the folder to hide or show its
	 * Minimize and Maximize affordances.
	 * 
	 * @param show
	 *            <code>true</code> - the min/max buttons are visible.
	 * @since 3.3
	 */
	public void showMinMax(boolean show) {
		StackPresentation pres = getPresentation();
		if (pres == null)
			return;
		
		if (pres instanceof TabbedStackPresentation)
			((TabbedStackPresentation)pres).showMinMax(show);
	}
}
