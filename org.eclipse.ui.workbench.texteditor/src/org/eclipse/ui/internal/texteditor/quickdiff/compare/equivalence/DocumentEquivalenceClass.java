/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.quickdiff.compare.equivalence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * @since 3.2
 */
public final class DocumentEquivalenceClass {

	private static final boolean DEBUG= false;

	private final ArrayList fHashes;
	private IDocument fDocument;
	private final IHashFunction fHashFunction;

	public DocumentEquivalenceClass(IDocument document) {
		this(document, new DJBHashFunction());
	}

	public DocumentEquivalenceClass(IDocument document, IHashFunction hashFunction) {
		fDocument= document;
		Object[] nulls= new Object[fDocument.getNumberOfLines()];
		fHashes= new ArrayList(Arrays.asList(nulls));

		if (hashFunction == null)
			throw new NullPointerException("hashFunction"); //$NON-NLS-1$
		fHashFunction= hashFunction;
	}

	/**
	 * Returns the equivalence hash for line <code>line</code>.
	 *
	 * @param line the line for which to get the equivalent hash
	 * @return the hash in the equivalence class defined by the hash
	 *         function
	 * @throws IndexOutOfBoundsException if <code>line</code> is not a
	 *         legal document line
	 * @throws ConcurrentModificationException if the document is
	 *         modified concurrently to this method call
	 */
	public Hash getHash(int line) {
		try {
			return internalGetHash(line);
		} catch (BadLocationException x) {
			throw new ConcurrentModificationException();
		}
	}

	private Hash internalGetHash(int line) throws BadLocationException {
		Hash hash= (Hash) fHashes.get(line);
		if (hash == null) {
			if (fDocument == null)
				throw new AssertionError("hash cannot be null after loadAndForget"); //$NON-NLS-1$

			IRegion lineRegion= fDocument.getLineInformation(line);
			String lineContents= fDocument.get(lineRegion.getOffset(), lineRegion.getLength());
			hash= fHashFunction.computeHash(lineContents);
			fHashes.set(line, hash);
		}

		return hash;
	}

	/**
	 * Cleanses the lines affected by the document event from the
	 * internal hash cache. Must be called before the document is
	 * modified (in documentAboutToBeChanged).
	 *
	 * @param event the document event
	 */
	public void update(DocumentEvent event) {
		if (fDocument == null)
			throw new IllegalStateException("update must not be called after loadAndForget"); //$NON-NLS-1$
		try {
			internalUpdate(event);
		} catch (BadLocationException x) {
			throw new ConcurrentModificationException();
		}
	}

	private void internalUpdate(DocumentEvent event) throws BadLocationException {
		int linesBefore= fDocument.getNumberOfLines(event.getOffset(), event.getLength());
		String text= event.getText();
		int linesAfter= (text == null ? 0 : fDocument.computeNumberOfLines(text)) + 1;
		int firstLine= fDocument.getLineOfOffset(event.getOffset());

		int delta= linesAfter - linesBefore;
		int changed= Math.min(linesAfter, linesBefore);

		if (delta > 0) {
			Object[] nulls= new Object[delta];
			fHashes.addAll(firstLine + changed, Arrays.asList(nulls));
		} else if (delta < 0) {
			fHashes.subList(firstLine, firstLine - delta).clear();
		}
		Collections.fill(fHashes.subList(firstLine, firstLine + changed), null);
	}

	/**
	 * @return the number of items
	 */
	public int getCount() {
		return fHashes.size();
	}

	public void setDocument(IDocument document) {
		Assert.isNotNull(document);
		if (DEBUG)
			Assert.isTrue(document.get().equals(fDocument.get()));
		fDocument= document;
	}

	/**
	 * Computes all hashes and forgets the document. Don't call update
	 * afterwards.
	 */
	public void loadAndForget() {
		int count= getCount();
		for (int line= 0; line < count; line++)
			getHash(line);

		fDocument= null;
	}
}
