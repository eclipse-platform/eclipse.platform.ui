package org.eclipse.ui.externaltools.internal.ant.view.actions;
/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.ant.view.elements.ProjectNode;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Action that removes selected build files from an <code>AntView</code>
 */
public class RemoveProjectAction extends Action implements IUpdate {
	
	private AntView view;
	
	public RemoveProjectAction(AntView view) {
		super(AntViewActionMessages.getString("RemoveProjectAction.Remove"), ExternalToolsImages.getImageDescriptor(IExternalToolsUIConstants.IMG_REMOVE)); //$NON-NLS-1$
		this.view= view;
		setToolTipText(AntViewActionMessages.getString("RemoveProjectAction.Remove_2")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		IStructuredSelection selection= (IStructuredSelection) view.getProjectViewer().getSelection();
		Iterator iter= selection.iterator();
		Object element;
		List projectNodes= new ArrayList();
		while (iter.hasNext()) {
			element= iter.next();
			if (element instanceof ProjectNode) {
				projectNodes.add(element);
			}
		}
		view.removeProjects(projectNodes);
	}
	
	/**
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		IStructuredSelection selection= (IStructuredSelection) view.getProjectViewer().getSelection();
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
