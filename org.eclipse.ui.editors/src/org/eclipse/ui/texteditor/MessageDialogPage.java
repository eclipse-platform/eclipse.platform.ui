/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;

/**
 * Dialog page used to show text or error message.
 *
 * @since 3.1
 */
class MessageDialogPage extends DialogPage {

	MessageRegion fMessageRegion;


	public MessageDialogPage(Composite parent) {
		createControl(parent);
	}

	public void createControl(Composite parent) {
		Composite composite1= new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite1.setLayout(layout);
		fMessageRegion= new MessageRegion();
		fMessageRegion.createContents(composite1);
		GridData messageData= new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		fMessageRegion.setMessageLayoutData(messageData);
		setControl(composite1);
	}

	public void setMessage(String newMessage,int newType) {
		super.setMessage(newMessage, newType);
		fMessageRegion.updateText(newMessage, newType);
	}

	public void setErrorMessage(String newMessage) {
		super.setErrorMessage(newMessage);
		fMessageRegion.updateText(newMessage, IMessageProvider.ERROR);
	}
}
