/************************************************************************
Copyright (c) 2000, 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal;

import java.util.*;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.*;
import org.eclipse.ui.part.MultiEditor;

/**
 * EditorPresentation is a wrapper for PartTabworkbook.
 */
public class EditorPresentation {
	private WorkbenchPage page;
	private ArrayList editorTable = new ArrayList(4);
	private Map mapEditorToPane = new HashMap(11);
	private EditorArea editorArea;
/**
 * Creates a new EditorPresentation.
 */
public EditorPresentation(WorkbenchPage page) {
	IPartDropListener partDropListener = new IPartDropListener() {
		public void dragOver(PartDropEvent e) {
			onPartDragOver(e);
		};
		public void drop(PartDropEvent e){
			onPartDrop(e);
		};
	};
	
	this.page = page;
	this.editorArea = new EditorArea(IPageLayout.ID_EDITOR_AREA, partDropListener, page);
}
/**
 * Closes all of the editors.
 */
public void closeAllEditors() {
	editorArea.removeAllEditors();
	ArrayList editorsToDispose = (ArrayList) editorTable.clone();
	editorTable.clear();
	for (int i = 0; i < editorsToDispose.size(); i++){
		((EditorPane)editorsToDispose.get(i)).dispose();
	}	
}
/**
 * Closes an editor.   
 *
 * @param part the editor to close
 */
public void closeEditor(IEditorReference ref) {
	EditorPane pane = (EditorPane)((WorkbenchPartReference)ref).getPane();
	closeEditor(pane);
}
/**
 * Closes an editor.   
 *
 * @param part the editor to close
 */
public void closeEditor(IEditorPart part) {
	EditorPane pane = (EditorPane)((PartSite)part.getEditorSite()).getPane();
	closeEditor(pane);
}
/**
 * Closes an editor.   
 *
 * @param part the editor to close
 */
private void closeEditor(EditorPane pane) {
	if (pane != null) {
		if (!(pane instanceof MultiEditorInnerPane))
			editorArea.removeEditor(pane);
		editorTable.remove(pane);
		pane.dispose();
	}
}
/**
 * Deref a given part.  Deconstruct its container as required.
 * Do not remove drag listeners.
 */
private void derefPart(LayoutPart part) {
	// Get vital part stats before reparenting.
	ILayoutContainer oldContainer = part.getContainer();
	
	// Reparent the part back to the main window
	part.reparent(editorArea.getParent());
	// Update container.
	if (oldContainer == null) 
		return;
	oldContainer.remove(part);
	LayoutPart[] children = oldContainer.getChildren();
	if (children == null || children.length == 0){
		// There are no more children in this container, so get rid of it
		if (oldContainer instanceof LayoutPart) {
			LayoutPart parent = (LayoutPart)oldContainer;
			ILayoutContainer parentContainer = parent.getContainer();
			if (parentContainer != null) {
				parentContainer.remove(parent);
				parent.dispose();
			}
		}
	}
}
/**
 * Dispose of the editor presentation. 
 */
public void dispose() {
	if (editorArea != null) {
		editorArea.dispose();
	}
}
/**
 * @see IEditorPresentation
 */
public String getActiveEditorWorkbookID() {
	return editorArea.getActiveWorkbookID();
}
/**
 * Returns an array of the open editors.
 *
 * @return an array of open editors
 */
public IEditorReference[] getEditors() {
	int nSize = editorTable.size();
	IEditorReference [] retArray = new IEditorReference[nSize];
	for (int i = 0; i < retArray.length; i++){
		retArray[i] = ((EditorPane)editorTable.get(i)).getEditorReference();
	}
	return retArray;
}
/**
 * Returns the editor area.
 */
public LayoutPart getLayoutPart() {
	return editorArea;
}
/**
 * Returns the active editor in this perspective.  If the editors appear
 * in a workbook this will be the visible editor.  If the editors are
 * scattered around the workbench this will be the most recent editor
 * to hold focus.
 *
 * @return the active editor, or <code>null</code> if no editor is active
 */
public IEditorReference getVisibleEditor() {
	EditorWorkbook activeWorkbook = editorArea.getActiveWorkbook();
	EditorPane pane = activeWorkbook.getVisibleEditor();
	if (pane != null) {
		IEditorReference result = pane.getEditorReference();
		IEditorPart editorPart = (IEditorPart)result.getPart(false);
		if((editorPart != null) && (editorPart instanceof MultiEditor)) {
			editorPart = ((MultiEditor)editorPart).getActiveEditor();
			EditorSite site = (EditorSite)editorPart.getSite();
			result = (IEditorReference)site.getPane().getPartReference();
		}
		return result;
	}
	return null;
}
/**
 * The active editor has failed to be restored. Find another editor, restore it
 * and make it visible.
 */
public void fixVisibleEditor() {
	EditorWorkbook activeWorkbook = editorArea.getActiveWorkbook();
	EditorPane pane = activeWorkbook.getVisibleEditor();
	if(pane == null) {
		LayoutPart editors[] = activeWorkbook.getChildren();
		if(editors.length > 0)
			pane = (EditorPane)editors[0];
	}
	if(pane != null) {
		IEditorReference result = pane.getEditorReference();
		IEditorPart editorPart = (IEditorPart)result.getPart(true);
		if(editorPart != null)
			activeWorkbook.setVisibleEditor(pane);
	}
}

public void moveEditor(IEditorPart part,int position) {
	EditorPane pane = (EditorPane)((EditorSite)part.getSite()).getPane();
	pane.getWorkbook().reorderTab(pane,position);
}
/**
 * Move a part from one position to another.
 * This implementation assumes the target is
 * an editor workbook. 
 */
private void movePart(LayoutPart part, int position, EditorWorkbook relativePart) {
	EditorArea sashContainer = relativePart.getEditorArea();
	if (sashContainer == null)
		return;
		
	// Remove the part from the current container.
	derefPart(part);
	// Add the part.
	int relativePosition = IPageLayout.LEFT;
	if (position == PartDragDrop.RIGHT)
		relativePosition = IPageLayout.RIGHT;
	else if (position == PartDragDrop.TOP)
		relativePosition = IPageLayout.TOP;
	else if (position == PartDragDrop.BOTTOM)
		relativePosition = IPageLayout.BOTTOM;
	if (part instanceof EditorWorkbook) {
		sashContainer.add(part, relativePosition, (float) 0.5, relativePart);
		((EditorWorkbook)part).becomeActiveWorkbook(true);
	}
	else {
		EditorWorkbook newWorkbook = new EditorWorkbook(editorArea);
		sashContainer.add(newWorkbook, relativePosition, (float) 0.5, relativePart);
		newWorkbook.add(part);
		newWorkbook.becomeActiveWorkbook(true);
	}
}
/**
 * Notification sent during drag and drop operation.
 * Only allow editors and editor workbooks to participate
 * in the drag. Only allow the drop on an editor workbook
 * within the same editor area.
 */
private void onPartDragOver(PartDropEvent e) {
	// If source and target are in different windows reject.
	if (e.dragSource != null && e.dropTarget != null) {
		if (e.dragSource.getWorkbenchWindow() != e.dropTarget.getWorkbenchWindow()) {
			e.relativePosition = PartDragDrop.INVALID;
			return;
		}
	}	
	
	// can't detach editor into its own window
	if (/*!detachable &&*/ e.relativePosition == PartDragDrop.OFFSCREEN){
		e.relativePosition = PartDragDrop.INVALID;
		return;
	}
	// can't drop unless over an editor workbook
	if (!(e.dropTarget instanceof EditorWorkbook)) {
		e.relativePosition = PartDragDrop.INVALID;
		return;
	}
	// handle drag of an editor
	if (e.dragSource instanceof EditorPane) {
		EditorWorkbook sourceWorkbook = ((EditorPane)e.dragSource).getWorkbook();
		// limitations when drop is over editor's own workbook
		if (sourceWorkbook == e.dropTarget) {
			// can't stack/detach/attach from same workbook when only one editor
			if (sourceWorkbook.getItemCount() == 1) {
				e.relativePosition = PartDragDrop.INVALID;
				return;
			}
		}
		
		// can't drop into another editor area
		EditorWorkbook targetWorkbook = (EditorWorkbook)e.dropTarget;
		if (sourceWorkbook.getEditorArea() != targetWorkbook.getEditorArea()) {
			e.relativePosition = PartDragDrop.INVALID;
			return;
		}
		// all seems well
		return;
	}
	// handle drag of an editor workbook
	if (e.dragSource instanceof EditorWorkbook) {
		// can't attach nor stack in same workbook
		if (e.dragSource == e.dropTarget) {
			e.relativePosition = PartDragDrop.INVALID;
			return;
		}
		// can't drop into another editor area
		EditorWorkbook sourceWorkbook = (EditorWorkbook)e.dragSource;
		EditorWorkbook targetWorkbook = (EditorWorkbook)e.dropTarget;
		if (sourceWorkbook.getEditorArea() != targetWorkbook.getEditorArea()) {
			e.relativePosition = PartDragDrop.INVALID;
			return;
		}
		
		// all seems well
		return;
	}
	// invalid case - do not allow a drop to happen
	e.relativePosition = PartDragDrop.INVALID;
}
/**
 * Notification sent when drop happens. Only editors
 * and editor workbooks were allowed to participate.
 * Only an editor workbook in the same editor area as
 * the drag started can accept the drop.
 */
private void onPartDrop(PartDropEvent e) {
	switch (e.relativePosition) {
		case PartDragDrop.OFFSCREEN:
			// This case is not supported and should never
			// happen. See onPartDragOver
			//detach(e.dragSource, e.x, e.y);
			break;
		case PartDragDrop.CENTER:
			if (e.dragSource instanceof EditorPane) {
				EditorWorkbook sourceWorkbook = ((EditorPane)e.dragSource).getWorkbook();
				if (sourceWorkbook == e.dropTarget) {
					sourceWorkbook.reorderTab((EditorPane)e.dragSource, e.cursorX, e.cursorY);
					break;
				}
			}
			stack(e.dragSource, (EditorWorkbook)e.dropTarget);
			break;
		case PartDragDrop.LEFT:
		case PartDragDrop.RIGHT:
		case PartDragDrop.TOP:
		case PartDragDrop.BOTTOM:
			if (page.isZoomed())
				page.zoomOut();
			movePart(e.dragSource, e.relativePosition, (EditorWorkbook)e.dropTarget);
			break;
	}
}
/**
 * Opens an editor within the presentation.  
 * </p>
 * @param part the editor
 */
public void openEditor(IEditorReference ref,IEditorReference[] innerEditors, boolean setVisible) {
	EditorPane pane = new MultiEditorOuterPane(ref, page, editorArea.getActiveWorkbook());
	initPane(pane,ref);
	for (int i = 0; i < innerEditors.length; i++) {
		EditorPane innerPane = new MultiEditorInnerPane(pane,innerEditors[i], page, editorArea.getActiveWorkbook());
		initPane(innerPane,innerEditors[i]);
	}
	// Show the editor.
	editorArea.addEditor(pane);
	if(setVisible)
		setVisibleEditor(ref, true);
}
/**
 * Opens an editor within the presentation.  
 * </p>
 * @param part the editor
 */
public void openEditor(IEditorReference ref,boolean setVisible) {
	
	EditorPane pane = new EditorPane(ref, page, editorArea.getActiveWorkbook());
	initPane(pane,ref);
	
	// Show the editor.
	editorArea.addEditor(pane);
	if(setVisible)
		setVisibleEditor(ref, true);
}
private EditorPane initPane(EditorPane pane, IEditorReference ref) {
	((WorkbenchPartReference)ref).setPane(pane);
	// Record the new editor.
	editorTable.add(pane);
	return pane;
}
/**
 * @see IPersistablePart
 */
public IStatus restoreState(IMemento memento) {
	// Restore the editor area workbooks layout/relationship
	return editorArea.restoreState(memento);
}
/**
 * @see IPersistablePart
 */
public IStatus saveState(IMemento memento) {
	// Save the editor area workbooks layout/relationship
	return editorArea.saveState(memento);
}
/**
 * @see IEditorPresentation
 */
public void setActiveEditorWorkbookFromID(String id) {
	editorArea.setActiveWorkbookFromID(id);
}
/**
 * Brings an editor to the front and gives it focus.
 *
 * @param part the editor to make visible
 * @param setFocus whether to give the editor focus
 * @return true if the active editor was changed, false if not.
 */
public boolean setVisibleEditor(IEditorReference ref, boolean setFocus) {
	IEditorReference visibleEditor = getVisibleEditor();
	if (ref != visibleEditor) {
		IEditorPart part = (IEditorPart)ref.getPart(true);
		EditorPane pane = null;
		if(part != null)
			pane = (EditorPane)((PartSite)part.getEditorSite()).getPane();
		if (pane != null) {
			if(pane instanceof MultiEditorInnerPane) {
				EditorPane parentPane = ((MultiEditorInnerPane)pane).getParentPane();
				EditorWorkbook activeWorkbook = parentPane.getWorkbook();
				EditorPane activePane = activeWorkbook.getVisibleEditor();
				if(activePane != parentPane)
					parentPane.getWorkbook().setVisibleEditor(parentPane);
				else
					return false;
			} else {
				pane.getWorkbook().setVisibleEditor(pane);
			}
			if (setFocus)
				part.setFocus();
			return true;
		}
	}
	return false;
}
private void stack(LayoutPart newPart, EditorWorkbook refPart) {
	editorArea.getControl().setRedraw(false);
	if (newPart instanceof EditorWorkbook) {
		EditorPane visibleEditor = ((EditorWorkbook)newPart).getVisibleEditor();
		LayoutPart[] children = ((EditorWorkbook)newPart).getChildren();
		for (int i = 0; i < children.length; i++)
			stackEditor((EditorPane)children[i], refPart);
		if (visibleEditor != null) {
			visibleEditor.setFocus();
			refPart.becomeActiveWorkbook(true);
			refPart.setVisibleEditor(visibleEditor);
		}
	}
	else {
		stackEditor((EditorPane)newPart, refPart);
		newPart.setFocus();
		refPart.becomeActiveWorkbook(true);
		refPart.setVisibleEditor((EditorPane)newPart);
	}
	editorArea.getControl().setRedraw(true);
}
private void stackEditor(EditorPane newPart, EditorWorkbook refPart) {
	// Remove the part from old container.
	derefPart(newPart);
	// Reparent part and add it to the workbook
	newPart.reparent(refPart.getParent());
	refPart.add(newPart);
}
/**
 * Method getWorkbooks.
 * @return ArrayList
 */
public ArrayList getWorkbooks() {
	return editorArea.getEditorWorkbooks();
}
/**
 * Open the list of editors
 */
public void openEditorList() {
		editorArea.getActiveWorkbook().openEditorList();
}
}
