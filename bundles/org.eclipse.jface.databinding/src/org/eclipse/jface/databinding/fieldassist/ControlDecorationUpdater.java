/*******************************************************************************
 * Copyright (c) 2009, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 268472)
 *     Matthew Hall - bug 300953
 ******************************************************************************/

package org.eclipse.jface.databinding.fieldassist;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * Controls the appearance of a ControlDecoration managed by a
 * ControlDecorationSupport.
 *
 * @since 1.4
 */
public class ControlDecorationUpdater {
	/**
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
	 * Returns an image to display in a ControlDecoration which is appropriate for
	 * the given status. The default implementation of this method returns an image
	 * according to <code>status.getSeverity()</code>:
	 * <ul>
	 * <li>IStatus.OK =&gt; No image
	 * <li>IStatus.INFO =&gt; FieldDecorationRegistry.DEC_INFORMATION
	 * <li>IStatus.WARNING =&gt; FieldDecorationRegistry.DEC_WARNING
	 * <li>IStatus.ERROR =&gt; FieldDecorationRegistry.DEC_ERROR
	 * <li>IStatus.CANCEL =&gt; FieldDecorationRegistry.DEC_ERROR
	 * <li>Other =&gt; No image
	 * </ul>
	 *
	 * @param status the status object.
	 * @return an image to display in a ControlDecoration which is appropriate for
	 *         the given status.
	 */
	protected Image getImage(IStatus status) {
		if (status == null) {
			return null;
		}

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
