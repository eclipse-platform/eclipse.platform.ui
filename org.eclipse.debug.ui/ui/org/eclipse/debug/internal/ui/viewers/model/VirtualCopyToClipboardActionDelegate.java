/*******************************************************************************
 *  Copyright (c) 2000, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

 
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.viewers.model.InternalTreeModelViewer.VirtualElement;
import org.eclipse.debug.internal.ui.viewers.model.InternalTreeModelViewer.VirtualModel;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class VirtualCopyToClipboardActionDelegate extends AbstractDebugActionDelegate {
	
	private ContentViewer fViewer;
	private static final String TAB = "\t"; //$NON-NLS-1$
	private static final String SEPARATOR = "line.separator"; //$NON-NLS-1$
	
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
	 * Appends the representation of the specified element (using the label provider and indent)
	 * to the buffer.  For elements down to stack frames, children representations
	 * are append to the buffer as well.
	 */
	protected void append(VirtualElement item, StringBuffer buffer, int indent) {
		for (int i= 0; i < indent; i++) {
			buffer.append(TAB);
		}
		String[] labels = item.getLabel();
		int count = labels.length;
		if(count > 0) {
			for (int i = 0; i < count; i++) {
				String text = labels[i];
				if(text != null && !text.trim().equals(IInternalDebugCoreConstants.EMPTY_STRING)) {
					buffer.append(text+TAB);
				}
			}
			buffer.append(System.getProperty(SEPARATOR));
		}
	}
	
	/**
	 * Do the specific action using the current selection.
	 */
	public void run(final IAction action) {
		if (fViewer instanceof InternalTreeModelViewer) {
			InternalTreeModelViewer viewer = (InternalTreeModelViewer) fViewer;
			TreeItem[] items = getPrunedSelection();
			TreePath root = null;
			int[] indexes = null;
			if (items.length != 0) {
				TreeItem anItem = items[0];
				TreeItem rootItem = anItem.getParentItem();
				if (rootItem == null) {
					root = TreePath.EMPTY;
				} else {
					root = viewer.getTreePathFromItem(rootItem);
				}
				indexes = new int[items.length];
				for (int i = 0; i < items.length; i++) {
					TreeItem child = items[i];
					if (rootItem == null) {
						indexes[i] = viewer.getTree().indexOf(child);
					} else {
						indexes[i] = rootItem.indexOf(child);
					}
				}
			}
			final VirtualModel model = viewer.buildVirtualModel(root, indexes);
			ProgressMonitorDialog dialog = new TimeTriggeredProgressMonitorDialog(fViewer.getControl().getShell(), 500);
			final IProgressMonitor monitor = dialog.getProgressMonitor();
			dialog.setCancelable(true);
					 
			final String[] columns = viewer.getPresentationContext().getColumns(); 
			final Object[] result = new Object[1];
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(final IProgressMonitor m) throws InvocationTargetException, InterruptedException {
					result[0] = model.populate(m, DebugUIPlugin.removeAccelerators(getAction().getText()), columns);
				}
			};
			try {
				dialog.run(true, true, runnable);
			} catch (InvocationTargetException e) {
				DebugUIPlugin.log(e);
				return;
			} catch (InterruptedException e) {
				return;
			}
			
			VirtualElement modelRoot = (VirtualElement) result[0];
			if (!monitor.isCanceled()) {
				if (root != null) {
					// walk down to nested root
					int depth = root.getSegmentCount();
					for (int i = 0; i < depth; i++) {
						VirtualElement[] children = modelRoot.getChildren();
						for (int j = 0; j < children.length; j++) {
							VirtualElement ve = children[j];
							if (ve != null) {
								modelRoot = ve;
								break;
							}
						}
					}
				}
				VirtualElement[] children = modelRoot.getChildren();
				if (children != null) {
					StringBuffer buffer = new StringBuffer();
					for (int i = 0; i < children.length; i++) {
						if (children[i] != null) {
							copy(children[i], buffer, 0);
						}
					}
					TextTransfer plainTextTransfer = TextTransfer.getInstance();
					Clipboard clipboard= new Clipboard(fViewer.getControl().getDisplay());		
					try {
						doCopy(clipboard, plainTextTransfer, buffer);
					} finally {
						clipboard.dispose();
					}
				}
			}
			
		}
	}
	
	/**
	 * @param item
	 * @param buffer
	 */
	protected void copy(VirtualElement item, StringBuffer buffer, int indent) {
		if (!item.isFiltered()) {
			append(item, buffer, indent);
			VirtualElement[] children = item.getChildren();
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					copy(children[i], buffer, indent + 1);
				}
			}
		}
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
			if (MessageDialog.openQuestion(fViewer.getControl().getShell(), ActionMessages.CopyToClipboardActionDelegate_Problem_Copying_to_Clipboard_1, ActionMessages.CopyToClipboardActionDelegate_There_was_a_problem_when_accessing_the_system_clipboard__Retry__2)) { // 
				doCopy(clipboard, plainTextTransfer, buffer);
			}
		}	
	}
	
	/**
	 * Returns the selected items in the tree, pruning children
	 * if from selected parents.
	 */
	protected TreeItem[] getPrunedSelection() {
		Control control = fViewer.getControl();
		List items = new ArrayList();
		if (control instanceof Tree) {
			Tree tree = (Tree) control;
			TreeItem[] selection = tree.getSelection();
			if (selection.length == 0) {
			    selection = tree.getItems();
			}

			for (int i = 0; i < selection.length; i++) {
				TreeItem item = selection[i];
				if (isEnabledFor(item.getData())) {
					if (walkHierarchy(item, items)) {
						items.add(item);
					}
				}
			}
		}
		return (TreeItem[]) items.toArray(new TreeItem[items.size()]);
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
	
	protected boolean getEnableStateForSelection(IStructuredSelection selection) {
	    if (selection.isEmpty()) {
	        return true;
	    } else {
	        return super.getEnableStateForSelection(selection);
	    }
	}
}
