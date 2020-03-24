/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

package org.eclipse.ui.tests.keys;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeySequenceText;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests Bug 42024
 *
 * @since 3.0
 */
public class Bug42024Test {

	/** The shell on which the <code>KeySequenceText</code> is placed. */
	private Shell shell = null;

	/** The instance of <code>KeySequenceText</code> we should tinker with. */
	private KeySequenceText text = null;

	@Before
	public void doSetUp() throws Exception {
		// Create a window with a KeySequenceText
		Display display = Display.getCurrent();
		shell = new Shell(display);
		shell.setLayout(new RowLayout());
		text = new KeySequenceText(new Text(shell, SWT.BORDER));

		// Open it
		shell.pack();
		shell.open();
	}

	@After
	public void doTearDown() throws Exception {
		// Close and destroy the window
		shell.close();
		shell.dispose();

		// Release the memory.
		shell = null;
		text = null;
	}

	/**
	 * Tests that the limiting facility on KeySequenceText allows an arbitrary
	 * number of key strokes, when the the limit is set to "infinite". In this
	 * case, we will use a six stroke sequence.
	 *
	 * @throws ParseException
	 *             If the test sequence cannot be parsed.
	 */
	@Test
	public void testInfiniteStrokes() throws ParseException {
		String keySequenceText = "A B C D E F"; //$NON-NLS-1$
		KeySequence keySequence = KeySequence.getInstance(keySequenceText);
		text.setKeyStrokeLimit(KeySequenceText.INFINITE);
		text.setKeySequence(keySequence);
		assertEquals(
				"Infinite limit but sequence changed.", keySequence, text.getKeySequence()); //$NON-NLS-1$
	}

	/**
	 * Tests that inserting a key sequence of matching length causes no change,
	 * but inserted a key sequence of one greater does cause a change --
	 * specifically truncation.
	 *
	 * @throws ParseException
	 *             If the test sequences cannot be parsed.
	 */
	@Test
	public void testTruncation() throws ParseException {
		final int length = 4;
		text.setKeyStrokeLimit(length);

		// Test matching length.
		String matchingKeySequenceText = "1 2 3 4"; //$NON-NLS-1$
		KeySequence matchingKeySequence = KeySequence
				.getInstance(matchingKeySequenceText);
		text.setKeySequence(matchingKeySequence);
		assertEquals(
				"Limit of four change four stroke sequence.", matchingKeySequence, text.getKeySequence()); //$NON-NLS-1$

		// Test one greater than length.
		String longerKeySequenceText = "1 2 3 4 5"; //$NON-NLS-1$
		KeySequence longerKeySequence = KeySequence
				.getInstance(longerKeySequenceText);
		text.setKeySequence(longerKeySequence);
		assertEquals(
				"Limit of four did not truncate to four.", length, text.getKeySequence().getKeyStrokes().length); //$NON-NLS-1$
	}

	/**
	 * Tests that a zero-length stroke can be inserted into the KeySequenceText --
	 * regardless of whether the stroke limit is some positive integer or
	 * infinite.
	 */
	@Test
	public void testZeroStroke() {
		KeySequence zeroStrokeSequence = KeySequence.getInstance();

		// Test with a limit of four.
		text.setKeyStrokeLimit(4);
		text.setKeySequence(zeroStrokeSequence);
		assertEquals(
				"Limit of four changed zero stroke sequence.", zeroStrokeSequence, text.getKeySequence()); //$NON-NLS-1$

		// Test with an infinite limit.
		text.setKeyStrokeLimit(KeySequenceText.INFINITE);
		text.setKeySequence(zeroStrokeSequence);
		assertEquals(
				"Infinite limit changed zero stroke sequence.", zeroStrokeSequence, text.getKeySequence()); //$NON-NLS-1$
	}
}
