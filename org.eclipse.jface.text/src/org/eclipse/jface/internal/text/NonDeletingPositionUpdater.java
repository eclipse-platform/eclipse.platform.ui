/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.Position;


/**
 * A position updater that never deletes a position. If the region containing
 * the position is deleted, the position is moved to the beginning/end (falling
 * together) of the change. If the region containing the position is replaced,
 * the position is placed at the same location inside the replacement text, but
 * always inside the replacement text.
 *
 * @since 3.1
 */
public final class NonDeletingPositionUpdater implements IPositionUpdater {
	/** The position category. */
	private final String fCategory;

	/**
	 * Creates a new updater for the given <code>category</code>.
	 *
	 * @param category the new category.
	 */
	public NonDeletingPositionUpdater(String category) {
		fCategory= category;
	}

	/*
	 * @see org.eclipse.jface.text.IPositionUpdater#update(org.eclipse.jface.text.DocumentEvent)
	 */
	public void update(DocumentEvent event) {

		int eventOffset= event.getOffset();
		int eventOldEndOffset= eventOffset + event.getLength();
		int eventNewLength= event.getText() == null ? 0 : event.getText().length();
		int eventNewEndOffset= eventOffset + eventNewLength;
		int deltaLength= eventNewLength - event.getLength();

		try {
			Position[] positions= event.getDocument().getPositions(fCategory);

			for (int i= 0; i != positions.length; i++) {

				Position position= positions[i];

				if (position.isDeleted())
					continue;

				int offset= position.getOffset();
				int length= position.getLength();
				int end= offset + length;

				if (offset > eventOldEndOffset) {
					// position comes way after change - shift
					position.setOffset(offset + deltaLength);
				} else if (end < eventOffset) {
					// position comes way before change - leave alone
				} else if (offset <= eventOffset && end >= eventOldEndOffset) {
					// event completely internal to the position - adjust length
					position.setLength(length + deltaLength);
				} else if (offset < eventOffset) {
					// event extends over end of position - include the
					// replacement text into the position
					position.setLength(eventNewEndOffset - offset);
				} else if (end > eventOldEndOffset) {
					// event extends from before position into it - adjust
					// offset and length, including the replacement text into
					// the position
					position.setOffset(eventOffset);
					int deleted= eventOldEndOffset - offset;
					position.setLength(length - deleted + eventNewLength);
				} else {
					// event comprises the position - keep it at the same
					// position, but always inside the replacement text
					int newOffset= Math.min(offset, eventNewEndOffset);
					int newEndOffset= Math.min(end, eventNewEndOffset);
					position.setOffset(newOffset);
					position.setLength(newEndOffset - newOffset);
				}
			}
		} catch (BadPositionCategoryException e) {
			// ignore and return
		}
	}

	/**
	 * Returns the position category.
	 *
	 * @return the position category
	 */
	public String getCategory() {
		return fCategory;
	}
}
