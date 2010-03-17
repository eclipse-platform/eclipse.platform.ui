/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *     Pawel Piech (Wind River) - added a breadcrumb mode to Debug view (Bug 252677)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.viewers.FindElementDialog;
import org.eclipse.debug.internal.ui.viewers.model.InternalTreeModelViewer.VirtualElement;
import org.eclipse.debug.internal.ui.viewers.model.InternalTreeModelViewer.VirtualModel;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;

import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.texteditor.IUpdate;

import com.ibm.icu.text.MessageFormat;

/**
 * Action which prompts the user to find/navigate to an element in a virtual tree.
 * 
 * @since 3.3
 */
public class VirtualFindAction extends Action implements IUpdate {
	
	private InternalTreeModelViewer fViewer;
	
	class FindLabelProvider extends LabelProvider {
		
		public FindLabelProvider() {
		}

		public Image getImage(Object element) {
			return ((VirtualElement)element).getImage();
		}

		public String getText(Object element) {
			return ((VirtualElement)element).getLabel()[0];
		}
		
	}

	public VirtualFindAction(InternalTreeModelViewer viewer) {
		setText(ActionMessages.FindAction_0);
		setId(DebugUIPlugin.getUniqueIdentifier() + ".FindElementAction"); //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.FIND_ELEMENT_ACTION);
		setActionDefinitionId(IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE);
		fViewer = viewer;
	}

	public void run() {
		final VirtualModel model = fViewer.buildVirtualModel(null, null);
		ProgressMonitorDialog dialog = new TimeTriggeredProgressMonitorDialog(fViewer.getControl().getShell(), 500);
		final IProgressMonitor monitor = dialog.getProgressMonitor();
		dialog.setCancelable(true);
				 
		String[] columns = fViewer.getPresentationContext().getColumns();
		String[] temp = null;
		if (columns == null || columns.length == 0) {
			temp = null;
		} else {
			temp = new String[]{columns[0]};
		}
		final String[] IDs = temp;
		final Object[] result = new Object[1];
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(final IProgressMonitor m) throws InvocationTargetException, InterruptedException {
				result[0] = model.populate(m, DebugUIPlugin.removeAccelerators(getText()), IDs);
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
		
		VirtualElement root = (VirtualElement) result[0];
		if (!monitor.isCanceled()) {
			List list = new ArrayList();
			collectAllChildren(root, list);
			performFind(list.toArray());
		}

	}
	
	/**
	 * Adds all children to the given list recursively.
	 * 
	 * @param collect
	 */
	private void collectAllChildren(VirtualElement element, List collect) {
		VirtualElement[] children = element.getChildren();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				if (!children[i].isFiltered()) {
					collect.add(children[i]);
					collectAllChildren(children[i], collect);
				}
			}
		}
	}
	
	protected void performFind(Object[] items) {
		FindElementDialog dialog = new FindElementDialog(fViewer.getControl().getShell(), new FindLabelProvider(), items);
		dialog.setTitle(ActionMessages.FindDialog_3);
		dialog.setMessage(ActionMessages.FindDialog_1);
		if (dialog.open() == Window.OK) {
			Object[] elements = dialog.getResult();
			if (elements.length == 1) {
				VirtualElement element = (VirtualElement)elements[0];
				TreePath path = element.realize();
				if (path != null) {
					fViewer.setSelection(new TreeSelection(path), true, true);
				} else {
					DebugUIPlugin.errorDialog(fViewer.getControl().getShell(), ActionMessages.VirtualFindAction_0,
							MessageFormat.format(ActionMessages.VirtualFindAction_1, new String[]{element.getLabel()[0]}),
							(IStatus)null);
				}
			}
		}
	}
	
	public void update() {
		setEnabled(fViewer.getInput() != null);
	}
	
}
