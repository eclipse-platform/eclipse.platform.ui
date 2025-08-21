/*******************************************************************************
 * Copyright (c) 2016, 2025 Red Hat Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mickael Istria, Sopot Cela (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests;

import static org.eclipse.ui.genericeditor.tests.contributions.BarContentAssistProcessor.BAR_CONTENT_ASSIST_PROPOSAL;
import static org.eclipse.ui.genericeditor.tests.contributions.LongRunningBarContentAssistProcessor.LONG_RUNNING_BAR_CONTENT_ASSIST_PROPOSAL;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.genericeditor.tests.contributions.EnabledPropertyTester;
import org.eclipse.ui.genericeditor.tests.contributions.LongRunningBarContentAssistProcessor;
import org.eclipse.ui.tests.harness.util.DisplayHelper;

import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * @since 1.0
 */
public class CompletionTest extends AbstratGenericEditorTest {

	private Shell completionShell;

	@Test
	@DisabledOnOs(value = OS.MAC, disabledReason = "test fails on Mac, see https://github.com/eclipse-platform/eclipse.platform.ui/issues/906")
	public void testCompletion() throws Exception {
		editor.selectAndReveal(3, 0);
		this.completionShell = openContentAssist(true);
		final Table completionProposalList = findCompletionSelectionControl(completionShell);
		checkCompletionContent(completionProposalList);
		// TODO find a way to actually trigger completion and verify result against
		// Editor content
		// Assert.assertEquals("Completion didn't complete", "bars are good for a
		// beer.", ((StyledText)editor.getAdapter(Control.class)).getText());
	}

	@Test
	public void testDefaultContentAssistBug570488() throws Exception {
		ILog log= ILog.of(Platform.getBundle("org.eclipse.jface.text"));
		TestLogListener listener= new TestLogListener();
		log.addLogListener(listener);
		createAndOpenFile("Bug570488.txt", "bar 'bar'");
		openContentAssist(false);
		DisplayHelper.runEventLoop(Display.getCurrent(), 0);
		assertFalse(listener.messages.stream().anyMatch(s -> s.matches(IStatus.ERROR)), "There are errors in the log");
		log.removeLogListener(listener);
	}

	@Test
	@DisabledOnOs(value = OS.MAC, disabledReason = "test fails on Mac, see https://github.com/eclipse-platform/eclipse.platform.ui/issues/906")
	public void testCompletionService() throws Exception {
		Bundle bundle= FrameworkUtil.getBundle(CompletionTest.class);
		assertNotNull(bundle);
		BundleContext bundleContext= bundle.getBundleContext();
		assertNotNull(bundleContext);
		MockContentAssistProcessor service= new MockContentAssistProcessor();
		ServiceRegistration<IContentAssistProcessor> registration= bundleContext.registerService(IContentAssistProcessor.class, service,
				new Hashtable<>(Collections.singletonMap("contentType", "org.eclipse.ui.genericeditor.tests.content-type")));
		DisplayHelper.runEventLoop(Display.getCurrent(), 0);
		editor.selectAndReveal(3, 0);
		this.completionShell= openContentAssist(true);
		final Table completionProposalList= findCompletionSelectionControl(completionShell);
		assertTrue(service.called, "Service was not called!");
		checkCompletionContent(completionProposalList);
		registration.unregister();
	}

	@Test
	@DisabledOnOs(value = OS.MAC, disabledReason = "test fails on Mac, see https://github.com/eclipse-platform/eclipse.platform.ui/issues/906")
	public void testCompletionUsingViewerSelection() throws Exception {
		editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("abc");
		editor.selectAndReveal(0, 3);
		this.completionShell= openContentAssist(true);
		final Table completionProposalList = findCompletionSelectionControl(completionShell);
		assertTrue(DisplayHelper.waitForCondition(completionProposalList.getDisplay(), 5000, () -> {
			assertFalse(completionProposalList.isDisposed(), "Completion proposal list was unexpectedly disposed");
			return Arrays.stream(completionProposalList.getItems()).map(TableItem::getText).anyMatch("ABC"::equals);
		}), "Proposal list did not contain expected item: ABC");
	}
	
