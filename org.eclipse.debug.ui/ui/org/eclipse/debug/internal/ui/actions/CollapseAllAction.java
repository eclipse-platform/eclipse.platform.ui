/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.handlers.CollapseAllHandler;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * CollapseAllAction
 */
public class CollapseAllAction extends Action implements IUpdate {
	
	private TreeModelViewer fViewer;
	
	public CollapseAllAction(TreeModelViewer viewer) {
		super(ActionMessages.CollapseAllAction_0, DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_COLLAPSE_ALL));
		setToolTipText(ActionMessages.CollapseAllAction_0);
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_COLLAPSE_ALL));
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_COLLAPSE_ALL));
		setActionDefinitionId(CollapseAllHandler.COMMAND_ID);
		fViewer = viewer;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fViewer.collapseAll();
	}

	public void update() {
	    setEnabled( fViewer.getInput() != null && fViewer.getChildCount(TreePath.EMPTY) > 0 );
	}
}
