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
package org.eclipse.ui.tests.api;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * The implementation of the multi-instance mock view.
 * Has a button to open another.
 *
 * @since 3.1
 */
public class MockViewPartMult extends MockViewPart {
	static int counter = 1;

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		Button button = new Button(parent, SWT.NONE);
		button.setText("Open Another");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openAnother();
			}
		});
	}

	@Override
	public String getTitle() {
		String title = super.getTitle();
		String secondaryId = getViewSite().getSecondaryId();
		if (secondaryId != null) {
			title += " " + secondaryId;
		}
		return title;
	}


	private void openAnother() {
		String secondaryId = String.valueOf(++counter);
		try {
			getSite().getPage().showView(MockViewPart.IDMULT, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
}
