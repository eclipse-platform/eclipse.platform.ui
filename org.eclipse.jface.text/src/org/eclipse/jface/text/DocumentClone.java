/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

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

		/*
		 * @see org.eclipse.jface.text.ITextStore#get(int)
		 */
		public char get(int offset) {
			return fContent.charAt(offset);
		}

		/*
		 * @see org.eclipse.jface.text.ITextStore#get(int, int)
		 */
		public String get(int offset, int length) {
			return fContent.substring(offset, offset + length);
		}

		/*
		 * @see org.eclipse.jface.text.ITextStore#getLength()
		 */
		public int getLength() {
			return fContent.length();
		}

		/*
		 * @see org.eclipse.jface.text.ITextStore#replace(int, int, java.lang.String)
		 */
		public void replace(int offset, int length, String text) {
		}

		/*
		 * @see org.eclipse.jface.text.ITextStore#set(java.lang.String)
		 */
		public void set(String text) {
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
		ConfigurableLineTracker tracker= new ConfigurableLineTracker(lineDelimiters);
		setLineTracker(tracker);
		getTracker().set(content);
		completeInitialization();
	}
}
