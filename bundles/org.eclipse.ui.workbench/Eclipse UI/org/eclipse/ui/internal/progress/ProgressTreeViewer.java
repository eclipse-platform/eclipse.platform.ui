/*
 * Created on Jul 7, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.ui.internal.progress;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

/**
 * @author tod
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
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
			treeItem.setForeground(
				treeItem.getDisplay().getSystemColor(SWT.COLOR_BLUE));
			return;
		}

		if (info.status.getCode() == IStatus.ERROR) {
			treeItem.setForeground(
				treeItem.getDisplay().getSystemColor(SWT.COLOR_RED));
			return;
		}
		treeItem.setForeground(
			treeItem.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));

	}

	/**
	 * @param parent
	 */
	public ProgressTreeViewer(Composite parent) {
		super(parent);
		// XXX Auto-generated constructor stub
	}

	/**
	 * @param parent
	 * @param style
	 */
	public ProgressTreeViewer(Composite parent, int style) {
		super(parent, style);
		// XXX Auto-generated constructor stub
	}

	/**
	 * @param tree
	 */
	public ProgressTreeViewer(Tree tree) {
		super(tree);
		// XXX Auto-generated constructor stub
	}

}
