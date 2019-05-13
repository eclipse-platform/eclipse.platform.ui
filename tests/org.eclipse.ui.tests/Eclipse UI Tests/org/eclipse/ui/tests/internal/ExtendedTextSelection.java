/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
package org.eclipse.ui.tests.internal;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IActionFilter;

public class ExtendedTextSelection extends TextSelection implements IAdaptable {
	static private ExtendedTextSelectionActionFilter filter = new ExtendedTextSelectionActionFilter();

	/**
	 * Constructor for ExtendedTextSelection.
	 * @param offset
	 * @param length
	 */
	public ExtendedTextSelection(int offset, int length) {
		super(offset, length);
	}

	/**
	 * Constructor for ExtendedTextSelection.
	 * @param document
	 * @param offset
	 * @param length
	 */
	public ExtendedTextSelection(IDocument document, int offset, int length) {
		super(document, offset, length);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IActionFilter.class) {
			return (T) filter;
		}
		return null;
	}

}

