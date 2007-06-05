/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.contributions;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

/**
 * Moved from org.eclipse.ui.examples.readmetool
 * 
 * @since 3.3
 */
public class ExampleControlContribution extends
		WorkbenchWindowControlContribution {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ControlContribution#createControl(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createControl(Composite parent) {
		// Create a composite to place the label in
		Composite comp = new Composite(parent, SWT.NONE);

		// Give some room around the control
		FillLayout layout = new FillLayout();
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		comp.setLayout(layout);

		// Create a label for the trim.
		Label ccCtrl = new Label(comp, SWT.BORDER | SWT.CENTER);
		ccCtrl.setBackground(parent.getDisplay().getSystemColor(
				SWT.COLOR_DARK_BLUE));
		ccCtrl.setForeground(parent.getDisplay()
				.getSystemColor(SWT.COLOR_WHITE));
		ccCtrl.setText(" Ctrl Contrib (" + getSideName(getCurSide()) + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		ccCtrl.setToolTipText("Ctrl Contrib Tooltip"); //$NON-NLS-1$

		return comp;
	}

	private String getSideName(int side) {
		if (side == SWT.TOP)
			return "Top"; //$NON-NLS-1$
		if (side == SWT.BOTTOM)
			return "Bottom"; //$NON-NLS-1$
		if (side == SWT.LEFT)
			return "Left"; //$NON-NLS-1$
		if (side == SWT.RIGHT)
			return "Right"; //$NON-NLS-1$

		return "Unknown Side"; //$NON-NLS-1$
	}
}
