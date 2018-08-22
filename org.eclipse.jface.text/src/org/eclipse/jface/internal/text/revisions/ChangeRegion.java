/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text.revisions;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.revisions.Revision;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.LineRange;

/**
 * A change region describes a contiguous range of lines that was changed in the same revision of a
 * document. Once it is adjusted to diff information, the originally contiguous range may be split
 * into several ranges or even be empty.
 *
 * @since 3.2
 */
public final class ChangeRegion {
	private final Revision fRevision;
	private final ILineRange fLines;
	private final List<Range> fAdjusted= new LinkedList<>();

	/**
	 * Creates a new change region for the given revision and line range.
	 *
	 * @param revision the revision of the new region
	 * @param lines the line range of the new region
	 * @throws IndexOutOfBoundsException if the line range is not well-formed
	 */
	public ChangeRegion(Revision revision, ILineRange lines) throws IndexOutOfBoundsException {
		Assert.isLegal(revision != null);
		Assert.isLegal(lines != null);
		fLines= Range.copy(lines);
		fRevision=revision;
		clearDiff();
	}

	/**
	 * Returns the revision that this region belongs to.
	 *
	 * @return the revision that this region belongs to
	 */
	public Revision getRevision() {
		return fRevision;
	}

	/**
	 * Returns the original (before applying diff information) line range of this change region.
	 *
	 * @return the original (before applying diff information) line range of this change region
	 */
	public ILineRange getOriginalRange() {
		return fLines;
	}

	/**
	 * Returns the list of {@link Range}s of this change region for which the revision
	 * information is still valid.
	 *
	 * @return the list of adjusted line ranges
	 */
	public List<Range> getAdjustedRanges() {
		return fAdjusted;
	}

	/**
	 * Returns the line coverage of the adjusted ranges, an empty range if the coverage is empty.
	 *
	 * @return the line coverage of the adjusted ranges
	 */
	public ILineRange getAdjustedCoverage() {
		if (fAdjusted.isEmpty())
			return new LineRange(fLines.getStartLine(), 0);

		Range first= fAdjusted.get(0);
		Range last= fAdjusted.get(fAdjusted.size() - 1);

		return Range.createAbsolute(first.start(), last.end());
	}

	/**
	 * Clears any adjusted ranges, restoring the original range.
	 */
	public void clearDiff() {
		fAdjusted.clear();
		fAdjusted.add(Range.copy(fLines));
	}

	/**
	 * Adjusts this change region to a diff hunk. This will change the adjusted ranges.
	 *
	 * @param hunk the diff hunk to adjust to
	 */
	public void adjustTo(Hunk hunk) {
		for (ListIterator<Range> it= fAdjusted.listIterator(); it.hasNext();) {
			Range range= it.next();

			// do we need a split?
			int unchanged= getUnchanged(hunk, range.start());
			if (unchanged > 0) {
				if (unchanged >= range.length())
					continue;
				range= range.split(unchanged);
				it.add(range);
				it.previous(); it.next(); // needed so we can remove below
			}

			int line= range.start();
			Assert.isTrue(hunk.line <= line);

			// by how much do we shrink?
			int overlap= getOverlap(hunk, line);
			if (overlap >= range.length()) {
				it.remove();
				continue;
			}

			// by how much do we move?
			range.moveBy(hunk.delta + overlap);
			range.resizeBy(-overlap);
		}

	}

	private int getUnchanged(Hunk hunk, int line) {
		return Math.max(0, hunk.line - line);
	}

	/*
	 * Returns the number of lines after line that the hunk reports as changed.
	 */
	private int getOverlap(Hunk hunk, int line) {

		int deltaLine= hunk.line + hunk.changed;
		if (hunk.delta >= 0) {
			if (deltaLine <= line)
				return 0;
			return deltaLine - line;
		}

		// hunk.delta < 0
		int hunkEnd= deltaLine - hunk.delta;
		int cutCount= hunkEnd - line;
		return Math.max(0, cutCount);
	}

	@Override
	public String toString() {
		return "ChangeRegion [" + fRevision.toString() + ", [" + fLines.getStartLine() + "+" + fLines.getNumberOfLines() + ")]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
