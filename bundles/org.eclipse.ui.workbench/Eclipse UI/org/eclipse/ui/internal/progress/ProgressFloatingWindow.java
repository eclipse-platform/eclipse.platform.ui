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

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.internal.AssociatedWindow;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * The ProgressFloatingWindow is a window that opens next to an
 * animation item.
 */
class ProgressFloatingWindow extends AssociatedWindow {

	ProgressTreeViewer viewer;
	WorkbenchWindow window;

	/**
	 * Create a new instance of the receiver.
	 * @param parent
	 * @param associatedControl
	 */
	ProgressFloatingWindow(Shell parent, Control associatedControl) {
		super(parent, associatedControl);

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
				adjustSize();
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.AbstractTreeViewer#createTreeItem(org.eclipse.swt.widgets.Widget, java.lang.Object, int)
			 */
			protected void createTreeItem(Widget parent, Object element, int index) {
				super.createTreeItem(parent, element, index);
				adjustSize();
			}

			/* (non-Javadoc)
			 * @see org.eclipse.ui.internal.progress.ProgressTreeViewer#doUpdateItem(org.eclipse.swt.widgets.Item, java.lang.Object)
			 */
			protected void doUpdateItem(Item item, Object element) {
				super.doUpdateItem(item, element);
				adjustSize();
			}

		};
		viewer.setUseHashlookup(true);
		viewer.setSorter(ProgressManagerUtil.getProgressViewerSorter());
		
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		initContentProvider();
		ProgressManagerUtil.initLabelProvider(viewer);

		return viewer.getControl();
	}

	/**
	 * Adjust the size of the viewer.
	 */
	private void adjustSize() {
		
		Point size = viewer.getTree().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		size.x += 5;
		size.y += 5;
		if(size.x > 500)
			size.x = 500;
		getShell().setSize(size);

	}

	/**
	 * Sets the content provider for the viewer.
	 */
	protected void initContentProvider() {
		IContentProvider provider = new CondensedProgressContentProvider(viewer);
		viewer.setContentProvider(provider);
		viewer.setInput(provider);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	protected Point getInitialSize() {
		return viewer.getControl().computeSize(100, 25);

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#initializeBounds()
	 */
	protected void initializeBounds() {
		super.initializeBounds();
		adjustSize();
	}

}
