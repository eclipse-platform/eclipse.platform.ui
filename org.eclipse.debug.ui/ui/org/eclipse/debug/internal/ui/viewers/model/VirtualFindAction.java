/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.viewers.FindElementDialog;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;

/**
 * Action which prompts the user to find/navigate to an element in a virtual tree.
 * 
 * @since 3.3
 */
public class VirtualFindAction extends Action implements IUpdate {
	
	private InternalTreeModelViewer fViewer;
	private boolean fDone = false;
	
	class FindLabelProvider extends LabelProvider {
		
		public FindLabelProvider() {
		}

		public Image getImage(Object element) {
			return ((TreeItem)element).getImage();
		}

		public String getText(Object element) {
			return ((TreeItem)element).getText();
		}
		
	}

	public VirtualFindAction(InternalTreeModelViewer viewer) {
		setText(ActionMessages.FindAction_0);
		setId(DebugUIPlugin.getUniqueIdentifier() + ".FindElementAction"); //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.FIND_ELEMENT_ACTION);
		setActionDefinitionId(IWorkbenchActionDefinitionIds.FIND_REPLACE);
		fViewer = viewer;
	}

	public void run() {
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
		fViewer.addLabelUpdateListener(listener);
		queued = fViewer.populateVitrualItems(); 
		
		if (queued) { 
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(final IProgressMonitor m) throws InvocationTargetException, InterruptedException {
					m.beginTask(DebugUIPlugin.removeAccelerators(getText()), IProgressMonitor.UNKNOWN);
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
		
		fViewer.removeLabelUpdateListener(listener);
		if (!monitor.isCanceled()) {
			Tree tree = (Tree) fViewer.getControl();
			List items = new ArrayList();
			collectItems(items, tree.getItems());
			performFind(items);
		}
	}
	
	private void collectItems(List list, TreeItem[] items) {
		for (int i = 0; i < items.length; i++) {
			TreeItem treeItem = items[i];
			list.add(treeItem);
			if (treeItem.getExpanded()) {
				collectItems(list, treeItem.getItems());
			}
		}
	}

	protected void performFind(List items) {
		FindElementDialog dialog = new FindElementDialog(fViewer.getControl().getShell(), new FindLabelProvider(), items.toArray()); 
		dialog.setTitle(ActionMessages.FindDialog_3);
		dialog.setMessage(ActionMessages.FindDialog_1);
		if (dialog.open() == Window.OK) {
			Object[] elements = dialog.getResult();
			if (elements.length == 1) {
				TreeItem item = (TreeItem) elements[0];
				List path = new ArrayList();
				while (item != null) {
					path.add(0, item.getData());
					item = item.getParentItem();
				}
				fViewer.setSelection(new TreeSelection(new TreePath(path.toArray())));
			}
		}
	}
	
	public void update() {
		setEnabled(fViewer.getInput() != null);
	}	
	
}
