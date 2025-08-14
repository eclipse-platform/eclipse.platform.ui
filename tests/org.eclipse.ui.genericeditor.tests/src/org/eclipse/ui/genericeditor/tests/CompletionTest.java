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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;

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

import org.junit.After;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
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

import org.eclipse.jface.util.Util;

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
	public void testCompletion() throws Exception {
		assumeFalse("test fails on Mac, see https://github.com/eclipse-platform/eclipse.platform.ui/issues/906", Util.isMac());
		editor.selectAndReveal(3, 0);
		this.completionShell= openConentAssist();
		final Table completionProposalList = findCompletionSelectionControl(completionShell);
		checkCompletionContent(completionProposalList);
		// TODO find a way to actually trigger completion and verify result against Editor content
		// Assert.assertEquals("Completion didn't complete", "bars are good for a beer.", ((StyledText)editor.getAdapter(Control.class)).getText());
	}

	@Test
	public void testDefaultContentAssistBug570488() throws Exception {
		ILog log= ILog.of(Platform.getBundle("org.eclipse.jface.text"));
		TestLogListener listener= new TestLogListener();
		log.addLogListener(listener);
		createAndOpenFile("Bug570488.txt", "bar 'bar'");
		openConentAssist(false);
		DisplayHelper.runEventLoop(Display.getCurrent(), 0);
		assertFalse("There are errors in the log", listener.messages.stream().anyMatch(s -> s.matches(IStatus.ERROR)));
		log.removeLogListener(listener);
	}

	@Test
	public void testCompletionService() throws Exception {
		assumeFalse("test fails on Mac, see https://github.com/eclipse-platform/eclipse.platform.ui/issues/906", Util.isMac());
		Bundle bundle= FrameworkUtil.getBundle(CompletionTest.class);
		assertNotNull(bundle);
		BundleContext bundleContext= bundle.getBundleContext();
		assertNotNull(bundleContext);
		MockContentAssistProcessor service= new MockContentAssistProcessor();
		ServiceRegistration<IContentAssistProcessor> registration= bundleContext.registerService(IContentAssistProcessor.class, service,
				new Hashtable<>(Collections.singletonMap("contentType", "org.eclipse.ui.genericeditor.tests.content-type")));
		DisplayHelper.runEventLoop(Display.getCurrent(), 0);
		editor.selectAndReveal(3, 0);
		this.completionShell= openConentAssist();
		final Table completionProposalList= findCompletionSelectionControl(completionShell);
		checkCompletionContent(completionProposalList);
		assertTrue("Service was not called!", service.called);
		registration.unregister();
	}

	@Test
	public void testCompletionUsingViewerSelection() throws Exception {
		assumeFalse("test fails on Mac, see https://github.com/eclipse-platform/eclipse.platform.ui/issues/906", Util.isMac());
		editor.getDocumentProvider().getDocument(editor.getEditorInput()).set("abc");
		editor.selectAndReveal(0, 3);
		this.completionShell= openConentAssist();
		final Table completionProposalList = findCompletionSelectionControl(completionShell);
		waitForProposalRelatedCondition("Proposal list did not contain expected item: ABC", completionProposalList,
				() -> Arrays.stream(completionProposalList.getItems()).map(TableItem::getText).anyMatch("ABC"::equals), 5_000);
	}
	
	private static void waitForProposalRelatedCondition(String errorMessage, Table completionProposalList, BooleanSupplier condition, int timeoutInMsec) {
		assertTrue(errorMessage, new DisplayHelper() {
			@Override
			protected boolean condition() {
				assertFalse("Completion proposal list was unexpectedly disposed", completionProposalList.isDisposed());
				return condition.getAsBoolean();
			}
		}.waitForCondition(completionProposalList.getDisplay(), timeoutInMsec));
	}
	
	@Test
	public void testEnabledWhenCompletion() throws Exception {
		// Confirm that when disabled, a completion shell is present
		EnabledPropertyTester.setEnabled(false);
		createAndOpenFile("enabledWhen.txt", "bar 'bar'");
		editor.selectAndReveal(3, 0);
		assertNull("A new shell was found", openConentAssist(false));
		cleanFileAndEditor();

		// Confirm that when enabled, a completion shell is present
		EnabledPropertyTester.setEnabled(true);
		createAndOpenFile("enabledWhen.txt", "bar 'bar'");
		editor.selectAndReveal(3, 0);		
		assertNotNull(openConentAssist());
	}

	private Shell openConentAssist() {
		return openConentAssist(true);
	}
	private Shell openConentAssist(boolean expectShell) {
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
		waitForProposalRelatedCondition("Proposal list did not show two initial items", completionProposalList, 
				() -> completionProposalList.getItemCount() == 2, 200);
		assertTrue("Missing computing info entry", isComputingInfoEntry(completionProposalList.getItem(0)));
		assertTrue("Missing computing info entry in proposal list", isComputingInfoEntry(completionProposalList.getItem(0)));
		final TableItem initialProposalItem = completionProposalList.getItem(1);
		final String initialProposalString = ((ICompletionProposal)initialProposalItem.getData()).getDisplayString();
		assertThat("Unexpected initial proposal item", 
				BAR_CONTENT_ASSIST_PROPOSAL, endsWith(initialProposalString));
		completionProposalList.setSelection(initialProposalItem);
		// asynchronous
		waitForProposalRelatedCondition("Proposal list did not show two items after finishing computing", completionProposalList, 
				() -> !isComputingInfoEntry(completionProposalList.getItem(0)) && completionProposalList.getItemCount() == 2,
				LongRunningBarContentAssistProcessor.DELAY + 200);
		final TableItem firstCompletionProposalItem = completionProposalList.getItem(0);
		final TableItem secondCompletionProposalItem = completionProposalList.getItem(1);
		String firstCompletionProposalText = ((ICompletionProposal)firstCompletionProposalItem.getData()).getDisplayString();
		String secondCompletionProposalText =  ((ICompletionProposal)secondCompletionProposalItem.getData()).getDisplayString();
		assertThat("Unexpected first proposal item", BAR_CONTENT_ASSIST_PROPOSAL, endsWith(firstCompletionProposalText));
		assertThat("Unexpected second proposal item", LONG_RUNNING_BAR_CONTENT_ASSIST_PROPOSAL, endsWith(secondCompletionProposalText));
		String selectedProposalString = ((ICompletionProposal)completionProposalList.getSelection()[0].getData()).getDisplayString();
		assertEquals("Addition of completion proposal should keep selection", initialProposalString, selectedProposalString);
	}
	
	private static boolean isComputingInfoEntry(TableItem item) {
		return item.getText().contains("Computing");
	}

	public static Shell findNewShell(Set<Shell> beforeShells, Display display, boolean expectShell) {
		Shell[] afterShells = Arrays.stream(display.getShells())
				.filter(Shell::isVisible)
				.filter(shell -> !beforeShells.contains(shell))
				.toArray(Shell[]::new);
		if (expectShell) {
			assertEquals("No new shell found", 1, afterShells.length);
		}
		return afterShells.length > 0 ? afterShells[0] : null;
	}

	@Test
	public void testCompletionFreeze_bug521484() throws Exception {
		assumeFalse("test fails on Mac, see https://github.com/eclipse-platform/eclipse.platform.ui/issues/906", Util.isMac());
		editor.selectAndReveal(3, 0);
		this.completionShell=openConentAssist();
		final Table completionProposalList = findCompletionSelectionControl(this.completionShell);
		// should be instantaneous, but happens to go asynchronous on CI so let's allow a wait
		waitForProposalRelatedCondition("Proposal list did not show two items", completionProposalList, 
				() -> completionProposalList.getItemCount() == 2, 200);
		assertTrue("Missing computing info entry", isComputingInfoEntry(completionProposalList.getItem(0)));
		// Some processors are long running, moving cursor can cause freeze (bug 521484)
		// asynchronous
		long timestamp = System.currentTimeMillis();
		emulatePressLeftArrowKey();
		DisplayHelper.sleep(editor.getSite().getShell().getDisplay(), 200); //give time to process events
		long processingDuration = System.currentTimeMillis() - timestamp;
		assertTrue("UI Thread frozen for " + processingDuration + "ms", processingDuration < LongRunningBarContentAssistProcessor.DELAY);
	}

	@Test
	public void testMoveCaretBackUsesAllProcessors_bug522255() throws Exception {
		assumeFalse("test fails on Mac, see https://github.com/eclipse-platform/eclipse.platform.ui/issues/906", Util.isMac());
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
		StyledText styledText = (StyledText) editor.getAdapter(Control.class);
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

	@After
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
