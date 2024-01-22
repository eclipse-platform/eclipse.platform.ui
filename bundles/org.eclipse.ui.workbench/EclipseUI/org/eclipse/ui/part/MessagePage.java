/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
package org.eclipse.ui.part;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * A message page display a message in a pagebook view.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @see PageBookView
 * @noextend This class is not intended to be subclassed by clients.
 */
public class MessagePage extends Page {
	private Composite pgComp;

	private Label msgLabel;

	private String message = "";//$NON-NLS-1$

	/**
	 * Creates a new page. The message is the empty string.
	 */
	public MessagePage() {
		// do nothing
	}

	@Override
	public void createControl(Composite parent) {
		// Message in default page of Outline should have margins
		pgComp = new Composite(parent, SWT.NULL);
		FillLayout layout = new FillLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		pgComp.setLayout(layout);

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
	 * @param message the message text
	 */
	public void setMessage(String message) {
		this.message = message;
		if (msgLabel != null) {
			msgLabel.setText(message);
		}
	}
}
