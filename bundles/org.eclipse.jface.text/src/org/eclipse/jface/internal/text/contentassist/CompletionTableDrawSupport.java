/*******************************************************************************
 * Copyright (c) 2024 SAP SE.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text.contentassist;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.jface.internal.text.TableOwnerDrawSupport;

/**
 * When a completion table (for example for code completion) is requested by the user, the user
 * needs to be able to continue typing in the linked text control. In such cases, the focus is not
 * on the completion table. To ensure the selected code completion proposal always displays in the
 * correct color, even if the completion table is not focused, the non-focused colors are overridden
 * with the focus colors.
 */
public class CompletionTableDrawSupport extends TableOwnerDrawSupport {

	public static void install(Table table) {
		TableOwnerDrawSupport listener= new CompletionTableDrawSupport(table);
		installListener(table, listener);
	}

	/**
	 * Stores the styled ranges in the given table item. See {@link TableOwnerDrawSupport}
	 *
	 * @param item table item
	 * @param column the column index
	 * @param ranges the styled ranges or <code>null</code> to remove them
	 */
	public static void storeStyleRanges(TableItem item, int column, StyleRange[] ranges) {
		TableOwnerDrawSupport.storeStyleRanges(item, column, ranges);
	}


	private CompletionTableDrawSupport(Table table) {
		super(table);
	}

	@Override
	protected Color getSelectedRowBackgroundColorNoFocus() {
		return super.getSelectedRowBackgroundColor();
	}

	@Override
	protected Color getSelectedRowForegroundColorNoFocus() {
		return super.getSelectedRowForegroundColor();
	}

}
