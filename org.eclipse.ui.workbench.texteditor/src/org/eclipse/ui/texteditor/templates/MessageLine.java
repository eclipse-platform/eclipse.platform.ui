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
package org.eclipse.ui.texteditor.templates;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.resource.JFaceColors;


/**
 * A message line displaying a status.
 *
 * @since 3.0
 */
class MessageLine extends CLabel {

	private Color fNormalMsgAreaBackground;

	/**
	 * Creates a new message line as a child of the given parent.
	 *
	 * @param parent the parent composite
	 */
	public MessageLine(Composite parent) {
		this(parent, SWT.LEFT);
	}

	/**
	 * Creates a new message line as a child of the parent and with the given SWT style bits.
	 *
	 * @param parent the parent composite
	 * @param style the style
	 */
	public MessageLine(Composite parent, int style) {
		super(parent, style);
		fNormalMsgAreaBackground= getBackground();
	}


	private Image findImage(IStatus status) {
		if (status.isOK()) {
			return null;
		}
		return null;
	}

	/**
	 * Sets the message and image to the given status.
	 * <code>null</code> is a valid argument and will set the empty text and no image
	 *
	 * @param status the status
	 */
	public void setErrorStatus(IStatus status) {
		if (status != null && !status.isOK()) {
			String message= status.getMessage();
			if (message != null && message.length() > 0) {
				setText(message);
				setImage(findImage(status));
				setBackground(JFaceColors.getErrorBackground(getDisplay()));
				return;
			}
		}
		setText(""); //$NON-NLS-1$
		setImage(null);
		setBackground(fNormalMsgAreaBackground);
	}
}