	@Test
	public void testEnabledWhenCompletion() throws Exception {
		// Confirm that when disabled, a completion shell is present
		EnabledPropertyTester.setEnabled(false);
		createAndOpenFile("enabledWhen.txt", "bar 'bar'");
		editor.selectAndReveal(3, 0);
		assertNull(openContentAssist(false), "A new shell was found");
		cleanFileAndEditor();

		// Confirm that when enabled, a completion shell is present
		EnabledPropertyTester.setEnabled(true);
		createAndOpenFile("enabledWhen.txt", "bar 'bar'");
		editor.selectAndReveal(3, 0);		
		assertNotNull(openContentAssist(true));
	}

	private Shell openContentAssist(boolean expectShell) {
		ContentAssistAction action = (ContentAssistAction) editor.getAction(ITextEditorActionConstants.CONTENT_ASSIST);
		action.update();
		final Set<Shell> beforeShells = Arrays.stream(editor.getSite().getShell().getDisplay().getShells()).filter(Shell::isVisible).collect(Collectors.toSet());
		action.run(); //opens shell
		Shell shell= findNewShell(beforeShells, editor.getSite().getShell().getDisplay(),expectShell);
		DisplayHelper.runEventLoop(PlatformUI.getWorkbench().getDisplay(), 100); // can dispose shell when focus lost during debugging
		return shell;
	}

	/**
	 * Checks that completion behaves as expected:
	 * 1. Computing is shown instantaneously
	 * 2. 1st proposal shown instantaneously
	 * 3. 2s later, 2nd proposal is shown
	 * @param completionProposalList the completion list
	 */
	private void checkCompletionContent(final Table completionProposalList) {
		// should be instantaneous, but happens to go asynchronous on CI so let's allow a wait
		assertTrue(DisplayHelper.waitForCondition(completionProposalList.getDisplay(), 200, () -> {
			assertFalse(completionProposalList.isDisposed(), "Completion proposal list was unexpectedly disposed");
			return completionProposalList.getItemCount() == 2 && completionProposalList.getItem(1).getData() != null;
		}), "Proposal list did not show two initial items");
		assertTrue(isComputingInfoEntry(completionProposalList.getItem(0)), "Missing computing info entry in proposal list");
		final TableItem initialProposalItem = completionProposalList.getItem(1);
		System.out.println(initialProposalItem.toString());
		final String initialProposalString = ((ICompletionProposal)initialProposalItem.getData()).getDisplayString();
		assertThat("Unexpected initial proposal item", 
				BAR_CONTENT_ASSIST_PROPOSAL, endsWith(initialProposalString));
		completionProposalList.setSelection(initialProposalItem);
		// asynchronous
		assertTrue(DisplayHelper.waitForCondition(completionProposalList.getDisplay(), LongRunningBarContentAssistProcessor.DELAY * 2, () -> {
			assertFalse(completionProposalList.isDisposed(), "Completion proposal list was unexpectedly disposed");
			return !isComputingInfoEntry(completionProposalList.getItem(0)) && completionProposalList.getItemCount() == 2;
		}), "Proposal list did not show two items after finishing computing");
		final TableItem firstCompletionProposalItem = completionProposalList.getItem(0);
		final TableItem secondCompletionProposalItem = completionProposalList.getItem(1);
		String firstCompletionProposalText = ((ICompletionProposal)firstCompletionProposalItem.getData()).getDisplayString();
		String secondCompletionProposalText =  ((ICompletionProposal)secondCompletionProposalItem.getData()).getDisplayString();
		assertThat("Unexpected first proposal item", BAR_CONTENT_ASSIST_PROPOSAL, endsWith(firstCompletionProposalText));
		assertThat("Unexpected second proposal item", LONG_RUNNING_BAR_CONTENT_ASSIST_PROPOSAL, endsWith(secondCompletionProposalText));
		String selectedProposalString = ((ICompletionProposal)completionProposalList.getSelection()[0].getData()).getDisplayString();
		assertEquals(initialProposalString, selectedProposalString, "Addition of completion proposal should keep selection");
	}
	
	private static boolean isComputingInfoEntry(TableItem item) {
		return item.getText().contains("Computing");
	}

	public static Shell findNewShell(Set<Shell> beforeShells, Display display, boolean expectShell) {
		List<Shell> afterShells = Arrays.stream(display.getShells())
				.filter(Shell::isVisible)
				.filter(shell -> !beforeShells.contains(shell))
				.toList();
		if (expectShell) {
			assertEquals(1, afterShells.size(), "No new shell found");
		}
		return afterShells.isEmpty() ? null : afterShells.get(0);
	}

