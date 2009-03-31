/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 268472)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional.fieldassist;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * <b>EXPERIMENTAL</b>: This class is not API. It is experimental and subject to
 * arbitrary change, including removal. Please provide feedback if you would
 * like to see this become API.
 * <p>
 * Controls the appearance of a ControlDecoration managed by a
 * ControlDecorationSupport.
 * 
 * @since 1.3
 */
public class ControlDecorationUpdater {
	/**
	 * <b>EXPERIMENTAL</b>: This method is not API. It is experimental and
	 * subject to arbitrary change, including removal. Please provide feedback
	 * if you would like to see this become API.
	 * <p>
	 * Updates the visibility, image, and description text of the given
	 * ControlDecoration to represent the given status.
	 * 
	 * @param decoration
	 *            the ControlDecoration to update
	 * @param status
	 *            the status to be displayed by the decoration
	 */
	protected void update(ControlDecoration decoration, IStatus status) {
		if (status == null || status.isOK()) {
			decoration.hide();
		} else {
			decoration.setImage(getImage(status));
			decoration.setDescriptionText(getDescriptionText(status));
			decoration.show();
		}
	}

	/**
	 * <b>EXPERIMENTAL</b>: This method is not API. It is experimental and
	 * subject to arbitrary change, including removal. Please provide feedback
	 * if you would like to see this become API.
	 * <p>
	 * Returns the description text to show in a ControlDecoration for the given
	 * status. The default implementation of this method returns
	 * status.getMessage().
	 * 
	 * @param status
	 *            the status object.
	 * @return the description text to show in a ControlDecoration for the given
	 *         status.
	 */
	protected String getDescriptionText(IStatus status) {
		return status == null ? "" : status.getMessage(); //$NON-NLS-1$
	}

	/**
	 * <b>EXPERIMENTAL</b>: This method is not API. It is experimental and
	 * subject to arbitrary change, including removal. Please provide feedback
	 * if you would like to see this become API.
	 * <p>
	 * Returns an image to display in a ControlDecoration which is appropriate
	 * for the given status. The default implementation of this method returns
	 * an image according to <code>status.getSeverity()</code>:
	 * <ul>
	 * <li>IStatus.OK => No image
	 * <li>IStatus.INFO => FieldDecorationRegistry.DEC_INFORMATION
	 * <li>IStatus.WARNING => FieldDecorationRegistry.DEC_WARNING
	 * <li>IStatus.ERROR => FieldDecorationRegistry.DEC_ERROR
	 * <li>IStatus.CANCEL => FieldDecorationRegistry.DEC_ERROR
	 * <li>Other => No image
	 * </ul>
	 * 
	 * @param status
	 *            the status object.
	 * @return an image to display in a ControlDecoration which is appropriate
	 *         for the given status.
	 */
	protected Image getImage(IStatus status) {
		if (status == null)
			return null;

		String fieldDecorationID = null;
		switch (status.getSeverity()) {
		case IStatus.INFO:
			fieldDecorationID = FieldDecorationRegistry.DEC_INFORMATION;
			break;
		case IStatus.WARNING:
			fieldDecorationID = FieldDecorationRegistry.DEC_WARNING;
			break;
		case IStatus.ERROR:
		case IStatus.CANCEL:
			fieldDecorationID = FieldDecorationRegistry.DEC_ERROR;
			break;
		}

		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
				.getFieldDecoration(fieldDecorationID);
		return fieldDecoration == null ? null : fieldDecoration.getImage();
	}
}
