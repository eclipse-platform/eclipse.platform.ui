/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
 *     Tom Eicher (Avaloq Evolution AG) - block selection mode
 *******************************************************************************/
package org.eclipse.jface.text;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;


/**
 * Standard implementation of {@link org.eclipse.jface.text.ITextSelection}.
 * <p>
 * Takes advantage of the weak contract of correctness of its interface. If
 * generated from a selection provider, it only remembers its offset and length
 * and computes the remaining information on request.</p>
 */
public class TextSelection implements IMultiTextSelection {

	/**
	 * Debug option for asserting valid offset and length.
	 *
	 * @since 3.5
	 */
	private static final boolean ASSERT_INVLID_SELECTION_NULL= "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.jface.text/assert/TextSelection/validConstructorArguments")); //$NON-NLS-1$ //$NON-NLS-2$

	/** Internal empty text selection */
	private static final ITextSelection NULL= new TextSelection();

	/**
	 * Returns the shared instance of the empty text selection.
	 *
	 * @return the shared instance of an empty text selection
	 */
	public static ITextSelection emptySelection() {
		return NULL;
	}

	/** Document which delivers the data of the selection, possibly <code>null</code>. */
	private final IDocument fDocument;
	/** Offset of the selection */
	private final int fOffset;
	/** Length of the selection */
	private final int fLength;


	/**
	 * Creates an empty text selection.
	 */
	private TextSelection() {
		fOffset= -1;
		fLength= -1;
		fDocument= null;
	}

	/**
	 * Creates a text selection for the given range. This
	 * selection object describes generically a text range and
	 * is intended to be an argument for the <code>setSelection</code>
	 * method of selection providers.
	 *
	 * @param offset the offset of the range, must not be negative
	 * @param length the length of the range, must not be negative
	 */
	public TextSelection(int offset, int length) {
		this(null, offset, length);
	}

	/**
	 * Creates a text selection for the given range of the given document.
	 * This selection object is created by selection providers in responds
	 * <code>getSelection</code>.
	 *
	 * @param document the document whose text range is selected in a viewer
	 * @param offset the offset of the selected range, must not be negative
	 * @param length the length of the selected range, must not be negative
	 */
	public TextSelection(IDocument document, int offset, int length) {
		if (ASSERT_INVLID_SELECTION_NULL) {
			Assert.isLegal(offset >= 0);
			Assert.isLegal(length >= 0);
		}
		fDocument= document;
		fOffset= offset;
		fLength= length;
	}

	/**
	 * Tells whether this text selection is the empty selection.
	 * <p>
	 * A selection of length 0 is not an empty text selection as it
	 * describes, e.g., the cursor position in a viewer.</p>
	 *
	 * @return <code>true</code> if this selection is empty
	 * @see #emptySelection()
	 */
	@Override
	public boolean isEmpty() {
		return this == NULL || /* backwards compatibility: */ fOffset < 0 || fLength < 0;
	}

	@Override
	public int getOffset() {
		return fOffset;
	}

	@Override
	public int getLength() {
		return fLength;
	}

	@Override
	public int getStartLine() {

		try {
			if (fDocument != null) {
				return fDocument.getLineOfOffset(fOffset);
			}
		} catch (BadLocationException x) {
			// ignore
		}

		return -1;
	}

	@Override
	public int getEndLine() {
		try {
			if (fDocument != null) {
				int endOffset= fOffset + fLength;
				if (fLength != 0) {
					endOffset--;
				}
				return fDocument.getLineOfOffset(endOffset);
			}
		} catch (BadLocationException x) {
			// ignore
		}

		return -1;
	}

	@Override
	public String getText() {
		try {
			if (fDocument != null) {
				return fDocument.get(fOffset, fLength);
			}
		} catch (BadLocationException x) {
			// ignore
		}

		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		TextSelection s= (TextSelection) obj;
		boolean sameRange= (s.fOffset == fOffset && s.fLength == fLength);
		if (sameRange) {

			if (s.fDocument == null && fDocument == null) {
				return true;
			}
			if (s.fDocument == null || fDocument == null) {
				return false;
			}

			try {
				String sContent= s.fDocument.get(fOffset, fLength);
				String content= fDocument.get(fOffset, fLength);
				return sContent.equals(content);
			} catch (BadLocationException x) {
				// ignore
			}
		}

		return false;
	}

	@Override
	public int hashCode() {
	 	int low= fDocument != null ? fDocument.hashCode() : 0;
	 	return (fOffset << 24) | (fLength << 16) | low;
	}

	/**
	 * Returns the document underlying the receiver, possibly <code>null</code>.
	 *
	 * @return the document underlying the receiver, possibly <code>null</code>
	 * @since 3.5
	 */
	protected IDocument getDocument() {
		return fDocument;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TextSelection [offset: ").append(fOffset); //$NON-NLS-1$
		int startLine = getStartLine();
		sb.append(", startLine: ").append(startLine); //$NON-NLS-1$
		int endLine = getEndLine();
		if (endLine != startLine) {
			sb.append(", endLine: ").append(endLine); //$NON-NLS-1$
		}
		sb.append(", length: ").append(fLength); //$NON-NLS-1$
		if (fLength != 0) {
			sb.append(", text: ").append(getText()); //$NON-NLS-1$
		}
		if (fDocument != null) {
			sb.append(", document: ").append(fDocument); //$NON-NLS-1$
		}
		sb.append("]"); //$NON-NLS-1$
		return sb.toString();
	}

	@Override
	public IRegion[] getRegions() {
		return new IRegion[] { new Region(getOffset(), getLength()) };
	}
}

