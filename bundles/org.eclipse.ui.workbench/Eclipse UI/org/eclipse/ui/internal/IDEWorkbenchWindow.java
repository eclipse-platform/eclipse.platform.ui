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

package org.eclipse.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

/**
 * The IDEWorkbenchWindow is the class that is used for the window in the
 * current Eclipse IDE.
 */
public class IDEWorkbenchWindow extends WorkbenchWindow {

	PerspectiveControl perspectiveControl;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param number
	 */
	public IDEWorkbenchWindow(int number) {
		super(number);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.WorkbenchWindow#getLayout()
	 */
	protected Layout getLayout() {
		return new FormLayout();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#initializeBounds()
	 */
	protected void initializeBounds() {
		super.initializeBounds();
		setLayoutDataForContents();
	}

	/**
	 * Set the layout data for the contents of the window.
	 *  
	 */
	private void setLayoutDataForContents() {

		FormData perspectiveData = new FormData();

		perspectiveData.top = new FormAttachment(0);
		perspectiveData.left = new FormAttachment(80);
		perspectiveData.right = new FormAttachment(100);
		perspectiveData.bottom =
			new FormAttachment(getShortcutBar().getControl(), 0, SWT.BOTTOM);

		perspectiveControl.getControl().setLayoutData(perspectiveData);

		FormData toolBarData = new FormData();
		toolBarData.top = new FormAttachment(0);
		toolBarData.left = new FormAttachment(0);
		toolBarData.right =
			new FormAttachment(perspectiveControl.getControl(), 0);

		getToolBarControl().setLayoutData(toolBarData);

		FormData fastViewData = new FormData();

		fastViewData.top = new FormAttachment(getToolBarControl(), 0);

		fastViewData.left = new FormAttachment(0);
		fastViewData.right =
			new FormAttachment(getToolBarControl(), 0, SWT.RIGHT);

		getShortcutBar().getControl().setLayoutData(fastViewData);

		FormData progressData = new FormData();
		progressData.left = new FormAttachment(0);
		progressData.right = new FormAttachment(10);
		progressData.bottom = new FormAttachment(100);
		progressData.top =
			new FormAttachment(getStatusLineManager().getControl(), 0, SWT.TOP);

		animationItem.getControl().setLayoutData(progressData);

		FormData statusLineData = new FormData();

		statusLineData.left = new FormAttachment(animationItem.getControl());
		statusLineData.right = new FormAttachment(100);
		statusLineData.bottom = new FormAttachment(100);

		getStatusLineManager().getControl().setLayoutData(statusLineData);
		FormData clientAreaData = new FormData();

		clientAreaData.top =
			new FormAttachment(getShortcutBar().getControl(), 0);

		clientAreaData.left = new FormAttachment(0);

		clientAreaData.bottom =
			new FormAttachment(getStatusLineManager().getControl(), 0);

		clientAreaData.right = new FormAttachment(100);

		getClientComposite().setLayoutData(clientAreaData);
		

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.WorkbenchWindow#createTrimWidgets(org.eclipse.swt.widgets.Shell)
	 */
	protected void createTrimWidgets(Shell shell) {
		super.createTrimWidgets(shell);
		createPerspectiveControl(shell);
	}

	private void createPerspectiveControl(Shell shell) {
		perspectiveControl = new PerspectiveControl(this);

		perspectiveControl.createControl(shell);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.WorkbenchWindow#shortCutBarStyle()
	 */
	protected int shortCutBarStyle() {
		return SWT.FLAT | SWT.WRAP | SWT.HORIZONTAL | SWT.RIGHT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.ApplicationWindow#showTopSeperator()
	 */
	protected boolean showTopSeperator() {
		return false;
	}

}
