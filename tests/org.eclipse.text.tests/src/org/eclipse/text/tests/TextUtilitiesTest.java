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
 *******************************************************************************/
package org.eclipse.text.tests;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.eclipse.jface.text.AbstractLineTracker.DelimiterInfo;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.TextUtilities;


/**
 * A test case for text utilities.
 */
public class TextUtilitiesTest {

	/**
	 * A document which is a copy of another document.
	 * Implementation uses old document state.
	 */
	private static class LazilyMirroredDocument extends Document {

		private final class DocumentListener implements IDocumentListener {
			@Override
			public void documentAboutToBeChanged(DocumentEvent event) { /* not used */ }
			@Override
			public void documentChanged(DocumentEvent event) {
				fEvents.add(event);
			}
		}

		/** The document listener. */
		private final DocumentListener fDocumentListener= new DocumentListener();

		/** The buffered events. */
		private final List<DocumentEvent> fEvents= new ArrayList<>();

		public LazilyMirroredDocument(IDocument document) {
			document.addDocumentListener(fDocumentListener);
		}

		private void flush() throws BadLocationException {
			DocumentEvent event= TextUtilities.mergeUnprocessedDocumentEvents(this, fEvents);
			if (event == null)
				return;

			replace(event.getOffset(), event.getLength(), event.getText());
			fEvents.clear();
		}

		/*
		 * Should override all other getXXX() methods as well, but it's sufficient for the test.
		 *
		 * @see org.eclipse.jface.text.IDocument#get()
		 */
		@Override
		public String get() {
			try {
				flush();
			} catch (BadLocationException e) {
				assertFalse(true);
			}
			return super.get();
		}
	}

	/**
	 * A document which is a copy of another document.
	 * Implementation uses new document state.
	 */
	private static class LazilyMirroredDocument2 extends Document {

		private final class DocumentListener implements IDocumentListener {
			@Override
			public void documentAboutToBeChanged(DocumentEvent event) { /* not used */ }
			@Override
			public void documentChanged(DocumentEvent event) {
				event= new DocumentEvent(event.getDocument(), event.getOffset(), event.getLength(), event.getText());
				fEvents.add(event);
			}
		}

		/** The document listener. */
		private final DocumentListener fDocumentListener= new DocumentListener();

		/** The buffered events. */
		private final List<DocumentEvent> fEvents= new ArrayList<>();

		public LazilyMirroredDocument2(IDocument document) {
			document.addDocumentListener(fDocumentListener);
		}

		private void flush() throws BadLocationException {
			DocumentEvent event= TextUtilities.mergeProcessedDocumentEvents(fEvents);
			if (event == null)
				return;

			replace(event.getOffset(), event.getLength(), event.getText());
			fEvents.clear();
		}

		/*
		 * Should override all other getXXX() methods as well, but it's sufficient for the test.
		 *
		 * @see org.eclipse.jface.text.IDocument#get()
		 */
		@Override
		public String get() {
			try {
				flush();
			} catch (BadLocationException e) {
				fail("bad implementation");
			}
			return super.get();
		}
	}


	private static DocumentEvent createRandomEvent(IDocument document, int maxLength, char character) {

		int index0= (int) (Math.random() * (maxLength + 1));
		int index1= (int) (Math.random() * (maxLength + 1));

		int offset= Math.min(index0, index1);
		int length= Math.max(index0, index1) - offset;

		int stringLength=  (int) (Math.random() * 10);
		StringBuilder buffer= new StringBuilder(stringLength);
		for (int i= 0; i < stringLength; ++i)
			buffer.append(character);

		return new DocumentEvent(document, offset, length, buffer.toString());
	}

	@Test
	public void testMergeEvents1() {
		IDocument reference= new Document();
		LazilyMirroredDocument testee= new LazilyMirroredDocument(reference);

		try {
			reference.replace(0, 0, "01234567890123");
			check(reference, testee);

			reference.replace(4, 3, "moo ");
			reference.replace(9, 2, "asd");
			check(reference, testee);

		} catch (BadLocationException e) {
			fail("bad location exception");
		}
	}

