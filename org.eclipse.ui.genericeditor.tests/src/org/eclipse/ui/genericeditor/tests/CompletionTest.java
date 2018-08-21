/*******************************************************************************
 * Copyright (c) 2016-2017 Red Hat Inc. and others
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.tests.util.DisplayHelper;

import org.eclipse.ui.genericeditor.tests.contributions.BarContentAssistProcessor;
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
	public void testCompletion() throws Exception {
		final Set<Shell> beforeShells = Arrays.stream(editor.getSite().getShell().getDisplay().getShells()).filter(Shell::isVisible).collect(Collectors.toSet());
		openConentAssist();
		this.completionShell= findNewShell(beforeShells);
		final Table completionProposalList = findCompletionSelectionControl(completionShell);
		checkCompletionContent(completionProposalList);
		// TODO find a way to actually trigger completion and verify result against Editor content
		// Assert.assertEquals("Completion didn't complete", "bars are good for a beer.", ((StyledText)editor.getAdapter(Control.class)).getText());
	}

	@Test
	public void testEnabledWhenCompletion() throws Exception {
		// Confirm that when disabled, a completion shell is present
		EnabledPropertyTester.setEnabled(false);
		createAndOpenFile("enabledWhen.txt", "bar 'bar'");
		final Set<Shell> beforeShells = Arrays.stream(editor.getSite().getShell().getDisplay().getShells()).filter(Shell::isVisible).collect(Collectors.toSet());
		openConentAssist();
		Shell[] afterShells = Arrays.stream(editor.getSite().getShell().getDisplay().getShells())
				.filter(Shell::isVisible)
				.filter(shell -> !beforeShells.contains(shell))
				.toArray(Shell[]::new);
		assertEquals("A new shell was found", 0, afterShells.length);
		cleanFileAndEditor();

		// Confirm that when enabled, a completion shell is present
		EnabledPropertyTester.setEnabled(true);
		createAndOpenFile("enabledWhen.txt", "bar 'bar'");
		final Set<Shell> beforeEnabledShells = Arrays.stream(editor.getSite().getShell().getDisplay().getShells()).filter(Shell::isVisible).collect(Collectors.toSet());
		openConentAssist();
		assertNotNull(findNewShell(beforeEnabledShells));
	}

	private void openConentAssist() {
		editor.selectAndReveal(3, 0);
		ContentAssistAction action = (ContentAssistAction) editor.getAction(ITextEditorActionConstants.CONTENT_ASSIST);
		action.update();
		action.run();
		waitAndDispatch(100);
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
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return completionProposalList.getItemCount() == 2;
			}
		}.waitForCondition(completionProposalList.getDisplay(), 200);
		assertEquals(2, completionProposalList.getItemCount());
		final TableItem computingItem = completionProposalList.getItem(0);
		assertTrue("Missing computing info entry", computingItem.getText().contains("Computing")); //$NON-NLS-1$ //$NON-NLS-2$
		TableItem completionProposalItem = completionProposalList.getItem(1);
		final ICompletionProposal selectedProposal = (ICompletionProposal)completionProposalItem.getData();
		assertTrue("Incorrect proposal content", BarContentAssistProcessor.PROPOSAL.endsWith(selectedProposal .getDisplayString()));
		completionProposalList.setSelection(completionProposalItem);
		// asynchronous
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return completionProposalList.getItem(0) != computingItem && completionProposalList.getItemCount() == 2;
			}
		}.waitForCondition(completionProposalList.getDisplay(), LongRunningBarContentAssistProcessor.DELAY + 200);
		completionProposalItem = completionProposalList.getItem(0);
		assertTrue("Proposal content seems incorrect", BarContentAssistProcessor.PROPOSAL.endsWith(((ICompletionProposal)completionProposalItem.getData()).getDisplayString()));
		TableItem otherProposalItem = completionProposalList.getItem(1);
		assertTrue("Proposal content seems incorrect", LongRunningBarContentAssistProcessor.PROPOSAL.endsWith(((ICompletionProposal)otherProposalItem.getData()).getDisplayString()));
		assertEquals("Addition of completion proposal should keep selection", selectedProposal, completionProposalList.getSelection()[0].getData());
	}

	private Shell findNewShell(Set<Shell> beforeShells) {
		Shell[] afterShells = Arrays.stream(editor.getSite().getShell().getDisplay().getShells())
				.filter(Shell::isVisible)
				.filter(shell -> !beforeShells.contains(shell))
				.toArray(Shell[]::new);
		assertEquals("No new shell found", 1, afterShells.length);
		return afterShells[0];
	}

	@Test
	public void testCompletionFreeze_bug521484() throws Exception {
		final Set<Shell> beforeShells = Arrays.stream(editor.getSite().getShell().getDisplay().getShells()).filter(Shell::isVisible).collect(Collectors.toSet());
		openConentAssist();
		this.completionShell= findNewShell(beforeShells);
		final Table completionProposalList = findCompletionSelectionControl(this.completionShell);
		// should be instantaneous, but happens to go asynchronous on CI so let's allow a wait
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return completionProposalList.getItemCount() == 2;
			}
		}.waitForCondition(completionShell.getDisplay(), 200);
		assertEquals(2, completionProposalList.getItemCount());
		final TableItem computingItem = completionProposalList.getItem(0);
		assertTrue("Missing computing info entry", computingItem.getText().contains("Computing")); //$NON-NLS-1$ //$NON-NLS-2$
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
		final Set<Shell> beforeShells = Arrays.stream(editor.getSite().getShell().getDisplay().getShells()).filter(Shell::isVisible).collect(Collectors.toSet());
		testCompletion();
		emulatePressLeftArrowKey();
		DisplayHelper.sleep(editor.getSite().getShell().getDisplay(), LongRunningBarContentAssistProcessor.DELAY + 500); // adding delay is a workaround for bug521484, use only 100ms without the bug
		this.completionShell= findNewShell(beforeShells);
		final Table completionProposalList = findCompletionSelectionControl(this.completionShell);
		assertEquals("Missing proposals from a Processor", 2, completionProposalList.getItemCount()); // replace with line below when #5214894 is done
		// checkCompletionContent(completionProposalList); // use this instead of assert above when #521484 is done
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

	private Table findCompletionSelectionControl(Widget control) {
		if (control instanceof Table) {
			return (Table)control;
		} else if (control instanceof Composite) {
			for (Widget child : ((Composite)control).getChildren()) {
				Table res = findCompletionSelectionControl(child);
				if (res != null) {
					return res;
				}
			}
		}
		return null;
	}

	@After
	public void closeShell() {
		if (this.completionShell != null && !completionShell.isDisposed()) {
			completionShell.close();
		}
	}
}
