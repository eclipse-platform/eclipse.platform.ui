/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		
		Button button = new Button(parent, SWT.NONE);
		button.setText("Open Another");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openAnother();
			}
		});
	}

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
