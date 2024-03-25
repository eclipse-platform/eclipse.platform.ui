/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Paul Pazderski - Bug 545252: cloned document was only partial read-only
 *******************************************************************************/
package org.eclipse.jface.text;

import java.util.Arrays;

import org.eclipse.core.runtime.Assert;


/**
 * An {@link org.eclipse.jface.text.IDocument} that is a read-only clone of another document.
 *
 * @since 3.0
 */
class DocumentClone extends AbstractDocument {

	private static class StringTextStore implements ITextStore {

		private String fContent;

		/**
		 * Creates a new string text store with the given content.
		 *
		 * @param content the content
		 */
		public StringTextStore(String content) {
			Assert.isNotNull(content);
			fContent= content;
		}

		@Override
		public char get(int offset) {
			return fContent.charAt(offset);
		}

		@Override
		public String get(int offset, int length) {
			return fContent.substring(offset, offset + length);
		}

		@Override
		public int getLength() {
			return fContent.length();
		}

		@Override
		public void replace(int offset, int length, String text) {
			// not allowed
		}

		@Override
		public void set(String text) {
			// not allowed
		}
	}

	/**
	 * Creates a new document clone with the given content.
	 *
	 * @param content the content
	 * @param lineDelimiters the line delimiters
	 */
	public DocumentClone(String content, String[] lineDelimiters) {
		super();
		setTextStore(new StringTextStore(content));

		boolean hasDefaultDelims= Arrays.equals(lineDelimiters, DefaultLineTracker.DELIMITERS);
		ILineTracker tracker= hasDefaultDelims ? new DefaultLineTracker() : new ConfigurableLineTracker(lineDelimiters);
		setLineTracker(tracker);
		getTracker().set(content);
		completeInitialization();
	}

	@Override
	public void replace(int pos, int length, String text) throws BadLocationException {
		// not allowed
	}

	@Override
	public void replace(int pos, int length, String text, long modificationStamp) throws BadLocationException {
		// not allowed
	}

	@Override
	public void set(String text) {
		// not allowed
	}

	@Override
	public void set(String text, long modificationStamp) {
		//not allowed
	}
}
