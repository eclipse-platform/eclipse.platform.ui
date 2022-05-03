/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests.contentassist;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.text.edits.InsertEdit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ContextInformationValidator;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.tests.util.DisplayHelper;

/**
 * Tests for Async completion proposal popup proposals filtering mechanics
 *
 * @author Alex Boyko
 *
 */
public class FilteringAsyncContentAssistTests {

	private Shell shell;
	private SourceViewer viewer;
	private ContentAssistant ca;

	@Before
	public void setup() {
		tearDown();

		shell = new Shell();
		shell.setSize(300, 300);
		shell.open();
		DisplayHelper.driveEventQueue(shell.getDisplay());

		viewer = new SourceViewer(shell, null, SWT.NONE);
		Document document = new Document();
		viewer.setDocument(document);
		ca = new ContentAssistant(true);
	}

	@After
	public void tearDown() {
		if (shell != null) {
			ca.uninstall();
			if (!shell.isDisposed()) {
				shell.dispose();
			}
			shell = null;
		}
	}

	/**
	 * Simple CA with 1 immediate CA processor. Empty text, invoke CA, verify 1
	 * proposal, apply it, verify the resultant text
	 *
	 * @throws Exception exception
	 */
	@Test
	public void testSimpleCa() throws Exception {

		ca.addContentAssistProcessor(new ImmediateContentAssistProcessor("xx"), IDocument.DEFAULT_CONTENT_TYPE);

		ca.install(viewer);

		viewer.setSelectedRange(0, 0);

		ca.showPossibleCompletions();

		DisplayHelper.sleep(shell.getDisplay(), 300);

		List<ICompletionProposal> computedProposals = getComputedProposals(ca);

		assertEquals(1, computedProposals.size());

		ICompletionProposal proposal = computedProposals.get(0);

		IDocument document = viewer.getDocument();

		proposal.apply(document);

		assertEquals("xx", document.get());
	}

	/**
	 * Simple CA with filtering with 2 immediate CA processors. Empty text
	 * initially. Invoke CA, verify 2 proposals, type 'x', verify only 1 proposal 1
	 *
	 * @throws Exception exception
	 */
	@Test
	public void testFilteredCa() throws Exception {
		IDocument document = viewer.getDocument();

		ca.addContentAssistProcessor(new ImmediateContentAssistProcessor("xx"), IDocument.DEFAULT_CONTENT_TYPE);
		ca.addContentAssistProcessor(new ImmediateContentAssistProcessor("yy"), IDocument.DEFAULT_CONTENT_TYPE);

		ca.install(viewer);

		viewer.setSelectedRange(1, 0);

		ca.showPossibleCompletions();

		DisplayHelper.sleep(shell.getDisplay(), 300);

		List<ICompletionProposal> computedProposals = getComputedProposals(ca);
		assertEquals(2, computedProposals.size());
		List<ICompletionProposal> filteredProposals = getFilteredProposals(ca);
		assertEquals(2, filteredProposals.size());

		new InsertEdit(0, "x").apply(document);
		viewer.setSelectedRange(1, 0);

		DisplayHelper.sleep(shell.getDisplay(), 300);

		computedProposals = getComputedProposals(ca);
		assertEquals(2, computedProposals.size());
		filteredProposals = getFilteredProposals(ca);
		assertEquals(1, filteredProposals.size());

		((ICompletionProposalExtension) filteredProposals.get(0)).apply(document, (char) 0,
				viewer.getSelectedRange().x);
		assertEquals("xx", document.get());
	}

