/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
package org.eclipse.jface.text.reconciler;

import org.eclipse.jface.text.ITypedRegion;


/**
 * A dirty region describes a document range which has been changed.
 */
public class DirtyRegion implements ITypedRegion {

	/**
	 * Identifies an insert operation.
	 */
	final static public String INSERT= "__insert"; //$NON-NLS-1$
	/**
	 * Identifies a remove operation.
	 */
	final static public String REMOVE= "__remove"; //$NON-NLS-1$

	/** The region's offset. */
	private int fOffset;
	/** The region's length. */
	private int fLength;
	/** Indicates the type of the applied change. */
	private final String fType;
	/** The text which has been inserted. */
	private String fText;

	/**
	 * Creates a new dirty region.
	 *
	 * @param offset the offset within the document where the change occurred
	 * @param length the length of the text within the document that changed
	 * @param type the type of change that this region represents: {@link #INSERT} {@link #REMOVE}
	 * @param text the substitution text
	 */
	public DirtyRegion(int offset, int length, String type, String text) {
		fOffset= offset;
		fLength= length;
		fType= normalizeTypeValue(type);
		fText= text;
	}

	/**
	 * Computes the normalized type value to ensure that the implementation can use object identity rather
	 * than equality.
	 *
	 * @param type the type value
	 * @return the normalized type value or <code>null</code>
	 * @since 3.1
	 */
	private String normalizeTypeValue(String type) {
		if (INSERT.equals(type)) {
			return INSERT;
		}
		if (REMOVE.equals(type)) {
			return REMOVE;
		}
		return null;
	}

	@Override
	public int getOffset() {
		return fOffset;
	}

	@Override
	public int getLength() {
		return fLength;
	}

	/*
	 * @see ITypedRegion#getType
	 */
	@Override
	public String getType() {
		return fType;
	}

	/**
	 * Returns the text that changed as part of the region change.
	 *
	 * @return the changed text
	 */
	public String getText() {
		return fText;
	}

	/**
	 * Modify the receiver so that it encompasses the region specified by the dirty region.
	 *
	 * @param dr the dirty region with which to merge
	 */
	void mergeWith(DirtyRegion dr) {
		int start= Math.min(fOffset, dr.fOffset);
		int end= Math.max(fOffset + fLength, dr.fOffset + dr.fLength);
		fOffset= start;
		fLength= end - start;
		fText= (dr.fText == null ? fText : (fText == null) ? dr.fText : fText + dr.fText);
	}
}
