/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
 ******************************************************************************/
package org.eclipse.ui.tests.contexts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;

/**
 * A message page display a message in a pagebook view, and activates a context.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @see PageBookView
 */
public class ContextPage extends Page {
	public static final String TEST_CONTEXT_ID = "org.eclipse.ui.tests.contexts.Page";

	private Composite pgComp;

	private Label msgLabel;

	private String message = "";//$NON-NLS-1$

	/**
	 * Creates a new page. The message is the empty string.
	 */
	public ContextPage() {
		// do nothing
	}

	@Override
	public void createControl(Composite parent) {
		// Message in default page of Outline should have margins
		pgComp = new Composite(parent, SWT.NULL);
		pgComp.setLayout(new FillLayout());

		msgLabel = new Label(pgComp, SWT.LEFT | SWT.TOP | SWT.WRAP);
		msgLabel.setText(message);
	}

	@Override
	public Control getControl() {
		return pgComp;
	}

	/**
	 * Sets focus to a part in the page.
	 */
	@Override
	public void setFocus() {
		// important to give focus to the composite rather than the label
		// as the composite will actually take focus (though hidden),
		// but setFocus on a Label is a no-op
		pgComp.setFocus();
	}

	/**
	 * Sets the message to the given string.
	 *
	 * @param msg
	 *            the message text
	 */
	public void setMessage(String msg) {
		this.message = msg;
		if (msgLabel != null) {
			msgLabel.setText(msg);
		}
	}

	@Override
	public void init(IPageSite pageSite) {
		super.init(pageSite);
		IContextService localService = getSite().getService(
				IContextService.class);
		localService.activateContext(TEST_CONTEXT_ID);
	}
}
