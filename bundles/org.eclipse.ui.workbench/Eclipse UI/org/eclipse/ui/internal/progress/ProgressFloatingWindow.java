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
package org.eclipse.ui.internal.progress;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.window.Window;

/**
 * The ProgressFloatingWindow is a window that opens next to an
 * animation item.
 */
class ProgressFloatingWindow extends Window {

	ProgressTreeViewer viewer;

	/**
	 * Create a new window with a shell based off of
	 * parent shell.
	 * @param parentShell
	 */
	public ProgressFloatingWindow(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.NONE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite root) {
		viewer = new ProgressTreeViewer(root, SWT.MULTI) {
			/* (non-Javadoc)
			 * @see org.eclipse.ui.internal.progress.ProgressTreeViewer#createChildren(org.eclipse.swt.widgets.Widget)
			 */
			protected void createChildren(Widget widget) {
				super.createChildren(widget);
				adjustSizeAndPosition();
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.AbstractTreeViewer#createTreeItem(org.eclipse.swt.widgets.Widget, java.lang.Object, int)
			 */
			protected void createTreeItem(Widget parent, Object element, int index) {
				super.createTreeItem(parent, element, index);
				adjustSizeAndPosition();
			}

			/* (non-Javadoc)
			 * @see org.eclipse.ui.internal.progress.ProgressTreeViewer#doUpdateItem(org.eclipse.swt.widgets.Item, java.lang.Object)
			 */
			protected void doUpdateItem(Item item, Object element) {
				super.doUpdateItem(item, element);
				adjustSizeAndPosition();
			}

		};
		viewer.setUseHashlookup(true);
		viewer.setSorter(ProgressManagerUtil.getProgressViewerSorter());
		
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_VERTICAL));

		initContentProvider();
		ProgressManagerUtil.initLabelProvider(viewer);

		return viewer.getControl();
	}

	/**
	 * Adjust the size and position of the viewer.
	 */
	private void adjustSizeAndPosition() {

		Rectangle position = getParentShell().getBounds();
		Point size = viewer.getTree().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		
		//Workaround for resize problem
		if(size.x > 500)
			size.x = 500;
		getShell().setSize(size);
		
		TreeItem[] items = viewer.getTree().getItems();
		for (int i = 0; i < items.length; i++) {
			TreeItem item = items[i];
		}

		Point windowLocation = new Point(position.x, position.y + position.height - size.y);
		getShell().setLocation(windowLocation);
	}

	/**
	 * Sets the content provider for the viewer.
	 */
	protected void initContentProvider() {
		IContentProvider provider = new ProgressContentProvider(viewer);
		viewer.setContentProvider(provider);
		viewer.setInput(provider);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	protected Point getInitialSize() {
		return viewer.getControl().computeSize(100, 25);

	}

}
