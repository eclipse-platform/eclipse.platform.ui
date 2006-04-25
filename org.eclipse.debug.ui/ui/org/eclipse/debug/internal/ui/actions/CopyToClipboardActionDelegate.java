/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

 
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.internal.ui.viewers.ILabelResult;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class CopyToClipboardActionDelegate extends AbstractDebugActionDelegate {
	
	private ContentViewer fViewer;
	private static final String TAB = "\t"; //$NON-NLS-1$
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
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
	protected void append(ILabelResult item, StringBuffer buffer, int indent) {
		for (int i= 0; i < indent; i++) {
			buffer.append(TAB);
		}
		String[] labels = item.getLabels();
		int count = labels.length;
		if(count > 0) {
			for (int i = 0; i < count; i++) {
				String text = labels[i];
				if(text != null && !text.trim().equals(EMPTY_STRING)) {
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
		if (fViewer instanceof AsynchronousTreeViewer) {
			// force labels to be created
			final AsynchronousTreeViewer atv = (AsynchronousTreeViewer)fViewer;
			List selectedItems = getPrunedSelection();
			TreeItem[]items = (TreeItem[]) selectedItems.toArray(new TreeItem[selectedItems.size()]);
			final Object[] elements = new Object[selectedItems.size()];
			for (int i = 0; i < elements.length; i++) {
				elements[i] = items[i].getData();
			}
			if (elements.length > 0) {
				final StringBuffer buffer = new StringBuffer();
				IRunnableWithProgress runnable = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask(DebugUIPlugin.removeAccelerators(action.getText()), elements.length * 2);
						for (int i = 0; i < elements.length; i++) {
							Object element = elements[i];
							if (element != null) {
								SubProgressMonitor sub = new SubProgressMonitor(monitor, 1);
								List results = atv.buildLabels(sub, element, ""); //$NON-NLS-1$
								if (monitor.isCanceled()) {
									throw new InterruptedException();
								}
								// now copy to buffer
								performCopy(results, buffer);
								monitor.worked(1);
							} else {
								monitor.worked(2);
							}
						}
						monitor.done();
					}
				};
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(fViewer.getControl().getShell());
				dialog.setCancelable(true);
				try {
					dialog.run(true, true, runnable);
				} catch (InvocationTargetException e) {
					DebugUIPlugin.log(e);
					return;
				} catch (InterruptedException e) {
					return;
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
	
	protected void performCopy(List results, StringBuffer buffer) {
		final Iterator iter= results.iterator();
		if (results.size() > 0) {
			int topLevel = ((ILabelResult)results.get(0)).getDepth();
			while (iter.hasNext()) {
				ILabelResult result = (ILabelResult) iter.next();
				append(result, buffer, result.getDepth() - topLevel);
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
	protected List getPrunedSelection() {
		Control control = fViewer.getControl();
		List items = new ArrayList();
		if (control instanceof Tree) {
			Tree tree = (Tree) control;
			TreeItem[] selection = tree.getSelection();

			for (int i = 0; i < selection.length; i++) {
				TreeItem item = selection[i];
				if (isEnabledFor(item.getData())) {
					if (walkHierarchy(item, items)) {
						items.add(item);
					}
				}
			}
		}
		return items;
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
	
	/**
	 * Only append children that are expanded in the tree viewer
	 */
	protected boolean shouldAppendChildren(TreeItem item) {
		return item.getExpanded();
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
