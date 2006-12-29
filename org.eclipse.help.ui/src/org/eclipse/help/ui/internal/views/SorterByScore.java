/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import org.eclipse.help.internal.search.SearchHit;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class SorterByScore extends ViewerComparator {
	public SorterByScore() {
		super(ReusableHelpPart.SHARED_COLLATOR);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		try {
			float rank1 = ((SearchHit) e1).getScore();
			float rank2 = ((SearchHit) e2).getScore();
			if (rank1 - rank2 > 0) {
				return -1;
			} else if (rank1 == rank2) {
				return 0;
			} else {
				return 1;
			}
		} catch (Exception e) {
		}
		return super.compare(viewer, e1, e2);
	}
}
