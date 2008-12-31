/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text.revisions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.source.ILineDiffInfo;
import org.eclipse.jface.text.source.ILineDiffer;


/**
 * Computes the diff hunks from an {@link ILineDiffer}.
 *
 * @since 3.3
 */
public final class HunkComputer {
	/**
	 * Converts the line-based information of {@link ILineDiffer} into {@link Hunk}s, grouping
	 * contiguous blocks of lines that are changed (added, deleted).
	 *
	 * @param differ the line differ to query
	 * @param lines the number of lines to query
	 * @return the corresponding {@link Hunk} information
	 */
	public static Hunk[] computeHunks(ILineDiffer differ, int lines) {
		List hunks= new ArrayList(lines);

		int added= 0;
		int changed= 0;
		ILineDiffInfo info= null;
		for (int line= 0; line < lines; line++) {
			info= differ.getLineInfo(line);
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
						hunks.add(new Hunk(line - changed - Math.max(0, added), added, changed));
						added= 0;
						changed= 0;
					}
			}
		}

		// last hunk
		if (info != null) {
			added -= info.getRemovedLinesBelow();
			if (added != 0 || changed != 0) {
				hunks.add(new Hunk(lines - changed, added, changed));
				added= 0;
				changed= 0;
			}
		}

		return (Hunk[]) hunks.toArray(new Hunk[hunks.size()]);
	}
	private HunkComputer() {
	}
}