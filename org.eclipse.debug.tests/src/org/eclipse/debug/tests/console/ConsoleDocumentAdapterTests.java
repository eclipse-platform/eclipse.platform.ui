/*******************************************************************************
 * Copyright (c) 2019 Paul Pazderski and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Paul Pazderski - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.tests.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import org.eclipse.debug.tests.AbstractDebugTest;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentAdapter;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.custom.TextChangedEvent;
import org.eclipse.swt.custom.TextChangingEvent;
import org.eclipse.ui.internal.console.ConsoleDocumentAdapter;

/**
 * Tests {@link ConsoleDocumentAdapter}.
 */
@SuppressWarnings("restriction")
public class ConsoleDocumentAdapterTests extends AbstractDebugTest {

	public ConsoleDocumentAdapterTests() {
		super(ConsoleDocumentAdapterTests.class.getSimpleName());
	}

	public ConsoleDocumentAdapterTests(String name) {
		super(name);
	}

	/**
	 * Test {@link ConsoleDocumentAdapter#setText(String)}.
	 */
	public void testSetText() {
		final ExpectingTextChangeListener eventListener = new ExpectingTextChangeListener(true, null);
		final IDocumentAdapter docAdapter = new ConsoleDocumentAdapter(-1);
		docAdapter.setDocument(new Document());
		docAdapter.addTextChangeListener(eventListener);

		final String text = "123456";
		eventListener.addExpectation(new TextEventExpectation(TextChangeEventType.SET));
		docAdapter.setText(text);
		assertContent(docAdapter, text);
		assertNumberOfLines(docAdapter, 1);
		checkLineMapping(docAdapter, null);

		for (String s : new String[] { "", null }) {
			docAdapter.setText(s);
			assertContent(docAdapter, "");
			assertNumberOfLines(docAdapter, 1);
			checkLineMapping(docAdapter, null);
		}
	}

	/**
	 * Test
	 * {@link IDocumentAdapter#setDocument(org.eclipse.jface.text.IDocument)}
	 * and ensure old document is indeed disconnected and not notified anymore.
	 */
	public void testChangeDocument() {
		final IDocumentAdapter docAdapter = new ConsoleDocumentAdapter(-1);
		final IDocument doc1 = new Document();
		final IDocument doc2 = new Document();
		final String text1 = "foo";
		final String text2 = "bar";

		docAdapter.setDocument(doc1);
		docAdapter.setText(text1);
		assertContent(docAdapter, text1);

		final String doc1Text = doc1.get();
		docAdapter.setDocument(doc2);
		docAdapter.setText(text2);
		assertContent(docAdapter, text2);
		assertEquals("Document was changed after disconnect.", doc1Text, doc1.get());

		docAdapter.setDocument(null);
	}

	private static void assertContent(IDocumentAdapter docAdapter, String content) {
		assertEquals("Adapter returned wrong content.", content, docAdapter.getTextRange(0, docAdapter.getCharCount()));
	}

	private static void assertNumberOfLines(IDocumentAdapter docAdapter, int expectedLines) {
		assertEquals("Adapter has wrong line count.", expectedLines, docAdapter.getLineCount());
	}

