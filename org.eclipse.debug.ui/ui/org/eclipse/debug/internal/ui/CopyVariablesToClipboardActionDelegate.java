package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.Iterator;
import java.util.List;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.*;
import org.eclipse.jface.viewers.*;

/**
 * Used to copy the values of variables to the clipboard from
 * the Variables and Inspector views.
 */
public class CopyVariablesToClipboardActionDelegate extends CopyToClipboardActionDelegate {
	
	private static final String PREFIX= "copy_variables_to_clipboard_action.";

	protected String getHelpContextId() {
		return IDebugHelpContextIds.COPY_VARIABLES_TO_CLIPBOARD_ACTION;
	}
	
	/**
	 * Only append children that are visible in the tree viewer
	 */
	protected boolean shouldAppendChildren(Object e) {
		return((TreeViewer)fViewer).getExpandedState(e);
	}
	
	/**
	 * @see ControlActionDelegate
	 */
	public void initializeForOwner(ControlAction controlAction) {		
		controlAction.setEnabled(!controlAction.getStructuredSelection().isEmpty());
		fViewer = (ContentViewer)controlAction.getSelectionProvider();		
	}
	
	/**
	 * @see ControlActionDelegate
	 */
	protected String getPrefix() {
		return PREFIX;
	}
	
	/**
	 * Returns the children of the parent after applying the filters
	 * that are present in the viewer.
	 */
	protected Object[] getChildren(Object parent) {
		Object[] children= ((BasicContentProvider)fViewer.getContentProvider()).getChildren(parent);
		ViewerFilter[] filters= ((StructuredViewer)fViewer).getFilters();
		if (filters != null) {
			for (int i= 0; i < filters.length; i++) {
				ViewerFilter f = filters[i];
				children = f.filter(fViewer, parent, children);
			}
		}
		return children;
	}
	
	/**
	 * @see ControlActionDelegate
	 */
	public boolean isEnabledFor(Object element) {
		return element instanceof IDebugElement || element instanceof InspectItem;
	}
	
	/**
	 * Returns whether the parent of the specified
	 * element is already contained in the collection.
	 */
	protected boolean walkHierarchy(Object element, List elements) {
		IDebugElement parent= null;
		if (element instanceof IVariable) {
			parent= ((IVariable)element).getParent();
		}
		if (parent == null || parent.getElementType() == IDebugElement.STACK_FRAME) {
			return true;
		}
		Iterator i= elements.iterator();
		while (i.hasNext()) {
			Object o= i.next();
			try {
				if (o instanceof IVariable
					&& ((IVariable)o).getValue().equals(parent)) {
						return false;
				}
				if (o instanceof InspectItem
					&& ((InspectItem)o).getValue().equals(parent)) {
						return false;
				}
			} catch (DebugException e) {
			}
		}
			
		return walkHierarchy(((IValue)parent).getVariable(), elements);
	}
}