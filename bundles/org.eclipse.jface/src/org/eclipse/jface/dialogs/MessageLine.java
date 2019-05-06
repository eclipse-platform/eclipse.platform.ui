/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Rüdiger Herrmann - 395426: [JFace] StatusDialog should escape ampersand in status message
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 546991
 *******************************************************************************/
package org.eclipse.jface.dialogs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * A message line displaying a status.
 *
 * @since 3.16
 */
public class MessageLine extends CLabel {

	/**
	 * Creates a new message line as a child of the given parent.
	 *
	 * @param parent a widget which will be the parent of the new instance (cannot
	 *               be null)
	 */
	public MessageLine(Composite parent) {
		this(parent, SWT.LEFT);
	}

	/**
	 * Creates a new message line as a child of the parent and with the given SWT
	 * stylebits.
	 *
	 * @param parent a widget which will be the parent of the new instance (cannot
	 *               be null)
	 * @param style  the style of widget to construct
	 */
	public MessageLine(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Find an image associated with the status.
	 *
	 * @param status the status to find the image for
	 * @return Image the image from {@link JFaceResources} associated with the
	 *         status
	 */
	private Image findImage(IStatus status) {
		if (status.isOK()) {
			return null;
		} else if (status.matches(IStatus.ERROR)) {
			return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
		} else if (status.matches(IStatus.WARNING)) {
			return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
		} else if (status.matches(IStatus.INFO)) {
			return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO);
		}
		return null;
	}

	/**
	 * Sets the message and image to the given status.
	 *
	 * @param status {@link IStatus} or <code>null</code>. <code>null</code> will
	 *               set the empty text and no image.
	 */
	public void setErrorStatus(IStatus status) {
		if (status != null && !status.isOK()) {
			String message = status.getMessage();
			if (message != null && message.length() > 0) {
				setText(LegacyActionTools.escapeMnemonics(message));
				setImage(findImage(status));
				return;
			}
		}
		setText(""); //$NON-NLS-1$
		setImage(null);
	}
}