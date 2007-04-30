/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.console;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.ui.internal.console.ConsoleHyperlinkPosition;

/**
 * When any region of a hyperlink is replaced, the hyperlink needs to be deleted.
 * 
 * @since 3.3
 */
public class HyperlinkUpdater extends DefaultPositionUpdater {

	/**
	 * @param category
	 */
	public HyperlinkUpdater() {
		super(ConsoleHyperlinkPosition.HYPER_LINK_CATEGORY);
	}
	/**
	 * When any region of a hyperlink is replaced, the hyperlink needs to be deleted.
	 *
	 * @return <code>true</code> if position has NOT been deleted
	 */
	protected boolean notDeleted() {

		int positionEnd = fPosition.offset + fPosition.length - 1;
		int editEnd = fOffset + fLength - 1;
		if ((fOffset <= fPosition.offset && (editEnd > fPosition.offset)) ||
			(fOffset < positionEnd && (editEnd > positionEnd)) ||
			(fOffset >= fPosition.offset && fOffset <= positionEnd) ||
			(editEnd >= fPosition.offset && editEnd <= positionEnd)) {

			fPosition.delete();

			try {
				fDocument.removePosition(ConsoleHyperlinkPosition.HYPER_LINK_CATEGORY, fPosition);
			} catch (BadPositionCategoryException x) {
			}

			return false;
		}

		return true;
	}
}