	/**
	 * Simple CA with filtering with 1 immediate CA processors. Empty text
	 * initially. Invoke CA, verify 1 proposal, type 'a', verify no proposals
	 *
	 * @throws Exception exception
	 */
	@Test
	public void testFilteredCa_AllFilteredOut() throws Exception {
		IDocument document = viewer.getDocument();

		ca.addContentAssistProcessor(new ImmediateContentAssistProcessor("xx"), IDocument.DEFAULT_CONTENT_TYPE);

		ca.install(viewer);

		viewer.setSelectedRange(1, 0);

		ca.showPossibleCompletions();

		DisplayHelper.sleep(shell.getDisplay(), 300);

		List<ICompletionProposal> computedProposals = getComputedProposals(ca);
		assertEquals(1, computedProposals.size());
		List<ICompletionProposal> filteredProposals = getFilteredProposals(ca);
		assertEquals(1, filteredProposals.size());

		new InsertEdit(0, "a").apply(document);
		viewer.setSelectedRange(1, 0);

		DisplayHelper.sleep(shell.getDisplay(), 600);

		filteredProposals = getFilteredProposals(ca);
		assertTrue(filteredProposals == null || filteredProposals.isEmpty());
	}

	/**
	 * CA with 1 immediate and 1 delayed CA processors. Empty text initially. Invoke
	 * CA, verify 1 proposal shows right away, and then another added later after
	 * delay
	 *
	 * @throws Exception exception
	 */
	@Test
	public void testMultipleCaProcessors() throws Exception {
		IDocument document = viewer.getDocument();

		ca.addContentAssistProcessor(new ImmediateContentAssistProcessor("xx"), IDocument.DEFAULT_CONTENT_TYPE);
		ca.addContentAssistProcessor(new DelayedContentAssistProcessor(singletonList("yy"), 3000, false),
				IDocument.DEFAULT_CONTENT_TYPE);

		ca.install(viewer);

		viewer.setSelectedRange(0, 0);

		ca.showPossibleCompletions();

		DisplayHelper.sleep(shell.getDisplay(), 300);

		List<ICompletionProposal> computedProposals = getComputedProposals(ca,
				p -> p instanceof IncompleteCompletionProposal);
		assertEquals(1, computedProposals.size());
		List<ICompletionProposal> filteredProposals = getFilteredProposals(ca,
				p -> p instanceof IncompleteCompletionProposal);
		assertEquals(1, filteredProposals.size());

		DisplayHelper.sleep(shell.getDisplay(), 3000);

		computedProposals = getComputedProposals(ca, p -> p instanceof IncompleteCompletionProposal);
		assertEquals(2, computedProposals.size());
		filteredProposals = getFilteredProposals(ca, p -> p instanceof IncompleteCompletionProposal);
		assertEquals(2, filteredProposals.size());

		((ICompletionProposalExtension) filteredProposals.get(1)).apply(document, (char) 0,
				viewer.getSelectedRange().x);
		assertEquals("yy", document.get());
	}

	/**
	 * CA with 1 CA processor for which the first request takes long time and consequent request are
	 * instant. Invoke CA. and type 'a' such that completions are not ready yet, but while recompute
	 * was cancelling futures the futures from previous invocation completed and scheduled an async
	 * UI runnable to show completions. Recompute is immediate. Hence proposals shown right away.
	 * However the async UI runnable to show old proposals runs after and overwrites the correct
	 * immediate proposals. Test that this behaviour is fixed
	 *
	 * @throws Exception exception
	 */
	@Test
	public void testCA_WithFirstDelayedThenImmediateProposals() throws Exception {
		IDocument document = viewer.getDocument();

		ca.addContentAssistProcessor(new LongInitialContentAssistProcessor(singletonList("abc"), 500, true),
				IDocument.DEFAULT_CONTENT_TYPE);

		ca.install(viewer);

		viewer.setSelectedRange(0, 0);

		ca.showPossibleCompletions();

		DisplayHelper.sleep(shell.getDisplay(), 200);
		new InsertEdit(0, "a").apply(document);
		viewer.setSelectedRange(1, 0);

		DisplayHelper.sleep(shell.getDisplay(), 3000);

		List<ICompletionProposal> filteredProposals= getFilteredProposals(ca,
				p -> p instanceof IncompleteCompletionProposal);
		assertTrue(filteredProposals != null);
		assertEquals(1, filteredProposals.size());

		filteredProposals.get(0).apply(document);

		assertEquals("aabc", document.get());

	}

