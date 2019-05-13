/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.api.workbenchpart;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

public class ViewWithDisposeException extends ViewPart {

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		Label testLabel = new Label(parent, SWT.NONE);

		testLabel.setText("This view is supposed to throw an exception when closed");
	}

	@Override
	public void setFocus() {

	}

	@Override
	public void dispose() {
		throw new RuntimeException("This exception was thrown intentionally as part of an error handling test");
	}
}
