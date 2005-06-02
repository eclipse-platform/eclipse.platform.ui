/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

/**
 * This class implements a simple dialog with text and progress bar.
 *  
 * @since 3.1
 *
 */
public class StartupProgressMonitorDialog extends ProgressMonitorDialog {

	/**
	 * Construct an instance of this dialog.
	 * @param parent
	 */
	public StartupProgressMonitorDialog(Shell parent) {
		super(parent);
		setShellStyle(SWT.NONE);
	}

	protected Control createContents(Composite parent) {

		Composite container = new Composite(parent, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		container.setLayoutData(gridData);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.horizontalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.verticalSpacing = 0;
		container.setLayout(gridLayout);

		Composite progressArea = new Composite(container, SWT.NONE);
		super.createContents(progressArea);

		return container;
	}
	
	/*
	 * Simple dialog has no image.
	 */
	protected Image getImage() {
		return null;
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		GridData gridData = (GridData) composite.getLayoutData();
		gridData.verticalAlignment = SWT.CENTER;

		// make the subTaskLabel height be just one line
		gridData = (GridData) subTaskLabel.getLayoutData();
		gridData.heightHint = SWT.DEFAULT;
		return composite;
	}

	/*
	 * see org.eclipse.jface.Window.getInitialLocation() 
	 */
	protected Point getInitialLocation(Point initialSize) {
		Composite parent = getShell().getParent();
		
		if (parent == null)
			return super.getInitialLocation(initialSize);

		Monitor monitor = parent.getMonitor();
		Rectangle monitorBounds = monitor.getClientArea();
		Point centerPoint = Geometry.centerPoint(monitorBounds);

		return new Point(centerPoint.x - (initialSize.x / 2), Math.max(
				monitorBounds.y, Math.min(centerPoint.y
						- (initialSize.y * 2 / 3), monitorBounds.y
						+ monitorBounds.height - initialSize.y)));
	}
	
	/*
	 * Do not call super as we do not want any buttons in the button bar.
	 */
	protected Control createButtonBar(Composite parent) {
		return null; 
	}
}
