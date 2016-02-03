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
package org.eclipse.text.tests;


import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.projection.ChildDocument;
import org.eclipse.jface.text.projection.ChildDocumentManager;

public class DocumentExtensionTest {


	static class Listener implements IDocumentListener {

		int fRepetitions= 1;
		private int fInvocations= 0;
		private boolean fIsPostNotificationSupported= true;

		@Override
		public void documentAboutToBeChanged(DocumentEvent e) {
			++ fInvocations;
		}

		@Override
		public void documentChanged(DocumentEvent e) {

			if (fInvocations > fRepetitions) {
				fInvocations= 0;
				return;
			}

			if (e.getDocument() instanceof IDocumentExtension) {
				IDocumentExtension extension= (IDocumentExtension) e.getDocument();
				Replace replace= getReplace(e);
				if (replace != null) {
					try {
						extension.registerPostNotificationReplace(this, replace);
					} catch (UnsupportedOperationException ex) {
						fIsPostNotificationSupported= false;
					}
				}
			}
		}

		/**
		 * Returns the replace.
		 *
		 * @param e the document event
		 * @return the replace
		 */
		protected Replace getReplace(DocumentEvent e) {
			return null;
		}

		public boolean isPostNotficationSupported() {
			return fIsPostNotificationSupported;
		}

	}

	static class Replace implements IDocumentExtension.IReplace {

		int fOffset;
		int fLength;
		String fText;

		public Replace() {
		}

		@Override
		public void perform(IDocument document, IDocumentListener owner) {
			try {
				document.replace(fOffset, fLength, fText);
			} catch (BadLocationException x) {
				assertTrue(false);
			}
		}
	}

	static class TestDocumentEvent extends DocumentEvent {

		public TestDocumentEvent(IDocument document, int offset, int length, String text) {
			super(document, offset, length, text);
		}

		public boolean isSameAs(DocumentEvent e) {
			return (e.getDocument() == getDocument() &&
							e.getOffset() == getOffset() &&
							e.getLength() == getLength() &&
							((e.getText() == null && getText() == null) || e.getText().equals(getText())));
		}
	}

	static class TestDocumentListener implements IDocumentListener {

		private IDocument fDocument1;
		private List<TestDocumentEvent> fTrace1;
		private TestDocumentEvent fExpected1;

		private List<TestDocumentEvent> fTrace2;
		private TestDocumentEvent fExpected2;

		private boolean fPopped= false;

		public TestDocumentListener(IDocument d1, List<TestDocumentEvent> t1, List<TestDocumentEvent> t2) {
			fDocument1= d1;
			fTrace1= t1;
			fTrace2= t2;
		}

		@Override
		public void documentAboutToBeChanged(DocumentEvent received) {
			if (!fPopped) {
				fPopped= true;
				fExpected1= fTrace1.remove(0);
				fExpected2= fTrace2.remove(0);
			}

			TestDocumentEvent e= (received.getDocument() == fDocument1 ? fExpected1 : fExpected2);
			assertTrue(e.isSameAs(received));
		}

		@Override
		public void documentChanged(DocumentEvent received) {
			TestDocumentEvent e= (received.getDocument() == fDocument1 ? fExpected1 : fExpected2);
			assertTrue(e.isSameAs(received));
			fPopped= false;
		}

	}

	@Test
	public void testAppend() {
		Listener listener= new Listener() {
			@Override
			protected Replace getReplace(DocumentEvent e) {
				String t= e.getText();
				if (t != null && t.length() > 0) {
					Replace r= new Replace();
					r.fOffset= (e.getOffset() + t.length());
					r.fLength= 0;
					r.fText= "x";
					return r;
				}
				return null;
			}
		};

		IDocument document= new Document();
		document.addDocumentListener(listener);

		try {
			document.replace(0, 0, "c");
			document.replace(0, 0, "b");
			document.replace(0, 0, "a");
		} catch (BadLocationException x) {
			assertTrue(false);
		}

		assertTrue("axbxcx".equals(document.get()));
	}
	
	@Test
	public void testRemove() {
		Listener listener= new Listener() {
			@Override
			protected Replace getReplace(DocumentEvent e) {
				String t= e.getText();
				if (t == null || t.length() == 0) {
					Replace r= new Replace();
					r.fOffset= e.getOffset();
					r.fLength= 0;
					r.fText= "y";
					return r;
				}
				return null;
			}
		};

		IDocument document= new Document("abc");
		document.addDocumentListener(listener);

		try {
			document.replace(2, 1, "");
			document.replace(1, 1, "");
			document.replace(0, 1, "");
		} catch (BadLocationException x) {
			assertTrue(false);
		}

		assertTrue("yyy".equals(document.get()));
	}
	
