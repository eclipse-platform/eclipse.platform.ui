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
package org.eclipse.help.ui.internal.search;
import org.eclipse.help.ui.internal.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.search.ui.*;
/**
 * Sorts results of help sarch in search view.
 */
public class HelpSearchSorter extends ViewerSorter {
	/**
	 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.ViewerSorter,java.lang.Object,java.lang.Object)
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		try {
			ISearchResultViewEntry entry1 = (ISearchResultViewEntry) e1;
			int order1 =
				Integer.parseInt(
					entry1.getSelectedMarker().getAttribute(
						IHelpUIConstants.HIT_MARKER_ATTR_ORDER,
						"0"));
			ISearchResultViewEntry entry2 = (ISearchResultViewEntry) e2;
			int order2 =
				Integer.parseInt(
					entry2.getSelectedMarker().getAttribute(
						IHelpUIConstants.HIT_MARKER_ATTR_ORDER,
						"0"));
			return order1 - order2;
		} catch (Exception e) {
		}
		return super.compare(viewer, e1, e2);
	}
}
