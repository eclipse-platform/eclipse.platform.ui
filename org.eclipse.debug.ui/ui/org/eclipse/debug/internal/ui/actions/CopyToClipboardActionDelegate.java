package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

public class CopyToClipboardActionDelegate extends AbstractDebugActionDelegate {
	
	private ContentViewer fViewer;
	
	/**
	 * @see AbstractDebugActionDelegate#initialize(IAction, ISelection)
	 */
	protected boolean initialize(IAction action, ISelection selection) {
		if (!isInitialized()) {
			IDebugView adapter= (IDebugView)getView().getAdapter(IDebugView.class);
			if (adapter != null) {
				if (adapter.getViewer() instanceof ContentViewer) {
					setViewer((ContentViewer) adapter.getViewer());
				}
				adapter.setAction(getActionId(), action);
			}
			return super.initialize(action, selection);
		} 
		return false;
	}

	protected String getActionId() {
		return IDebugView.COPY_ACTION;
	}
	/**
	 * @see AbstractDebugActionDelegate#isEnabledFor(Object)
	 */
	protected boolean isEnabledFor(Object element) {
		return getViewer() != null && element instanceof IDebugElement;
	}

	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction(Object element, StringBuffer buffer) {
		append(element, buffer, (ILabelProvider)getViewer().getLabelProvider(), 0);
	}

	/** 
	 * Appends the representation of the specified element (using the label provider and indent)
	 * to the buffer.  For elements down to stack frames, children representations
	 * are append to the buffer as well.
	 */
	protected void append(Object e, StringBuffer buffer, ILabelProvider lp, int indent) {
		for (int i= 0; i < indent; i++) {
			buffer.append('\t');
		}
		buffer.append(lp.getText(e));
		buffer.append(System.getProperty("line.separator")); //$NON-NLS-1$
		if (shouldAppendChildren(e)) {
			Object[] children= new Object[0];
			children= getChildren(e);
			for (int i = 0;i < children.length; i++) {
				Object de= children[i];
				append(de, buffer, lp, indent + 1);
			}
		}
	}
	
	protected Object getParent(Object e) {
		return ((ITreeContentProvider) getViewer().getContentProvider()).getParent(e);
	}
	
	/**
	 * Returns the children of the parent after applying the filters
	 * that are present in the viewer.
	 */
	protected Object[] getChildren(Object parent) {
		Object[] children= ((ITreeContentProvider)getViewer().getContentProvider()).getChildren(parent);
		ViewerFilter[] filters= ((StructuredViewer)getViewer()).getFilters();
		if (filters != null) {
			for (int i= 0; i < filters.length; i++) {
				ViewerFilter f = filters[i];
				children = f.filter(getViewer(), parent, children);
			}
		}
		return children;
	}
	
	/**
	 * Do the specific action using the current selection.
	 */
	public void run(IAction action) {
		final Iterator iter= pruneSelection();
		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
			public void run() {
				StringBuffer buffer= new StringBuffer();
				while (iter.hasNext()) {
					doAction(iter.next(), buffer);
				}
				TextTransfer plainTextTransfer = TextTransfer.getInstance();
				Clipboard clipboard= new Clipboard(getViewer().getControl().getDisplay());		
				try {
					doCopy(clipboard, plainTextTransfer, buffer);
				} finally {
					clipboard.dispose();
				}
			}
		});
	}
	
	protected void doCopy(Clipboard clipboard, TextTransfer plainTextTransfer, StringBuffer buffer) {
		try {
			clipboard.setContents(
					new String[]{buffer.toString()}, 
					new Transfer[]{plainTextTransfer});
		} catch (SWTError e){
			if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) {
				throw e;
			}
			if (MessageDialog.openQuestion(getViewer().getControl().getShell(), ActionMessages.getString("CopyToClipboardActionDelegate.Problem_Copying_to_Clipboard_1"), ActionMessages.getString("CopyToClipboardActionDelegate.There_was_a_problem_when_accessing_the_system_clipboard._Retry__2"))) { //$NON-NLS-1$ //$NON-NLS-2$
				doCopy(clipboard, plainTextTransfer, buffer);
			}
		}	
	}
	
	/**
	 * Removes the duplicate items from the selection.
	 * That is, if both a parent and a child are in a selection
	 * remove the child.
	 */
	protected Iterator pruneSelection() {
		IStructuredSelection selection= (IStructuredSelection)getViewer().getSelection();
		List elements= new ArrayList(selection.size());
		Iterator iter= selection.iterator();
		while (iter.hasNext()) {
			Object element= iter.next();
			if (isEnabledFor(element)) {
				if(walkHierarchy(element, elements)) {
					elements.add(element);
				}
			}
		}
		return elements.iterator();
	}
	
	/**
	 * Returns whether the parent of the specified
	 * element is already contained in the collection.
	 */
	protected boolean walkHierarchy(Object element, List elements) {
		Object parent= getParent(element);
		if (parent == null) {
			return true;
		}
		if (elements.contains(parent)) {
			return false;
		}
		return walkHierarchy(parent, elements);		
	}
	
	protected boolean shouldAppendChildren(Object e) {
		return e instanceof IDebugTarget || e instanceof IThread;
	}
			
	protected ContentViewer getViewer() {
		return fViewer;
	}

	protected void setViewer(ContentViewer viewer) {
		fViewer = viewer;
	}
	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction(Object element) {
		//not used
	}
}