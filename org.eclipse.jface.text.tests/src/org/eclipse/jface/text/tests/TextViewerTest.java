/*******************************************************************************
 * Copyright (c) 2014, 2019 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 * 	   Mickael Istria (Red Hat Inc.) - [Bug 544708] Ctrl+Home
 * 	   Paul Pazderski - [Bug 545530] Test for TextViewer's default IDocumentAdapter implementation.
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.jface.util.Util;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BlockTextSelection;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentAdapter;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.jface.text.hyperlink.URLHyperlinkDetector;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.tests.util.DisplayHelper;

/**
 * Basic tests for TextViewer.
 */
public class TextViewerTest {

	@Rule public ScreenshotOnFailureRule screenshotRule = new ScreenshotOnFailureRule();
	private Shell fShell;

	@After
	public void tearDown() {
		if (fShell != null && !fShell.isDisposed()) {
			fShell.dispose();
		}
		fShell= null;
	}

	@Test
	public void testSetRedraw_Bug441827() throws Exception {
		fShell= new Shell();
		TextViewer textViewer= new TextViewer(fShell, SWT.NONE);
		Document document= new Document("abc");
		textViewer.setDocument(document);
		int len= document.getLength();
		// Select the whole document with the caret at the beginning.
		textViewer.setSelectedRange(len, -len);
		assertEquals(0, textViewer.getSelectedRange().x);
		assertEquals(len, textViewer.getSelectedRange().y);
		assertEquals(0, textViewer.getTextWidget().getCaretOffset());
		textViewer.setRedraw(false);
		textViewer.setRedraw(true);
		// Check that the selection and the caret position are preserved.
		assertEquals(0, textViewer.getSelectedRange().x);
		assertEquals(len, textViewer.getSelectedRange().y);
		assertEquals(0, textViewer.getTextWidget().getCaretOffset());
	}

	@Test
	public void testCaretMoveChangesSelection() throws Exception {
		fShell= new Shell();
		TextViewer textViewer= new TextViewer(fShell, SWT.NONE);
		Document document= new Document("abc");
		textViewer.setDocument(document);
		int len= document.getLength();
		// Select the whole document with the caret at the beginning.
		textViewer.setSelectedRange(0, len);
		ITextSelection selection= (ITextSelection) textViewer.getSelectionProvider().getSelection();
		assertEquals(0, selection.getOffset());
		assertEquals(len, selection.getLength());
		textViewer.getTextWidget().setCaretOffset(1);
		selection= (ITextSelection) textViewer.getSelectionProvider().getSelection();
		assertEquals(1, selection.getOffset());
		assertEquals(0, selection.getLength());
	}

	@Test
	public void testGetCachedSelection() throws Exception {
		fShell= new Shell();
		TextViewer textViewer= new TextViewer(fShell, SWT.NONE);
		Document document= new Document("abc");
		textViewer.setDocument(document);
		int len= document.getLength();
		// Select the whole document with the caret at the beginning.
		textViewer.setSelectedRange(0, len);
		checkInAndOutUIThread(() -> {
			ITextSelection selection= textViewer.getLastKnownSelection();
			assertEquals(0, selection.getOffset());
			assertEquals(len, selection.getLength());
		});
	}

	@Test
	public void testBlockSelectionAccessors() throws Exception {
		fShell= new Shell();
		ITextViewer textViewer= new TextViewer(fShell, SWT.NONE);
		Document document= new Document("0123\n4567\n89ab\ncdef");
		textViewer.setDocument(document);
		// Select the whole document with the caret at the beginning.
		StyledText textWidget= textViewer.getTextWidget();
		textWidget.setBlockSelection(true);
		fShell.setLayout(new FillLayout());
		fShell.open();
		textViewer.getSelectionProvider().setSelection(new BlockTextSelection(textViewer.getDocument(), 1, 1, 2, 2, textWidget.getTabs()));
		BlockTextSelection sel= (BlockTextSelection) textViewer.getSelectionProvider().getSelection();
		assertEquals(1, sel.getStartLine());
		assertEquals(2, sel.getEndLine());
		assertEquals(1, sel.getStartColumn());
		assertEquals(2, sel.getEndColumn());
	}


