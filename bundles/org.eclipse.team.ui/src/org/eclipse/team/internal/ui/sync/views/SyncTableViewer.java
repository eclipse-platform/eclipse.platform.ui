/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.sync.views;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;

public class SyncTableViewer extends TableViewer implements INavigableControl {

	public SyncTableViewer(Table table) {
		super(table);
	}

	public boolean gotoDifference(int direction) {
		Control c = getControl();

		if (!(c instanceof Table))
			return false;

		Table table = (Table)c;
		int inc = direction == NEXT ? 1 : -1;		
		int total = table.getItemCount();
		int next = table.getSelectionIndex() + inc;
		if(next >= total || next < 0) {
			return true;
		}
		table.setSelection(next);
		return false;
	}
}
