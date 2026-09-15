/*******************************************************************************
 * Copyright (c) 2014, 2025 Google, Inc and others.
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
 * 	   Latha Patil (ETAS GmbH) - Issue 865 - Test for Surround the selected text with brackets
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;

import org.eclipse.test.Screenshots;

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
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import org.eclipse.ui.tests.harness.util.DisplayHelper;

/**
 * Basic tests for TextViewer.
 */
public class TextViewerTest {

	private Shell fShell;

	@BeforeEach
	public void before() {
		fShell= new Shell();
	}

	@RegisterExtension
	public TestWatcher screenshotRule = new TestWatcher() {
		@Override
		public void testFailed(ExtensionContext context, Throwable cause) {
			Screenshots.takeScreenshot(TextViewerTest.class, context.getDisplayName());
		}
	};

	@Test
	public void testSetRedraw_Bug441827() throws Exception {
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
		assumeFalse(Util.isMac(), "See bug 541415. For whatever reason, this shortcut doesn't work on Mac");
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
		assumeFalse(Util.isMac(), "See bug 541415. For whatever reason, this shortcut doesn't work on Mac");
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

	@Test
	public void testSpecialCharacterSelection() {
		fShell= new Shell();
		final TextViewer textViewer= new TextViewer(fShell, SWT.NONE);
		StyledText textWidget= textViewer.getTextWidget();
		textViewer.setDocument(new Document());
		StyledTextContent content= textViewer.getTextWidget().getContent();
		assumeNotNull(content);
		content.setText("Hello");
		content.replaceTextRange(0, 5, "(");
		assertEquals("Expected text after replacement", "(Hello)", textWidget.getText());
	}

	/**
	 * Test if {@link TextViewer}s default {@link IDocumentAdapter} implementation adhere to
	 * {@link IDocumentAdapter}s JavaDoc.
	 */
	@Test
	public void testDefaultContentImplementation() {
		final StyledTextContent content;
		try {
			final TextViewer textViewer= new TextViewer(fShell, SWT.NONE);
			textViewer.setDocument(new Document());
			content= textViewer.getTextWidget().getContent();
		} catch (Exception ex) {
			fail("Failed to obtain default instance of TextViewers document adapter. " + ex.getMessage());
			return;
		}
		assertNotNull(content);

		final String line0= "Hello ";
		final String line1= "";
		final String line2= "World!";
		final String text= line0 + "\n" + line1 + "\r\n" + line2;
		content.setText(text);
		assertEquals("H", content.getTextRange(0, 1), "Get text range failed.");
		assertEquals("ll", content.getTextRange(2, 2), "Get text range failed.");
		assertEquals(text.length(), content.getCharCount(), "Adapter content length wrong.");
		assertEquals(line0, content.getLine(0), "Adapter returned wrong content.");
		assertEquals(line1, content.getLine(1), "Adapter returned wrong content.");
		assertEquals(line2, content.getLine(2), "Adapter returned wrong content.");

		content.setText("\r\n\r\n");
		assertEquals(0, content.getLineAtOffset(0), "Wrong line for offset.");
		assertEquals(0, content.getLineAtOffset(1), "Wrong line for offset.");
		assertEquals(1, content.getLineAtOffset(2), "Wrong line for offset.");
		assertEquals(1, content.getLineAtOffset(3), "Wrong line for offset.");
		assertEquals(2, content.getLineAtOffset(4), "Wrong line for offset.");
		assertEquals(content.getLineCount() - 1, content.getLineAtOffset(content.getCharCount()), "Wrong line for offset.");

		content.setText(null);
		assertEquals(1, content.getLineCount(), "Adapter returned wrong line count.");
		content.setText("");
		assertEquals(1, content.getLineCount(), "Adapter returned wrong line count.");
		content.setText("a\n");
		assertEquals(2, content.getLineCount(), "Adapter returned wrong line count.");
		content.setText("\n\n");
		assertEquals(3, content.getLineCount(), "Adapter returned wrong line count.");

		content.setText("\r\ntest\r\n");
		assertEquals(0, content.getOffsetAtLine(0), "Wrong offset for line.");
		assertEquals(2, content.getOffsetAtLine(1), "Wrong offset for line.");
		assertEquals(8, content.getOffsetAtLine(2), "Wrong offset for line.");
		content.setText("");
		assertEquals(0, content.getOffsetAtLine(0), "Wrong offset for line.");
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
		DisplayHelper.runEventLoop(display, 0);
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
		DisplayHelper.runEventLoop(display, 0);
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
		TextViewer textViewer= new TextViewer(fShell, SWT.NONE);
		textViewer.setSelection(TextSelection.emptySelection());
		// assert no exception is thrown
	}

	@Test
	public void testSelectionFromViewerState() {
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

	@Test
	public void testSurroundwithBracketsStrategy() {
		fShell= new Shell();
		final SourceViewer sourceViewer= new SourceViewer(fShell, null, SWT.NONE);
		sourceViewer.configure(new SourceViewerConfiguration());
		sourceViewer.setDocument(new Document("Test sample to surround the selected text with brackets"));
		StyledText text= sourceViewer.getTextWidget();
		text.setText("Test sample to surround the selected text with brackets");
		text.setSelection(15, 23);
		assertEquals(23, text.getCaretOffset());
		assertEquals("surround", text.getSelectionText());
		text.insert("[");
		assertEquals("Test sample to [surround] the selected text with brackets", text.getText());
		assertEquals(24, text.getCaretOffset());
	}
}