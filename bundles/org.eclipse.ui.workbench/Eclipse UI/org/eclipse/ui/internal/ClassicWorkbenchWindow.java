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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * The ClassicWorkbenchWindow is the workbench window used for
 * the classic look in Eclipse
 */
public class ClassicWorkbenchWindow
	extends WorkbenchWindow
	implements IWorkbenchWindow {	

	private Label separator2;
	private Label separator3;

	/**
	 * Create a new instance of the receiver.
	 * @param number
	 */
	public ClassicWorkbenchWindow(int number) {
		super(number);
	}

	/**
	 * Returns the separator2 control.
	 */
	Label getSeparator2() {
		return separator2;
	}

	/**
	 * Returns the separator3 control.
	 */
	Label getSeparator3() {
		return separator3;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.WorkbenchWindow#getLayout()
	 */
	protected Layout getLayout() {
			return new ClassicWorkbenchWindowLayout(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.WorkbenchWindow#createTrimWidgets(org.eclipse.swt.widgets.Shell)
	 */
	protected void createTrimWidgets(Shell shell) {
		super.createTrimWidgets(shell);
		separator2 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator3 = new Label(shell, SWT.SEPARATOR | SWT.VERTICAL);
		
	}

}
