/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.ExecutionException;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.commands.Priority;
import org.eclipse.ui.part.MultiEditor;

/**
 * EditorAreaHelper is a wrapper for PartTabworkbook.
 */
public class EditorAreaHelper {
	private WorkbenchPage page;
	private ArrayList editorTable = new ArrayList(4);
	private EditorSashContainer editorArea;
	private HandlerSubmission openEditorDropDownHandlerSubmission;
	/**
	 * Creates a new EditorAreaHelper.
	 */
	public EditorAreaHelper(WorkbenchPage page) {

		this.page = page;
		this.editorArea = new EditorSashContainer(IPageLayout.ID_EDITOR_AREA, page);
		
		this.editorArea.createControl(page.getClientComposite());
		
        final Shell shell = page.getWorkbenchWindow().getShell();
        IHandler openEditorDropDownHandler = new AbstractHandler() {

            public Object execute(Map parameterValuesByName) throws ExecutionException {
            	displayEditorList();
                return null;
            }
        };
        openEditorDropDownHandlerSubmission = new HandlerSubmission(null,
                shell, null, "org.eclipse.ui.window.openEditorDropDown", //$NON-NLS-1$
                openEditorDropDownHandler, Priority.MEDIUM);
        
    	PlatformUI.getWorkbench().getCommandSupport().addHandlerSubmission(
                openEditorDropDownHandlerSubmission);
	}
	/**
	 * Displays a list of open editors
	 */
	public void displayEditorList() {
		EditorStack activeWorkbook = editorArea.getActiveWorkbook();
    	if (activeWorkbook != null) {
    		activeWorkbook.showPartList();
    	}
	}
	
	/**
	 * Closes all of the editors.
	 */
	public void closeAllEditors() {
		editorArea.removeAllEditors();
		ArrayList editorsToDispose = (ArrayList) editorTable.clone();
		editorTable.clear();
		for (int i = 0; i < editorsToDispose.size(); i++) {
			((EditorPane) editorsToDispose.get(i)).dispose();
		}
	}
	/**
	 * Closes an editor.   
	 *
	 * @param part the editor to close
	 */
	public void closeEditor(IEditorReference ref) {
		EditorPane pane = (EditorPane) ((WorkbenchPartReference) ref).getPane();
		closeEditor(pane);
	}
	/**
	 * Closes an editor.   
	 *
	 * @param part the editor to close
	 */
	public void closeEditor(IEditorPart part) {
		EditorPane pane = (EditorPane) ((PartSite) part.getEditorSite()).getPane();
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
	public static void derefPart(LayoutPart part) {

		// Get vital part stats before reparenting.
		ILayoutContainer oldContainer = part.getContainer();

		// Reparent the part back to the main window
		//part.reparent(editorArea.getParent());
		// Update container.
		if (oldContainer == null)
			return;
		oldContainer.remove(part);
		LayoutPart[] children = oldContainer.getChildren();
		if (children == null || children.length == 0) {
			// There are no more children in this container, so get rid of it
			if (oldContainer instanceof LayoutPart) {
				LayoutPart parent = (LayoutPart) oldContainer;
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
        PlatformUI.getWorkbench().getCommandSupport()
        .removeHandlerSubmission(
                openEditorDropDownHandlerSubmission);

		
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
		IEditorReference[] retArray = new IEditorReference[nSize];
		for (int i = 0; i < retArray.length; i++) {
			retArray[i] = ((EditorPane) editorTable.get(i)).getEditorReference();
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
		EditorStack activeWorkbook = editorArea.getActiveWorkbook();
		EditorPane pane = activeWorkbook.getVisibleEditor();
		if (pane != null) {
			IEditorReference result = pane.getEditorReference();
			IEditorPart editorPart = (IEditorPart) result.getPart(false);
			if ((editorPart != null) && (editorPart instanceof MultiEditor)) {
				editorPart = ((MultiEditor) editorPart).getActiveEditor();
				EditorSite site = (EditorSite) editorPart.getSite();
				result = (IEditorReference) site.getPane().getPartReference();
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
		EditorStack activeWorkbook = editorArea.getActiveWorkbook();
		EditorPane pane = activeWorkbook.getVisibleEditor();
		if (pane == null) {
			LayoutPart editors[] = activeWorkbook.getChildren();
			if (editors.length > 0)
				pane = (EditorPane) editors[0];
		}
		if (pane != null) {
			IEditorReference result = pane.getEditorReference();
			IEditorPart editorPart = (IEditorPart) result.getPart(true);
			if (editorPart != null)
				activeWorkbook.setVisibleEditor(pane);
		}
	}

	public void moveEditor(IEditorPart part, int position) {
		EditorPane pane = (EditorPane) ((EditorSite) part.getSite()).getPane();
		//TODO commented this out during presentations works
		//pane.getWorkbook().reorderTab(pane, position);
	}
	/**
	 * Opens an editor within the presentation but does not give it focus.
	 * </p>
	 * @param part the editor
	 */
	public void openEditor(
		IEditorReference ref,
		IEditorReference[] innerEditors,
		boolean setVisible) {
		EditorPane pane = new MultiEditorOuterPane(ref, page, editorArea.getActiveWorkbook());
		initPane(pane, ref);
		for (int i = 0; i < innerEditors.length; i++) {
			EditorPane innerPane =
				new MultiEditorInnerPane(
					pane,
					innerEditors[i],
					page,
					editorArea.getActiveWorkbook());
			initPane(innerPane, innerEditors[i]);
		}
		// Show the editor.
		editorArea.addEditor(pane);
		if (setVisible)
			setVisibleEditor(ref, false);
	}
	/**
	 * Opens an editor within the presentation but does not give it focus.
	 * </p>
	 * @param part the editor
	 */
	public void openEditor(IEditorReference ref, boolean setVisible) {

		EditorPane pane = new EditorPane(ref, page, editorArea.getActiveWorkbook());
		initPane(pane, ref);

		// Show the editor.
		editorArea.addEditor(pane);
		if (setVisible)
			setVisibleEditor(ref, false);
	}
	private EditorPane initPane(EditorPane pane, IEditorReference ref) {
		((WorkbenchPartReference) ref).setPane(pane);
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
	 * Makes sure the visible editor's tab is visible.
	 */
	public void showVisibleEditor() {
		EditorStack activeWorkbook = editorArea.getActiveWorkbook();
		if (activeWorkbook != null)
			activeWorkbook.showVisibleEditor();
	}
	/**
	 * Brings an editor to the front and optionally gives it focus.
	 *
	 * @param part the editor to make visible
	 * @param setFocus whether to give the editor focus
	 * @return true if the visible editor was changed, false if not.
	 */
	public boolean setVisibleEditor(IEditorReference ref, boolean setFocus) {
		IEditorReference visibleEditor = getVisibleEditor();
		if (ref != visibleEditor) {
			IEditorPart part = (IEditorPart) ref.getPart(true);
			EditorPane pane = null;
			if (part != null)
				pane = (EditorPane) ((PartSite) part.getEditorSite()).getPane();
			if (pane != null) {
				if (pane instanceof MultiEditorInnerPane) {
					EditorPane parentPane = ((MultiEditorInnerPane) pane).getParentPane();
					EditorStack activeWorkbook = parentPane.getWorkbook();
					EditorPane activePane = activeWorkbook.getVisibleEditor();
					if (activePane != parentPane)
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


/**
 * Method getWorkbooks.
 * @return ArrayList
 */
public ArrayList getWorkbooks() {
	return editorArea.getEditorWorkbooks();
}

}
