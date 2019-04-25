/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;

/**
 * @since 3.3
 *
 */
public class ComboSingleSelectionIndexProperty extends SingleSelectionIndexProperty<Combo> {
	/**
	 *
	 */
	public ComboSingleSelectionIndexProperty() {
		super(new int[] { SWT.Selection, SWT.DefaultSelection });
	}

	@Override
	int doGetIntValue(Combo source) {
		return source.getSelectionIndex();
	}

	@Override
	void doSetIntValue(Combo source, int value) {
		if (value == -1)
			source.deselectAll();
		else
			source.select(value);
	}

	@Override
	public String toString() {
		return "Combo.selectionIndex <int>"; //$NON-NLS-1$
	}
}
