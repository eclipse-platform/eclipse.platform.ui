/*******************************************************************************
 * Copyright (c) 2019, 2026 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 * Christoph Läubrich - add additional test
 *******************************************************************************/
package org.eclipse.jface.text.tests.contentassist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.SourceViewer;

import org.eclipse.ui.tests.harness.util.DisplayHelper;

public class AsyncContentAssistTest {

	private ILogListener listener;

	private IStatus errorStatus;

	private Shell shell;

	@BeforeEach
	public void setUp() {
		shell= new Shell();
		listener= (status, _) -> {
			if (status.getSeverity() == IStatus.ERROR && "org.eclipse.jface.text".equals(status.getPlugin())) {
				errorStatus= status;
			}
		};
		Platform.addLogListener(listener);
	}

	@AfterEach
	public void tearDown() {
		shell.dispose();
		Platform.removeLogListener(listener);
	}

	@Test
	public void testAsyncFailureStackOverflow() {
		SourceViewer viewer= new SourceViewer(shell, null, SWT.NONE);
		Document document= new Document("a");
		viewer.setDocument(document);
		ContentAssistant contentAssistant= new ContentAssistant(true);
		contentAssistant.addContentAssistProcessor(new DelayedErrorContentAssistProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		contentAssistant.addContentAssistProcessor(new ImmediateContentAssistProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		contentAssistant.install(viewer);
		contentAssistant.showPossibleCompletions();
		document.set("ab"); // Simulate user typing a key when popup visible
		DisplayHelper.sleep(shell.getDisplay(), 2000);
		assertNotNull(errorStatus);
	}

	@Test
	public void testSyncFailureNPE() {
		SourceViewer viewer= new SourceViewer(shell, null, SWT.NONE);
		Document document= new Document("a");
		viewer.setDocument(document);
		ContentAssistant contentAssistant= new ContentAssistant(true);
		contentAssistant.addContentAssistProcessor(new ImmediateContentAssistProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		contentAssistant.addContentAssistProcessor(new ImmediateNullContentAssistProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		contentAssistant.install(viewer);
		contentAssistant.showPossibleCompletions();
		document.set("ab"); // Simulate user typing a key when popup visible
		DisplayHelper.sleep(shell.getDisplay(), 1000);
		assertNull(errorStatus);
	}

	@Test
	public void testCompletePrefix() {
		shell.setLayout(new FillLayout());
		shell.setSize(500, 300);
		SourceViewer viewer= new SourceViewer(shell, null, SWT.NONE);
		Document document= new Document("b");
		viewer.setDocument(document);
		viewer.setSelectedRange(1, 0);
		ContentAssistant contentAssistant= new ContentAssistant(true);
		contentAssistant.addContentAssistProcessor(new BarContentAssistProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		contentAssistant.enablePrefixCompletion(true);
		contentAssistant.install(viewer);
		shell.open();
		DisplayHelper.runEventLoop(shell.getDisplay(), 0);
		Display display= shell.getDisplay();
		final Collection<Shell> beforeShells= AbstractContentAssistTest.getCurrentShells();
		contentAssistant.showPossibleCompletions();
		Shell newShell= AbstractContentAssistTest.findNewShell(beforeShells);
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				Table completionTable= findCompletionSelectionControl(newShell);
				return Arrays.stream(completionTable.getItems()).map(TableItem::getText).anyMatch(item -> item.contains(BarContentAssistProcessor.PROPOSAL.substring(document.getLength())));
			}
		}.waitForCondition(display, 2000), "Completion item not shown");
	}

	@Test
	public void testCompleteActivationChar() {
		shell.setLayout(new FillLayout());
		shell.setSize(500, 300);
		SourceViewer viewer= new SourceViewer(shell, null, SWT.NONE);
		Document document= new Document("something");
		viewer.setDocument(document);
		viewer.setSelectedRange(1, 0);
		ContentAssistant contentAssistant= new ContentAssistant(true);
		BarContentAssistProcessor processor= new BarContentAssistProcessor();
		processor.setCompletionProposalAutoActivationChar('b');
		contentAssistant.addContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
		contentAssistant.enablePrefixCompletion(true);
		contentAssistant.enableAutoActivation(true);
		contentAssistant.setAutoActivationDelay(0);
		contentAssistant.install(viewer);
		shell.open();
		Display display= shell.getDisplay();
		DisplayHelper.runEventLoop(display, 0);
		Control control= viewer.getTextWidget();
		control.forceFocus();
		DisplayHelper.runEventLoop(display, 0);
		final Collection<Shell> beforeShells= AbstractContentAssistTest.getCurrentShells();
		// Use notifyListeners instead of Display.post() for reliable event delivery
		// Display.post() sends native OS events that may not be processed reliably
		// in headless CI environments
		Event keyEvent= new Event();
		keyEvent.widget= control;
		keyEvent.type= SWT.KeyDown;
		keyEvent.character= 'b';
		keyEvent.keyCode= 'b';
		control.notifyListeners(SWT.KeyDown, keyEvent);
		AbstractContentAssistTest.processEvents();
		Shell newShell= AbstractContentAssistTest.findNewShell(beforeShells);
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				Table completionTable= findCompletionSelectionControl(newShell);
				return Arrays.stream(completionTable.getItems()).map(TableItem::getText).anyMatch(item -> item.contains(BarContentAssistProcessor.PROPOSAL.substring(document.getLength())));
			}
		}.waitForCondition(display, 4000), "Completion item not shown");
	}

	@Test
	public void testAutoInsertSingleProposal() {
		Document document= new Document("b");
		ContentAssistant contentAssistant= createAutoInsertingContentAssistant(new BarContentAssistProcessor());
		installAndOpen(contentAssistant, document);
		final Collection<Shell> beforeShells= AbstractContentAssistTest.getCurrentShells();
		contentAssistant.showPossibleCompletions();
		assertInsertedWithoutPopup(document, beforeShells);
	}

	@Test
	public void testAutoInsertSingleProposalWithPrefixCompletion() {
		Document document= new Document("b");
		ContentAssistant contentAssistant= createAutoInsertingContentAssistant(new BarContentAssistProcessor());
		contentAssistant.enablePrefixCompletion(true);
		installAndOpen(contentAssistant, document);
		final Collection<Shell> beforeShells= AbstractContentAssistTest.getCurrentShells();
		contentAssistant.showPossibleCompletions();
		assertInsertedWithoutPopup(document, beforeShells);
	}

	@Test
	public void testNoAutoInsertOfMultipleProposals() {
		Document document= new Document("b");
		ContentAssistant contentAssistant= createAutoInsertingContentAssistant(new BarContentAssistProcessor());
		contentAssistant.addContentAssistProcessor(new BarContentAssistProcessor("bazaar"), IDocument.DEFAULT_CONTENT_TYPE);
		installAndOpen(contentAssistant, document);
		final Collection<Shell> beforeShells= AbstractContentAssistTest.getCurrentShells();
		contentAssistant.showPossibleCompletions();
		assertProposalShown(beforeShells, "azaar");
		assertEquals("b", document.get());
	}

	@Test
	public void testNoAutoInsertOnAutoActivation() {
		BarContentAssistProcessor processor= new BarContentAssistProcessor();
		processor.setCompletionProposalAutoActivationChar('b');
		ContentAssistant contentAssistant= createAutoInsertingContentAssistant(processor);
		contentAssistant.enableAutoActivation(true);
		contentAssistant.setAutoActivationDelay(0);
		typeAndAssertNotInserted(contentAssistant, 'b');
	}

	@Test
	public void testNoAutoInsertOnCompletionOnType() {
		ContentAssistant contentAssistant= createAutoInsertingContentAssistant(new BarContentAssistProcessor());
		contentAssistant.enableAutoActivation(true);
		contentAssistant.setAutoActivationDelay(0);
		contentAssistant.enableAutoActivateCompletionOnType(true);
		typeAndAssertNotInserted(contentAssistant, 'b');
	}

	/**
	 * Regression test for https://github.com/eclipse-platform/eclipse.platform.ui/issues/3582: the
	 * popup closer must not accumulate display filters when the proposal popup is recreated while
	 * typing.
	 * <p>
	 * The popup closer's mouse move filter asks the additional info control whether it contains the
	 * event's widget. Each mouse move event must therefore lead to exactly one such call; more
	 * calls mean that the filter is registered several times on the display.
	 * </p>
	 */
	@Test
	public void testPopupCloserFiltersDoNotAccumulate() throws Exception {
		Document document= new Document("b");
		ContentAssistant contentAssistant= new ContentAssistant(true);
		contentAssistant.addContentAssistProcessor(new ImmediateContentAssistProcessor() {
			@Override
			public ICompletionProposal[] computeCompletionProposals(ITextViewer textViewer, int offset) {
				return new ICompletionProposal[] { new CompletionProposal("bar", offset, 0, 3, null, "bar", null, "additional info") };
			}
		}, IDocument.DEFAULT_CONTENT_TYPE);
		DefaultInformationControl[] spiedInformationControl= new DefaultInformationControl[1];
		contentAssistant.setInformationControlCreator(parent -> {
			// Construct the spy through the real constructor, so that the listeners the control
			// registers on itself during construction are registered on the spy as well
			spiedInformationControl[0]= mock(DefaultInformationControl.class,
					withSettings().useConstructor(parent).defaultAnswer(CALLS_REAL_METHODS));
			return spiedInformationControl[0];
		});
		SourceViewer viewer= installAndOpen(contentAssistant, document);
		Display display= shell.getDisplay();
		final Collection<Shell> beforeShells= AbstractContentAssistTest.getCurrentShells();
		contentAssistant.showPossibleCompletions();
		assertProposalShown(beforeShells, "bar");
		assertPopupCloserHandlesMouseMoveOnce(viewer.getTextWidget(), spiedInformationControl);

		// Simulate typing while the popup is open; each change recomputes the proposals and recreates the popup
		for (char c : "abc".toCharArray()) {
			Shell proposalShell= findProposalShell(display);
			assertNotNull(proposalShell, "Proposal popup not found before typing '" + c + "'");
			document.replace(document.getLength(), 0, String.valueOf(c));
			viewer.setSelectedRange(document.getLength(), 0);
			// Make sure the bug scenario is exercised: typing must recreate the popup, which reinstalls the popup closer
			assertTrue(DisplayHelper.waitForCondition(display, 4000, () -> proposalShell.isDisposed() && findProposalShell(display) != null),
					"Proposal popup was not recreated after typing '" + c + "'");
		}
		assertPopupCloserHandlesMouseMoveOnce(viewer.getTextWidget(), spiedInformationControl);
	}

	private Shell findProposalShell(Display display) {
		return Arrays.stream(display.getShells())
				.filter(s -> s != shell && s.isVisible() && findCompletionSelectionControl(s) != null)
				.findFirst()
				.orElse(null);
	}

	private static void assertPopupCloserHandlesMouseMoveOnce(Control control, DefaultInformationControl[] spiedInformationControl) {
		assertTrue(DisplayHelper.waitForCondition(control.getDisplay(), 4000, () -> spiedInformationControl[0] != null && spiedInformationControl[0].isVisible()), "Additional info not shown");
		clearInvocations(spiedInformationControl[0]);
		Event mouseMove= new Event();
		mouseMove.widget= control;
		mouseMove.type= SWT.MouseMove;
		control.notifyListeners(SWT.MouseMove, mouseMove);
		verify(spiedInformationControl[0], times(1)).containsControl(control);
	}

	private static ContentAssistant createAutoInsertingContentAssistant(BarContentAssistProcessor processor) {
		ContentAssistant contentAssistant= new ContentAssistant(true);
		contentAssistant.addContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
		contentAssistant.enableAutoInsert(true);
		return contentAssistant;
	}

	private SourceViewer installAndOpen(ContentAssistant contentAssistant, Document document) {
		shell.setLayout(new FillLayout());
		shell.setSize(500, 300);
		SourceViewer viewer= new SourceViewer(shell, null, SWT.NONE);
		viewer.setDocument(document);
		viewer.setSelectedRange(document.getLength(), 0);
		contentAssistant.install(viewer);
		shell.open();
		Display display= shell.getDisplay();
		DisplayHelper.runEventLoop(display, 0);
		viewer.getTextWidget().forceFocus();
		DisplayHelper.runEventLoop(display, 0);
		return viewer;
	}

	private void assertInsertedWithoutPopup(Document document, Collection<Shell> beforeShells) {
		Display display= shell.getDisplay();
		DisplayHelper.waitForCondition(display, 2000, () -> BarContentAssistProcessor.PROPOSAL.equals(document.get()));
		DisplayHelper.runEventLoop(display, 100);
		assertEquals(BarContentAssistProcessor.PROPOSAL, document.get(), "Single proposal not inserted");
		assertEquals(List.of(), AbstractContentAssistTest.findNewShells(beforeShells), "Proposal popup shown");
	}

	private void typeAndAssertNotInserted(ContentAssistant contentAssistant, char c) {
		Document document= new Document("");
		Control control= installAndOpen(contentAssistant, document).getTextWidget();
		final Collection<Shell> beforeShells= AbstractContentAssistTest.getCurrentShells();
		Event keyEvent= new Event();
		keyEvent.widget= control;
		keyEvent.type= SWT.KeyDown;
		keyEvent.character= c;
		keyEvent.keyCode= c;
		control.notifyListeners(SWT.KeyDown, keyEvent);
		AbstractContentAssistTest.processEvents();
		assertProposalShown(beforeShells, BarContentAssistProcessor.PROPOSAL.substring(1));
		assertEquals(String.valueOf(c), document.get());
	}

	private void assertProposalShown(Collection<Shell> beforeShells, String proposal) {
		Shell newShell= AbstractContentAssistTest.findNewShell(beforeShells);
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				Table completionTable= findCompletionSelectionControl(newShell);
				return Arrays.stream(completionTable.getItems()).map(TableItem::getText).anyMatch(item -> item.contains(proposal));
			}
		}.waitForCondition(shell.getDisplay(), 4000), "Completion item not shown");
	}

	private static Table findCompletionSelectionControl(Widget control) {
		if (control instanceof Table) {
			return (Table) control;
		} else if (control instanceof Composite) {
			for (Widget child : ((Composite) control).getChildren()) {
				Table res= findCompletionSelectionControl(child);
				if (res != null) {
					return res;
				}
			}
		}
		return null;
	}
}
