/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

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
			public void documentAboutToBeChanged(DocumentEvent event) {}
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
			public void documentAboutToBeChanged(DocumentEvent event) {}
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
				Assert.fail("bad implementation");
			}
			return super.get();
		}
	}


	/**
	 * Constructor for UtilitiesTest.
	 * 
	 * @param name the name
	 */
	private static DocumentEvent createRandomEvent(IDocument document, int maxLength, char character) {

		int index0= (int) (Math.random() * (maxLength + 1));
		int index1= (int) (Math.random() * (maxLength + 1));

		int offset= Math.min(index0, index1);
		int length= Math.max(index0, index1) - offset;

		int stringLength=  (int) (Math.random() * 10);
		StringBuffer buffer= new StringBuffer(stringLength);
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
			Assert.fail("bad location exception");
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

			for (Iterator<DocumentEvent> iterator= events.iterator(); iterator.hasNext();) {
				DocumentEvent event= iterator.next();
				currentLength += event.getText().length() - event.getLength();
			}

			for (int i= 0; i < 500; i++) {
				char character= (char) (32 + i % 95);
				DocumentEvent event= createRandomEvent(reference, currentLength, character);
				currentLength += event.getText().length() - event.getLength();
				events.add(event);
			}

			for (Iterator<DocumentEvent> iterator= events.iterator(); iterator.hasNext();) {
				DocumentEvent event= iterator.next();

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
			Assert.fail("bad location exception");
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

			for (Iterator<DocumentEvent> iterator= events.iterator(); iterator.hasNext();) {
				DocumentEvent event= iterator.next();
				currentLength += event.getText().length() - event.getLength();
			}

			for (int i= 0; i < 500; i++) {
				char character= (char) (32 + i % 95);
				DocumentEvent event= createRandomEvent(reference, currentLength, character);
				currentLength += event.getText().length() - event.getLength();
				events.add(event);
			}

			for (Iterator<DocumentEvent> iterator= events.iterator(); iterator.hasNext();) {
				DocumentEvent event= iterator.next();

				reference.replace(event.getOffset(), event.getLength(), event.getText());
				if (Math.random() < 0.3) {
					check(reference, testee);
				}
			}

			check(reference, testee);

		} catch (BadLocationException e) {
			Assert.fail("bad location exception");
		}
	}

	private static void check(IDocument reference, IDocument testee) {
		Assert.assertEquals(reference.get(), testee.get());
	}
	
	@Test
	public void testIndexOf() {
		int[] result;
		result= TextUtilities.indexOf(new String[] {"a", "ab", "abc"}, "xxxxxxxxxx", 0);
		assertEquals(-1, result[0]);
		assertEquals(-1, result[1]);

		result= TextUtilities.indexOf(new String[] {"a", "ab", "abc"}, "foobarabcd", 0);
		assertEquals(4, result[0]);
		assertEquals(0, result[1]);
	}

}