	@Test
	public void testMergeEvents() {
		IDocument reference= new Document();
		LazilyMirroredDocument testee= new LazilyMirroredDocument(reference);

		try {

			List<DocumentEvent> events= new ArrayList<>();
			int currentLength= 0;

			events.add(new DocumentEvent(reference, 0, 0, "foo bar goo haa"));
			events.add(new DocumentEvent(reference, 0, "foo bar goo haa".length(), "foo bar goo haa"));
			events.add(new DocumentEvent(reference, 4, 4, "xxxx"));
			events.add(new DocumentEvent(reference, 4, 4, "yyy"));
			events.add(new DocumentEvent(reference, 4, 3, "moo "));
			events.add(new DocumentEvent(reference, 9, 2, "asd"));
			events.add(new DocumentEvent(reference, 0, 2, "asd"));

			for (DocumentEvent event : events) {
				currentLength += event.getText().length() - event.getLength();
			}

			for (int i= 0; i < 500; i++) {
				char character= (char) (32 + i % 95);
				DocumentEvent event= createRandomEvent(reference, currentLength, character);
				currentLength += event.getText().length() - event.getLength();
				events.add(event);
			}

			for (DocumentEvent event : events) {

//				System.err.println(event.getOffset() + ", " + event.getLength() + ", [" + event.getText() + "]") ;

				reference.replace(event.getOffset(), event.getLength(), event.getText());
				if (Math.random() < 0.3) {
//					System.err.println("check");
					check(reference, testee);
//					System.err.println("length= " + reference.getLength());
				}
			}

			check(reference, testee);

//			System.out.println("[" + reference.get() + "]");
//			System.out.println("[" + testee.get() + "]");

		} catch (BadLocationException e) {
			fail("bad location exception");
		}
	}

	@Test
	public void testMergeEvents2() {
		IDocument reference= new Document();
		LazilyMirroredDocument2 testee= new LazilyMirroredDocument2(reference);

		try {

			List<DocumentEvent> events= new ArrayList<>();
			int currentLength= 0;

			events.add(new DocumentEvent(reference, 0, 0, "foo bar goo haa"));
			events.add(new DocumentEvent(reference, 0, "foo bar goo haa".length(), "foo bar goo haa"));
			events.add(new DocumentEvent(reference, 4, 4, "xxxx"));
			events.add(new DocumentEvent(reference, 4, 4, "yyy"));
			events.add(new DocumentEvent(reference, 4, 3, "moo "));
			events.add(new DocumentEvent(reference, 9, 2, "asd"));
			events.add(new DocumentEvent(reference, 0, 2, "asd"));

			for (DocumentEvent event : events) {
				currentLength += event.getText().length() - event.getLength();
			}

			for (int i= 0; i < 500; i++) {
				char character= (char) (32 + i % 95);
				DocumentEvent event= createRandomEvent(reference, currentLength, character);
				currentLength += event.getText().length() - event.getLength();
				events.add(event);
			}

			for (DocumentEvent event : events) {
				reference.replace(event.getOffset(), event.getLength(), event.getText());
				if (Math.random() < 0.3) {
					check(reference, testee);
				}
			}

			check(reference, testee);

		} catch (BadLocationException e) {
			fail("bad location exception");
		}
	}

