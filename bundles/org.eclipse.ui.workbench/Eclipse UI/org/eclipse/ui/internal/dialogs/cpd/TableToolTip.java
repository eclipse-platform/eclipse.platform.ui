/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs.cpd;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * A tooltip with useful information based on the type of ContributionItem
 * the cursor hovers over in a Table.
 *
 * @since 3.5
 */
class TableToolTip extends NameAndDescriptionToolTip {
	private Table table;

	public TableToolTip(Table table) {
		super(table, RECREATE);
		this.table = table;
	}

	@Override
	protected Object getModelElement(Event event) {
		TableItem tableItem = table.getItem(new Point(event.x, event.y));
		if (tableItem == null) {
			return null;
		}
		return tableItem.getData();
	}
}