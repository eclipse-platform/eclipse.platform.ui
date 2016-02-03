/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextStore;

/**
 *
 * @since 3.2
 */
public abstract class AbstractLineTrackerTest {
	protected ITextStore fText;
	protected ILineTracker  fTracker;

	protected final void checkLines(int[] lines) throws BadLocationException {
		assertEquals("number of lines", lines.length, fTracker.getNumberOfLines());

		for (int i= 0; i < lines.length; i++) {
			IRegion line= fTracker.getLineInformation(i);

			assertEquals("line lenght of line " + i, lines[i],  line.getLength());

			assertEquals("line offset of line " + i, getLineOffset(i, lines),  line.getOffset());
		}
	}

	abstract int getLineOffset(int line, int[] lines);

	protected final void replace(int offset, int length, String text) throws BadLocationException {
		fTracker.replace(offset, length, text);
		fText.replace(offset, length, text);
	}

	protected final void set(String string) {
		fText.set(string);
		fTracker.set(string);
    }

}
