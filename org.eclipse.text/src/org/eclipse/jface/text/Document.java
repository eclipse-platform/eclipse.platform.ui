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
package org.eclipse.jface.text;


/**
 * Default document implementation. Uses a {@link org.eclipse.jface.text.GapTextStore} wrapped
 * inside a {@link org.eclipse.jface.text.CopyOnWriteTextStore} as text store.
 * <p>
 * The used line tracker considers the following strings as line delimiters: "\n", "\r", "\r\n". In
 * case of a text replacement across line delimiter boundaries and with different line delimiters,
 * the line tracker might have to be repaired. Use
 * {@link #isLineInformationRepairNeeded(int, int, String)} before doing the text replace if you
 * have the need to discover such a situation.
 * </p>
 * <p>
 * The document is ready to use. It has a default position category for which a default position
 * updater is installed.
 * </p>
 * <p>
 * <strong>Performance:</strong> The implementation should perform reasonably well for typical
 * source code documents. It is not designed for very large documents of a size of several
 * megabytes. Space-saving implementations are initially used for both the text store and the line
 * tracker; the first modification after a {@link #set(String) set} incurs the cost to transform the
 * document structures to efficiently handle updates.
 * </p>
 * <p>
 * See {@link GapTextStore} and <code>TreeLineTracker</code> for algorithmic behavior of the used
 * document structures.
 * </p>
 * 
 * @see org.eclipse.jface.text.GapTextStore
 * @see org.eclipse.jface.text.CopyOnWriteTextStore
 */
public class Document extends AbstractDocument {
	/**
	 * Creates a new empty document.
	 */
	public Document() {
		super();
		setTextStore(new CopyOnWriteTextStore(new GapTextStore()));
		setLineTracker(new DefaultLineTracker());
		completeInitialization();
	}

	/**
	 * Creates a new document with the given initial content.
	 *
	 * @param initialContent the document's initial content
	 */
	public Document(String initialContent) {
		super();
		setTextStore(new CopyOnWriteTextStore(new GapTextStore()));
		setLineTracker(new DefaultLineTracker());
		getStore().set(initialContent);
		getTracker().set(initialContent);
		completeInitialization();
	}

	@Override
	public boolean isLineInformationRepairNeeded(int offset, int length, String text) throws BadLocationException {
		if ((0 > offset) || (0 > length) || (offset + length > getLength()))
			throw new BadLocationException();

		return isLineInformationRepairNeeded(text) || isLineInformationRepairNeeded(get(offset, length));
	}

	/**
	 * Checks whether the line information needs to be repaired.
	 *
	 * @param text the text to check
	 * @return <code>true</code> if the line information must be repaired
	 * @since 3.4
	 */
	private boolean isLineInformationRepairNeeded(String text) {
		if (text == null)
			return false;

		int length= text.length();
		if (length == 0)
			return false;

		int rIndex= text.indexOf('\r');
		int nIndex= text.indexOf('\n');
		if (rIndex == -1 && nIndex == -1)
			return false;

		if (rIndex > 0 && rIndex < length-1 && nIndex > 1 && rIndex < length-2)
			return false;

		String defaultLD= null;
		try {
			defaultLD= getLineDelimiter(0);
		} catch (BadLocationException x) {
			return true;
		}

		if (defaultLD == null)
			return false;

		defaultLD= getDefaultLineDelimiter();

		if (defaultLD.length() == 1) {
			if (rIndex != -1 && !"\r".equals(defaultLD)) //$NON-NLS-1$
				return true;
			if (nIndex != -1 && !"\n".equals(defaultLD)) //$NON-NLS-1$
				return true;
		} else if (defaultLD.length() == 2)
			return rIndex == -1 || nIndex - rIndex != 1;

		return false;
	}

}
