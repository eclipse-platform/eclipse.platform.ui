/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text.revisions;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.source.ILineDiffInfo;
import org.eclipse.jface.text.source.ILineDiffer;


/**
 * Applies diff information to a list of change regions.
 *  
 * @since 3.2
 */
public final class DiffApplier {
	/**
	 * Adjusts the {@link ChangeRegion}s in <code>regions</code> to the diff information provided
	 * by <code>lineDiffer</code>.
	 * 
	 * @param regions the list of {@link ChangeRegion}s to adjust
	 * @param lineDiffer the differ
	 * @param numberOfLines the number of lines to adjust
	 */
	public void applyDiff(List regions, ILineDiffer lineDiffer, int numberOfLines) {
		clearDiffs(regions);
		
		int added= 0;
		int changed= 0;
		ILineDiffInfo info= null;
		for (int line= 0; line < numberOfLines; line++) {
			info= lineDiffer.getLineInfo(line);
			if (info == null)
				continue;
			
			int changeType= info.getChangeType();
			switch (changeType) {
				case ILineDiffInfo.ADDED:
					added++;
					continue;
				case ILineDiffInfo.CHANGED:
					changed++;
					continue;
				case ILineDiffInfo.UNCHANGED:
					added -= info.getRemovedLinesAbove();
					if (added != 0 || changed != 0) {
						applyDiff(regions, new Hunk(line - changed - Math.max(0, added), added, changed));
						added= 0;
						changed= 0;
						info= null;
					}
			}
		}
		
		// last hunk
		if (info != null) {
			added -= info.getRemovedLinesAbove();
			if (added != 0 || changed != 0) {
				applyDiff(regions, new Hunk(numberOfLines - changed, added, changed));
				added= 0;
				changed= 0;
			}
			
		}
		
	}

	private void clearDiffs(List regions) {
		for (Iterator it= regions.iterator(); it.hasNext();) {
			ChangeRegion region= (ChangeRegion) it.next();
			region.clearDiff();
		}
	}

	/**
	 * Adjusts the change regions to one diff hunk.
	 * 
	 * @param regions the list of {@link ChangeRegion}s
	 * @param hunk the diff hunk to apply
	 */
	private void applyDiff(List regions, Hunk hunk) {
		for (Iterator it= regions.iterator(); it.hasNext();) {
			ChangeRegion region= (ChangeRegion) it.next();
			region.adjustTo(hunk);
		}
	}
}