	/**
	 * CA with filtering with 1 immediate and 1 delayed CA processors. Empty text
	 * initially. Invoke CA, verify 1 proposal shows right away, type `a` before
	 * delayed proposal calculated, verify immediate proposal filtered out
	 *
	 * Bug: filtering only applied after all CA processors have completed
	 *
	 * @throws Exception exception
	 */
	@Test @Ignore
	public void testFastCompletionsNotFilteredUntilLongComplitionsCalculated() throws Exception {
		IDocument document = viewer.getDocument();

		ca.addContentAssistProcessor(new ImmediateContentAssistProcessor("xxxx"), IDocument.DEFAULT_CONTENT_TYPE);
		ca.addContentAssistProcessor(new DelayedContentAssistProcessor(singletonList("yyyy"), 5000, false),
				IDocument.DEFAULT_CONTENT_TYPE);

		ca.install(viewer);

		viewer.setSelectedRange(1, 0);

		ca.showPossibleCompletions();

		DisplayHelper.sleep(shell.getDisplay(), 300);

		List<ICompletionProposal> computedProposals = getComputedProposals(ca,
				p -> p instanceof IncompleteCompletionProposal);
		assertEquals(1, computedProposals.size());
		List<ICompletionProposal> filteredProposals = getFilteredProposals(ca,
				p -> p instanceof IncompleteCompletionProposal);
		assertEquals(1, filteredProposals.size());

		new InsertEdit(0, "a").apply(document);
		viewer.setSelectedRange(1, 0);

		DisplayHelper.sleep(shell.getDisplay(), 1000);

		filteredProposals = getFilteredProposals(ca, p -> p instanceof IncompleteCompletionProposal);
		assertTrue(filteredProposals == null || filteredProposals.isEmpty());
	}

	@Test
	public void testProposalValidation() throws Exception {
		IDocument document= viewer.getDocument();

		BlockingProcessor processor= new BlockingProcessor("abcd()");
		ca.addContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);

		ca.install(viewer);

		viewer.setSelectedRange(0, 0);

		ca.showPossibleCompletions();
		DisplayHelper.sleep(shell.getDisplay(), 50);

		new InsertEdit(0, "a").apply(document);
		viewer.setSelectedRange(1, 0);
		new InsertEdit(1, "b").apply(document);
		viewer.setSelectedRange(2, 0);

		processor.blocked.countDown();
		DisplayHelper.sleep(shell.getDisplay(), 100);

		new InsertEdit(2, "c").apply(document);
		viewer.setSelectedRange(3, 0);
		new InsertEdit(3, "d").apply(document);
		viewer.setSelectedRange(4, 0);

		DisplayHelper.sleep(shell.getDisplay(), 100);

		List<ICompletionProposal> filteredProposals= getFilteredProposals(ca,
				p -> p instanceof CompletionProposal);
		assertTrue(filteredProposals != null);
		assertEquals(1, filteredProposals.size());

		filteredProposals.get(0).apply(document);

