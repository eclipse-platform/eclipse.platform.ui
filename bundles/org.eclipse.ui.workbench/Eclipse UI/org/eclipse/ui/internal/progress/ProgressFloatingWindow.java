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
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.internal.AssociatedWindow;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * The ProgressFloatingWindow is a window that opens next to an animation item.
 */
class ProgressFloatingWindow extends AssociatedWindow {

	TableViewer viewer;
	WorkbenchWindow window;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param parent
	 * @param associatedControl
	 */
	ProgressFloatingWindow(Shell parent, Control associatedControl) {
		super(parent, associatedControl);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite root) {

		viewer = new TableViewer(root, SWT.MULTI) {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.TableViewer#doUpdateItem(org.eclipse.swt.widgets.Widget, java.lang.Object, boolean)
			 */
			protected void doUpdateItem(
				Widget widget,
				Object element,
				boolean fullMap) {
				super.doUpdateItem(widget, element, fullMap);
				adjustSize();
			}

		};
		viewer.setUseHashlookup(true);
		viewer.setSorter(ProgressManagerUtil.getProgressViewerSorter());

		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		initContentProvider();
		viewer.setLabelProvider(new LabelProvider(){
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				JobInfo info = (JobInfo) element;
				if(info.hasTaskInfo())
					return info.getTaskInfo().getDisplayStringWithoutTask();
				else
					return info.getJob().getName();
			}
		});

		return viewer.getControl();
	}

	/**
	 * Adjust the size of the viewer.
	 */
	private void adjustSize() {

		Point size = viewer.getTable().computeSize(SWT.DEFAULT, SWT.DEFAULT);

		size.x += 5;
		size.y += 5;
		if (size.x > 500)
			size.x = 500;
		getShell().setSize(size);

		moveShell(getShell());

	}

	/**
	 * Sets the content provider for the viewer.
	 */
	protected void initContentProvider() {
		IContentProvider provider = new ProgressTableContentProvider(viewer);
		
		viewer.setContentProvider(provider);
		viewer.setInput(provider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.AssociatedWindow#getTransparencyValue()
	 */
	protected int getTransparencyValue() {
		return 95;
	}

}
