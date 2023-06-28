/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.ui.internal.texteditor;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.Position;

import org.eclipse.ui.IEditorInput;

/**
 * Data structure representing an edit position.
 *
 * @since 2.1
 */
public final class EditPosition {

	/** The editor input */
	private final IEditorInput fEditorInput;
	/** The editor ID */
	private final String fEditorId;
	/** The position */
	private final Position fPosition;
	/**
	 * how many characters may come in between two edit positions for them to
	 * still be lumped into same bucket in position history (designed to prevent
	 * filling history with meaningless noise of very similar positions)
	 */
	public static final int PROXIMITY_THRESHOLD = 30;

	/**
	 * Creates a new edit position.
	 * @param editorInput the editor input
	 * @param editorId the editor ID
	 * @param pos the position
	 */
	public EditPosition(IEditorInput editorInput, String editorId, Position pos) {
		Assert.isNotNull(editorInput);
		Assert.isNotNull(editorId);
		fEditorId= editorId;
		fEditorInput= editorInput;
		fPosition= pos;
	}

	/**
	 * Returns the editor input for this edit position.
	 *
	 * @return the editor input of this edit position
	 */
	public IEditorInput getEditorInput() {
		return fEditorInput;
	}

	/**
	 * Returns the editor id for this edit position.
	 *
	 * @return the editor input of this edit position
	 */
	public String getEditorId() {
		return fEditorId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EditPosition ["); //$NON-NLS-1$
		if (fEditorInput != null) {
			builder.append("input="); //$NON-NLS-1$
			builder.append(fEditorInput);
			builder.append(", "); //$NON-NLS-1$
		}
		if (fEditorId != null) {
			builder.append("editorId="); //$NON-NLS-1$
			builder.append(fEditorId);
			builder.append(", "); //$NON-NLS-1$
		}
		if (fPosition != null) {
			builder.append("position="); //$NON-NLS-1$
			builder.append(fPosition);
		}
		builder.append("]"); //$NON-NLS-1$
		return builder.toString();
	}

	/**
	 * Returns the position.
	 *
	 * @return the position
	 */
	public Position getPosition() {
		return fPosition;
	}

	/**
	 * @param a         Position to compare the other arg against
	 * @param b         Another position to compare the first arg against
	 * @param threshold The maximum allowed distance between Position args for them
	 *                  to be considered co-located
	 * @return true if both Position args are colocated as defined by the threshold
	 *         param
	 * @since 3.15
	 */
	public static boolean areCoLocated(Position a, Position b, int threshold) {
		if (a == null || b == null) {
			return false;
		}
		int center1 = a.offset + (a.length / 2);
		int center2 = b.offset + (b.length / 2);
		int centerDistance = Math.abs(center1 - center2);
		int minWithoutOverlap = a.length / 2 + b.length / 2;
		return centerDistance < (minWithoutOverlap + threshold);
	}

	/**
	 * @since 3.15
	 */
	public static boolean areCoLocated(Position a, Position b) {
		return EditPosition.areCoLocated(a, b, EditPosition.PROXIMITY_THRESHOLD);
	}

	/**
	 * @since 3.15
	 */
	public static boolean areCoLocated(EditPosition a, EditPosition b, int threshold) {
		return a != null && b != null && a.getEditorInput().getName()
				.equals(b.getEditorInput().getName())
				&& EditPosition.areCoLocated(a.getPosition(), b.getPosition(), threshold);
	}

	/**
	 * @since 3.15
	 */
	public static boolean areCoLocated(EditPosition a, EditPosition b) {
		return EditPosition.areCoLocated(a, b, EditPosition.PROXIMITY_THRESHOLD);
	}
}