		assertEquals("abcd()", document.get());

	}

	static class ImmediateContentAssistProcessor implements IContentAssistProcessor {

		final private List<String> templates;
		final private boolean incomplete;

		ImmediateContentAssistProcessor(String... templates) {
			this(Arrays.asList(templates), false);
		}

		ImmediateContentAssistProcessor(List<String> templates, boolean incomplete) {
			this.templates= templates;
			this.incomplete = incomplete;
		}

		@Override
		public ICompletionProposal[] computeCompletionProposals(ITextViewer textViewer, int offset) {
			List<ICompletionProposal> proposals= new ArrayList<>();
			try {
				IDocument document= textViewer.getDocument();
				for (String template : templates) {
					if (document != null && (document.getLength() == 0 || isSubstringFoundOrderedInString(document.get(0, offset), template))) {
						if (incomplete) {
							proposals.add(new IncompleteCompletionProposal(template, offset, 0, offset, template));
						} else {
							proposals.add(new CompletionProposal(template, offset, 0, offset, template));
						}
					}
				}
			} catch (BadLocationException e) {
				throw new IllegalStateException("Error computing proposals");
			}
			return proposals.toArray(new ICompletionProposal[0]);
		}

		@Override
		public IContextInformation[] computeContextInformation(ITextViewer textViewer, int offset) {
			return new IContextInformation[0];
		}

		@Override
		public char[] getCompletionProposalAutoActivationCharacters() {
			return new char[0];
		}

		@Override
		public char[] getContextInformationAutoActivationCharacters() {
			return new char[0];
		}

		@Override
		public String getErrorMessage() {
			return "No proposals!";
		}

		@Override
		public IContextInformationValidator getContextInformationValidator() {
			return new ContextInformationValidator(this);
		}

	}

	static class DelayedContentAssistProcessor extends ImmediateContentAssistProcessor {

		protected long delay;

		DelayedContentAssistProcessor(List<String> templates, long delay, boolean incomplete) {
			super(templates, incomplete);
			this.delay = delay;
		}

		@Override
		public ICompletionProposal[] computeCompletionProposals(ITextViewer textViewer, int offset) {
			if (delay > 0) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					throw new IllegalStateException("Cannot generate delayed content assist proposals!");
				}
			}
			return super.computeCompletionProposals(textViewer, offset);
		}
	}

	private class LongInitialContentAssistProcessor extends DelayedContentAssistProcessor {

		LongInitialContentAssistProcessor(List<String> templates, long delay, boolean incomplete) {
			super(templates, delay, incomplete);
		}

		@Override
		public ICompletionProposal[] computeCompletionProposals(ITextViewer textViewer, int offset) {
			ICompletionProposal[] completionProposals= super.computeCompletionProposals(viewer, offset);
			delay = 0;
			return completionProposals;
		}
	}

	private class BlockingProcessor extends ImmediateContentAssistProcessor {

		final CountDownLatch blocked= new CountDownLatch(1);

		BlockingProcessor(String... templates) {
			super(Arrays.asList(templates), false);
		}

		@Override
		public ICompletionProposal[] computeCompletionProposals(ITextViewer textViewer, int offset) {
			try {
				blocked.await();
			} catch (InterruptedException e) {
				throw new IllegalStateException("Cannot generate delayed content assist proposals!");
			}

			return super.computeCompletionProposals(textViewer, offset);
		}
	}

	@SuppressWarnings("unchecked")
	private static List<ICompletionProposal> getComputedProposals(ContentAssistant ca) throws Exception {
		Field f = ContentAssistant.class.getDeclaredField("fProposalPopup");
		f.setAccessible(true);
		Object caPopup = f.get(ca);
		assertEquals("org.eclipse.jface.text.contentassist.AsyncCompletionProposalPopup", caPopup.getClass().getName());
		Class<?> caPopupSuperClass = caPopup.getClass().getSuperclass();
		assertEquals("org.eclipse.jface.text.contentassist.CompletionProposalPopup", caPopupSuperClass.getName());
		Field computedProposals = caPopupSuperClass.getDeclaredField("fComputedProposals");
		computedProposals.setAccessible(true);
		return (List<ICompletionProposal>) computedProposals.get(caPopup);
	}

	@SuppressWarnings("unchecked")
	static List<ICompletionProposal> getFilteredProposals(ContentAssistant ca) throws Exception {
		Field f = ContentAssistant.class.getDeclaredField("fProposalPopup");
		f.setAccessible(true);
		Object caPopup = f.get(ca);
		assertEquals("org.eclipse.jface.text.contentassist.AsyncCompletionProposalPopup", caPopup.getClass().getName());
		Class<?> caPopupSuperClass = caPopup.getClass().getSuperclass();
		assertEquals("org.eclipse.jface.text.contentassist.CompletionProposalPopup", caPopupSuperClass.getName());
		Field computedProposals = caPopupSuperClass.getDeclaredField("fFilteredProposals");
		computedProposals.setAccessible(true);
		return (List<ICompletionProposal>) computedProposals.get(caPopup);
	}

	private static List<ICompletionProposal> getComputedProposals(ContentAssistant ca, Predicate<ICompletionProposal> p)
			throws Exception {
		List<ICompletionProposal> computedProposals = getComputedProposals(ca);
		return computedProposals == null ? null : computedProposals.stream().filter(p).collect(Collectors.toList());
	}

	private static List<ICompletionProposal> getFilteredProposals(ContentAssistant ca, Predicate<ICompletionProposal> p)
			throws Exception {
		List<ICompletionProposal> filteredProposals = getFilteredProposals(ca);
		return filteredProposals == null ? null : filteredProposals.stream().filter(p).collect(Collectors.toList());
	}

	private static class IncompleteCompletionProposal implements ICompletionProposal {

		/** The string to be displayed in the completion proposal popup. */
		private String fDisplayString;
		/** The replacement string. */
		protected String fReplacementString;
		/** The replacement offset. */
		protected int fReplacementOffset;
		/** The replacement length. */
		protected int fReplacementLength;
		/** The cursor position after this proposal has been applied. */
		private int fCursorPosition;

		public IncompleteCompletionProposal(String replacementString, int replacementOffset, int replacementLength, int cursorPosition, String displayString) {
			fReplacementString= replacementString;
			fReplacementOffset= replacementOffset;
			fReplacementLength= replacementLength;
			fCursorPosition= cursorPosition;
			fDisplayString= displayString;
		}

		@Override
		public void apply(IDocument document) {
			try {
				document.replace(fReplacementOffset, fReplacementLength, fReplacementString);
			} catch (BadLocationException x) {
				// ignore
			}
		}

		@Override
		public Point getSelection(IDocument document) {
			return new Point(fReplacementOffset + fCursorPosition, 0);
		}

		@Override
		public IContextInformation getContextInformation() {
			return null;
		}

		@Override
		public Image getImage() {
			return null;
		}

		@Override
		public String getDisplayString() {
			if (fDisplayString != null)
				return fDisplayString;
			return fReplacementString;
		}

		@Override
		public String getAdditionalProposalInfo() {
			return null;
		}
	}

	static class CompletionProposal extends IncompleteCompletionProposal
			implements ICompletionProposalExtension, ICompletionProposalExtension2, ICompletionProposalExtension3 {

		public CompletionProposal(String replacementString, int replacementOffset, int replacementLength,
				int cursorPosition, String displayString) {
			super(replacementString, replacementOffset, replacementLength, cursorPosition, displayString);
		}

		@Override
		public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
			apply(viewer.getDocument());
		}

		@Override
		public void selected(ITextViewer viewer, boolean smartToggle) {
			// nothing
		}

		@Override
		public void unselected(ITextViewer viewer) {
			// nothing
		}

		@Override
		public boolean validate(IDocument document, int offset, DocumentEvent event) {
			if (event != null) {
				fReplacementLength += event.fText.length() - event.fLength;
			}
			if (offset > fReplacementOffset) {
				try {
					return isSubstringFoundOrderedInString(document.get(fReplacementOffset, offset - fReplacementOffset), fReplacementString);
				} catch (BadLocationException e) {
					throw new IllegalStateException("Completion validation failed");
				}
			}
			return false;
		}

		@Override
		public void apply(IDocument document, char trigger, int offset) {
			apply(document);
		}

		@Override
		public boolean isValidFor(IDocument document, int offset) {
			return validate(document, offset, null);
		}

		@Override
		public char[] getTriggerCharacters() {
			return new char[0];
		}

		@Override
		public int getContextInformationPosition() {
			return 0;
		}

		@Override
		public int getPrefixCompletionStart(IDocument document, int completionOffset) {
			return 0;
		}

		@Override
		public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
			return getDisplayString();
		}

		@Override
		public IInformationControlCreator getInformationControlCreator() {
			return null;
		}

		@Override
		public String toString() {
			return getDisplayString();
		}
	}

	@SuppressWarnings("boxing")
	private static boolean isSubstringFoundOrderedInString(String subString, String string) {
		int lastIndex = 0;
		subString = subString.toLowerCase();
		string = string.toLowerCase();
		for (Character c : subString.toCharArray()) {
			int index = string.indexOf(c, lastIndex);
			if (index < 0) {
				return false;
			} else {
				lastIndex = index + 1;
			}
		}
		return true;
	}

}
