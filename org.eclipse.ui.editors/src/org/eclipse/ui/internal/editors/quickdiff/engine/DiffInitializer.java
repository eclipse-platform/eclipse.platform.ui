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
package org.eclipse.ui.internal.editors.quickdiff.engine;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ILineDiffInfo;

import org.eclipse.ui.internal.editors.quickdiff.compare.rangedifferencer.IRangeComparator;
import org.eclipse.ui.internal.editors.quickdiff.compare.rangedifferencer.RangeDifference;
import org.eclipse.ui.internal.editors.quickdiff.compare.rangedifferencer.RangeDifferencer;
import org.eclipse.ui.internal.editors.quickdiff.compare.rangedifferencer.DocLineComparator;

/**
 * Utility class that bridges between the diff engine of org.eclipse.compare and the 
 * incremental diff engine in quickdiff.
 * @since 3.0
 */
class DiffInitializer {
	/**
	 * Local implemenentation of the <code>ILineDiffInfo</code> interface.
	 */
	private static final class DiffRegion implements ILineDiffInfo {
		int type;
		int first;
		int lines;
		int deletedBehind;
		int deletedBefore= 0;
		ArrayList restore= new ArrayList();

		int last() {
			return first + lines - 1;
		}

		DiffRegion(int type, int removedLines) {
			this(type, removedLines, 0);
		}
		
		DiffRegion(int type, int removedAfter, int removedBefore) {
			this.type= type;
			deletedBehind= removedAfter;
			deletedBefore= removedBefore;
			Assert.isTrue(type == ADDED || type == UNCHANGED || type == CHANGED);
			Assert.isTrue(removedAfter >= 0);
			Assert.isTrue(removedBefore >= 0);
		}

		/*
		 * @see org.eclipse.ui.internal.editors.text.LineNumberChangeRulerColumn.LineDiffer.LineDiffInfo#getRemovedLinesBelow()
		 */
		public int getRemovedLinesBelow() {
			return deletedBehind;
		}

		/*
		 * @see org.eclipse.ui.internal.editors.text.LineNumberChangeRulerColumn.LineDiffer.LineDiffInfo#getType()
		 */
		public int getType() {
			return type;
		}

		/*
		 * @see org.eclipse.ui.internal.editors.text.LineNumberChangeRulerColumn.LineDiffer.LineDiffInfo#getRemovedLinesAbove()
		 */
		public int getRemovedLinesAbove() {
			return deletedBefore;
		}

		/*
		 * @see org.eclipse.ui.internal.editors.text.LineNumberChangeRulerColumn.LineDiffer.LineDiffInfo#hasChanges()
		 */
		public boolean hasChanges() {
			return type != UNCHANGED || deletedBefore > 0 || deletedBehind > 0;
		}

		/*
		 * @see org.eclipse.ui.internal.editors.text.LineNumberChangeRulerColumn.LineDiffer.LineDiffInfo#getOriginalText()
		 */
		public String[] getOriginalText() {
			return (String[])restore.toArray(new String[restore.size()]);
		}
	}
	
	/**
	 * Initializes the given <code>ILineDiffer</code> with the differences in <code>actual</code> when
	 * compared to <code>reference</code>.
	 * @param differ the <code>ILineDiffer</code> to be initialized
	 * @param reference the reference document
	 * @param actual the actual (current) document
	 */
	public static void initializeDiffer(IProgressMonitor pm, DocumentLineDiffer differ, IDocument reference, IDocument actual) {
		// 1: call the compare engine
		IRangeComparator ref= new DocLineComparator(reference, null, false);
		IRangeComparator act= new DocLineComparator(actual, null, false);
		RangeDifference[] diffs= RangeDifferencer.findDifferences(pm, ref, act);
		// diffs contains the two-way diff of the two documents - only changes are reported
		
		// 2: split RangeDifferences into single-line ILineDiffInfos
		SortedMap map= new TreeMap(); // the map that will be fed to differ.initialize()
		for (int i= 0; i < diffs.length; i++) {
			if (pm != null) pm.worked(1);
			RangeDifference d= diffs[i];
			// changed: number of changed lines
			int changed= Math.min(d.leftLength(), d.rightLength());
			// lineDelta > 0 ==> lineDelta deleted lines
			// lineDelta < 0 ==> lineDelta added lines
			int lineDelta= d.leftLength() - d.rightLength();
			int deleted= lineDelta > 0 ? lineDelta : 0;
			int added= lineDelta < 0 ? -lineDelta : 0;
			
			// for every RangeDifference: for every contained line, add a ILineDiffInfo
			try {
				// add changed region lines first
				int line= d.rightStart();
				int leftLine= d.leftStart();
				int changedAbs= line + changed;
				DiffRegion last= null;
				while (line < changedAbs) {
					last= new DiffRegion(ILineDiffInfo.CHANGED, 0);
					last.restore.add(reference.get(reference.getLineOffset(leftLine), reference.getLineLength(leftLine)));
					// add deleted lines to last changed entry
					map.put(new Integer(line), last);
					line++;
					leftLine++;
				}
				
				// add deleted lines
				if (deleted > 0) {
					boolean addLast= last == null;
					if (addLast) {
						last= new DiffRegion(ILineDiffInfo.UNCHANGED, 0);
						last.restore.add(reference.get(reference.getLineOffset(leftLine - 1), reference.getLineLength(leftLine - 1)));
					} 
					last.deletedBehind= deleted;
					for (int j= leftLine; j < leftLine + deleted; j++)
						last.restore.add(reference.get(reference.getLineOffset(j), reference.getLineLength(j)));
					if (addLast) map.put(new Integer(line - 1), last);
				}

				// add added lines
				int addedAbs= changedAbs + added;
				while (line < addedAbs) {
					last= new DiffRegion(ILineDiffInfo.ADDED, 0);
					map.put(new Integer(line), last);
					line++;
				}
			} catch (BadLocationException e) {
			}
		}
		
		// 3: feed the table into the differ
		differ.initialize(map, actual.getNumberOfLines());
	}
}
