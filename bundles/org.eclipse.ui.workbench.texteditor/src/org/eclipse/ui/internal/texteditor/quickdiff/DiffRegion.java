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
package org.eclipse.ui.internal.texteditor.quickdiff;

import java.util.List;
import java.util.ListIterator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ILineDiffInfo;

import org.eclipse.ui.internal.texteditor.NLSUtility;

import org.eclipse.compare.rangedifferencer.RangeDifference;


/**
 * The local implementation of <code>ILineDiffInfo</code>. As instances are
 * also <code>Annotation</code>s, they can be used in
 * <code>DocumentLineDiffer</code>s <code>IAnnotationModel</code> protocol.
 *
 * @since 3.0
 */
public final class DiffRegion extends Annotation implements ILineDiffInfo {
	private final QuickDiffRangeDifference fDifference;

	private final int fOffset;

	private final List<QuickDiffRangeDifference> fList;

	private final IDocument fDocument;

	/**
	 * Creates a new diff region.
	 *
	 * @param difference the range difference
	 * @param offset the offset
	 * @param differences the list of differences
	 * @param source the document
	 */
	public DiffRegion(QuickDiffRangeDifference difference, int offset, List<QuickDiffRangeDifference> differences, IDocument source) {
		super("org.eclipse.ui.workbench.texteditor.quickdiffChange", false, null); //$NON-NLS-1$
		fOffset= offset;
		fDifference= difference;
		fList= differences;
		fDocument= source;
	}

	@Override
	public String getType() {
		// we return unknown for unchanged regions to avoid
		// them getting displayed.
		return switch (getChangeType()) {
			case CHANGED -> {
				int r= fDifference.rightLength();
				int l= fDifference.leftLength();
				int c= Math.min(r, l);
				if (c == 0 && r - l < 0)
					yield "org.eclipse.ui.workbench.texteditor.quickdiffDeletion"; //$NON-NLS-1$
				else
					yield "org.eclipse.ui.workbench.texteditor.quickdiffChange"; //$NON-NLS-1$
			}
			case ADDED -> "org.eclipse.ui.workbench.texteditor.quickdiffAddition"; //$NON-NLS-1$
			case UNCHANGED -> "org.eclipse.ui.workbench.texteditor.quickdiffUnchanged"; //$NON-NLS-1$
			default -> TYPE_UNKNOWN;
		};
	}

	@Override
	public int getRemovedLinesBelow() {
		if (fOffset == fDifference.rightLength() - 1) {

			if (getChangeType() != UNCHANGED)
				return Math.max(fDifference.leftLength() - fDifference.rightLength(), 0);

			synchronized (fList) {
				for (ListIterator<QuickDiffRangeDifference> it= fList.listIterator(); it.hasNext();) {
					if (fDifference.equals(it.next())) {
						if (it.hasNext()) {
							QuickDiffRangeDifference next= it.next();
							if (next.rightLength() == 0)
								return Math.max(next.leftLength() - next.rightLength(), 0);
						}
						break;
					}
				}
			}
		}
		return 0;
	}

	@Override
	public int getChangeType() {
		if (fDifference.kind() == RangeDifference.NOCHANGE)
			return UNCHANGED;
		if (fOffset >= fDifference.leftLength())
			return ADDED;
		return CHANGED;
	}

	@Override
	public int getRemovedLinesAbove() {
		if (getChangeType() == UNCHANGED && fOffset == 0) {
			synchronized (fList) {
				for (ListIterator<QuickDiffRangeDifference> it= fList.listIterator(fList.size()); it.hasPrevious();) {
					if (fDifference.equals(it.previous())) {
						if (it.hasPrevious()) {
							QuickDiffRangeDifference previous= it.previous();
							return Math.max(previous.leftLength() - previous.rightLength(), 0);
						}
						break;
					}
				}
			}
		}
		return 0;
	}

	@Override
	public boolean hasChanges() {
		return getChangeType() != UNCHANGED || getRemovedLinesAbove() > 0 || getRemovedLinesBelow() > 0;
	}

	@Override
	public String[] getOriginalText() {
		IDocument doc= fDocument;
		if (doc != null) {
			int startLine= fDifference.leftStart() + fOffset;
			if (startLine >= fDifference.leftEnd())
				return new String[0]; // original text of an added line is
									  // empty

			int endLine= startLine + getRemovedLinesBelow();
			if (getChangeType() == UNCHANGED)
				startLine++;
			String[] ret= new String[endLine - startLine + 1];
			for (int i= 0; i < ret.length; i++) {
				try {
					ret[i]= doc.get(doc.getLineOffset(startLine + i), doc.getLineLength(startLine + i));
				} catch (BadLocationException e) {
					ret[i]= ""; //$NON-NLS-1$
				}
			}
			return ret;
		}

		// in initialization phase?
		return new String[0];
	}

	@Override
	public String getText() {
		int r= fDifference.rightLength();
		int l= fDifference.leftLength();
		int c= Math.min(r, l);
		int a= r - l;
		String changed= c > 0 ? NLSUtility.format(QuickDiffMessages.quickdiff_annotation_changed, Integer.valueOf(c)) : null;
		String added;
		if (a > 0)
			added= NLSUtility.format(QuickDiffMessages.quickdiff_annotation_added, Integer.valueOf(a));
		else if (a < 0)
			added= NLSUtility.format(QuickDiffMessages.quickdiff_annotation_deleted, Integer.valueOf(-a));
		else
			added= null;
		String line= c > 1 || c == 0 && Math.abs(a) > 1 ? QuickDiffMessages.quickdiff_annotation_line_plural : QuickDiffMessages.quickdiff_annotation_line_singular;

		String ret= (changed != null ? changed : "") + (changed != null ? " " + line : "")   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				+ (changed != null && added != null ? ", " : " ") + (added != null ? added : "")  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
				+ (added != null && changed == null ? " " + line : ""); //$NON-NLS-1$//$NON-NLS-2$
		return ret;
	}

	/**
	 * @return Returns the difference.
	 */
	public QuickDiffRangeDifference getDifference() {
		return fDifference;
	}

	/**
	 * @return Returns the offset.
	 */
	public int getOffset() {
		return fOffset;
	}
}
