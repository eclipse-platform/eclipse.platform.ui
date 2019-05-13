/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.rcp.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

/**
 * Minimal view, for the RCP tests.
 */
public class EmptyView extends ViewPart {

	public static final String ID = "org.eclipse.ui.tests.rcp.util.EmptyView"; //$NON-NLS-1$

	private Label label;

	public EmptyView() {
		// do nothing
	}

	@Override
	public void createPartControl(Composite parent) {
		label = new Label(parent, SWT.NONE);
		label.setText("Empty view");
	}

	@Override
	public void setFocus() {
		label.setFocus();
	}
}
