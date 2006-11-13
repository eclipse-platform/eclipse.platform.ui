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
package org.eclipse.debug.internal.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.viewers.FindElementDialog;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.internal.ui.viewers.ILabelResult;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;

/**
 * Action which prompts the user to find/navigate to an element in an async tree.
 */
public class FindElementAction extends Action implements IUpdate {
	
	private AsynchronousTreeViewer fViewer;
	private IViewPart fView;
	private List fLabelResults;
	private Map fElementToResult = new HashMap();
	
	class FindLabelProvider extends LabelProvider {
		
		public FindLabelProvider() {
		}

		public Image getImage(Object element) {
			ILabelResult result = (ILabelResult) fElementToResult.get(element);
			if (result != null) {
				Image[] images = result.getImages();
				if (images != null && images.length > 0) {
					return images[0];
				}
			}
			return null;
		}

		public String getText(Object element) {
			ILabelResult result = (ILabelResult) fElementToResult.get(element);
			if (result != null) {
				String[] labels = result.getLabels();
				if (labels != null && labels.length > 0) {
					return labels[0];
				}
			}
			return ""; //$NON-NLS-1$
		}
		
	}

	public FindElementAction(IViewPart view, AsynchronousTreeViewer viewer) {
		setText(ActionMessages.FindAction_0);
		setId(DebugUIPlugin.getUniqueIdentifier() + ".FindElementAction"); //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.FIND_ELEMENT_ACTION);
		setActionDefinitionId(IWorkbenchActionDefinitionIds.FIND_REPLACE);
		fViewer = viewer;
		fView = view;
	}

	public void run() {
		final Object element = fViewer.getControl().getData();
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				fLabelResults = fViewer.buildLabels(monitor, element, DebugUIPlugin.removeAccelerators(ActionMessages.FindAction_0));
				if (monitor.isCanceled()) {
					throw new InterruptedException();
				}
			}
		};
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(fView.getSite().getShell());
		dialog.setCancelable(true);
		try {
			dialog.run(true, true, runnable);
		} catch (InvocationTargetException e) {
			DebugUIPlugin.log(e);
			return;
		} catch (InterruptedException e) {
			return;
		}
		Iterator iter = fLabelResults.iterator();
		while (iter.hasNext()) {
			ILabelResult result = (ILabelResult) iter.next();
			fElementToResult.put(result.getElement(), result);
		}		
		performFind();
		fElementToResult.clear();
		fLabelResults.clear();
	}

	protected void performFind() {
		FindElementDialog dialog = new FindElementDialog(fView.getSite().getShell(), new FindLabelProvider(), fElementToResult.keySet().toArray()); 
		dialog.setTitle(ActionMessages.FindDialog_3);
		dialog.setMessage(ActionMessages.FindDialog_1);
		if (dialog.open() == Window.OK) {
			Object[] elements = dialog.getResult();
			if (elements.length == 1) {
				ILabelResult result = (ILabelResult) fElementToResult.get(elements[0]);
				TreePath treePath = result.getTreePath();
				if (treePath != null) {
					fViewer.setSelection(new TreeSelection(treePath), true, true);
				}
			}
		}
	}
	
	public void update() {
		setEnabled(fViewer.getInput() != null);
	}	
	
}
