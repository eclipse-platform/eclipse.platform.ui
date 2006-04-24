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

 
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.progress.UIJob;

public class CopyToClipboardActionDelegate extends AbstractDebugActionDelegate {
	
	private ContentViewer fViewer;
	private static final String TAB = "\t"; //$NON-NLS-1$
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final String COPY_JOB_NAME = "Copy"; //$NON-NLS-1$
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
			buffer.append(TAB);
		}
		int count = item.getParent().getColumnCount();
		String text = item.getText();
		if(count > 0) {
			for (int i = 0; i < count; i++) {
				text = item.getText(i);
				if(text.trim().equals(EMPTY_STRING)) {
					text = "<no "+item.getParent().getColumn(i).getText().toLowerCase()+">"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				buffer.append(text+TAB);
			}
		}
		else {
			buffer.append(text);
		}
		buffer.append(System.getProperty(SEPARATOR));
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
		if (fViewer instanceof AsynchronousTreeViewer) {
			// force labels to be created
			final AsynchronousTreeViewer atv = (AsynchronousTreeViewer)fViewer;
			atv.forceLabelPopulation();
			Job labelUpdate = new Job(COPY_JOB_NAME) {
				protected IStatus run(IProgressMonitor monitor) {
					int i = 0;
					while (atv.hasPendingUpdates() && i < 30) {
						try {
							Thread.sleep(100);
							i++;
						} catch (InterruptedException e) {
						}
					}
					Job copyJob = new UIJob(COPY_JOB_NAME) {
						public IStatus runInUIThread(IProgressMonitor m) {
							performCopy();
							return Status.OK_STATUS;
						}
					
					};
					copyJob.setSystem(true);
					copyJob.setPriority(Job.INTERACTIVE);
					copyJob.schedule();
					return Status.OK_STATUS;
				}
			};
			labelUpdate.setSystem(true);
			IWorkbenchSiteProgressService ps = (IWorkbenchSiteProgressService) getView().getSite().getAdapter(IWorkbenchSiteProgressService.class);
			if (ps == null) {
				labelUpdate.schedule();
			} else {
				ps.schedule(labelUpdate);
			}
			// wait for labels to complete
		} else {
			performCopy();
		}
	}
	
	protected void performCopy() {
		final Iterator iter= pruneSelection();
		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
			public void run() {
				StringBuffer buffer= new StringBuffer();
				Control ctrl = fViewer.getControl();
				if(ctrl instanceof Tree) {
					Tree tree = (Tree)ctrl;
					TreeColumn[] columns = tree.getColumns();
					if(columns.length > 0) {
						for (int i = 0; i < columns.length; i++) {
							buffer.append(columns[i].getText());
							if(i+1 < columns.length) {
								buffer.append(TAB+TAB);
							}
						}
						buffer.append(System.getProperty(SEPARATOR));
						buffer.append(System.getProperty(SEPARATOR));
					}
				}
				while (iter.hasNext()) {
					doAction((TreeItem) iter.next(), buffer);
				}
				TextTransfer plainTextTransfer = TextTransfer.getInstance();
				Clipboard clipboard= new Clipboard(fViewer.getControl().getDisplay());		
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
			if (MessageDialog.openQuestion(fViewer.getControl().getShell(), ActionMessages.CopyToClipboardActionDelegate_Problem_Copying_to_Clipboard_1, ActionMessages.CopyToClipboardActionDelegate_There_was_a_problem_when_accessing_the_system_clipboard__Retry__2)) { // 
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
