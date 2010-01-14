/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.variables;

 
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.ui.actions.SelectionProviderAction;

/**
 * Action for activating the cell editor in the Variables view's viewer.
 * The action is a selection listener for the viewer, however the 
 * variables view is also responsible for calling {@link #focusCellChanged()}
 * when the focus cell changes within the same selection.
 *
 * @since 3.6
 */
public class ActivateCellEditorAction extends SelectionProviderAction {
    
    private TreeModelViewer fViewer;
    private final boolean fIsFullSelectionMode;

    /**
     * Creates a new ActivateCellEditorAction for the given variables view
     * @param viewer the viewer for which this action will activate 
     * cell editors
     */
	public ActivateCellEditorAction(TreeModelViewer viewer) {
		super(viewer, ActionMessages.ChangeVariableValue_title); 
		fViewer= viewer;
		fIsFullSelectionMode = (fViewer.getControl().getStyle() & SWT.FULL_SELECTION) != 0;
	}

	/**
	 * Notifies the action that the active viewer cell has changed in the viewer.
	 * This method should be called when the selection has changed but the viewer
	 * row has not.
	 * @param newCell The new cell that has focus.
	 */
	public void focusCellChanged(ViewerCell newCell) {
	    update((IStructuredSelection)fViewer.getSelection(), newCell);
	}

	/**
	 * Returns the current focus cell in viewer.
	 */
	protected ViewerCell getFocusCell() {
	    ColumnViewerEditor viewerEditor = fViewer.getColumnViewerEditor();
	    if (viewerEditor != null) {
	        return viewerEditor.getFocusCell();
	    }
	    return null;
	}
	
	/**
	 * Updates the enabled state of this action based
	 * on the selection and active focus cell.
	 */
	protected void update(IStructuredSelection sel, ViewerCell cell) {
	    setEnabled(fIsFullSelectionMode && sel.size() == 1 &&  cell != null && isEnabled(sel.getFirstElement(), cell));
	}
	    
	/**
	 * Returns whether the current cell can be modified by the element modifier.
	 */
	private boolean isEnabled(Object element, ViewerCell cell) {
        IElementEditor elementEditor = (IElementEditor)DebugPlugin.getAdapter(element, IElementEditor.class);
        if (elementEditor != null) {
            String property = null;
            Object[] columnnProperties = fViewer.getColumnProperties();
            if (columnnProperties != null && columnnProperties.length > cell.getColumnIndex()) {
                property = (String)columnnProperties[cell.getColumnIndex()];
            }
            ICellModifier cellModifier = elementEditor.getCellModifier(fViewer.getPresentationContext(), element);
            return cellModifier.canModify(element, property);
        }
        return false;
	}

	/**
	 * @see IAction#run()
	 */
	public void run() {
	    // Triggers a cell editor activation in viewer.
        ViewerCell cell = fViewer.getColumnViewerEditor().getFocusCell();
        if (cell != null) {
            fViewer.triggerEditorActivationEvent(new ColumnViewerEditorActivationEvent(cell));      
        }
	}
	
	/**
	 * @see SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(IStructuredSelection sel) {
		update(sel, getFocusCell());
	}
}

