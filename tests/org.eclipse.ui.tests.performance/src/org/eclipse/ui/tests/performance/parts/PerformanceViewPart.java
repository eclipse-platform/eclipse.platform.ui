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
package org.eclipse.ui.tests.performance.parts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

/**
 * @since 3.1
 */
public class PerformanceViewPart extends ViewPart {

	private Label control;

	/**
	 *
	 */
	public PerformanceViewPart() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		control = new Label(parent, SWT.NONE);
	}

	@Override
	public void setFocus() {
		control.setFocus();
	}

}
