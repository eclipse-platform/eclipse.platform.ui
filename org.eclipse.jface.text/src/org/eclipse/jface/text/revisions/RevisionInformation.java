/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.internal.text.revisions.Hunk;

import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.information.IInformationProviderExtension2;

/**
 * Encapsulates revision information for one line-based document.
 * <p>
 * Clients may instantiate.
 * </p>
 *
 * @since 3.2
 * @see Revision
 */
public final class RevisionInformation implements ITextHoverExtension, IInformationProviderExtension2 {
	/** The revisions, element type: {@link Revision}. */
	private final List fRevisions= new ArrayList();
	/** A unmodifiable view of <code>fRevisions</code>. */
	private final List fRORevisions= Collections.unmodifiableList(fRevisions);
	/**
	 * The flattened list of {@link RevisionRange}s, unmodifiable. <code>null</code> if the list
	 * must be re-computed.
	 *
	 * @since 3.3
	 */
	private List fRanges= null;

	/**
	 * The hover control creator. Can be <code>null</code>.
	 *
	 * @since 3.3
	 */
	private IInformationControlCreator fHoverControlCreator;

	/**
	 * The information presenter control creator. Can be <code>null</code>.
	 *
	 * @since 3.3
	 */
	private IInformationControlCreator fInformationPresenterControlCreator;

	/**
	 * Creates a new revision information model.
	 */
	public RevisionInformation() {
	}

	/**
	 * Adds a revision.
	 *
	 * @param revision a revision
	 */
	public void addRevision(Revision revision) {
		Assert.isLegal(revision != null);
		fRevisions.add(revision);
	}

	/**
	 * Returns the contained revisions.
	 *
	 * @return an unmodifiable view of the contained revisions (element type: {@link Revision})
	 */
	public List getRevisions() {
		return fRORevisions;
	}

	/**
	 * Returns the line ranges of this revision information. The returned information is only valid
	 * at the moment it is returned, and may change as the annotated document is modified. See
	 * {@link IRevisionListener} for a way to be informed when the revision information changes. The
	 * returned list is sorted by document offset.
	 *
	 * @return an unmodifiable view of the line ranges (element type: {@link RevisionRange})
	 * @see IRevisionListener
	 * @since 3.3
	 */
	public List getRanges() {
		if (fRanges == null) {
			List ranges= new ArrayList(fRevisions.size() * 2); // wild size guess
			for (Iterator it= fRevisions.iterator(); it.hasNext();) {
				Revision revision= (Revision) it.next();
				ranges.addAll(revision.getRegions());
			}

			// sort by start line
			Collections.sort(ranges, new Comparator() {
				public int compare(Object o1, Object o2) {
					RevisionRange r1= (RevisionRange) o1;
					RevisionRange r2= (RevisionRange) o2;

					return r1.getStartLine() - r2.getStartLine();
				}
			});

			fRanges= Collections.unmodifiableList(ranges);
		}
		return fRanges;
	}

	/**
	 * Adjusts the revision information to the given diff information. Any previous diff information
	 * is discarded. <strong>Note:</strong> This is an internal framework method and must not be
	 * called by clients.
	 * 
	 * @param hunks the diff hunks to adjust the revision information to
	 * @since 3.3
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void applyDiff(Hunk[] hunks) {
		fRanges= null; // mark for recomputation
		for (Iterator revisions= getRevisions().iterator(); revisions.hasNext();)
			((Revision) revisions.next()).applyDiff(hunks);
	}

	/*
	 * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
	 * @since 3.3
	 */
	public IInformationControlCreator getHoverControlCreator() {
		return fHoverControlCreator;
	}

	/**
	 * {@inheritDoc}
	 * @return the information control creator or <code>null</code>
	 * @since 3.3
	 */
	public IInformationControlCreator getInformationPresenterControlCreator() {
		return fInformationPresenterControlCreator;
	}

	/**
	 * Sets the hover control creator.
	 * <p>
	 * <strong>Note:</strong> The created information control must be able to display the object
	 * returned by the concrete implementation of {@link Revision#getHoverInfo()}.
	 * </p>
	 *
	 * @param creator the control creator
	 * @since 3.3
	 */
	public void setHoverControlCreator(IInformationControlCreator creator) {
		fHoverControlCreator= creator;
	}

	/**
	 * Sets the information presenter control creator.
	 *
	 * @param creator the control creator
	 * @since 3.3
	 */
	public void setInformationPresenterControlCreator(IInformationControlCreator creator) {
		fInformationPresenterControlCreator= creator;
	}
}
