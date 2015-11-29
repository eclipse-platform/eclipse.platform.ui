/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (anton.leherbauer@windriver.com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

import org.eclipse.core.runtime.Assert;


/**
 * Copy-on-write <code>ITextStore</code> wrapper.
 * <p>
 * This implementation uses an unmodifiable text store for the initial content. Upon first
 * modification attempt, the unmodifiable store is replaced with a modifiable instance which must be
 * supplied in the constructor.
 * </p>
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 *
 * @since 3.2
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CopyOnWriteTextStore implements ITextStore {

	/**
	 * An unmodifiable String based text store. It is not possible to modify the initial content.
	 * Trying to {@link #replace} a text range or {@link #set} new content will throw an
	 * <code>UnsupportedOperationException</code>.
	 */
	private static class StringTextStore implements ITextStore {

		/** Minimum text limit whether to enable String copying */
		private static final int SMALL_TEXT_LIMIT= 1024 * 1024;

		/** Represents the content of this text store. */
		private final String fText;
		
		/** Minimum length limit below which {@link #get(int, int)} will return a String copy */
		private final int fCopyLimit;

		/**
		 * Create an empty text store.
		 */
		private StringTextStore() {
			this(""); //$NON-NLS-1$
		}

		/**
		 * Create a text store with initial content.
		 *
		 * @param text the initial content
		 */
		private StringTextStore(String text) {
			super();
			fText= text != null ? text : ""; //$NON-NLS-1$
			fCopyLimit= fText.length() > SMALL_TEXT_LIMIT ? fText.length() / 2 : 0;
		}

		@Override
		public char get(int offset) {
			return fText.charAt(offset);
		}

		@Override
		public String get(int offset, int length) {
			if (length < fCopyLimit) {
				// create a copy to avoid sharing of contained char[] - bug 292664
				return new String(fText.substring(offset, offset + length).toCharArray());
			}
			return fText.substring(offset, offset + length);
		}

		@Override
		public int getLength() {
			return fText.length();
		}

		@Override
		public void replace(int offset, int length, String text) {
			// modification not supported
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(String text) {
			// modification not supported
			throw new UnsupportedOperationException();
		}

	}

	/** The underlying "real" text store */
	protected ITextStore fTextStore= new StringTextStore();

	/** A modifiable <code>ITextStore</code> instance */
	private final ITextStore fModifiableTextStore;

	/**
	 * Creates an empty text store. The given text store will be used upon first modification
	 * attempt.
	 *
	 * @param modifiableTextStore a modifiable <code>ITextStore</code> instance, may not be
	 *            <code>null</code>
	 */
	public CopyOnWriteTextStore(ITextStore modifiableTextStore) {
		Assert.isNotNull(modifiableTextStore);
		fTextStore= new StringTextStore();
		fModifiableTextStore= modifiableTextStore;
	}

	@Override
	public char get(int offset) {
		return fTextStore.get(offset);
	}

	@Override
	public String get(int offset, int length) {
		return fTextStore.get(offset, length);
	}

	@Override
	public int getLength() {
		return fTextStore.getLength();
	}

	@Override
	public void replace(int offset, int length, String text) {
		if (fTextStore != fModifiableTextStore) {
			String content= fTextStore.get(0, fTextStore.getLength());
			fTextStore= fModifiableTextStore;
			fTextStore.set(content);
		}
		fTextStore.replace(offset, length, text);
	}

	@Override
	public void set(String text) {
		fTextStore= new StringTextStore(text);
		fModifiableTextStore.set(""); //$NON-NLS-1$
	}

}