	/**
	 * Goes through every line in adapted document and checks if value of
	 * {@link IDocumentAdapter#getOffsetAtLine(int)} for a line returns the same
	 * line again if put into {@link IDocumentAdapter#getLineAtOffset(int)}.
	 * <p>
	 * If random source is provided test is more intensive and to some extend
	 * tests {@link IDocumentAdapter#getLine(int)}.
	 * </p>
	 *
	 * @param docAdapter document adapter to test
	 * @param rand Optional. If provided will request line from random offset of
	 *            this line instead of first and check lines in random order.
	 */
	private static void checkLineMapping(IDocumentAdapter docAdapter, Random rand) {
		final int n = docAdapter.getLineCount();
		final List<Integer> lines = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			lines.add(i);
		}
		if (rand != null) {
			Collections.shuffle(lines, rand);
		}
		for (int lineIndex : lines) {
			int offset = docAdapter.getOffsetAtLine(lineIndex);
			if (rand != null) {
				int lineLength = docAdapter.getLine(lineIndex).length();
				if (lineLength > 0) {
					offset += rand.nextInt(lineLength);
				}
			}
			final int testLineIndex = docAdapter.getLineAtOffset(offset);
			assertEquals("Got wrong line with offset " + offset + ".", lineIndex, testLineIndex);
		}
	}

	private static class TextEventExpectation {
		// common attributes
		/** Expected text change event type. */
		public final TextChangeEventType type;

		// text changing only attributes
		/**
		 * Expected value of {@link TextChangingEvent#start} or
		 * <code>null</code> to ignore.
		 */
		public final Integer start;
		/**
		 * Expected value of {@link TextChangingEvent#newText} or
		 * <code>null</code> to ignore.
		 */
		public final String newText;
		/**
		 * Expected value of {@link TextChangingEvent#replaceCharCount} or
		 * <code>null</code> to ignore.
		 */
		public final Integer replaceCharCount;
		/**
		 * Expected value of {@link TextChangingEvent#newCharCount} or
		 * <code>null</code> to ignore.
		 */
		public final Integer newCharCount;
		/**
		 * Expected value of {@link TextChangingEvent#replaceLineCount} or
		 * <code>null</code> to ignore.
		 */
		public final Integer replaceLineCount;
		/**
		 * Expected value of {@link TextChangingEvent#newLineCount} or
		 * <code>null</code> to ignore.
		 */
		public final Integer newLineCount;

		public TextEventExpectation(TextChangeEventType type) {
			this(type, null, null, null, null, null, null);
		}

		public TextEventExpectation(TextChangeEventType type, Integer start, String newText, Integer replaceCharCount, Integer newCharCount, Integer replaceLineCount, Integer newLineCount) {
			super();
			this.type = type;
			this.start = start;
			this.newText = newText;
			this.replaceCharCount = replaceCharCount;
			this.newCharCount = newCharCount;
			this.replaceLineCount = replaceLineCount;
			this.newLineCount = newLineCount;
		}
	}

	/**
	 * A {@link TextChangeListener} which has some expectations about generated
	 * text change events and is not gladly disappointed.
	 */
	private static class ExpectingTextChangeListener implements TextChangeListener {

		final Queue<TextEventExpectation> expectations = new LinkedList<>();
		final boolean allowUnexpectedEvents;
		/**
		 * The {@link IDocumentAdapter} generating the events. If set some
		 * additional checks are performed.
		 */
		final IDocumentAdapter docAdapter;
		/**
		 * If all events are checked ({@link #allowUnexpectedEvents} = false)
		 * and {@link #docAdapter} is set do some additional validations.
		 */
		TextChangingEvent lastEvent;
		int linesBeforeReplace = -1;
		int lengthBeforeReplace = -1;

		public ExpectingTextChangeListener(boolean allowUnexpectedEvents, IDocumentAdapter docAdapter) {
			super();
			this.allowUnexpectedEvents = allowUnexpectedEvents;
			this.docAdapter = docAdapter;
		}

		public void addExpectation(TextEventExpectation expectation) {
			expectations.add(expectation);
		}

		@Override
		public void textChanging(TextChangingEvent event) {
			final TextEventExpectation expectation = checkCommon(TextChangeEventType.CHANGING);
			if (expectation == null) {
				return;
			}

			if (expectation.start != null) {
				assertEquals("event.start", (int) expectation.start, event.start);
			}
			if (expectation.newText != null) {
				assertEquals("event.newText", expectation.newText, event.newText);
			}
			if (expectation.replaceCharCount != null) {
				assertEquals("event.replaceCharCount", (int) expectation.replaceCharCount, event.replaceCharCount);
			}
			if (expectation.newCharCount != null) {
				assertEquals("event.newCharCount", (int) expectation.newCharCount, event.newCharCount);
			}
			if (expectation.replaceLineCount != null) {
				assertEquals("event.replaceLineCount", (int) expectation.replaceLineCount, event.replaceLineCount);
			}
			if (expectation.newLineCount != null) {
				assertEquals("event.newLineCount", (int) expectation.newLineCount, event.newLineCount);
			}

			if (!allowUnexpectedEvents && docAdapter != null) {
				lastEvent = event;
				linesBeforeReplace = docAdapter.getLineCount();
				lengthBeforeReplace = docAdapter.getCharCount();

				final int charactersBehindEventStart = docAdapter.getCharCount() - event.start;
				assertTrue("Tried to remove more characters than available.", event.replaceCharCount <= charactersBehindEventStart);

				final int linesBehindEvent = docAdapter.getLineCount() - docAdapter.getLineAtOffset(event.start);
				assertTrue("Tried to remove more lines than available.", event.replaceLineCount <= linesBehindEvent);
			}
		}

		@Override
		public void textChanged(TextChangedEvent event) {
			checkCommon(TextChangeEventType.CHANGED);

			if (docAdapter != null && lastEvent != null) {
				final int lastEventOffset = lastEvent.start;
				final int lastEventLineIndex = docAdapter.getLineAtOffset(lastEventOffset);
				assertEquals("Line of offset " + lastEventOffset + " has changed after text change.", lastEventLineIndex, docAdapter.getLineAtOffset(lastEventOffset));

				// check if predicted changes are correct
				final int predictedDocLength = lengthBeforeReplace - lastEvent.replaceCharCount + lastEvent.newCharCount;
				assertEquals("New widget length not as announce by changing event.", predictedDocLength, docAdapter.getCharCount());
				final int predictedDocLines = linesBeforeReplace - lastEvent.replaceLineCount + lastEvent.newLineCount;
				assertEquals("New widget line number not as announce by changing event.", predictedDocLines, docAdapter.getLineCount());
				assertEquals("Inserted text not found in document.", lastEvent.newText, docAdapter.getTextRange(lastEvent.start, lastEvent.newText.length()));
			}
		}

		@Override
		public void textSet(TextChangedEvent event) {
			checkCommon(TextChangeEventType.SET);
		}

		private TextEventExpectation checkCommon(TextChangeEventType eventType) {
			final TextEventExpectation expectation = expectations.poll();
			if (expectation == null && allowUnexpectedEvents) {
				return null;
			}
			assertNotNull("Unexpected event.", expectation);
			if (expectation == null) {
				// prevents wrong compiler warning 'expectation may be null'
				return null;
			}
			if (expectation.type != eventType && allowUnexpectedEvents) {
				return null;
			}
			assertEquals("Wrong event type.", expectation.type, eventType);
			return expectation;
		}
	}

	public enum TextChangeEventType {
		CHANGING, CHANGED, SET,
	}
}
