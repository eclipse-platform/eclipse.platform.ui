/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

 
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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

public class CopyToClipboardActionDelegate extends AbstractDebugActionDelegate {
	
	private TreeViewer fViewer;
	
	/**
	 * @see AbstractDebugActionDelegate#initialize(IAction, ISelection)
	 */
	protected boolean initialize(IAction action, ISelection selection) {
		if (!isInitialized()) {
			IDebugView adapter= (IDebugView)getView().getAdapter(IDebugView.class);
			if (adapter != null) {
				if (adapter.getViewer() instanceof ContentViewer) {
					setViewer((TreeViewer) adapter.getViewer());
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
	protected void doAction(TreeItem item, StringBuffer buffer) {
		append(item, buffer, 0);
	}

	/** 
	 * Appends the representation of the specified element (using the label provider and indent)
	 * to the buffer.  For elements down to stack frames, children representations
	 * are append to the buffer as well.
	 */
	protected void append(TreeItem item, StringBuffer buffer, int indent) {
		for (int i= 0; i < indent; i++) {
			buffer.append('\t');
		}
		buffer.append(item.getText());
		buffer.append(System.getProperty("line.separator")); //$NON-NLS-1$
		if (shouldAppendChildren(item)) {
			TreeItem[] children= item.getItems();
			for (int i = 0;i < children.length; i++) {
				TreeItem child= children[i];
				append(child, buffer, indent + 1);
			}
		}
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
					doAction((TreeItem) iter.next(), buffer);
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
			if (MessageDialog.openQuestion(getViewer().getControl().getShell(), ActionMessages.CopyToClipboardActionDelegate_Problem_Copying_to_Clipboard_1, ActionMessages.CopyToClipboardActionDelegate_There_was_a_problem_when_accessing_the_system_clipboard__Retry__2)) { // 
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
		TreeItem[] selection= getViewer().getTree().getSelection();
		List items= new ArrayList(selection.length);
		for (int i = 0; i < selection.length; i++) {
			TreeItem item= selection[i];
			if (isEnabledFor(item.getData())) {
				if(walkHierarchy(item, items)) {
					items.add(item);
				}
			}			
		}
		return items.iterator();
	}
	
	/**
	 * Returns whether the parent of the specified
	 * element is already contained in the collection.
	 */
	protected boolean walkHierarchy(TreeItem item, List elements) {
		TreeItem parent= item.getParentItem();
		if (parent == null) {
			return true;
		}
		if (elements.contains(parent)) {
			return false;
		}
		return walkHierarchy(parent, elements);		
	}
	
	protected boolean shouldAppendChildren(TreeItem item) {
		Object data= item.getData();
		return data instanceof IDebugTarget || data instanceof IThread;
	}
			
	protected TreeViewer getViewer() {
		return fViewer;
	}

	protected void setViewer(TreeViewer viewer) {
		fViewer = viewer;
	}
	/**
	 * @see AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction(Object element) {
		//not used
	}
}