	@Test
	public void testRepeatedAppend() {
		Listener listener= new Listener() {
			@Override
			protected Replace getReplace(DocumentEvent e) {
				String t= e.getText();
				if (t != null && t.length() > 0) {
					Replace r= new Replace();
					r.fOffset= (e.getOffset() + t.length());
					r.fLength= 0;
					r.fText= "x";
					return r;
				}
				return null;
			}
		};
		listener.fRepetitions= 5;

		IDocument document= new Document();
		document.addDocumentListener(listener);

		try {
			document.replace(0, 0, "c");
			document.replace(0, 0, "b");
			document.replace(0, 0, "a");
		} catch (BadLocationException x) {
			assertTrue(false);
		}

		assertTrue("axxxxxbxxxxxcxxxxx".equals(document.get()));
	}

	private List<TestDocumentEvent> createTrace(IDocument document, int repetitions) {
		int i;
		List<TestDocumentEvent> trace= new ArrayList<>();

		trace.add(new TestDocumentEvent(document, 0, 0, "c"));
		for (i= 0; i < repetitions; i++)
			trace.add(new TestDocumentEvent(document, 1 + i, 0, "x"));

		trace.add(new TestDocumentEvent(document, 0, 0, "b"));
		for (i= 0; i < repetitions; i++)
			trace.add(new TestDocumentEvent(document, 1 + i, 0, "x"));

		trace.add(new TestDocumentEvent(document, 0, 0, "a"));
		for (i= 0; i < repetitions; i++)
			trace.add(new TestDocumentEvent(document, 1 + i, 0, "x"));

		return trace;
	}

	private void internalTestChildDocument(boolean modifyParent, boolean postModifyParent,  int repetitions) {

		IDocument childDocument= null;
		IDocument parentDocument= new Document();
		ChildDocumentManager manager= new ChildDocumentManager();
		try {
			childDocument= manager.createSlaveDocument(parentDocument);
			if (childDocument instanceof ChildDocument) {
				ChildDocument child= (ChildDocument) childDocument;
				child.setParentDocumentRange(0, parentDocument.getLength());
			}
		} catch (BadLocationException x) {
			assertTrue(false);
		}

		TestDocumentListener l= new TestDocumentListener(parentDocument, createTrace(parentDocument, repetitions), createTrace(childDocument, repetitions));
		parentDocument.addDocumentListener(l);
		childDocument.addDocumentListener(l);

		Listener modifier= new Listener() {
			@Override
			protected Replace getReplace(DocumentEvent e) {
				String t= e.getText();
				if (t != null && t.length() > 0) {
					Replace r= new Replace();
					r.fOffset= (e.getOffset() + t.length());
					r.fLength= 0;
					r.fText= "x";
					return r;
				}
				return null;
			}
		};
		modifier.fRepetitions= repetitions;

		IDocument document= postModifyParent ? parentDocument : childDocument;
		document.addDocumentListener(modifier);

		document= modifyParent ? parentDocument : childDocument;

		try {
			document.replace(0, 0, "c");
			if (!modifier.isPostNotficationSupported())
				throw new UnsupportedOperationException();
			document.replace(0, 0, "b");
			if (!modifier.isPostNotficationSupported())
				throw new UnsupportedOperationException();
			document.replace(0, 0, "a");
			if (!modifier.isPostNotficationSupported())
				throw new UnsupportedOperationException();
		} catch (BadLocationException x) {
			assertTrue(false);
		}
	}
	
	@Test
	public void testChildDocumentPP() {
		internalTestChildDocument(true, true, 1);
	}
	
	@Test
	public void testChildDocumentCC() {
		try {
			internalTestChildDocument(false, false, 1);
		} catch (UnsupportedOperationException x) {
		}

	}
	
	@Test
	public void testChildDocumentRepeatedPP() {
		internalTestChildDocument(true, true, 5);
	}
	
	@Test
	public void testChildDocumentRepeatedCC() {
		try {
			internalTestChildDocument(false, false, 5);
		} catch (UnsupportedOperationException e) {
		}
	}

	/**
	 * Tests that this is not supported.
	 */
	@Test
	public void testChildDocumentPC() {
		try {
			internalTestChildDocument(true, false, 1);
			assertTrue(false);
		} catch (UnsupportedOperationException x) {
		}
	}
	
	@Test
	public void testChildDocumentCP() {
		internalTestChildDocument(false, true, 1);
	}

	/**
	 * Tests that this is not supported.
	 */	
	@Test
	public void testChildDocumentRepeatedPC() {
		try {
			internalTestChildDocument(true, false, 5);
			assertTrue(false);
		} catch (UnsupportedOperationException x) {
		}
	}
	
	@Test
	public void testChildDocumentRepeatedCP() {
		internalTestChildDocument(false, true, 5);
	}
}
