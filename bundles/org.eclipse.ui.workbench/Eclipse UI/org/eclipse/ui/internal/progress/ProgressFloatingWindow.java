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
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;

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
		setShellStyle(SWT.RESIZE | SWT.ON_TOP);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#getLayout()
	 */
	protected Layout getLayout() {
		GridLayout layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		return layout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite root) {

		viewer = new TableViewer(root, SWT.MULTI) {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.TableViewer#doUpdateItem(org.eclipse.swt.widgets.Widget,
			 *      java.lang.Object, boolean)
			 */
			protected void doUpdateItem(Widget widget, Object element, boolean fullMap) {
				super.doUpdateItem(widget, element, fullMap);
				adjustSize();
			}

		};
		viewer.setUseHashlookup(true);
		viewer.setSorter(ProgressManagerUtil.getProgressViewerSorter());
		viewer.getControl().setBackground(
			viewer.getControl().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));

		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		initContentProvider();
		viewer.setLabelProvider(new LabelProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				JobTreeElement info = (JobTreeElement) element;
							
				return info.getCondensedDisplayString();
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

		Region oldRegion = getShell().getRegion();
		Point shellSize = getShell().getSize();
		Region r = new Region(getShell().getDisplay());
		Rectangle rect = new Rectangle(0, 0, shellSize.x, shellSize.y);
		r.add(rect);
		Region cornerRegion = new Region(getShell().getDisplay());

		//top right corner region
		cornerRegion.add(new Rectangle(shellSize.x - 5, 0, 5, 1));
		cornerRegion.add(new Rectangle(shellSize.x - 3, 1, 3, 1));
		cornerRegion.add(new Rectangle(shellSize.x - 2, 2, 2, 1));
		cornerRegion.add(new Rectangle(shellSize.x - 1, 3, 1, 2));

		//bottom right corner region
		int y = shellSize.y;
		cornerRegion.add(new Rectangle(shellSize.x - 5, y - 1, 5, 1));
		cornerRegion.add(new Rectangle(shellSize.x - 3, y - 2, 3, 1));
		cornerRegion.add(new Rectangle(shellSize.x - 2, y - 3, 2, 1));
		cornerRegion.add(new Rectangle(shellSize.x - 1, y - 5, 1, 2));

		//top left corner region
		cornerRegion.add(new Rectangle(0, 0, 5, 1));
		cornerRegion.add(new Rectangle(0, 1, 3, 1));
		cornerRegion.add(new Rectangle(0, 2, 2, 1));
		cornerRegion.add(new Rectangle(0, 3, 1, 2));

		//bottom left corner region
		cornerRegion.add(new Rectangle(0, y - 5, 1, 2));
		cornerRegion.add(new Rectangle(0, y - 3, 2, 1));
		cornerRegion.add(new Rectangle(0, y - 2, 3, 1));
		cornerRegion.add(new Rectangle(0, y - 1, 5, 1));

		r.subtract(cornerRegion);
		getShell().setRegion(r);

		if (oldRegion != null)
			oldRegion.dispose();

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
		return 50;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		if (getShell() == null)
			return super.close();
		Region oldRegion = getShell().getRegion();
		boolean result = super.close();
		if (result && oldRegion != null)
			oldRegion.dispose();
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#open()
	 */
	public int open() {
		if (getShell() == null) {
			// create the window
			create();
		}

		// limit the shell size to the display size
		constrainShellSize();
		
		// open the window
		getShell().setVisible(true);

		return getReturnCode();
		
	}

}
