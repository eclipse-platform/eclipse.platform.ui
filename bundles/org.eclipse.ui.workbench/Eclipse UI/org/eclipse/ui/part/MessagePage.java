/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.part;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;		

/**
 * A message page display a message in a pagebook view.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @see PageBookView
 */
public class MessagePage extends Page {
	private Composite pgComp;
	private Label msgLabel;
	private String message = "";//$NON-NLS-1$
/**
 * Creates a new page. The message is the empty string.
 */
public MessagePage() {
}
/* (non-Javadoc)
 * Method declared on IPage.
 */
public void createControl(Composite parent) {
	// Message in default page of Outline should have margins
	pgComp = new Composite(parent, SWT.NULL);
	pgComp.setLayout(new FillLayout());
	
	msgLabel = new Label(pgComp, SWT.LEFT | SWT.TOP | SWT.WRAP);
	msgLabel.setText(message);
}
/* (non-Javadoc)
 * Method declared on IPage.
 */
public Control getControl() {
	return pgComp;
}
/**
 * Sets focus to a part in the page.
 */
public void setFocus() {
	msgLabel.setFocus();
}
/**
 * Sets the message to the given string.
 *
 * @param message the message text
 */
public void setMessage(String message) {
	this.message = message;
	if (msgLabel != null)
		msgLabel.setText(message);
}
}
