/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;


import java.util.LinkedList;


/**
 * A text store that optimizes a given source text store for sequential rewriting.
 * While rewritten it keeps a list of replace command that serve as patches for
 * the source store. Only on request, the source store is indeed manipulated
 * by applying the patch commands to the source text store.
 *
 * @since 2.0
 * @deprecated since 3.3 as {@link GapTextStore} performs better even for sequential rewrite scenarios
 */
@Deprecated
public class SequentialRewriteTextStore implements ITextStore {

	/**
	 * A buffered replace command.
	 */
	private static class Replace {
		public int newOffset;
		public final int offset;
		public final int length;
		public final String text;

		public Replace(int offset, int newOffset, int length, String text) {
			this.newOffset= newOffset;
			this.offset= offset;
			this.length= length;
			this.text= text;
		}
	}

	/** The list of buffered replacements. */
	private LinkedList<Replace> fReplaceList;
	/** The source text store */
	private ITextStore fSource;
	/** A flag to enforce sequential access. */
	private static final boolean ASSERT_SEQUENTIALITY= false;


	/**
	 * Creates a new sequential rewrite store for the given source store.
	 *
	 * @param source the source text store
	 */
	public SequentialRewriteTextStore(ITextStore source) {
		fReplaceList= new LinkedList<>();
		fSource= source;
	}

	/**
	 * Returns the source store of this rewrite store.
	 *
	 * @return  the source store of this rewrite store
	 */
	public ITextStore getSourceStore() {
		commit();
		return fSource;
	}

	@Override
	public void replace(int offset, int length, String text) {
		if (text == null)
			text= ""; //$NON-NLS-1$

		if (fReplaceList.size() == 0) {
			fReplaceList.add(new Replace(offset, offset, length, text));

		} else {
			Replace firstReplace= fReplaceList.getFirst();
			Replace lastReplace= fReplaceList.getLast();

			// backward
			if (offset + length <= firstReplace.newOffset) {
				int delta= text.length() - length;
				if (delta != 0) {
					for (Replace replace : fReplaceList) {
						replace.newOffset += delta;
					}
				}

				fReplaceList.addFirst(new Replace(offset, offset, length, text));

			// forward
			} else if (offset >= lastReplace.newOffset + lastReplace.text.length()) {
				int delta= getDelta(lastReplace);
				fReplaceList.add(new Replace(offset - delta, offset, length, text));

			} else if (ASSERT_SEQUENTIALITY) {
				throw new IllegalArgumentException();

			} else {
				commit();
				fSource.replace(offset, length, text);
			}
		}
	}

	@Override
	public void set(String text) {
		fSource.set(text);
		fReplaceList.clear();
	}

	@Override
	public String get(int offset, int length) {

		if (fReplaceList.isEmpty())
			return fSource.get(offset, length);


		Replace firstReplace= fReplaceList.getFirst();
		Replace lastReplace= fReplaceList.getLast();

		// before
		if (offset + length <= firstReplace.newOffset) {
			return fSource.get(offset, length);

			// after
		} else if (offset >= lastReplace.newOffset + lastReplace.text.length()) {
			int delta= getDelta(lastReplace);
			return fSource.get(offset - delta, length);

		} else if (ASSERT_SEQUENTIALITY) {
			throw new IllegalArgumentException();

		} else {

			int delta= 0;
			for (Replace replace : fReplaceList) {
				if (offset + length < replace.newOffset) {
					return fSource.get(offset - delta, length);

				} else if (offset >= replace.newOffset && offset + length <= replace.newOffset + replace.text.length()) {
					return replace.text.substring(offset - replace.newOffset, offset - replace.newOffset + length);

				} else if (offset >= replace.newOffset + replace.text.length()) {
					delta= getDelta(replace);
					continue;

				} else {
					commit();
					return fSource.get(offset, length);
				}
			}

			return fSource.get(offset - delta, length);
		}

	}

	/**
	 * Returns the difference between the offset in the source store and the "same" offset in the
	 * rewrite store after the replace operation.
	 *
	 * @param replace the replace command
	 * @return the difference
	 */
	private static final int getDelta(Replace replace) {
		return replace.newOffset - replace.offset + replace.text.length() - replace.length;
	}

	@Override
	public char get(int offset) {
		if (fReplaceList.isEmpty())
			return fSource.get(offset);

		Replace firstReplace= fReplaceList.getFirst();
		Replace lastReplace= fReplaceList.getLast();

		// before
		if (offset < firstReplace.newOffset) {
			return fSource.get(offset);

			// after
		} else if (offset >= lastReplace.newOffset + lastReplace.text.length()) {
			int delta= getDelta(lastReplace);
			return fSource.get(offset - delta);

		} else if (ASSERT_SEQUENTIALITY) {
			throw new IllegalArgumentException();

		} else {

			int delta= 0;
			for (Replace replace : fReplaceList) {
				if (offset < replace.newOffset)
					return fSource.get(offset - delta);

				else if (offset < replace.newOffset + replace.text.length())
					return replace.text.charAt(offset - replace.newOffset);

				delta= getDelta(replace);
			}

			return fSource.get(offset - delta);
		}
	}

	@Override
	public int getLength() {
		if (fReplaceList.isEmpty())
			return fSource.getLength();

		Replace lastReplace= fReplaceList.getLast();
		return fSource.getLength() + getDelta(lastReplace);
	}

	/**
	 * Disposes this rewrite store.
	 */
	public void dispose() {
		fReplaceList= null;
		fSource= null;
	}

	/**
	 * Commits all buffered replace commands.
	 */
	private void commit() {

		if (fReplaceList.isEmpty())
			return;

		StringBuffer buffer= new StringBuffer();

		int delta= 0;
		for (Replace replace : fReplaceList) {
			int offset= buffer.length() - delta;
			buffer.append(fSource.get(offset, replace.offset - offset));
			buffer.append(replace.text);
			delta= getDelta(replace);
		}

		int offset= buffer.length() - delta;
		buffer.append(fSource.get(offset, fSource.getLength() - offset));

		fSource.set(buffer.toString());
		fReplaceList.clear();
	}
}
