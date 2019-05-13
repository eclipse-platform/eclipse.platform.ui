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
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

public class ViewWithInitException extends ViewPart {

	@Override
	public void init(IViewSite site) throws PartInitException {
		throw new PartInitException("This exception was thrown intentionally as part of an error handling test");
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		Label message = new Label(parent, SWT.NONE);
		message.setText("This view threw an exception on init. You should not be able to read this");
	}

	@Override
	public void setFocus() {

	}

}
