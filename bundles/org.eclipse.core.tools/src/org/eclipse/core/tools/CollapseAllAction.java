/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tools;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * @since 3.0
 */
public class CollapseAllAction extends Action implements IAction {

	private static final String label = "Collapse All"; //$NON-NLS-1$
	private static final String imageName = "collapseall.gif"; //$NON-NLS-1$
	private TreeViewer viewer;

	public CollapseAllAction(TreeViewer viewer) {
		super(label);
		this.setToolTipText(label);
		this.viewer = viewer;
		this.setImageDescriptor(CoreToolsPlugin.createImageDescriptor(imageName));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		viewer.collapseAll();
	}
}