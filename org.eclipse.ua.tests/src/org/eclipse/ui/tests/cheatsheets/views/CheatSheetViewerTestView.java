/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.cheatsheets.views;


import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.cheatsheets.*;
import org.eclipse.ui.cheatsheets.ICheatSheetViewer;
import org.eclipse.ui.part.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.SWT;


public class CheatSheetViewerTestView extends ViewPart {
	private ICheatSheetViewer viewer;
	private static boolean switcher = false;

	/**
	 * The constructor.
	 */
	public CheatSheetViewerTestView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		RowLayout rowLayout = new RowLayout ();
		rowLayout.type = SWT.VERTICAL;
		composite.setLayout(rowLayout);

		new Label(composite, SWT.NONE).setText("Test of the ICheatSheetviewer.");
		new Label(composite, SWT.NONE).setText("The Simple Java Application cheat sheet should be shown.");

		viewer = CheatSheetViewerFactory.createCheatSheetView();
		if(switcher) {
			switcher = !switcher;
			new Label(composite, SWT.NONE);
			new Label(composite, SWT.NONE).setText("Method order:");
			new Label(composite, SWT.NONE).setText("viewer.setInput called first");
			new Label(composite, SWT.NONE).setText("viewer.createPartControl called second");
			new Label(composite, SWT.NONE).setText("Close the view and reopen to reverse the order");
			new Label(composite, SWT.NONE);
			viewer.setInput("org.eclipse.jdt.helloworld");
			viewer.createPartControl(composite);
		} else {
			switcher = !switcher;
			new Label(composite, SWT.NONE);
			new Label(composite, SWT.NONE).setText("Method order:");
			new Label(composite, SWT.NONE).setText("viewer.createPartControl called first");
			new Label(composite, SWT.NONE).setText("viewer.setInput called second");
			new Label(composite, SWT.NONE).setText("Close the view and reopen to reverse the order");
			new Label(composite, SWT.NONE);
			viewer.createPartControl(composite);
			viewer.setInput("org.eclipse.jdt.helloworld");
		}
		viewer.getControl().setLayoutData(new RowData(200, 350));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
	}
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		if(viewer != null)
			viewer.setFocus();
	}
}