	private static void check(IDocument reference, IDocument testee) {
		Assertions.assertEquals(reference.get(), testee.get());
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testIndexOf() {
		int[] result;
		result = TextUtilities.indexOf(new String[0], "xxxxxxxxxx", 0);
		assertEquals(-1, result[0]);
		assertEquals(-1, result[1]);

		result = TextUtilities.indexOf(new String[] { "a", "ab", "abc" }, "xxxxxxxxxx", 0);
		assertEquals(-1, result[0]);
		assertEquals(-1, result[1]);

		result = TextUtilities.indexOf(new String[] { "a", "ab", "abc" }, "foobarabcd", 0);
		assertEquals(4, result[0]);
		assertEquals(0, result[1]);

		result = TextUtilities.indexOf(new String[] { "ab", "ab" }, "foobarabcd", 0);
		assertEquals(6, result[0]);
		assertEquals(0, result[1]);

		result = TextUtilities.indexOf(new String[] { "", "ab", "abc" }, "foobarabcd", 0);
		assertEquals(6, result[0]);
		assertEquals(2, result[1]);

		result = TextUtilities.indexOf(new String[] { "arac", "", "fuu" }, "foobarabcd", 0);
		assertEquals(0, result[0]);
		assertEquals(1, result[1]);

		result = TextUtilities.indexOf(new String[] { "", "" }, "foobarabcd", 0);
		assertEquals(0, result[0]);
		assertEquals(1, result[1]);

		result = TextUtilities.indexOf(new String[] { "" }, "foobarabcd", 5);
		// looks strange that searching from offset 5 returns match offset 0 but that is
		// how it was implemented
		assertEquals(0, result[0]);
		assertEquals(0, result[1]);

		result = TextUtilities.indexOf(new String[] { "abc" }, "foobarabcd", -5);
		assertEquals(6, result[0]);
		assertEquals(0, result[1]);

		result = TextUtilities.indexOf(new String[] { "abc" }, "foobarabcd", 20);
		assertEquals(-1, result[0]);
		assertEquals(-1, result[1]);

		try {
			TextUtilities.indexOf(null, "foobarabcd", 0);
			fail("Exception not thrown");
		} catch (NullPointerException ex) {
			// expected
		}

		try {
			TextUtilities.indexOf(new String[] { "abc", null }, "foobarabcd", 0);
			fail("Exception not thrown");
		} catch (NullPointerException ex) {
			// expected
		}

		try {
			TextUtilities.indexOf(new String[] { "abc" }, null, 0);
			fail("Exception not thrown");
		} catch (NullPointerException ex) {
			// expected
		}
	}

	@Test
	public void testNextDelimiter() {
		DelimiterInfo result;
		result = TextUtilities.nextDelimiter("abc\ndef", 0);
		assertEquals(3, result.delimiterIndex);
		assertEquals("\n", result.delimiter);

		result = TextUtilities.nextDelimiter("abc\ndef", 5);
		assertEquals(-1, result.delimiterIndex);
		assertEquals(null, result.delimiter);

		result = TextUtilities.nextDelimiter("abc\rdef\n123", 0);
		assertEquals(3, result.delimiterIndex);
		assertEquals("\r", result.delimiter);

		result = TextUtilities.nextDelimiter("abc+\r\ndef\n123", 0);
		assertEquals(4, result.delimiterIndex);
		assertEquals("\r\n", result.delimiter);

		result = TextUtilities.nextDelimiter("abc~>\r\r\ndef\n123", 0);
		assertEquals(5, result.delimiterIndex);
		assertEquals("\r", result.delimiter);

		result = TextUtilities.nextDelimiter("\nabc~>\r\r\ndef\n123", 0);
		assertEquals(0, result.delimiterIndex);
		assertEquals("\n", result.delimiter);

		result = TextUtilities.nextDelimiter("abc~>123\r\n", 0);
		assertEquals(8, result.delimiterIndex);
		assertEquals("\r\n", result.delimiter);

		result = TextUtilities.nextDelimiter("abc~>\r\r\ndef\n123", 9);
		assertEquals(11, result.delimiterIndex);
		assertEquals("\n", result.delimiter);

		result = TextUtilities.nextDelimiter("", 0);
		assertEquals(-1, result.delimiterIndex);
		assertEquals(null, result.delimiter);

		result = TextUtilities.nextDelimiter("abc123", 0);
		assertEquals(-1, result.delimiterIndex);
		assertEquals(null, result.delimiter);
	}
}
