/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

/**
 * The ProgressTreeViewer is a tree viewer that handles the coloring 
 * of text.
 */
class ProgressTreeViewer extends TreeViewer {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#doUpdateItem(org.eclipse.swt.widgets.Item, java.lang.Object)
	 */
	protected void doUpdateItem(Item item, Object element) {
		super.doUpdateItem(item, element);
		if (element instanceof JobInfo) {
			if (item != null && item instanceof TreeItem)
				updateColors((TreeItem) item, (JobInfo) element);
		}
	}

	private void updateColors(TreeItem treeItem, JobInfo info) {

		if (info.status.getCode() == JobInfo.PENDING_STATUS) {
			treeItem.setForeground(JFaceColors.getActiveHyperlinkText(treeItem.getDisplay()));
			return;
		}

		if (info.status.getCode() == IStatus.ERROR) {
			treeItem.setForeground(JFaceColors.getErrorText(treeItem.getDisplay()));
			return;
		}
		treeItem.setForeground(
			treeItem.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));

	}


	/**
	 * Create a new instance of the receiver with the supplied parent
	 * and style.
	 * @param parent
	 * @param style
	 */
	public ProgressTreeViewer(Composite parent, int style) {
		super(parent, style);
	}

}
