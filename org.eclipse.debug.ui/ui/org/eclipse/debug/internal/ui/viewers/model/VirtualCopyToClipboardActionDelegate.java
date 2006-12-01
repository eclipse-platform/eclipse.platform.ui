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
package org.eclipse.debug.internal.ui.viewers.model;

 
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
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

public class VirtualCopyToClipboardActionDelegate extends AbstractDebugActionDelegate {
	
	private ContentViewer fViewer;
	private static final String TAB = "\t"; //$NON-NLS-1$
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final String SEPARATOR = "line.separator"; //$NON-NLS-1$
	private boolean fDone = false;
	
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
	protected void append(TreeItem item, StringBuffer buffer, int indent) {
		for (int i= 0; i < indent; i++) {
			buffer.append(TAB);
		}
		String[] labels = null;
		if (((InternalTreeModelViewer)fViewer).isShowColumns()) {
			labels = new String[((InternalTreeModelViewer)fViewer).getPresentationContext().getColumns().length];
			for (int i = 0; i < labels.length; i++) {
				labels[i] = item.getText(i);
			}
		} else {
			labels = new String[]{item.getText()};
		}
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
		if (fViewer instanceof InternalTreeModelViewer) {
			InternalTreeModelViewer viewer = (InternalTreeModelViewer) fViewer;
			fDone = false;
			final Object lock = new Object();
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(fViewer.getControl().getShell());
			final IProgressMonitor monitor = dialog.getProgressMonitor();
			dialog.setCancelable(true);
			
			boolean queued = false;
			ILabelUpdateListener listener = new ILabelUpdateListener() {
				public void labelUpdatesComplete() {
					synchronized (lock) {
						fDone = true;
						lock.notifyAll();
					}
				}
				public void labelUpdatesBegin() {
				}
				public void labelUpdateStarted(ILabelUpdate update) {
				}
				public void labelUpdateComplete(ILabelUpdate update) {
					monitor.worked(1);
				}
			};
			viewer.addLabelUpdateListener(listener);
			queued = viewer.populateVitrualItems(); 
			
			if (queued) { 
				IRunnableWithProgress runnable = new IRunnableWithProgress() {
					public void run(final IProgressMonitor m) throws InvocationTargetException, InterruptedException {
						m.beginTask(DebugUIPlugin.removeAccelerators(getAction().getText()), IProgressMonitor.UNKNOWN);
						synchronized (lock) { 
							if (!fDone) {
								lock.wait();
							}
						}
						m.done();
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
			}
			
			viewer.removeLabelUpdateListener(listener);
			if (!monitor.isCanceled()) {
				List roots = getPrunedSelection();
				Iterator iterator = roots.iterator();
				StringBuffer buffer = new StringBuffer();
				while (iterator.hasNext()) {
					TreeItem item = (TreeItem) iterator.next();
					copy(item, buffer, 0);
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
	
	/**
	 * @param item
	 * @param buffer
	 */
	protected void copy(TreeItem item, StringBuffer buffer, int indent) {
		append(item, buffer, indent);
		if (item.getExpanded()) {
			TreeItem[] items = item.getItems();
			for (int i = 0; i < items.length; i++) {
				copy(items[i], buffer, indent + 1);
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
