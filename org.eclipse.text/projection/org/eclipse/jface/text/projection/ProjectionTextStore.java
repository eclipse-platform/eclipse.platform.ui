/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.projection;


import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextStore;
import org.eclipse.jface.text.Region;


/**
 * A text store representing the projection defined by the given document
 * information mapping.
 *
 * @since 3.0
 */
class ProjectionTextStore implements ITextStore {

	/**
	 * Implementation of {@link IRegion} that can be reused
	 * by setting the offset and the length.
	 */
	private static class ReusableRegion implements IRegion {

		private int fOffset;
		private int fLength;

		/*
		 * @see org.eclipse.jface.text.IRegion#getLength()
		 */
		public int getLength() {
			return fLength;
		}

		/*
		 * @see org.eclipse.jface.text.IRegion#getOffset()
		 */
		public int getOffset() {
			return fOffset;
		}

		/**
		 * Updates this region.
		 *
		 * @param offset the new offset
		 * @param length the new length
		 */
		public void update(int offset, int length) {
			fOffset= offset;
			fLength= length;
		}
	}

	/** The master document */
	private IDocument fMasterDocument;
	/** The document information mapping */
	private IMinimalMapping fMapping;
	/** Internal region used for querying the mapping. */
	private ReusableRegion fReusableRegion= new ReusableRegion();


	/**
	 * Creates a new projection text store for the given master document and
	 * the given document information mapping.
	 *
	 * @param masterDocument the master document
	 * @param mapping the document information mapping
	 */
	public ProjectionTextStore(IDocument masterDocument, IMinimalMapping mapping) {
		fMasterDocument= masterDocument;
		fMapping= mapping;
	}

	private void internalError() {
		throw new IllegalStateException();
	}

	/*
	 * @see org.eclipse.jface.text.ITextStore#set(java.lang.String)
	 */
	public void set(String contents) {

		IRegion masterRegion= fMapping.getCoverage();
		if (masterRegion == null)
			internalError();

		try {
			fMasterDocument.replace(masterRegion.getOffset(), masterRegion.getLength(), contents);
		} catch (BadLocationException e) {
			internalError();
		}
	}

	/*
	 * @see org.eclipse.jface.text.ITextStore#replace(int, int, java.lang.String)
	 */
	public void replace(int offset, int length, String text) {
		fReusableRegion.update(offset, length);
		try {
			IRegion masterRegion= fMapping.toOriginRegion(fReusableRegion);
			fMasterDocument.replace(masterRegion.getOffset(), masterRegion.getLength(), text);
		} catch (BadLocationException e) {
			internalError();
		}
	}

	/*
	 * @see org.eclipse.jface.text.ITextStore#getLength()
	 */
	public int getLength() {
		return fMapping.getImageLength();
	}

	/*
	 * @see org.eclipse.jface.text.ITextStore#get(int)
	 */
	public char get(int offset) {
		try {
			int originOffset= fMapping.toOriginOffset(offset);
			return fMasterDocument.getChar(originOffset);
		} catch (BadLocationException e) {
			internalError();
		}

		// unreachable
		return (char) 0;
	}

	/*
	 * @see ITextStore#get(int, int)
	 */
	public String get(int offset, int length) {
		try {
			IRegion[] fragments= fMapping.toExactOriginRegions(new Region(offset, length));
			StringBuffer buffer= new StringBuffer();
			for (int i= 0; i < fragments.length; i++) {
				IRegion fragment= fragments[i];
				buffer.append(fMasterDocument.get(fragment.getOffset(), fragment.getLength()));
			}
			return buffer.toString();
		} catch (BadLocationException e) {
			internalError();
		}

		// unreachable
		return null;
	}
}
