package org.eclipse.ui.externaltools.internal.ant.antview.actions;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.externaltools.internal.ant.antview.tree.ProjectNode;
import org.eclipse.ui.externaltools.internal.ant.antview.views.AntView;
import org.eclipse.ui.externaltools.internal.ant.antview.views.AntViewContentProvider;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;
import org.eclipse.ui.texteditor.IUpdate;

public class RemoveProjectAction extends Action implements IUpdate {

	public RemoveProjectAction(String label, ImageDescriptor imageDescriptor) {
		super(label, imageDescriptor);
		setToolTipText(label);
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		AntView view= AntUtil.getAntView();
		if (view == null) {
			return;
		}
		IStructuredSelection selection= (IStructuredSelection) view.getTreeViewer().getSelection();
		AntViewContentProvider provider= view.getViewContentProvider();
		Iterator iter= selection.iterator();
		Object element;
		while (iter.hasNext()) {
			element= iter.next();
			if (element instanceof ProjectNode) {
				provider.removeNode((ProjectNode) element);
			}
		}
		view.refresh();
	}

	/**
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		AntView view= AntUtil.getAntView();
		if (view == null) {
			setEnabled(false);
		}
		IStructuredSelection selection= (IStructuredSelection) view.getTreeViewer().getSelection();
		if (selection.isEmpty()) {
			setEnabled(false);
			return;
		}
		Object element;
		Iterator iter= selection.iterator();
		while (iter.hasNext()) {
			element= iter.next();
			if (!(element instanceof ProjectNode)) {
				setEnabled(false);
				return;
			}
		}
		setEnabled(true);
	}

}