	@Test
	@DisabledOnOs(value = OS.MAC, disabledReason = "test fails on Mac, see https://github.com/eclipse-platform/eclipse.platform.ui/issues/906")
	public void testCompletionFreeze_bug521484() throws Exception {
		editor.selectAndReveal(3, 0);
		this.completionShell=openContentAssist(true);
		final Table completionProposalList = findCompletionSelectionControl(this.completionShell);
		// should be instantaneous, but happens to go asynchronous on CI so let's allow a wait
		assertTrue(DisplayHelper.waitForCondition(completionProposalList.getDisplay(), 200, () -> {
			assertFalse(completionProposalList.isDisposed(), "Completion proposal list was unexpectedly disposed");
			return completionProposalList.getItemCount() == 2;
		}), "Proposal list did not show two items");
		assertTrue(isComputingInfoEntry(completionProposalList.getItem(0)), "Missing computing info entry");
		// Some processors are long running, moving cursor can cause freeze (bug 521484)
		// asynchronous
		long timestamp = System.currentTimeMillis();
		emulatePressLeftArrowKey();
		DisplayHelper.sleep(editor.getSite().getShell().getDisplay(), 200); //give time to process events
		long processingDuration = System.currentTimeMillis() - timestamp;
		assertTrue(processingDuration < LongRunningBarContentAssistProcessor.DELAY, "UI Thread frozen for " + processingDuration + "ms");
	}

	@Test
	@DisabledOnOs(value = OS.MAC, disabledReason = "test fails on Mac, see https://github.com/eclipse-platform/eclipse.platform.ui/issues/906")
	public void testMoveCaretBackUsesAllProcessors_bug522255() throws Exception {
		testCompletion();
		emulatePressLeftArrowKey();
		final Set<Shell> beforeShells = Arrays.stream(editor.getSite().getShell().getDisplay().getShells()).filter(Shell::isVisible).collect(Collectors.toSet());
		DisplayHelper.sleep(editor.getSite().getShell().getDisplay(), 200);
		this.completionShell= findNewShell(beforeShells, editor.getSite().getShell().getDisplay(), true);
		final Table completionProposalList = findCompletionSelectionControl(this.completionShell);
		checkCompletionContent(completionProposalList);
	}

	private void emulatePressLeftArrowKey() {
		editor.selectAndReveal(((ITextSelection)editor.getSelectionProvider().getSelection()).getOffset() - 1, 0);
		Control styledText = editor.getAdapter(Control.class);
		Event e = new Event();
		e.type = ST.VerifyKey;
		e.widget = styledText;
		e.keyCode = SWT.ARROW_LEFT;
		e.display = styledText.getDisplay();
		styledText.notifyListeners(ST.VerifyKey, e);
	}

	public static Table findCompletionSelectionControl(Widget control) {
		Queue<Widget> widgetsToProcess = new LinkedList<>();
		widgetsToProcess.add(control);
		while (!widgetsToProcess.isEmpty()) {
			Widget child = widgetsToProcess.poll();
			if (child instanceof Table table) {
				return table;
			} else if (child instanceof Composite composite) {
				widgetsToProcess.addAll(Arrays.asList(composite.getChildren()));
			}
		}
		fail("No completion selection control found in widget: " + control);
		return null;
	}

	@AfterEach
	public void closeShell() {
		if (this.completionShell != null && !completionShell.isDisposed()) {
			completionShell.close();
		}
	}

	private static final class TestLogListener implements ILogListener {

		List<IStatus> messages= new ArrayList<>();

		@Override
		public void logging(IStatus status, String plugin) {
			messages.add(status);
		}

	}

	private static final class MockContentAssistProcessor implements IContentAssistProcessor {

		private boolean called;

		@Override
		public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
			called= true;
			return null;
		}

		@Override
		public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
			return null;
		}

		@Override
		public char[] getCompletionProposalAutoActivationCharacters() {
			return null;
		}

		@Override
		public char[] getContextInformationAutoActivationCharacters() {
			return null;
		}

		@Override
		public String getErrorMessage() {
			return null;
		}

		@Override
		public IContextInformationValidator getContextInformationValidator() {
			return null;
		}

	}
}