	private void checkInAndOutUIThread(Runnable r) throws InterruptedException {
		// first run in UI Thread, forward exceptions
		r.run();
		// then run in non-UI Thread
		Job job = Job.create("Check in non-UI Thread", monitor -> {
			try {
				r.run();
				return Status.OK_STATUS;
			} catch (Throwable t) {
				return new Status(IStatus.ERROR, "org.eclipse.jface.text.tests", t.getMessage(), t);
			}
		});
		job.schedule();
		job.join();
		if (!job.getResult().isOK()) {
			Throwable ex = job.getResult().getException();
			if (ex != null) {
				throw new AssertionError("Assertion fail in non-UI Thread", ex);
			} else {
				fail(job.getResult().toString());
			}
		}
	}

	@Test
	public void testCtrlHomeViewportListener() {
		Assume.assumeFalse("See bug 541415. For whatever reason, this shortcut doesn't work on Mac", Util.isMac());
		fShell= new Shell();
		fShell.setLayout(new FillLayout());
		fShell.setSize(500, 200);
		SourceViewer textViewer= new SourceViewer(fShell, null, SWT.NONE);
		textViewer.setDocument(new Document(generate5000Lines()));
		fShell.open();
		textViewer.revealRange(4000, 1);
		AtomicBoolean notifyHomeReached= new AtomicBoolean();
		ctrlEnd(textViewer);
		DisplayHelper.sleep(textViewer.getTextWidget().getDisplay(), 1000);
		textViewer.addViewportListener(offset -> notifyHomeReached.set(offset == 0));
		ctrlHome(textViewer);
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return notifyHomeReached.get();
			}
		}.waitForCondition(textViewer.getTextWidget().getDisplay(), 3000));
	}

	@Test
	public void testCtrlEndViewportListener() {
		Assume.assumeFalse("See bug 541415. For whatever reason, this shortcut doesn't work on Mac", Util.isMac());
		fShell= new Shell();
		fShell.setLayout(new FillLayout());
		fShell.setSize(500, 200);
		SourceViewer textViewer= new SourceViewer(fShell, null, SWT.NONE);
		Document document= new Document(generate5000Lines());
		textViewer.setDocument(document);
		fShell.open();
		AtomicBoolean notifyEndReached= new AtomicBoolean();
		textViewer.addViewportListener(offset -> notifyEndReached.set(offset > 4000));
		ctrlEnd(textViewer);
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return notifyEndReached.get();
			}
		}.waitForCondition(textViewer.getControl().getDisplay(), 3000));
	}

	/**
	 * Test if {@link TextViewer}s default {@link IDocumentAdapter} implementation adhere to
	 * {@link IDocumentAdapter}s JavaDoc.
	 */
	@Test
	public void testDefaultContentImplementation() {
		fShell= new Shell();
		final StyledTextContent content;
		try {
			final TextViewer textViewer= new TextViewer(fShell, SWT.NONE);
			textViewer.setDocument(new Document());
			content= textViewer.getTextWidget().getContent();
		} catch (Exception ex) {
			fail("Failed to obtain default instance of TextViewers document adapter. " + ex.getMessage());
			return;
		}
		assumeNotNull(content);

		final String line0= "Hello ";
		final String line1= "";
		final String line2= "World!";
		final String text= line0 + "\n" + line1 + "\r\n" + line2;
		content.setText(text);
		assertEquals("Get text range failed.", "H", content.getTextRange(0, 1));
		assertEquals("Get text range failed.", "ll", content.getTextRange(2, 2));
		assertEquals("Adapter content length wrong.", text.length(), content.getCharCount());
		assertEquals("Adapter returned wrong content.", line0, content.getLine(0));
		assertEquals("Adapter returned wrong content.", line1, content.getLine(1));
		assertEquals("Adapter returned wrong content.", line2, content.getLine(2));

		content.setText("\r\n\r\n");
		assertEquals("Wrong line for offset.", 0, content.getLineAtOffset(0));
		assertEquals("Wrong line for offset.", 0, content.getLineAtOffset(1));
		assertEquals("Wrong line for offset.", 1, content.getLineAtOffset(2));
		assertEquals("Wrong line for offset.", 1, content.getLineAtOffset(3));
		assertEquals("Wrong line for offset.", 2, content.getLineAtOffset(4));
		assertEquals("Wrong line for offset.", content.getLineCount() - 1, content.getLineAtOffset(content.getCharCount()));

		content.setText(null);
		assertEquals("Adapter returned wrong line count.", 1, content.getLineCount());
		content.setText("");
		assertEquals("Adapter returned wrong line count.", 1, content.getLineCount());
		content.setText("a\n");
		assertEquals("Adapter returned wrong line count.", 2, content.getLineCount());
		content.setText("\n\n");
		assertEquals("Adapter returned wrong line count.", 3, content.getLineCount());

		content.setText("\r\ntest\r\n");
		assertEquals("Wrong offset for line.", 0, content.getOffsetAtLine(0));
		assertEquals("Wrong offset for line.", 2, content.getOffsetAtLine(1));
		assertEquals("Wrong offset for line.", 8, content.getOffsetAtLine(2));
		content.setText("");
		assertEquals("Wrong offset for line.", 0, content.getOffsetAtLine(0));
	}

	public static void ctrlEnd(ITextViewer viewer) {
		postKeyEvent(viewer.getTextWidget(), SWT.END, SWT.CTRL, SWT.KeyDown);
	}

	public static void ctrlHome(ITextViewer viewer) {
		postKeyEvent(viewer.getTextWidget(), SWT.HOME, SWT.CTRL, SWT.KeyDown);
	}

	static void postKeyEvent(Control widget, int keyCode, int stateMask, int type) {
		Display display= widget.getDisplay();
		widget.setFocus();
		DisplayHelper.driveEventQueue(display);
		Event event = new Event();
		event.widget = widget;
		event.keyCode = keyCode;
		event.stateMask = stateMask;
		event.type = type;
		event.doit = true;
		// display.post(event) seem not always work, see bug 541415
		Listener[] listeners= widget.getListeners(type);
		for (Listener listener : listeners) {
			listener.handleEvent(event);
		}
		DisplayHelper.driveEventQueue(display);
	}

	public static String generate5000Lines() {
		StringBuilder b = new StringBuilder("start");
		for (int i = 0; i < 5000; i++) {
			b.append('\n');
		}
		b.append("end");
		return b.toString();
	}

	@Test
	public void testShiftLeft() {
		fShell= new Shell();
		TextViewer textViewer= new TextViewer(fShell, SWT.NONE);
		{
			// Normal case, both lines match prefix
			Document document= new Document("//line1\n//line2");
			textViewer.setDocumentPartitioning(IDocumentExtension3.DEFAULT_PARTITIONING);
			textViewer.setDocument(document);
			textViewer.setDefaultPrefixes(new String[] { "//" }, IDocument.DEFAULT_CONTENT_TYPE);

			textViewer.doOperation(ITextOperationTarget.SELECT_ALL);
			textViewer.doOperation(ITextOperationTarget.STRIP_PREFIX);

			assertEquals("line1\nline2", textViewer.getDocument().get());
		}
		{
			// Don't shift anything, as 2nd line does not match any prefix
			Document document= new Document("//line1\nline2");
			textViewer.setDocumentPartitioning(IDocumentExtension3.DEFAULT_PARTITIONING);
			textViewer.setDocument(document);
			textViewer.setDefaultPrefixes(new String[] { "//" }, IDocument.DEFAULT_CONTENT_TYPE);

			textViewer.doOperation(ITextOperationTarget.SELECT_ALL);
			textViewer.doOperation(ITextOperationTarget.STRIP_PREFIX);

			assertEquals("//line1\nline2", textViewer.getDocument().get());
		}
		{
			// Shift line1, since line2 matches the allowed empty prefix
			Document document= new Document("//line1\nline2");
			textViewer.setDocumentPartitioning(IDocumentExtension3.DEFAULT_PARTITIONING);
			textViewer.setDocument(document);
			textViewer.setDefaultPrefixes(new String[] { "//", "" }, IDocument.DEFAULT_CONTENT_TYPE);

			textViewer.doOperation(ITextOperationTarget.SELECT_ALL);
			textViewer.doOperation(ITextOperationTarget.STRIP_PREFIX);

			assertEquals("line1\nline2", textViewer.getDocument().get());
		}
	}

	private String toString(Document document, IHyperlink[] links) {
		if (links == null) {
			return "[]";
		}
		return Arrays.stream(links).map(l -> {
			IRegion region= l.getHyperlinkRegion();
			try {
				String fromDocument= document.get(region.getOffset(), region.getLength());
				if (l instanceof URLHyperlink) {
					assertEquals(((URLHyperlink) l).getURLString(), fromDocument);
				}
				return fromDocument;
			} catch (BadLocationException e) {
				return "Invalid region <" + region + '>';
			}
		}).collect(Collectors.joining(",", "[", "]"));
	}

	private void checkHyperlink(TextViewer textViewer, int pos, String text, String expected) {
		Document document= new Document(text);
		textViewer.setDocumentPartitioning(IDocumentExtension3.DEFAULT_PARTITIONING);
		textViewer.setDocument(document);
		IRegion region= new Region(pos, 0);
		URLHyperlinkDetector detector= new URLHyperlinkDetector();
		IHyperlink[] hyperlinks= detector.detectHyperlinks(textViewer, region, false);
		String found= toString(document, hyperlinks);
		assertEquals(expected, found);
	}

	@Test
	public void testURLHyperlinkDetector() {
		fShell= new Shell();
		TextViewer textViewer= new TextViewer(fShell, SWT.NONE);
		checkHyperlink(textViewer, 3, "https://foo ", "[https://foo]");
		checkHyperlink(textViewer, 0, "", "[]");
		checkHyperlink(textViewer, 3, "https", "[]");
		checkHyperlink(textViewer, 3, "https://", "[]");
		checkHyperlink(textViewer, 3, "https:// ", "[]");
		checkHyperlink(textViewer, 3, "https:// foo", "[]");
		checkHyperlink(textViewer, 3, "https://foo bar", "[https://foo]");
		checkHyperlink(textViewer, 3, "\"https://\" foo bar", "[]");
		checkHyperlink(textViewer, 3, "\"https:// \" foo bar", "[]");
		checkHyperlink(textViewer, 3, "\"https:// foo\" bar", "[]");
		checkHyperlink(textViewer, 15, "https:// foo https://bar bar", "[https://bar]");
		checkHyperlink(textViewer, 24, "https:// foo https://bar bar", "[https://bar]");
		checkHyperlink(textViewer, 15, "<a href=\"test:https://bugs.eclipse.org/bugs\"></a>", "[https://bugs.eclipse.org/bugs]");
		checkHyperlink(textViewer, 19, "<a href=\"scm:git:https://bugs.eclipse.org/bugs\"></a>", "[https://bugs.eclipse.org/bugs]");
		checkHyperlink(textViewer, 40, "Find more information at https://www.eclipse.org.", "[https://www.eclipse.org]");
		checkHyperlink(textViewer, 3, "http://... links should not be used anymore; use https://... instead.", "[]");
		checkHyperlink(textViewer, 50, "http://... links should not be used anymore; use https://... instead.", "[]");
	}

	@Test
	public void testPasteMultiLines() {
		fShell= new Shell();
		TextViewer textViewer= new TextViewer(fShell, SWT.NONE);
		Document document= new Document();
		textViewer.setDocument(document);
		new Clipboard(fShell.getDisplay()).setContents(new Object[] { "a" + System.lineSeparator() + "a" }, new Transfer[] { TextTransfer.getInstance() }, DND.CLIPBOARD);
		textViewer.doOperation(ITextOperationTarget.PASTE);
		assertEquals("a" + System.lineSeparator() + "a", textViewer.getTextWidget().getText());
		//
		document.set("a\na\na\nb");
		textViewer.setSelectedRange(0, 6);
		new Clipboard(fShell.getDisplay()).setContents(new Object[] { "b" }, new Transfer[] { TextTransfer.getInstance() }, DND.CLIPBOARD);
		textViewer.doOperation(ITextOperationTarget.PASTE);
		assertEquals("bb", textViewer.getTextWidget().getText());
	}

	@Test
	public void testSetSelectionNoDoc() {
		fShell= new Shell();
		TextViewer textViewer= new TextViewer(fShell, SWT.NONE);
		textViewer.setSelection(TextSelection.emptySelection());
		// assert no exception is thrown
	}

	@Test
	public void testSelectionFromViewerState() {
		fShell= new Shell();
		TextViewer textViewer= new TextViewer(fShell, SWT.NONE);
		textViewer.setDocument(new Document(
				"/**\n"
						+ " *\n"
						+ " * HEADER\n"
						+ " */\n"
						+ "package pack;\n"
						+ "\n"
						+ "public final class C {\n"
						+ "    /** \n"
						+ "* javadoc\n"
						+ "     */\n"
						+ "    public void method() {\n"
						+ "/* a\n"
						+ "comment */\n"
						+ "int local;\n"
						+ "    }\n"
						+ "}\n"));
		textViewer.setSelectedRange(118, 0);
		ITextSelection textSelection= (ITextSelection) textViewer.getSelection();
		assertEquals(118, textSelection.getOffset());
		textViewer.setRedraw(false); // switch to usage of ViewerState
		textViewer.setSelectedRange(113, 15);
		textSelection= (ITextSelection) textViewer.getSelection();
		assertEquals(113, textSelection.getOffset());
	}
}
