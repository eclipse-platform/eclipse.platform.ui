/*
 * Created on Jan 14, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import org.eclipse.help.internal.search.SearchHit;
import org.eclipse.jface.viewers.*;

public class SorterByScore extends ViewerSorter {
	/**
	 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,java.lang.Object,java.lang.Object)
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