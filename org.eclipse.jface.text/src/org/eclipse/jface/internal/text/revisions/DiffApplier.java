/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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


public final class DiffApplier {
	
	private void applyDiff(List regions, Hunk hunk) {
		for (Iterator it= regions.iterator(); it.hasNext();) {
			ChangeRegion region= (ChangeRegion) it.next();
			region.adjustTo(hunk);
		}
	}
	
	public void applyDiff(List regions, ILineDiffer lineDiffer, int numberOfLines) {
		clearDiff(regions);
		
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

	private void clearDiff(List regions) {
		for (Iterator it= regions.iterator(); it.hasNext();) {
			ChangeRegion region= (ChangeRegion) it.next();
			region.clearDiff();
		}
	}

}