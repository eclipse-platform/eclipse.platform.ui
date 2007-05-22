/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.part.MultiEditor;

/**
 * EditorAreaHelper is a wrapper for PartTabworkbook.
 */
public class EditorAreaHelper {

    //private ArrayList editorTable = new ArrayList(4);

    private EditorSashContainer editorArea;

    /**
     * Creates a new EditorAreaHelper.
     */
    public EditorAreaHelper(WorkbenchPage page) {
        this.editorArea = new EditorSashContainer(IPageLayout.ID_EDITOR_AREA,
                page, page.getClientComposite());

        this.editorArea.createControl(page.getClientComposite());
        this.editorArea.setActive(true);
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
        EditorPane pane = (EditorPane) ((PartSite) part.getEditorSite())
                .getPane();
        closeEditor(pane);
    }

    /**
     * Closes an editor.   
     *
     * @param part the editor to close
     */
    private void closeEditor(EditorPane pane) {
        if (pane != null) {
            if (!(pane instanceof MultiEditorInnerPane)) {
				editorArea.removeEditor(pane);
			}
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
        if (oldContainer == null) {
			return;
		}
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
        if (editorArea != null) {
            editorArea.setActive(false);
            editorArea.dispose();
        }
    }

    /**
     * @see IEditorPresentation
     */
    public String getActiveEditorWorkbookID() {
        return editorArea.getActiveWorkbookID();
    }

    public EditorStack getActiveWorkbook() {
        return editorArea.getActiveWorkbook();
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
        EditorPane pane = (EditorPane)activeWorkbook.getSelection();
        if (pane != null) {
            IEditorReference result = pane.getEditorReference();
            IEditorPart editorPart = (IEditorPart) result.getPart(false);
            if ((editorPart != null) && (editorPart instanceof MultiEditor)) {
                editorPart = ((MultiEditor) editorPart).getActiveEditor();
                EditorSite site = (EditorSite) editorPart.getSite();
                result = (IEditorReference) site.getPartReference();
            }
            return result;
        }
        return null;
    }

    public void moveEditor(IEditorPart part, int position) {
        /*EditorPane pane = (EditorPane)*/ ((EditorSite) part.getSite()).getPane();
        //TODO commented this out during presentations works
        //pane.getWorkbook().reorderTab(pane, position);
    }



    /**
     * Main entry point for adding an editor. Adds the editor to the layout in the given
     * stack, and notifies the workbench page when done.
     * 
     * @param ref editor to add
     * @param workbookId workbook that will contain the editor (or null if the editor
     * should be added to the default workbook)
     */
    public void addEditor(EditorReference ref, String workbookId) {
        IEditorReference refs[] = editorArea.getPage().getEditorReferences();
        for (int i = 0; i < refs.length; i++) {
            if (ref == refs[i]) {
				return;
			}
        }
        
        if (!(ref.getPane() instanceof MultiEditorInnerPane)) {
            
            EditorStack stack = null;
            
            if (workbookId != null) {
                stack = getWorkbookFromID(workbookId);
            }
            
            if (stack == null) {
                stack = getActiveWorkbook();
            }
            
            addToLayout((EditorPane)ref.getPane(), stack);
        }
        
        editorArea.getPage().partAdded(ref);
    }
    
    private void addToLayout(EditorPane pane, EditorStack stack) {
        //EditorStack stack = editorArea.getActiveWorkbook();
        pane.setWorkbook(stack);
        
        editorArea.addEditor(pane, stack);
    }


    /**
     * @see IPersistablePart
     */
    public IStatus restoreState(IMemento memento) {
        // Restore the editor area workbooks layout/relationship
        return editorArea.restoreState(memento);
    }

    /**
     * Restore the presentation
     * @param areaMem
     * @return
     */
    public IStatus restorePresentationState(IMemento areaMem) {
        return editorArea.restorePresentationState(areaMem);
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
            if (part != null) {
				pane = (EditorPane) ((PartSite) part.getEditorSite()).getPane();
			}
            if (pane != null) {
                if (pane instanceof MultiEditorInnerPane) {
                    EditorPane parentPane = ((MultiEditorInnerPane) pane)
                            .getParentPane();
                    EditorStack activeWorkbook = parentPane.getWorkbook();
                    PartPane activePane = activeWorkbook.getSelection();
                    if (activePane != parentPane) {
						parentPane.getWorkbook().setSelection(parentPane);
					} else {
						return false;
					}
                } else {
                    pane.getWorkbook().setSelection(pane);
                }
                if (setFocus) {
					part.setFocus();
				}
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
    
    public IEditorReference[] getEditors() {
        List result = new ArrayList();
        List workbooks = editorArea.getEditorWorkbooks();
        
        for (Iterator iter = workbooks.iterator(); iter.hasNext();) {
            PartStack stack = (PartStack) iter.next();
            
            LayoutPart[] children = stack.getChildren();
            
            for (int i = 0; i < children.length; i++) {
                LayoutPart part = children[i];
                
                result.add(((PartPane)part).getPartReference());
            }
        }
        
        return (IEditorReference[]) result.toArray(new IEditorReference[result.size()]);
    }

    public EditorStack getWorkbookFromID(String workbookId) {
        return editorArea.getWorkbookFromID(workbookId);
    }

    public void updateStackButtons() {
    	editorArea.updateStackButtons();
    }
}
