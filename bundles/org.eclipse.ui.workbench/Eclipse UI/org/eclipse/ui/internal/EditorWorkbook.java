/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *  IBM Corporation - initial API and implementation 
 * 	Cagatay Kavukcuoglu <cagatayk@acm.org> - Fix for bug 10025 - Resizing views 
 *    should not use height ratios		
 ************************************************************************/

package org.eclipse.ui.internal;

import java.util.Arrays;
import java.util.List;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.presentations.SystemMenuCloseAllEditors;
import org.eclipse.ui.internal.presentations.SystemMenuCloseOtherEditors;
import org.eclipse.ui.internal.presentations.SystemMenuPinEditor;
import org.eclipse.ui.internal.presentations.SystemMenuSize;
import org.eclipse.ui.internal.presentations.UpdatingActionContributionItem;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
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
public class EditorWorkbook extends PartStack {
	
    private EditorArea editorArea;

    private WorkbenchPage page;  
    
    private SystemMenuSize sizeItem = new SystemMenuSize(null);
    private SystemMenuPinEditor pinEditorItem = new SystemMenuPinEditor(null);
    private SystemMenuCloseAllEditors closeAllItem = new SystemMenuCloseAllEditors(null);
    private SystemMenuCloseOtherEditors closeAllOthersItem = new SystemMenuCloseOtherEditors(null);
	 
    public EditorWorkbook(EditorArea editorArea, WorkbenchPage page) {
        super(); //$NON-NLS-1$
        this.editorArea = editorArea;
        setID(this.toString());
        // Each folder has a unique ID so relative positioning is unambiguous.
        // save off a ref to the page
        //@issue is it okay to do this??
        //I think so since a PartTabFolder is
        //not used on more than one page.
        this.page = page;
    }
    
	public void addSystemActions(IMenuManager menuManager) {
		pinEditorItem = new SystemMenuPinEditor((EditorPane)getVisiblePart());
		appendToGroupIfPossible(menuManager, "misc", new UpdatingActionContributionItem(pinEditorItem)); //$NON-NLS-1$
		sizeItem = new SystemMenuSize((PartPane)getVisiblePart());
		appendToGroupIfPossible(menuManager, "size", sizeItem); //$NON-NLS-1$
		closeAllOthersItem = new SystemMenuCloseOtherEditors((EditorPane)getVisiblePart());
		appendToGroupIfPossible(menuManager, "close", new UpdatingActionContributionItem(closeAllOthersItem)); //$NON-NLS-1$
		closeAllItem = new SystemMenuCloseAllEditors((EditorPane)getVisiblePart());
		appendToGroupIfPossible(menuManager, "close", new UpdatingActionContributionItem(closeAllItem)); //$NON-NLS-1$
	}

    public boolean isMoveable(IPresentablePart part) {
    	return true;
    }

    public boolean isCloseable(IPresentablePart part) {
        return true;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.IStackPresentationSite#supportsState(int)
	 */
	public boolean supportsState(int state) {
		return state == IStackPresentationSite.STATE_MAXIMIZED || state == IStackPresentationSite.STATE_RESTORED;
	}

    /**
     * Factory method for editor workbooks.
     */
    public static EditorWorkbook newEditorWorkbook(EditorArea editorArea,
            WorkbenchPage page) {
        return new EditorWorkbook(editorArea, page);
    }
    
    protected void add(LayoutPart newChild, IPresentablePart position) {
    	super.add(newChild, position);
        
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

    public void createControl(Composite parent) {

    	if (!isDisposed()) {
    		return;
    	}
    	
        AbstractPresentationFactory factory = ((WorkbenchWindow) page
                .getWorkbenchWindow()).getWindowConfigurer()
                .getPresentationFactory();
        super.createControl(parent, factory.createPresentation(parent,
                getPresentationSite(),
                AbstractPresentationFactory.ROLE_EDITOR_WORKBOOK, page
                        .getPerspective().getId(), getID()));

    }
    
    public void setSelection(LayoutPart part) {

        super.setSelection(part);
        
        EditorPane pane = (EditorPane)part;
        
        sizeItem.setPane(pane);
        pinEditorItem.setPane(pane);
        closeAllItem.setPane(pane);
        closeAllOthersItem.setPane(pane);
    }

    /**
     * Update the container to show the correct visible tab based on the
     * activation list.
     * 
     * @param org.eclipse.ui.internal.ILayoutContainer
     */
    private void updateContainerVisibleTab() {
        LayoutPart[] parts = getChildren();

        if (parts.length < 1) {
            setSelection(null);
            return;
        }

        PartPane selPart = null;
        int topIndex = 0;
        IWorkbenchPartReference sortedPartsArray[] = page.getSortedParts();
        List sortedParts = Arrays.asList(sortedPartsArray);
        for (int i = 0; i < parts.length; i++) {
            if (parts[i] instanceof PartPane) {
                IWorkbenchPartReference part = ((PartPane) parts[i])
                        .getPartReference();
                int index = sortedParts.indexOf(part);
                if (index >= topIndex) {
                    topIndex = index;
                    selPart = (PartPane) parts[i];
                }
            }
        }

        setSelection(selPart);
    }
    
    public Control[] getTabList() {
    	return getTabList(getVisiblePart());
    }

    public void removeAll() {
    	LayoutPart[] children = getChildren();
    	
        for (int i = 0; i < children.length; i++)
            remove((EditorPane) children[i]);
    }

    public boolean isActiveWorkbook() {
        EditorArea area = getEditorArea();

        if (area != null)
            return area.isActiveWorkbook(this);
        else
            return false;
    }

    public void becomeActiveWorkbook(boolean hasFocus) {
        EditorArea area = getEditorArea();

        if (area != null) area.setActiveWorkbook(this, hasFocus);
    }

    public void tabFocusHide() {
        if (getControl() == null) return;

        setActive(false /*isActiveWorkbook()*/);
    }

    public void tabFocusShow(boolean hasFocus) {
        if (getControl() == null) return;

        setActive(hasFocus);
    }

    public EditorPane[] getEditors() {
    	LayoutPart[] children = getChildren();
    	
    	EditorPane[] panes = new EditorPane[children.length];
    	for (int idx = 0; idx < children.length; idx++) {
    		panes[idx] = (EditorPane)children[idx];
    	}
    	
        return panes;
    }

    public EditorArea getEditorArea() {
        return editorArea;
    }

    public EditorPane getVisibleEditor() {
        return (EditorPane) getVisiblePart();
    }

    public void setVisibleEditor(EditorPane editorPane) {
        setSelection(editorPane);
    }

    public void showVisibleEditor() {
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
		EditorPresentation.derefPart(toDeref);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.PartStack#allowsDrop(org.eclipse.ui.internal.PartPane)
	 */
	protected boolean allowsDrop(PartPane part) {
		return part instanceof EditorPane;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.PartStack#setActive(boolean)
	 */
	public void setActive(boolean activeState) {
		int state = StackPresentation.AS_INACTIVE;
		if (activeState) {
			state = StackPresentation.AS_ACTIVE_FOCUS;
		} else if (isActiveWorkbook()){
			state = StackPresentation.AS_ACTIVE_NOFOCUS;
		}
		
		getPresentation().setActive(state);
	}
	
	public void setFocus() {
		super.setFocus();
		becomeActiveWorkbook(true);
	}
}