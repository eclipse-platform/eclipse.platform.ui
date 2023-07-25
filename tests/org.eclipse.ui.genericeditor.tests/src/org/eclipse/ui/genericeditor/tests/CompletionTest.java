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
import static org.eclipse.ui.tests.harness.util.DisplayHelper.runEventLoop;
import static org.eclipse.ui.tests.harness.util.DisplayHelper.sleep;
import static org.eclipse.ui.tests.harness.util.DisplayHelper.waitForCondition;
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
import java.util.function.BooleanSupplier;
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
		Shell shell = openContentAssistWithLongRunningProposalComputation();
		final Table completionProposalList = findCompletionSelectionControl(shell);
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
		assertNull(openContentAssist(), "No shell is expected to open");
		runEventLoop(Display.getCurrent(), 0);
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
		runEventLoop(Display.getCurrent(), 0);
		editor.selectAndReveal(3, 0);
		Shell shell = openContentAssistWithLongRunningProposalComputation();
		final Table completionProposalList = findCompletionSelectionControl(shell);
		assertTrue(service.called, "Service was not called!");
		checkCompletionContent(completionProposalList);
		registration.unregister();
	}

	@Test
	@DisabledOnOs(value = OS.MAC, disabledReason = "test fails on Mac, see https://github.com/eclipse-platform/eclipse.platform.ui/issues/906")
	public void testCompletionUsingViewerSelection() throws Exception {
		editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("abc");
		editor.selectAndReveal(0, 3);
		final Shell shell = openContentAssist();
		assertNotNull(shell, "Shell is expected to open for completion proposals");
		final Table completionProposalList = findCompletionSelectionControl(shell);
		waitForProposalRelatedCondition("Proposal list did not contain expected item 'ABC'", completionProposalList,
				() -> Arrays.stream(completionProposalList.getItems()).map(TableItem::getText).anyMatch("ABC"::equals), 5_000);
	}

	private static void waitForProposalRelatedCondition(String expectedListContentDescription,
			Table completionProposalList, BooleanSupplier condition, int timeoutInMsec) {
		boolean result = waitForCondition(completionProposalList.getDisplay(), timeoutInMsec, () -> {
			assertFalse(completionProposalList.isDisposed(), "Completion proposal list was unexpectedly disposed");
			return condition.getAsBoolean();
		});
		assertTrue(result, expectedListContentDescription + " but contained: "
				+ Arrays.toString(completionProposalList.getItems()));
	}

	@Test
	public void testEnabledWhenCompletion() throws Exception {
		// Confirm that when disabled, a completion shell is not present
		EnabledPropertyTester.setEnabled(false);
		createAndOpenFile("enabledWhen.txt", "bar 'bar'");
		editor.selectAndReveal(3, 0);
		assertNull(openContentAssist(), "No shell is expected to open");
		cleanFileAndEditor();

		// Confirm that when enabled, a completion shell is present
		EnabledPropertyTester.setEnabled(true);
		createAndOpenFile("enabledWhen.txt", "bar 'bar'");
		editor.selectAndReveal(3, 0);
		assertNotNull(openContentAssist(), "Shell is expected to open for completion proposals");
	}

	private Shell openContentAssistWithLongRunningProposalComputation() {
		LongRunningBarContentAssistProcessor.enable();
		Shell shell = openContentAssist();
		assertNotNull(shell, "Shell is expected to open for completion proposals");
		return shell;
	}

	private Shell openContentAssist() {
		ContentAssistAction action = (ContentAssistAction) editor.getAction(ITextEditorActionConstants.CONTENT_ASSIST);
		action.update();
		final Set<Shell> beforeShells = Arrays.stream(editor.getSite().getShell().getDisplay().getShells())
				.filter(Shell::isVisible).collect(Collectors.toSet());
		action.run();
		Shell shell = findNewShell(beforeShells, editor.getSite().getShell().getDisplay());
		runEventLoop(PlatformUI.getWorkbench().getDisplay(), 100);
		if (shell != null) {
			this.completionShell = shell;
		}
		return shell;
	}

	/**
	 * Checks that completion behaves as expected:
	 * 1. Computing is shown instantaneously
	 * 2. 1st proposal shown instantaneously
	 * 3. Calculation finishes when the test explicitly releases it
	 * @param completionProposalList the completion list
	 */
	private void checkCompletionContent(final Table completionProposalList) {
		waitForProposalRelatedCondition("Proposal list should show two initial items", completionProposalList,
				() -> completionProposalList.getItemCount() == 2
						&& completionProposalList.getItem(1).getData() != null,
				200);
		assertTrue(isComputingInfoEntry(completionProposalList.getItem(0)), "Missing computing info entry");
		final TableItem initialProposalItem = completionProposalList.getItem(1);
		final String initialProposalString = ((ICompletionProposal) initialProposalItem.getData()).getDisplayString();
		assertThat("Unexpected initial proposal item",
				BAR_CONTENT_ASSIST_PROPOSAL, endsWith(initialProposalString));
		completionProposalList.setSelection(initialProposalItem);

		LongRunningBarContentAssistProcessor.finish();
		waitForProposalRelatedCondition("Proposal list should contain two items", completionProposalList,
				() -> !isComputingInfoEntry(completionProposalList.getItem(0))
						&& completionProposalList.getItemCount() == 2,
				5_000);
		final TableItem firstCompletionProposalItem = completionProposalList.getItem(0);
		final TableItem secondCompletionProposalItem = completionProposalList.getItem(1);
		String firstCompletionProposalText = ((ICompletionProposal) firstCompletionProposalItem.getData()).getDisplayString();
		String secondCompletionProposalText = ((ICompletionProposal) secondCompletionProposalItem.getData()).getDisplayString();
		assertThat("Unexpected first proposal item", BAR_CONTENT_ASSIST_PROPOSAL, endsWith(firstCompletionProposalText));
		assertThat("Unexpected second proposal item", LONG_RUNNING_BAR_CONTENT_ASSIST_PROPOSAL,
				endsWith(secondCompletionProposalText));
		String selectedProposalString = ((ICompletionProposal) completionProposalList.getSelection()[0].getData())
				.getDisplayString();
		assertEquals(initialProposalString, selectedProposalString,
				"Addition of completion proposal should keep selection");
	}

	private static boolean isComputingInfoEntry(TableItem item) {
		return item.getText().contains("Computing");
	}

	public static Shell findNewShell(Set<Shell> beforeShells, Display display) {
		List<Shell> afterShells = Arrays.stream(display.getShells())
				.filter(Shell::isVisible)
				.filter(shell -> !beforeShells.contains(shell))
				.toList();
		assertTrue(afterShells.size() <= 1, "More than one new shell was found");
		return afterShells.isEmpty() ? null : afterShells.get(0);
	}

	@Test
	@DisabledOnOs(value = OS.MAC, disabledReason = "test fails on Mac, see https://github.com/eclipse-platform/eclipse.platform.ui/issues/906")
	public void testCompletionFreeze_bug521484() throws Exception {
		editor.selectAndReveal(3, 0);
		final Shell shell = openContentAssistWithLongRunningProposalComputation();
		assertNotNull(shell, "Shell is expected to open for completion proposals");
		final Table completionProposalList = findCompletionSelectionControl(shell);
		waitForProposalRelatedCondition("Proposal list should show two items", completionProposalList,
				() -> completionProposalList.getItemCount() == 2, 200);
		assertTrue(isComputingInfoEntry(completionProposalList.getItem(0)), "Missing computing info entry");
		long timestamp = System.currentTimeMillis();
		emulatePressLeftArrowKey();
		sleep(editor.getSite().getShell().getDisplay(), 200);
		long processingDuration = System.currentTimeMillis() - timestamp;
		assertTrue(processingDuration < LongRunningBarContentAssistProcessor.TIMEOUT_MSEC,
				"UI Thread frozen for " + processingDuration + "ms");
	}

	@Test
	@DisabledOnOs(value = OS.MAC, disabledReason = "test fails on Mac, see https://github.com/eclipse-platform/eclipse.platform.ui/issues/906")
	public void testMoveCaretBackUsesAllProcessors_bug522255() throws Exception {
		editor.selectAndReveal(3, 0);
		Shell shell = openContentAssistWithLongRunningProposalComputation();
		final Table completionProposalList = findCompletionSelectionControl(shell);
		checkCompletionContent(completionProposalList);
		LongRunningBarContentAssistProcessor.enable();
		emulatePressLeftArrowKey();
		final Set<Shell> beforeShells = Arrays.stream(editor.getSite().getShell().getDisplay().getShells())
				.filter(Shell::isVisible).collect(Collectors.toSet());
		sleep(editor.getSite().getShell().getDisplay(), 200);
		assertTrue(shell.isDisposed(), "Completion proposal shell should be disposed after moving the cursor");
		this.completionShell = findNewShell(beforeShells, editor.getSite().getShell().getDisplay());
		assertNotNull(completionShell, "Shell is expected to open for completion proposals");
		final Table newCompletionProposalList = findCompletionSelectionControl(completionShell);
		checkCompletionContent(newCompletionProposalList);
	}

	private void emulatePressLeftArrowKey() {
		editor.selectAndReveal(((ITextSelection) editor.getSelectionProvider().getSelection()).getOffset() - 1, 0);
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

	@AfterEach
	public void stopLongRunningCompletionProposalProcessor() {
		LongRunningBarContentAssistProcessor.finish();
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
			this.called = true;
			return new ICompletionProposal[0];
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
