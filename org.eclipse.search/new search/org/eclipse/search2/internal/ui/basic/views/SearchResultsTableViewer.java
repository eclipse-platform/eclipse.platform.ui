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
package org.eclipse.search2.internal.ui.basic.views;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.TableViewer;

/**
 * @author Thomas Mäder
 *
 */
public class SearchResultsTableViewer extends TableViewer implements INavigate {


	public SearchResultsTableViewer(Composite parent, int style) {
		super(parent, style);
		setUseHashlookup(true);
	}

	public void navigateNext(boolean forward) {
		int itemCount= getTable().getItemCount();
		if (itemCount == 0)
			return;
		int[] selection= getTable().getSelectionIndices();
		int nextIndex= 0;
		if (selection.length > 0) {
			if (forward) {
				nextIndex= selection[selection.length-1]+1;
				if (nextIndex > itemCount)
					nextIndex= 0;
			} else {
				nextIndex= selection[0]-1;
				if (nextIndex < 0)
					nextIndex= itemCount-1;
			}
		}
		getTable().setSelection(nextIndex);
	}

	public void add(Object element) {
		if (findItem(element) != null)
			return;
		super.add(element);
	}

}
