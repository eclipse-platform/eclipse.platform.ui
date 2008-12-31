/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.revisions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.graphics.RGB;

import org.eclipse.jface.internal.text.revisions.ChangeRegion;
import org.eclipse.jface.internal.text.revisions.Hunk;

import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.source.ILineRange;

/**
 * Describes a revision of a document. A revision consists of one ore more {@link ILineRange}s.
 * <p>
 * Clients may subclass.
 * </p>
 *
 * @since 3.2
 */
public abstract class Revision {
	/** The original list of change regions, element type: {@link ChangeRegion}. */
	private final List fChangeRegions= new ArrayList();
	/**
	 * The cached list of adjusted ranges, element type: {@link RevisionRange}. <code>null</code>
	 * if the list must be re-computed. Unmodifiable.
	 *
	 * @since 3.3
	 */
	private List fRanges= null;

	/**
	 * Creates a new revision.
	 */
	protected Revision() {
	}

	/**
	 * Adds a line range to this revision. The range must be non-empty and have a legal start line
	 * (not -1).
	 *
	 * @param range a line range that was changed with this revision
	 * @throws IndexOutOfBoundsException if the line range is empty or has a negative start line
	 */
	public final void addRange(ILineRange range) throws IndexOutOfBoundsException {
		fChangeRegions.add(new ChangeRegion(this, range));
	}

	/**
	 * Returns the contained {@link RevisionRange}s adapted to the current diff state. The returned
	 * information is only valid at the moment it is returned, and may change as the annotated
	 * document is modified.
	 *
	 * @return an unmodifiable view of the contained ranges (element type: {@link RevisionRange})
	 */
	public final List getRegions() {
		if (fRanges == null) {
			List ranges= new ArrayList(fChangeRegions.size());
			for (Iterator it= fChangeRegions.iterator(); it.hasNext();) {
				ChangeRegion region= (ChangeRegion) it.next();
				for (Iterator inner= region.getAdjustedRanges().iterator(); inner.hasNext();) {
					ILineRange range= (ILineRange) inner.next();
					ranges.add(new RevisionRange(this, range));
				}
			}
			fRanges= Collections.unmodifiableList(ranges);
		}
		return fRanges;
	}

	/**
	 * Adjusts the revision information to the given diff information. Any previous diff information
	 * is discarded.
	 *
	 * @param hunks the diff hunks to adjust the revision information to
	 * @since 3.3
	 */
	final void applyDiff(Hunk[] hunks) {
		fRanges= null; // mark for recomputation
		for (Iterator regions= fChangeRegions.iterator(); regions.hasNext();) {
			ChangeRegion region= (ChangeRegion) regions.next();
			region.clearDiff();
			for (int i= 0; i < hunks.length; i++) {
				Hunk hunk= hunks[i];
				region.adjustTo(hunk);
			}
		}
	}

	/**
	 * Returns the hover information that will be shown when the user hovers over the a change
	 * region of this revision.
	 * <p>
	 * <strong>Note:</strong> The hover information control which is used to display the information
	 * must be able process the given object. If the default information control creator is used
	 * the supported format is simple text, full HTML or an HTML fragment.
	 * </p>
	 *
	 * @return the hover information for this revision or <code>null</code> for no hover
	 * @see RevisionInformation#setHoverControlCreator(IInformationControlCreator)
	 */
	public abstract Object getHoverInfo();

	/**
	 * Returns the author color for this revision. This color can be used to visually distinguish
	 * one revision from another, for example as background color.
	 * <p>
	 * Revisions from the same author must return the same color and revisions from different authors
	 * must return distinct colors.</p>
	 *
	 * @return the RGB color for this revision's author
	 */
	public abstract RGB getColor();

	/**
	 * Returns the unique (within the document) id of this revision. This may be the version string
	 * or a different identifier.
	 *
	 * @return the id of this revision
	 */
	public abstract String getId();

	/**
	 * Returns the modification date of this revision.
	 *
	 * @return the modification date of this revision
	 */
	public abstract Date getDate();

	/*
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Revision " + getId(); //$NON-NLS-1$
	}

	/**
	 * Returns the display string for the author of this revision.
	 * <p>
	 * Subclasses should replace - the default implementation returns the empty string.
	 * </p>
	 *
	 * @return the author name
	 * @since 3.3
	 */
	public String getAuthor() {
		return ""; //$NON-NLS-1$
	}
}
